package com.yogpc.qp.tile

import java.lang.{Boolean => JBool, Byte => JByte, Integer => JInt}
import javax.annotation.Nonnull

import com.yogpc.qp.block.ADismCBlock
import com.yogpc.qp.compat.{INBTReadable, INBTWritable, InvUtils}
import com.yogpc.qp.packet.PacketHandler
import com.yogpc.qp.packet.advquarry.AdvModeMessage
import com.yogpc.qp.tile.TileAdvQuarry.{DigRange, ItemElement, ItemList, QEnch}
import com.yogpc.qp.version.VersionUtil
import com.yogpc.qp.{Config, NonNullList, PowerManager, QuarryPlus, QuarryPlusI, ReflectionHelper, _}
import net.minecraft.block.Block
import net.minecraft.block.properties.PropertyHelper
import net.minecraft.block.state.IBlockState
import net.minecraft.enchantment.{Enchantment, EnchantmentHelper}
import net.minecraft.entity.item.{EntityItem, EntityXPOrb}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.{ItemBlock, ItemStack}
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraft.util.math.BlockPos.MutableBlockPos
import net.minecraft.util.math.{AxisAlignedBB, BlockPos, ChunkPos}
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.{EnumFacing, ITickable}
import net.minecraft.world.World
import net.minecraftforge.common.ForgeChunkManager.Type
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.Constants
import net.minecraftforge.common.{ForgeChunkManager, IShearable}
import net.minecraftforge.fluids.capability.templates.FluidHandlerFluidMap
import net.minecraftforge.fluids.capability.{CapabilityFluidHandler, FluidTankProperties, IFluidHandler, IFluidTankProperties}
import net.minecraftforge.fluids.{Fluid, FluidStack, FluidTank, FluidUtil}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import net.minecraftforge.items.{CapabilityItemHandler, IItemHandlerModifiable}

import scala.collection.JavaConverters._
import scala.collection.convert.WrapAsJava

class TileAdvQuarry extends APowerTile with IEnchantableTile with HasInv with ITickable with IDebugSender with IChunkLoadTile {
    self =>
    private[this] var mDigRange = TileAdvQuarry.defaultRange
    var ench = TileAdvQuarry.defaultEnch
    var target = BlockPos.ORIGIN
    var framePoses = List.empty[BlockPos]
    val fluidStacks = scala.collection.mutable.Map.empty[Fluid, IFluidHandler]
    val cacheItems = new ItemList
    val itemHandler = new ItemHandler
    val fluidHandler = new FluidHandler
    val mode = new Mode
    val ACTING: PropertyHelper[JBool] = ADismCBlock.ACTING

    override def update() = {
        super.update()
        if (!getWorld.isRemote && !Config.content.disableController) {
            if (mode is TileAdvQuarry.MAKEFRAME) {
                @inline
                def makeFrame(): Unit = {
                    if (target == getPos) {
                        target = nextFrameTarget
                        return
                    } else if (!getWorld.isAirBlock(target)) {
                        val list = NonNullList.create[ItemStack]()
                        val state = getWorld.getBlockState(target)

                        if (state.getBlock == QuarryPlusI.blockFrame) {
                            target = nextFrameTarget
                            return
                        }

                        if (ench.silktouch && state.getBlock.canSilkHarvest(getWorld, target, state, null)) {
                            val energy = PowerManager.calcEnergyBreak(self, state.getBlockHardness(getWorld, target), -1, ench.unbreaking)
                            if (useEnergy(energy, energy, false) == energy) {
                                useEnergy(energy, energy, true)
                                list.add(ReflectionHelper.invoke(TileBasic.createStackedBlock, state.getBlock, state).asInstanceOf[ItemStack])
                                getWorld.setBlockToAir(target)
                            }
                        } else {
                            val energy = PowerManager.calcEnergyBreak(self, state.getBlockHardness(getWorld, target), ench.fortune, ench.unbreaking)
                            if (useEnergy(energy, energy, false) == energy) {
                                useEnergy(energy, energy, true)
                                list.addAll(state.getBlock.getDrops(getWorld, target, state, ench.fortune))
                                getWorld.setBlockToAir(target)
                            }
                        }
                        list.asScala.foreach(cacheItems.add)
                    }

                    if (PowerManager.useEnergyFrameBuild(self, ench.unbreaking)) {
                        getWorld.setBlockState(target, QuarryPlusI.blockFrame.getDefaultState)
                        target = nextFrameTarget
                    }
                }

                @inline
                def nextFrameTarget: BlockPos = {
                    framePoses match {
                        case p :: rest => framePoses = rest; p
                        case Nil => mode set TileAdvQuarry.BREAKBLOCK; digRange.min
                    }
                }

                if (framePoses.isEmpty) {
                    val headtail = TileAdvQuarry.getFramePoses(digRange)
                    target = headtail.head
                    framePoses = headtail.tail
                }
                for (_ <- 0 until 4)
                    if (mode is TileAdvQuarry.MAKEFRAME)
                        makeFrame()
            } else if (mode is TileAdvQuarry.BREAKBLOCK) {
                @inline
                def breakBlocks(): Boolean = {
                    val digPoses = List.iterate(target.down(), target.getY - 1)(_.down())
                    val list = NonNullList.create[ItemStack]()

                    if (target.getX % 3 == 0) {
                        val axis = new AxisAlignedBB(new BlockPos(target.getX - 6, 1, target.getZ - 6), target.add(6, 0, 6))
                        //catch dropped items
                        getWorld.getEntitiesWithinAABB(classOf[EntityItem], axis).asScala.filter(APacketTile.nonNull.test).foreach(entity => {
                            if (!entity.isDead) {
                                val drop = entity.getEntityItem
                                if (drop.getCount > 0) {
                                    entity.getEntityWorld.removeEntity(entity)
                                    list.add(drop)
                                }
                            }
                        })
                        //remove XPs
                        getWorld.getEntitiesWithinAABB(classOf[EntityXPOrb], axis).asScala.filter(APacketTile.nonNull.test).foreach(entityXPOrb => {
                            if (!entityXPOrb.isDead)
                                entityXPOrb.getEntityWorld.removeEntity(entityXPOrb)
                        })
                    }

                    var destroy, dig, drain, shear = Nil: List[BlockPos]
                    val flags = Array(target.getX == digRange.minX, target.getX == digRange.maxX, target.getZ == digRange.minZ, target.getZ == digRange.maxZ)
                    var requireEnergy = 0d
                    for (pos <- digPoses) {
                        val state = getWorld.getBlockState(pos)
                        if (!state.getBlock.isAir(state, getWorld, pos)) {
                            if (TilePump.isLiquid(state, false, getWorld, pos)) {
                                requireEnergy += PowerManager.calcEnergyPumpDrain(ench.unbreaking, 1, 0)
                                drain = pos :: drain
                            } else {
                                val blockHardness = state.getBlockHardness(getWorld, pos)
                                if (blockHardness != -1 && !blockHardness.isInfinity) {
                                    state.getBlock match {
                                        case _ if TileAdvQuarry.noDigBLOCKS.exists(_.contain(state)) =>
                                            requireEnergy += PowerManager.calcEnergyBreak(this, blockHardness, 0, ench.unbreaking)
                                            destroy = pos :: destroy
                                        case leave: IShearable if leave.isLeaves(state, getWorld, pos) =>
                                            requireEnergy += PowerManager.calcEnergyBreak(this, blockHardness, ench.mode, ench.unbreaking)
                                            if (ench.silktouch)
                                                shear = pos :: shear
                                            else
                                                dig = pos :: dig
                                        case _ => requireEnergy += PowerManager.calcEnergyBreak(this, blockHardness, ench.mode, ench.unbreaking)
                                            dig = pos :: dig
                                    }
                                } else if (Config.content.removeBedrock && (state.getBlock == Blocks.BEDROCK) &&
                                  ((pos.getY > 0 && pos.getY <= 5) || (pos.getY > 122 && pos.getY < 127))) {
                                    requireEnergy += 200
                                    destroy = pos :: destroy
                                } else if (state.getBlock == Blocks.PORTAL) {
                                    getWorld.setBlockToAir(pos)
                                    requireEnergy += 200
                                }
                            }

                            def checkandsetFrame(world: World, thatPos: BlockPos): Unit = {
                                if (TilePump.isLiquid(world.getBlockState(thatPos), false, world, thatPos)) {
                                    world.setBlockState(thatPos, QuarryPlusI.blockFrame.getDamiingState)
                                }
                            }

                            if (flags(0)) { //-x
                                checkandsetFrame(getWorld, pos.offset(EnumFacing.WEST))
                                if (flags(2)) { //-z, -x
                                    checkandsetFrame(getWorld, pos.offset(EnumFacing.NORTH).offset(EnumFacing.WEST))
                                }
                                else if (flags(3)) { //+z, -x
                                    checkandsetFrame(getWorld, pos.offset(EnumFacing.SOUTH).offset(EnumFacing.WEST))
                                }
                            }
                            else if (flags(1)) { //+x
                                checkandsetFrame(getWorld, pos.offset(EnumFacing.EAST))
                                if (flags(2)) { //-z, +x
                                    checkandsetFrame(getWorld, pos.offset(EnumFacing.NORTH).offset(EnumFacing.EAST))
                                }
                                else if (flags(3)) { //+z, +x
                                    checkandsetFrame(getWorld, pos.offset(EnumFacing.SOUTH).offset(EnumFacing.EAST))
                                }
                            }
                            if (flags(2)) { //-z
                                checkandsetFrame(getWorld, pos.offset(EnumFacing.NORTH))
                            }
                            else if (flags(3)) { //+z
                                checkandsetFrame(getWorld, pos.offset(EnumFacing.SOUTH))
                            }
                        }
                    }

                    requireEnergy *= 1.25
                    if (useEnergy(requireEnergy, requireEnergy, false) == requireEnergy) {
                        useEnergy(requireEnergy, requireEnergy, true)
                        dig.foreach(p => {
                            val state = getWorld.getBlockState(p)
                            if (ench.silktouch && state.getBlock.canSilkHarvest(getWorld, p, state, null)) {
                                list.add(ReflectionHelper.invoke(TileBasic.createStackedBlock, state.getBlock, state).asInstanceOf[ItemStack])
                            } else {
                                list.addAll(state.getBlock.getDrops(getWorld, p, state, ench.fortune))
                            }
                            getWorld.setBlockState(p, Blocks.AIR.getDefaultState, 2)
                        })
                        if (shear.nonEmpty) {
                            val itemShear = new ItemStack(net.minecraft.init.Items.SHEARS)
                            EnchantmentHelper.setEnchantments(ench.getMap.collect { case (a, b) if b > 0 => (Enchantment.getEnchantmentByID(a), JInt.valueOf(b)) }.asJava, itemShear)
                            shear.foreach(p => {
                                val state = getWorld.getBlockState(p)
                                val block = state.getBlock.asInstanceOf[Block with IShearable]
                                list.addAll(block.onSheared(itemShear, getWorld, p, ench.fortune))
                                getWorld.setBlockState(p, Blocks.AIR.getDefaultState, 2)
                            })
                        }
                        destroy.foreach(getWorld.setBlockState(_, Blocks.AIR.getDefaultState, 2))
                        drain.foreach(p => {
                            val handler = Option(FluidUtil.getFluidHandler(getWorld, p, EnumFacing.UP))
                            val fluidOp = handler.map(_.getTankProperties.apply(0)).flatMap(p => Option(p.getContents)).map(_.getFluid)
                            fluidOp match {
                                case Some(fluid) => handler.map(_.drain(Fluid.BUCKET_VOLUME, false)).foreach(s => fluidStacks.get(fluid) match {
                                    case Some(tank) => tank.fill(s, true)
                                    case None => fluidStacks.put(fluid, new FluidTank(s, Int.MaxValue))
                                })
                                case None => //QuarryPlus.LOGGER.error(s"Adv Fluid null, ${getWorld.getBlockState(p)}, ${FluidUtil.getFluidHandler(getWorld, p, EnumFacing.UP)}")
                            }
                            getWorld.setBlockState(p, Blocks.AIR.getDefaultState, 2)
                        })
                        list.asScala.foreach(cacheItems.add)
                        true
                    } else {
                        false
                    }
                }

                if (breakBlocks()) {
                    var i = 0
                    do {
                        i += 1
                        val x = target.getX + 1
                        if (x > digRange.maxX) {
                            val z = target.getZ + 1
                            if (z > digRange.maxZ) {
                                //Finished.
                                target = digRange.min
                                mode set TileAdvQuarry.CHECKLIQUID
                            } else {
                                target = new BlockPos(digRange.minX, target.getY, z)
                            }
                        } else {
                            target = new BlockPos(x, target.getY, target.getZ)
                        }
                    } while (i < 32 && {
                        val p = new MutableBlockPos(target)
                        Range.inclusive(1, target.getY).forall { i => p.setY(i); getWorld.isAirBlock(p) }
                    })
                }

            } else if (mode is TileAdvQuarry.NOTNEEDBREAK) {
                if (digRange.defined && !Config.content.noEnergy)
                    if (getStoredEnergy > getMaxStored * 0.3)
                        mode set TileAdvQuarry.MAKEFRAME
            } else if (mode is TileAdvQuarry.CHECKLIQUID) {
                for (_ <- 0 until 32) {
                    if (mode is TileAdvQuarry.CHECKLIQUID) {
                        List.iterate(target.down(), target.getY - 1)(_.down()).filter(p => {
                            val state = getWorld.getBlockState(p)
                            if (!state.getBlock.isAir(state, getWorld, p)) {
                                TilePump.isLiquid(state, false, getWorld, p)
                            } else {
                                false
                            }
                        }).foreach(getWorld.setBlockToAir)

                        val x = target.getX + 1
                        if (x > digRange.maxX) {
                            val z = target.getZ + 1
                            if (z > digRange.maxZ) {
                                //Finished.
                                target = BlockPos.ORIGIN
                                mode set TileAdvQuarry.NONE
                            } else {
                                target = new BlockPos(digRange.minX, target.getY, z)
                            }
                        } else {
                            target = new BlockPos(x, target.getY, target.getZ)
                        }
                    }
                }
            } else if (mode is TileAdvQuarry.FILLBLOCKS) {
                val handler = InvUtils.findItemHander(getWorld, getPos.up, EnumFacing.DOWN).orNull
                if (handler != null) {
                    val list = Range(0, handler.getSlots).find(i => VersionUtil.nonEmpty(handler.getStackInSlot(i))).map(handler.extractItem(_, 64, false)).toList
                    list.filter(_.getItem.isInstanceOf[ItemBlock]).flatMap(i => Range(0, i.getCount)).foreach(_ => {
                        if (mode is TileAdvQuarry.FILLBLOCKS) {
                            val state = InvUtils.getStateFromItem(list.head.getItem.asInstanceOf[ItemBlock], list.head.getItemDamage)
                            getWorld.setBlockState(new BlockPos(target.getX, if (Config.content.removeBedrock) 1 else 5, target.getZ), state)
                            val x = target.getX + 1
                            if (x > digRange.maxX) {
                                val z = target.getZ + 1
                                if (z > digRange.maxZ) {
                                    //Finished.
                                    target = BlockPos.ORIGIN
                                    mode set TileAdvQuarry.NONE
                                } else {
                                    target = new BlockPos(digRange.minX, target.getY, z)
                                }
                            } else {
                                target = new BlockPos(x, target.getY, target.getZ)
                            }
                        }
                    })
                } else {
                    target = BlockPos.ORIGIN
                    mode set TileAdvQuarry.NONE
                }
            }
            if (!isEmpty) {
                var break = false
                var is = cacheItems.list.remove(0).toStack
                while (!break) {
                    val stack = InvUtils.injectToNearTile(getWorld, getPos, is)
                    if (stack.getCount > 0) {
                        cacheItems.add(stack)
                        break = true
                    }
                    if (isEmpty || break) {
                        break = true
                    } else {
                        is = cacheItems.list.remove(0).toStack
                    }
                }
            }
        }
    }

    override protected def isWorking = mode.isWorking

    override def G_reinit(): Unit = {
        mode.set(TileAdvQuarry.NOTNEEDBREAK)
        if (!digRange.defined) {
            digRange = makeRangeBox()
        }
    }

    def energyConfigure(): Unit = {
        if (!mode.isWorking) {
            this.configure(0, getMaxStored)
        } else if (mode.reduceRecieve) {
            this.configure(ench.maxRecieve / 128, TileAdvQuarry.MAX_STORED)
        } else {
            this.configure(ench.maxRecieve, TileAdvQuarry.MAX_STORED)
        }
    }

    override def readFromNBT(nbttc: NBTTagCompound) = {
        super.readFromNBT(nbttc)
        ench = QEnch.readFromNBT(nbttc)
        digRange = DigRange.readFromNBT(nbttc)
        target = BlockPos.fromLong(nbttc.getLong("NBT_TARGET"))
        mode.readFromNBT(nbttc)
        cacheItems.readFromNBT(nbttc)
        val l = nbttc.getTagList("NBT_FLUIDLIST", Constants.NBT.TAG_COMPOUND)
        Range(0, l.tagCount()).foreach(i => {
            val tank = new FluidTank(0)
            CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.readNBT(tank, null, l.get(i))
            if (tank.getFluid != null) {
                fluidStacks.put(tank.getFluid.getFluid, tank)
            }
        })
    }

    override def writeToNBT(nbttc: NBTTagCompound) = {
        ench.writeToNBT(nbttc)
        digRange.writeToNBT(nbttc)
        nbttc.setLong("NBT_TARGET", target.toLong)
        mode.writeToNBT(nbttc)
        cacheItems.writeToNBT(nbttc)
        val l = new NBTTagList
        fluidStacks.foreach { case (_, tank) => l.appendTag(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.writeNBT(tank, null)) }
        nbttc.setTag("NBT_FLUIDLIST", l)
        super.writeToNBT(nbttc)
    }

    /**
      * @return Map (Enchantment id, level)
      */
    override def getEnchantments = ench.getMap.collect { case (a, b) if b > 0 => (JInt.valueOf(a), JByte.valueOf(b)) }.asJava

    /**
      * @param id    Enchantment id
      * @param value level
      */
    override def setEnchantent(id: Short, value: Short) = ench = ench.set(id, value)

    override def isItemValidForSlot(index: Int, stack: ItemStack) = false

    override def setInventorySlotContents(index: Int, stack: ItemStack) = {
        if (VersionUtil.nonEmpty(stack)) {
            QuarryPlus.LOGGER.warn("QuarryPlus WARN: call setInventorySlotContents with non empty ItemStack.")
        } else {
            removeStackFromSlot(index)
        }
    }

    override def decrStackSize(index: Int, count: Int) = cacheItems.decrease(index, count)

    override def getSizeInventory = cacheItems.list.size

    override def removeStackFromSlot(index: Int) = cacheItems.list.remove(index).toStack

    override val getInventoryStackLimit = 1

    override def clear() = cacheItems.list.clear()

    override def isEmpty = cacheItems.list.isEmpty

    override def getStackInSlot(index: Int) = cacheItems.list(index).toStack

    override val getDebugName = "tile.chunkdestroyer.name"

    override def isUsableByPlayer(player: EntityPlayer) = self.getWorld.getTileEntity(self.getPos) eq this

    override def getDebugmessages = if (!Config.content.disableController) {
        import scala.collection.JavaConverters._
        List(new TextComponentString("Items to extract = " + cacheItems.list.size),
            new TextComponentString("Liquid to extract = " + fluidStacks.size),
            new TextComponentString("Next target = " + target.toString),
            new TextComponentString(mode.toString),
            new TextComponentString(digRange.toString),
            new TextComponentString("Resent 5 seconds, used " + getInfoEnergyPerTick + " MJ/t")).asJava
    } else {
        java.util.Collections.singletonList(new TextComponentString("ChunkDestroyer is disabled."))
    }

    override def hasCapability(capability: Capability[_], facing: EnumFacing) = {
        capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ||
          (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && Config.content.enableChunkDestroyerFluidHander) ||
          super.hasCapability(capability, facing)
    }

    override def getCapability[T](capability: Capability[T], facing: EnumFacing) = {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler)
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && Config.content.enableChunkDestroyerFluidHander) {
            CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler)
        } else
            super.getCapability(capability, facing)
    }

    override def hasFastRenderer: Boolean = true

    override def getRenderBoundingBox: AxisAlignedBB = {
        if (digRange.defined) {
            digRange.rendrBox
        } else
            super.getRenderBoundingBox
    }

    override def onLoad(): Unit = {
        super.onLoad()
        energyConfigure()
    }

    override def onChunkUnload(): Unit = {
        if (!getWorld.isRemote)
            mode set TileAdvQuarry.NONE
        ForgeChunkManager.releaseTicket(this.chunkTicket)
        super.onChunkUnload()
    }

    private[this] var chunkTicket: ForgeChunkManager.Ticket = _

    override def requestTicket(): Unit = {
        if (this.chunkTicket != null) return
        this.chunkTicket = ForgeChunkManager.requestTicket(QuarryPlus.INSTANCE, getWorld, Type.NORMAL)
        if (this.chunkTicket == null) return
        val tag = this.chunkTicket.getModData
        tag.setInteger("quarryX", getPos.getX)
        tag.setInteger("quarryY", getPos.getY)
        tag.setInteger("quarryZ", getPos.getZ)
        forceChunkLoading(this.chunkTicket)
    }

    override def forceChunkLoading(ticket: ForgeChunkManager.Ticket): Unit = {
        if (this.chunkTicket == null) this.chunkTicket = ticket
        val quarryChunk = new ChunkPos(getPos)
        ForgeChunkManager.forceChunk(ticket, quarryChunk)
    }

    def makeRangeBox() = {
        val facing = getWorld.getBlockState(getPos).getValue(ADismCBlock.FACING).getOpposite
        val link = List(getPos.offset(facing), getPos.offset(facing.rotateYCCW), getPos.offset(facing.rotateY)).map(getWorld.getTileEntity(_))
          .collectFirst { case m: TileMarker if m.link != null =>
              val poses = (m.min().add(+1, 0, +1), m.max().add(-1, 0, -1))
              m.removeFromWorldWithItem().asScala.foreach(cacheItems.add)
              poses
          }.getOrElse({
            val chunkPos = new ChunkPos(getPos)
            val y = getPos.getY
            (new BlockPos(chunkPos.getXStart, y, chunkPos.getZStart), new BlockPos(chunkPos.getXEnd, y, chunkPos.getZEnd))
        })
        new TileAdvQuarry.DigRange(link._1, link._2)
    }

    def digRange = mDigRange

    def digRange_=(@Nonnull digRange: TileAdvQuarry.DigRange): Unit = {
        require(digRange != null, "DigRange must not be null.")
        mDigRange = digRange
    }

    def stickActivated(): Unit = {
        //Called when noEnergy is true and block is right clicked with stick (item)
        if (mode is TileAdvQuarry.NOTNEEDBREAK) {
            mode set TileAdvQuarry.MAKEFRAME
        }
    }

    def startFillMode(): Unit = {
        if ((mode is TileAdvQuarry.NONE) && digRange.defined && preparedFiller()) {
            mode set TileAdvQuarry.FILLBLOCKS
            target = digRange.min
        }
    }

    @SideOnly(Side.CLIENT)
    def recieveModeMassage(modeTag: NBTTagCompound): Runnable = new Runnable {
        override def run(): Unit = {
            mode.readFromNBT(modeTag)
            digRange = DigRange.readFromNBT(modeTag)
        }
    }

    def preparedFiller(): Boolean = {
        val need = (digRange.maxX - digRange.minX) * (digRange.maxZ - digRange.minZ)
        val stacks = InvUtils.findItemHander(getWorld, getPos.up, EnumFacing.DOWN).toList
          .flatMap(handler => Range(0, handler.getSlots).map(handler.getStackInSlot))
        stacks.nonEmpty && stacks.head.getItem.isInstanceOf[ItemBlock] && stacks.forall(_.isItemEqual(stacks.head)) && stacks.map(_.getCount).sum >= need
    }

    private[TileAdvQuarry] class ItemHandler extends IItemHandlerModifiable {
        override def setStackInSlot(slot: Int, stack: ItemStack): Unit = self.setInventorySlotContents(slot, stack)

        override def getStackInSlot(slot: Int): ItemStack = self.getStackInSlot(slot)

        override def extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack = {
            if (simulate) {
                cacheItems.list(slot) match {
                    case ItemElement(i, size) => i.toStack(Math.min(amount, Math.min(size, i.itemStackLimit)))
                }
            } else {
                self.decrStackSize(slot, amount)
            }
        }

        //        override def getSlotLimit(slot: Int): Int = 1

        override def getSlots: Int = self.getSizeInventory

        override def insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack = stack
    }

    private[TileAdvQuarry] class FluidHandler extends FluidHandlerFluidMap(WrapAsJava.mutableMapAsJavaMap(fluidStacks)) {
        val emptyProperty = new FluidTankProperties(null, 0, false, false)

        /**
          * Not fillable.
          */
        override def fill(resource: FluidStack, doFill: Boolean): Int = 0

        override def getTankProperties: Array[IFluidTankProperties] = {
            if (fluidStacks.nonEmpty) {
                fluidStacks.map { case (_, handler) => val s = handler.drain(Int.MaxValue, false)
                    new FluidTankProperties(s, s.amount, false, true)
                }.toArray
            } else {
                Array(emptyProperty)
            }
        }

        override def toString: String = {
            "ChunkDestroyer FluidHandler contents = " + getTankProperties.map { c =>
                Option(c.getContents) match {
                    case Some(s) => (s.getFluid.getName, s.amount)
                    case None => ("No liqud", 0)
                }
            }.mkString(", ")
        }
    }

    private[TileAdvQuarry] class Mode extends INBTWritable with INBTReadable[Mode] {

        import TileAdvQuarry._

        private[this] var mode: Modes = NONE

        def set(newmode: Modes): Unit = {
            mode = newmode
            if (!getWorld.isRemote) {
                energyConfigure()
                PacketHandler.sendToAround(AdvModeMessage.create(self), getWorld, getPos)
            }
            val state = getWorld.getBlockState(getPos)
            if (state.getValue(ACTING)) {
                if (newmode == NONE || newmode == NOTNEEDBREAK) {
                    validate()
                    getWorld.setBlockState(getPos, state.withProperty(ACTING, JBool.FALSE))
                    validate()
                    getWorld.setTileEntity(getPos, self)
                }
            } else {
                if (newmode != NONE && newmode != NOTNEEDBREAK) {
                    validate()
                    getWorld.setBlockState(getPos, state.withProperty(ACTING, JBool.TRUE))
                    validate()
                    getWorld.setTileEntity(getPos, self)
                }
            }
        }

        def is(modes: Modes): Boolean = mode == modes

        def isWorking: Boolean = !is(NONE)

        def reduceRecieve: Boolean = is(MAKEFRAME)

        override def toString: String = "ChunkDestroyer mode = " + mode

        override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
            val tag = new NBTTagCompound
            tag.setInteger("mode", mode.index)
            nbt.setTag(NBT_MODE, tag)
            nbt
        }

        override def readFromNBT(tag: NBTTagCompound): Mode = {
            if (tag hasKey NBT_MODE) {
                val t = tag.getCompoundTag(NBT_MODE)
                this.mode = t.getInteger("mode") match {
                    case 0 => NONE
                    case 1 => NOTNEEDBREAK
                    case 2 => MAKEFRAME
                    case 3 => BREAKBLOCK
                    case 4 => CHECKLIQUID
                    case 5 => FILLBLOCKS
                    case _ => throw new IllegalStateException("No available mode")
                }
            }
            this
        }
    }

    override def getName = getDebugName
}

object TileAdvQuarry {

    final val MAX_STORED = 300 * 256
    final val noDigBLOCKS = Set(
        BlockWrapper(Blocks.STONE.getDefaultState, ignoreMeta = true),
        BlockWrapper(Blocks.COBBLESTONE.getDefaultState),
        BlockWrapper(Blocks.DIRT.getDefaultState, ignoreProperty = true),
        BlockWrapper(Blocks.GRASS.getDefaultState, ignoreProperty = true),
        BlockWrapper(Blocks.NETHERRACK.getDefaultState),
        BlockWrapper(Blocks.SANDSTONE.getDefaultState, ignoreMeta = true),
        BlockWrapper(Blocks.RED_SANDSTONE.getDefaultState, ignoreMeta = true))
    private final val ENERGYLIMIT_LIST = IndexedSeq(512, 1024, 2048, 4096, 8192, MAX_STORED)
    private final val NBT_QENCH = "nbt_qench"
    private final val NBT_DIGRANGE = "nbt_digrange"
    private final val NBT_MODE = "nbt_quarrymode"
    private final val NBT_ITEMLIST = "nbt_itemlist"
    private final val NBT_ITEMELEMENTS = "nbt_itemelements"

    val defaultEnch = QEnch(efficiency = 0, unbreaking = 0, fortune = 0, silktouch = false)
    val defaultRange: DigRange = NoDefinedRange

    private[TileAdvQuarry] case class QEnch(efficiency: Byte, unbreaking: Byte, fortune: Byte, silktouch: Boolean) extends INBTWritable {

        require(efficiency >= 0 && unbreaking >= 0 && fortune >= 0, "Chunk Destroyer Enchantment error")

        import IEnchantableTile._

        def set(id: Short, level: Int): QEnch = {
            id match {
                case EfficiencyID => this.copy(efficiency = level.toByte)
                case UnbreakingID => this.copy(unbreaking = level.toByte)
                case FortuneID => this.copy(fortune = level.toByte)
                case SilktouchID => this.copy(silktouch = level > 0)
                case _ => this
            }
        }

        def getMap = Map(EfficiencyID -> efficiency, UnbreakingID -> unbreaking,
            FortuneID -> fortune, SilktouchID -> silktouch.compare(false).toByte)

        val maxRecieve = if (efficiency >= 5) ENERGYLIMIT_LIST(5) / 10 else ENERGYLIMIT_LIST(efficiency) / 10

        val mode: Byte = if (silktouch) -1 else fortune

        override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
            val t = new NBTTagCompound
            t.setByte("efficiency", efficiency)
            t.setByte("unbreaking", unbreaking)
            t.setByte("fortune", fortune)
            t.setBoolean("silktouch", silktouch)
            nbt.setTag(NBT_QENCH, t)
            nbt
        }
    }

    object QEnch extends INBTReadable[QEnch] {
        override def readFromNBT(tag: NBTTagCompound): QEnch = {
            if (tag.hasKey(NBT_QENCH)) {
                val t = tag.getCompoundTag(NBT_QENCH)
                QEnch(t.getByte("efficiency"), t.getByte("unbreaking"), t.getByte("fortune"), t.getBoolean("silktouch"))
            } else
                QEnch(efficiency = 0, unbreaking = 0, fortune = 0, silktouch = false)
        }
    }

    case class DigRange(minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int) extends INBTWritable {
        def this(minPos: BlockPos, maxPos: BlockPos) {
            this(minPos.getX, minPos.getY, minPos.getZ, maxPos.getX, maxPos.getY, maxPos.getZ)
        }

        val defined = true

        def min: BlockPos = new BlockPos(minX, minY, minZ)

        val rendrBox = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ)

        override val toString: String = s"Dig Range from ($minX, $minY, $minZ) to ($maxX, $maxY, $maxZ)"

        override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
            val t = new NBTTagCompound
            t.setBoolean("defined", defined)
            t.setInteger("minX", minX)
            t.setInteger("minY", minY)
            t.setInteger("minZ", minZ)
            t.setInteger("maxX", maxX)
            t.setInteger("maxY", maxY)
            t.setInteger("maxZ", maxZ)
            nbt.setTag(NBT_DIGRANGE, t)
            nbt
        }
    }

    object DigRange extends INBTReadable[DigRange] {
        override def readFromNBT(tag: NBTTagCompound): DigRange = {
            if (tag.hasKey(NBT_DIGRANGE)) {
                val t = tag.getCompoundTag(NBT_DIGRANGE)
                if (t.getBoolean("defined")) {
                    DigRange(t.getInteger("minX"), t.getInteger("minY"), t.getInteger("minZ"),
                        t.getInteger("maxX"), t.getInteger("maxY"), t.getInteger("maxZ"))
                } else {
                    NoDefinedRange
                }
            } else
                NoDefinedRange
        }
    }

    private object NoDefinedRange extends DigRange(BlockPos.ORIGIN, BlockPos.ORIGIN) {
        override val defined: Boolean = false
        override val min: BlockPos = BlockPos.ORIGIN
        override val toString: String = "Dig Range Not Defined"
    }

    class ItemList extends INBTWritable with INBTReadable[ItemList] {
        val list = scala.collection.mutable.ArrayBuffer.empty[ItemElement]

        def add(itemDamage: ItemDamage, count: Int): Unit = {
            val i = list.indexWhere(_.itemDamage == itemDamage)
            if (i > -1) {
                val e = list(i).count
                val newCount = e + count
                if (newCount > 0) {
                    list.update(i, ItemElement(itemDamage, newCount))
                } else {
                    list.remove(i)
                }
            } else {
                if (count > 0)
                    list += ItemElement(itemDamage, count)
            }
        }

        def add(stack: ItemStack): Unit = {
            add(ItemDamage(stack), stack.getCount)
        }

        def decrease(index: Int, count: Int): ItemStack = {
            val t = list(index)
            val min = Math.min(count, t.itemDamage.itemStackLimit)
            if (t.count <= min) {
                list.remove(index)
                t.itemDamage.toStack(t.count)
            } else {
                list(index) = ItemElement(t.itemDamage, t.count - min)
                t.itemDamage.toStack(min)
            }
        }

        override def toString: String = "ItemList size = " + list.size

        override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
            val t = new NBTTagCompound
            val l = new NBTTagList
            list.map(_.toStack.serializeNBT()).foreach(l.appendTag(_))
            t.setTag(NBT_ITEMELEMENTS, l)
            nbt.setTag(NBT_ITEMLIST, t)
            nbt
        }

        override def readFromNBT(tag: NBTTagCompound): ItemList = {
            if (tag.hasKey(NBT_ITEMLIST)) {
                val l = tag.getCompoundTag(NBT_ITEMLIST).getTagList(NBT_ITEMELEMENTS, Constants.NBT.TAG_COMPOUND)
                Range(0, l.tagCount()).foreach(i => add(VersionUtil.fromNBTTag(l.getCompoundTagAt(i))))
            }
            this
        }
    }

    case class ItemElement(itemDamage: ItemDamage, count: Int) {
        def toStack = itemDamage.toStack(count)

        override def toString: String = itemDamage.toString + " x" + count
    }

    private[TileAdvQuarry] case class BlockWrapper(state: IBlockState, ignoreProperty: Boolean = false, ignoreMeta: Boolean = false) {
        def contain(that: IBlockState): Boolean = {
            if (ignoreMeta) {
                state.getBlock == that.getBlock
            } else if (ignoreProperty) {
                state.getBlock == that.getBlock &&
                  state.getBlock.getMetaFromState(state) == that.getBlock.getMetaFromState(that)
            } else {
                state == that
            }
        }
    }

    sealed class Modes(val index: Int)

    case object NONE extends Modes(0)

    case object NOTNEEDBREAK extends Modes(1)

    case object MAKEFRAME extends Modes(2)

    case object BREAKBLOCK extends Modes(3)

    case object CHECKLIQUID extends Modes(4)

    case object FILLBLOCKS extends Modes(5)

    def getFramePoses(digRange: DigRange): List[BlockPos] = {
        val builder = List.newBuilder[BlockPos]
        for (i <- 0 to 4) {
            builder += new BlockPos(digRange.minX - 1, digRange.maxY + 4 - i, digRange.minZ - 1)
            builder += new BlockPos(digRange.minX - 1, digRange.maxY + 4 - i, digRange.maxZ + 1)
            builder += new BlockPos(digRange.maxX + 1, digRange.maxY + 4 - i, digRange.maxZ + 1)
            builder += new BlockPos(digRange.maxX + 1, digRange.maxY + 4 - i, digRange.minZ - 1)
        }
        for (x <- digRange.minX to digRange.maxX) {
            builder += new BlockPos(x, digRange.maxY + 4, digRange.minZ - 1)
            builder += new BlockPos(x, digRange.maxY + 0, digRange.minZ - 1)
            builder += new BlockPos(x, digRange.maxY + 0, digRange.maxZ + 1)
            builder += new BlockPos(x, digRange.maxY + 4, digRange.maxZ + 1)
        }
        for (z <- digRange.minZ to digRange.maxZ) {
            builder += new BlockPos(digRange.minX - 1, digRange.maxY + 4, z)
            builder += new BlockPos(digRange.minX - 1, digRange.maxY + 0, z)
            builder += new BlockPos(digRange.maxX + 1, digRange.maxY + 0, z)
            builder += new BlockPos(digRange.maxX + 1, digRange.maxY + 4, z)
        }
        builder.result()
    }
}