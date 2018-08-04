package com.yogpc.qp.tile

import java.lang.{Boolean => JBool}

import com.yogpc.qp.block.ADismCBlock
import com.yogpc.qp.compat.{INBTReadable, INBTWritable, InvUtils}
import com.yogpc.qp.gui.TranslationKeys
import com.yogpc.qp.packet.PacketHandler
import com.yogpc.qp.packet.advquarry.{AdvContentMessage, AdvModeMessage}
import com.yogpc.qp.tile.TileAdvQuarry.{DigRange, ItemElement, ItemList, QEnch}
import com.yogpc.qp.version.VersionUtil
import com.yogpc.qp.{Config, PowerManager, QuarryPlus, QuarryPlusI, ReflectionHelper, _}
import javax.annotation.Nonnull
import net.minecraft.block.Block
import net.minecraft.block.properties.PropertyHelper
import net.minecraft.block.state.IBlockState
import net.minecraft.enchantment.{Enchantment, EnchantmentHelper}
import net.minecraft.entity.item.{EntityItem, EntityXPOrb}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.{ItemBlock, ItemStack}
import net.minecraft.nbt.{NBTTagCompound, NBTTagList, NBTTagLong}
import net.minecraft.util.math.BlockPos.MutableBlockPos
import net.minecraft.util.math.{AxisAlignedBB, BlockPos, ChunkPos}
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.{EnumFacing, EnumHand, ITickable, NonNullList}
import net.minecraft.world.{World, WorldServer}
import net.minecraftforge.common.ForgeChunkManager.Type
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.Constants
import net.minecraftforge.common.{ForgeChunkManager, IShearable}
import net.minecraftforge.fluids.capability.{CapabilityFluidHandler, FluidTankProperties, IFluidHandler, IFluidTankProperties}
import net.minecraftforge.fluids.{Fluid, FluidStack, FluidTank, FluidUtil}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import net.minecraftforge.items.{CapabilityItemHandler, IItemHandlerModifiable}

import scala.collection.JavaConverters._

class TileAdvQuarry extends APowerTile with IEnchantableTile with HasInv with ITickable with IDebugSender with IChunkLoadTile {
    self =>
    private[this] var mDigRange = TileAdvQuarry.defaultRange
    var ench = TileAdvQuarry.defaultEnch
    var target = BlockPos.ORIGIN
    var framePoses = List.empty[BlockPos]
    var chunks = List.empty[ChunkPos]
    val fluidStacks = scala.collection.mutable.Map.empty[FluidStack, FluidTank]
    val cacheItems = new ItemList
    val itemHandler = new ItemHandler
    val fluidHandlers = EnumFacing.VALUES.map(f => (f, new FluidHandler(facing = f))).toMap.withDefaultValue(new FluidHandler(null))
    val fluidExtractFacings = EnumFacing.VALUES.map(f => (f, scala.collection.mutable.Set.empty[FluidStack])).toMap
    val mode = new Mode
    val ACTING: PropertyHelper[JBool] = ADismCBlock.ACTING

    override def update() = {
        super.update()
        if (!getWorld.isRemote && !machineDisabled) {
            if (mode is TileAdvQuarry.MAKEFRAME) {
                @inline
                def makeFrame(): Unit = {
                    if (target == getPos) {
                        target = nextFrameTarget
                        return
                    } else if (!getWorld.isAirBlock(target)) {
                        val list = NonNullList.create[ItemStack]
                        val state = getWorld.getBlockState(target)

                        if (state.getBlock == QuarryPlusI.blockFrame) {
                            target = nextFrameTarget
                            return
                        }

                        if (ench.silktouch && state.getBlock.canSilkHarvest(getWorld, target, state, null)) {
                            val energy = PowerManager.calcEnergyBreak(state.getBlockHardness(getWorld, target), -1, ench.unbreaking)
                            if (useEnergy(energy, energy, false) == energy) {
                                useEnergy(energy, energy, true)
                                list.add(ReflectionHelper.invoke(TileBasic.createStackedBlock, state.getBlock, state).asInstanceOf[ItemStack])
                                getWorld.setBlockToAir(target)
                            }
                        } else {
                            val energy = PowerManager.calcEnergyBreak(state.getBlockHardness(getWorld, target), ench.fortune, ench.unbreaking)
                            if (useEnergy(energy, energy, false) == energy) {
                                useEnergy(energy, energy, true)
                                TileBasic.getDrops(getWorld, target, state, state.getBlock, ench.fortune, list)
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
                    chunks = digRange.chunkSeq
                }
                chunkLoad()

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
                        getWorld.getEntitiesWithinAABB(classOf[EntityItem], axis).asScala.filter(nonNull).foreach(entity => {
                            if (!entity.isDead) {
                                val drop = entity.getItem
                                if (drop.getCount > 0) {
                                    entity.getEntityWorld.removeEntity(entity)
                                    list.add(drop)
                                }
                            }
                        })
                        //remove XPs
                        getWorld.getEntitiesWithinAABB(classOf[EntityXPOrb], axis).asScala.filter(nonNull).foreach(entityXPOrb => {
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
                            if (TilePump.isLiquid(state)) {
                                requireEnergy += PowerManager.calcEnergyPumpDrain(ench.unbreaking, 1, 0)
                                drain = pos :: drain
                            } else {
                                val blockHardness = state.getBlockHardness(getWorld, pos)
                                if (blockHardness != -1 && !blockHardness.isInfinity) {
                                    state.getBlock match {
                                        case _ if TileAdvQuarry.noDigBLOCKS.exists(_.contain(state)) =>
                                            requireEnergy += PowerManager.calcEnergyBreak(blockHardness, 0, ench.unbreaking)
                                            destroy = pos :: destroy
                                        case leave: IShearable if leave.isLeaves(state, getWorld, pos) =>
                                            requireEnergy += PowerManager.calcEnergyBreak(blockHardness, ench.mode, ench.unbreaking)
                                            if (ench.silktouch)
                                                shear = pos :: shear
                                            else
                                                dig = pos :: dig
                                        case _ => requireEnergy += PowerManager.calcEnergyBreak(blockHardness, ench.mode, ench.unbreaking)
                                            dig = pos :: dig
                                    }
                                } else if (Config.content.removeBedrock && (state.getBlock == Blocks.BEDROCK) &&
                                  ((pos.getY > 0 && pos.getY <= 5) || (pos.getY > 122 && pos.getY < 127))) {
                                    if (Config.content.collectBedrock) {
                                        requireEnergy += 600
                                        dig = pos :: dig
                                    } else {
                                        requireEnergy += 200
                                        destroy = pos :: destroy
                                    }
                                } else if (state.getBlock == Blocks.PORTAL) {
                                    getWorld.setBlockToAir(pos)
                                    requireEnergy += 20
                                }
                            }

                            def checkandsetFrame(world: World, thatPos: BlockPos): Unit = {
                                if (TilePump.isLiquid(world.getBlockState(thatPos))) {
                                    world.setBlockState(thatPos, QuarryPlusI.blockFrame.getDammingState)
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
                            val fakePlayer = QuarryFakePlayer.get(getWorld.asInstanceOf[WorldServer])
                            fakePlayer.setHeldItem(EnumHand.MAIN_HAND, getEnchantedPickaxe)
                            if (ench.silktouch && state.getBlock.canSilkHarvest(getWorld, p, state, fakePlayer)) {
                                list.add(ReflectionHelper.invoke(TileBasic.createStackedBlock, state.getBlock, state).asInstanceOf[ItemStack])
                            } else {
                                TileBasic.getDrops(getWorld, p, state, state.getBlock, ench.fortune, list)
                            }
                            fakePlayer.setHeldItem(EnumHand.MAIN_HAND, VersionUtil.empty())
                            getWorld.setBlockState(p, Blocks.AIR.getDefaultState, 2)
                        })
                        if (shear.nonEmpty) {
                            val itemShear = new ItemStack(net.minecraft.init.Items.SHEARS)
                            EnchantmentHelper.setEnchantments(ench.getMap.collect { case (a, b) if b > 0 => (Enchantment.getEnchantmentByID(a), Int.box(b)) }.asJava, itemShear)
                            for (p <- shear) {
                                val state = getWorld.getBlockState(p)
                                val block = state.getBlock.asInstanceOf[Block with IShearable]
                                list.addAll(block.onSheared(itemShear, getWorld, p, ench.fortune))
                                getWorld.setBlockState(p, Blocks.AIR.getDefaultState, 2)
                            }
                        }
                        destroy.foreach(getWorld.setBlockState(_, Blocks.AIR.getDefaultState, 2))
                        for (p <- drain) {
                            val handler = Option(FluidUtil.getFluidHandler(getWorld, p, EnumFacing.UP))
                            val fluidOp = handler.map(_.getTankProperties.apply(0)).flatMap(p => Option(p.getContents))
                            fluidOp match {
                                case Some(fluidStack) => handler.map(_.drain(Fluid.BUCKET_VOLUME, false)).foreach(s => fluidStacks.get(fluidStack) match {
                                    case Some(tank) => tank.fill(s, true)
                                    case None => fluidStacks.put(fluidStack, new QuarryTank(s, Int.MaxValue))
                                })
                                case None => //QuarryPlus.LOGGER.error(s"Adv Fluid null, ${getWorld.getBlockState(p)}, ${FluidUtil.getFluidHandler(getWorld, p, EnumFacing.UP)}")
                            }
                            getWorld.setBlockState(p, Blocks.AIR.getDefaultState, 2)
                        }
                        list.asScala.foreach(cacheItems.add)
                        true
                    } else {
                        false
                    }
                }

                chunkLoad()
                val n = if (chunks.isEmpty) digRange.timeInTick else 1
                for (_ <- 0 until n) if (mode is TileAdvQuarry.BREAKBLOCK)
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
                for (_ <- 0 until 32 * digRange.timeInTick) {
                    if (mode is TileAdvQuarry.CHECKLIQUID) {
                        List.iterate(target.down(), target.getY - 1)(_.down()).filter(p => {
                            val state = getWorld.getBlockState(p)
                            !state.getBlock.isAir(state, getWorld, p) && TilePump.isLiquid(state)
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
                    val list = Range(0, handler.getSlots).find(i => {
                        val stack = handler.getStackInSlot(i)
                        VersionUtil.nonEmpty(stack) && stack.getItem.isInstanceOf[ItemBlock]
                    }).map(handler.extractItem(_, 64, false)).toList
                    list.flatMap(i => Range(0, i.getCount)).foreach(_ => {
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
                var is = cacheItems.remove(0)
                while (!break) {
                    val stack = InvUtils.injectToNearTile(getWorld, getPos, is)
                    if (stack.getCount > 0) {
                        cacheItems.add(stack)
                        break = true
                    }
                    if (isEmpty || break) {
                        break = true
                    } else {
                        is = cacheItems.remove(0)
                    }
                }
            }
        }
    }

    private def chunkLoad() = {
        if (chunks.nonEmpty) {
            val chunkPos = chunks.head
            val bool = getWorld.isChunkGeneratedAt(chunkPos.x, chunkPos.z)
            if (Config.content.debug) {
                QuarryPlus.LOGGER.debug("Chunk has already loaded : " + bool + chunkPos.x + chunkPos.z)
            }
            if (!bool)
                getWorld.getChunkFromChunkCoords(chunkPos.x, chunkPos.z)
            chunks = chunks.tail
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
        nbttc.getTagList("NBT_FLUIDLIST", Constants.NBT.TAG_COMPOUND).tagIterator.foreach(tag => {
            val tank = new QuarryTank(null, 0)
            CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.readNBT(tank, null, tag)
            if (tank.getFluid != null) {
                fluidStacks.put(tank.getFluid, tank)
            }
        })
        val l2 = nbttc.getTagList("NBT_CHUNKLOADLIST", Constants.NBT.TAG_DOUBLE)
        chunks = Range(0, l2.tagCount()).map(i => new ChunkPos(BlockPos.fromLong(l2.get(i).asInstanceOf[NBTTagLong].getLong))).toList
    }

    override def writeToNBT(nbttc: NBTTagCompound) = {
        ench.writeToNBT(nbttc)
        digRange.writeToNBT(nbttc)
        nbttc.setLong("NBT_TARGET", target.toLong)
        mode.writeToNBT(nbttc)
        cacheItems.writeToNBT(nbttc)
        val l1 = new NBTTagList
        fluidStacks.foreach { case (_, tank) => l1.appendTag(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.writeNBT(tank, null)) }
        nbttc.setTag("NBT_FLUIDLIST", l1)
        val l2 = new NBTTagList
        chunks.foreach(c => l2.appendTag(new NBTTagLong(c.getBlock(0, 0, 0).toLong)))
        nbttc.setTag("NBT_CHUNKLOADLIST", l2)
        super.writeToNBT(nbttc)
    }

    /**
      * @return Map (Enchantment id, level)
      */
    override def getEnchantments = ench.getMap.collect(enchantCollector).asJava

    /**
      * @param id    Enchantment id
      * @param value level
      */
    override def setEnchantent(id: Short, value: Short) = ench = ench.set(id, value)

    override def getEnchantedPickaxe: ItemStack = ench.pickaxe

    override def isItemValidForSlot(index: Int, stack: ItemStack) = false

    override def setInventorySlotContents(index: Int, stack: ItemStack) = {
        if (VersionUtil.nonEmpty(stack)) {
            QuarryPlus.LOGGER.warn("QuarryPlus WARN: call setInventorySlotContents with non empty ItemStack.")
        } else {
            removeStackFromSlot(index)
        }
    }

    override def decrStackSize(index: Int, count: Int) = cacheItems.decrease(index, count)

    override def getSizeInventory = Math.max(cacheItems.list.size, 1)

    override def removeStackFromSlot(index: Int) = cacheItems.remove(index)

    override val getInventoryStackLimit = 1

    override def clear() = cacheItems.list.clear()

    override def isEmpty = cacheItems.list.isEmpty

    override def getStackInSlot(index: Int) = cacheItems.getStack(index)

    override val getDebugName = TranslationKeys.advquarry

    override def isUsableByPlayer(player: EntityPlayer) = self.getWorld.getTileEntity(self.getPos) eq this

    override def getDebugmessages = {
        import scala.collection.JavaConverters._
        List("Items to extract = " + cacheItems.list.size,
            "Liquid to extract = " + fluidStacks.size,
            "Next target = " + target.toString,
            mode.toString,
            digRange.toString).map(toComponentString).asJava
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
            CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandlers(facing))
        } else
            super.getCapability(capability, facing)
    }

    override def hasFastRenderer: Boolean = true

    override def getRenderBoundingBox: AxisAlignedBB = {
        if (digRange.defined) digRange.rendrBox
        else super.getRenderBoundingBox
    }

    override def getMaxRenderDistanceSquared: Double = {
        if (digRange.defined) digRange.lengthSq
        else super.getMaxRenderDistanceSquared
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
        val link = getNeighbors(facing).map(getWorld.getTileEntity(_))
          .collectFirst { case m: TileMarker if m.link != null =>
              val poses = (m.min().add(+1, 0, +1), m.max().add(-1, 0, -1))
              m.removeFromWorldWithItem().asScala.foreach(cacheItems.add)
              poses
          }.getOrElse {
            val chunkPos = new ChunkPos(getPos)
            val y = getPos.getY
            (new BlockPos(chunkPos.getXStart, y, chunkPos.getZStart), new BlockPos(chunkPos.getXEnd, y, chunkPos.getZEnd))
        }
        new TileAdvQuarry.DigRange(link._1, link._2)
    }

    def digRange = mDigRange

    def digRange_=(@Nonnull digRange: TileAdvQuarry.DigRange): Unit = {
        require(digRange != null, "DigRange must not be null.")
        mDigRange = digRange
    }

    def stickActivated(player: EntityPlayer): Unit = {
        //Called when noEnergy is true and block is right clicked with stick (item)
        if (machineDisabled) {
            VersionUtil.sendMessage(player, new TextComponentString("ChunkDestroyer is disabled."), true)
        } else if (mode is TileAdvQuarry.NOTNEEDBREAK) {
            mode set TileAdvQuarry.MAKEFRAME
        }
    }

    def startFillMode(): Unit = {
        if ((mode is TileAdvQuarry.NONE) && digRange.defined && preparedFiller) {
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

    def preparedFiller: Boolean = {
        val y = if (Config.content.removeBedrock) 1 else 5
        if (BlockPos.getAllInBoxMutable(new BlockPos(digRange.minX, y, digRange.minZ), new BlockPos(digRange.maxX, y, digRange.maxZ))
          .iterator().asScala.forall(getWorld.isAirBlock)) {
            val need = (digRange.maxX - digRange.minX + 1) * (digRange.maxZ - digRange.minZ + 1)
            val stacks = InvUtils.findItemHander(getWorld, getPos.up, EnumFacing.DOWN).toList
              .flatMap(handler => Range(0, handler.getSlots).map(handler.getStackInSlot))
            val blocks = stacks.filter(s => VersionUtil.nonEmpty(s) && s.getItem.isInstanceOf[ItemBlock])
            blocks.nonEmpty &&
              stacks.forall(stack => VersionUtil.isEmpty(stack) || !stack.getItem.isInstanceOf[ItemBlock] || stack.isItemEqual(blocks.head)) &&
              blocks.map(_.getCount).sum >= need
        } else false
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

        override def getSlotLimit(slot: Int): Int = 1

        override def getSlots: Int = self.getSizeInventory

        override def insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack = stack
    }

    private[TileAdvQuarry] class FluidHandler(val facing: EnumFacing) extends IFluidHandler {
        //FluidHandlerFluidMap(fluidStacks.asJava) {

        def fluids = if (facing != null) fluidStacks.filterKeys(fluidExtractFacings(facing)) else fluidStacks.toMap

        /**
          * Not fillable.
          */
        override def fill(resource: FluidStack, doFill: Boolean): Int = 0

        override def getTankProperties: Array[IFluidTankProperties] = {
            val array = fluids.flatMap {
                case (_, handler) => Option(handler.drain(Int.MaxValue, false))
                  .collect { case s if s.amount > 0 => new FluidTankProperties(s, s.amount, false, true) }
                  .toList
            }.toArray
            if (array.length == 0) {
                IDummyFluidHandler.emptyPropertyArray
            } else {
                array.asInstanceOf[Array[IFluidTankProperties]]
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

        override def drain(resource: FluidStack, doDrain: Boolean): FluidStack = {
            //The map of fluid doesn't have duplicated key.
            Option(resource).filter(_.amount > 0).collect(fluids).map(_.drain(resource, doDrain)).orNull
        }

        override def drain(maxDrain: Int, doDrain: Boolean): FluidStack = {
            fluids.values.toStream.map(_.drain(maxDrain, doDrain)).find(nonNull).orNull
        }
    }

    private[TileAdvQuarry] class QuarryTank(s: FluidStack, a: Int) extends FluidTank(s, a) {
        setTileEntity(TileAdvQuarry.this)

        override def onContentsChanged(): Unit = {
            super.onContentsChanged()
            if (this.getFluidAmount == 0) {
                TileAdvQuarry.this.fluidStacks.retain { case (_, v) => v != this }
                if (!tile.getWorld.isRemote) {
                    PacketHandler.sendToAround(AdvContentMessage.create(TileAdvQuarry.this), getWorld, getPos)
                }
            }
        }
    }

    private[TileAdvQuarry] class Mode extends INBTWritable with INBTReadable[Mode] {

        import TileAdvQuarry._

        private[this] var mode: Modes = NONE

        def set(newmode: Modes): Unit = {
            mode = newmode
            val world1 = getWorld
            val pos1 = getPos
            if (!world1.isRemote) {
                energyConfigure()
                PacketHandler.sendToAround(AdvModeMessage.create(self), world1, pos1)
            }
            val state = world1.getBlockState(pos1)
            if (state.getValue(ACTING)) {
                if (newmode == NONE || newmode == NOTNEEDBREAK) {
                    validate()
                    world1.setBlockState(pos1, state.withProperty(ACTING, JBool.FALSE))
                    validate()
                    world1.setTileEntity(pos1, self)
                }
            } else {
                if (newmode != NONE && newmode != NOTNEEDBREAK) {
                    validate()
                    world1.setBlockState(pos1, state.withProperty(ACTING, JBool.TRUE))
                    validate()
                    world1.setTileEntity(pos1, self)
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
                    case _ => throw new IllegalStateException("Invalid mode")
                }
            }
            this
        }
    }

    override def getName = getDebugName

    override protected def getSymbol = TileAdvQuarry.SYMBOL
}

object TileAdvQuarry {
    final val SYMBOL = Symbol("ChunkDestroyer")

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
    val defaultRange: DigRange = new DigRange(BlockPos.ORIGIN, BlockPos.ORIGIN) {
        override val defined: Boolean = false
        override val toString: String = "Dig Range Not Defined"
        override val timeInTick = 0

        override def min: BlockPos = BlockPos.ORIGIN
    }

    private[TileAdvQuarry] case class QEnch(efficiency: Int, unbreaking: Int, fortune: Int, silktouch: Boolean) extends INBTWritable {

        require(efficiency >= 0 && unbreaking >= 0 && fortune >= 0, "Chunk Destroyer Enchantment error")
        val pickaxe = new ItemStack(net.minecraft.init.Items.DIAMOND_PICKAXE)
        EnchantmentHelper.setEnchantments(getMap.collect {
            case (id, level) if level > 0 => (Enchantment.getEnchantmentByID(id), Int.box(level))
        }.asJava, pickaxe)

        import IEnchantableTile._

        def set(id: Short, level: Int): QEnch = {
            id match {
                case EfficiencyID => this.copy(efficiency = level)
                case UnbreakingID => this.copy(unbreaking = level)
                case FortuneID => this.copy(fortune = level)
                case SilktouchID => this.copy(silktouch = level > 0)
                case _ => this
            }
        }

        def getMap = Map(EfficiencyID -> efficiency, UnbreakingID -> unbreaking,
            FortuneID -> fortune, SilktouchID -> silktouch.compare(false))

        val maxRecieve = if (efficiency >= 5) ENERGYLIMIT_LIST(5) / 10d else ENERGYLIMIT_LIST(efficiency) / 10d

        val mode: Byte = if (silktouch) -1 else fortune.toByte

        override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
            val t = new NBTTagCompound
            t.setInteger("efficiency", efficiency)
            t.setInteger("unbreaking", unbreaking)
            t.setInteger("fortune", fortune)
            t.setBoolean("silktouch", silktouch)
            nbt.setTag(NBT_QENCH, t)
            nbt
        }
    }

    object QEnch extends INBTReadable[QEnch] {
        override def readFromNBT(tag: NBTTagCompound): QEnch = {
            if (tag.hasKey(NBT_QENCH)) {
                val t = tag.getCompoundTag(NBT_QENCH)
                QEnch(t.getInteger("efficiency"), t.getInteger("unbreaking"), t.getInteger("fortune"), t.getBoolean("silktouch"))
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

        final val rendrBox = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ)

        final val lengthSq = min.distanceSq(maxZ, maxY, maxZ)

        val timeInTick: Int = {
            val length = (maxX + maxZ - minX - minZ) / 2
            if (length < 128) {
                1
            } else {
                length / 128
            }
        }

        def chunkSeq: List[ChunkPos] = {
            val a = for (x <- Range(minX, maxX, 16) :+ maxX;
                         z <- Range(minZ, maxZ, 16) :+ maxZ
            ) yield new ChunkPos(x >> 4, z >> 4)
            a.toList
        }

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
                    defaultRange
                }
            } else
                defaultRange
        }
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

        def add(stack: ItemStack): Unit = add(ItemDamage(stack), stack.getCount)

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

        def getStack(index: Int): ItemStack = {
            if (index < list.size)
                list(index).toStack
            else
                ItemStack.EMPTY
        }

        def remove(index: Int): ItemStack = {
            if (index < list.size)
                list.remove(index).toStack
            else
                ItemStack.EMPTY
        }

        override def toString: String = "ItemList size = " + list.size

        override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
            val t = new NBTTagCompound
            val l = new NBTTagList
            list.map(_.toNBT).foreach(l.appendTag(_))
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

        def toNBT = {
            val nbt = toStack.serializeNBT()
            nbt.removeTag("Count")
            nbt.setInteger("Count", count)
            nbt
        }

        override def toString: String = itemDamage.toString + " x" + count
    }

    private[TileAdvQuarry] case class BlockWrapper(state: IBlockState, ignoreProperty: Boolean = false, ignoreMeta: Boolean = false)
      extends java.util.function.Predicate[IBlockState] {

        def apply(v1: IBlockState): Boolean = contain(v1)

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

        override def test(t: IBlockState): Boolean = contain(t)
    }

    sealed class Modes(val index: Int, override val toString: String)

    val NONE = new Modes(0, "NONE")

    val NOTNEEDBREAK = new Modes(1, "NOTNEEDBREAK")

    val MAKEFRAME = new Modes(2, "MAKEFRAME")

    val BREAKBLOCK = new Modes(3, "BREAKBLOCK")

    val CHECKLIQUID = new Modes(4, "CHECKLIQUID")

    val FILLBLOCKS = new Modes(5, "FILLBLOCKS")

    def getFramePoses(digRange: DigRange): List[BlockPos] = {
        val builder = List.newBuilder[BlockPos]
        val minX = digRange.minX
        val maxX = digRange.maxX
        val maxY = digRange.maxY
        val minZ = digRange.minZ
        val maxZ = digRange.maxZ
        for (i <- 0 to 4) {
            builder += new BlockPos(minX - 1, maxY + 4 - i, minZ - 1)
            builder += new BlockPos(minX - 1, maxY + 4 - i, maxZ + 1)
            builder += new BlockPos(maxX + 1, maxY + 4 - i, maxZ + 1)
            builder += new BlockPos(maxX + 1, maxY + 4 - i, minZ - 1)
        }
        for (x <- minX to maxX) {
            builder += new BlockPos(x, maxY + 4, minZ - 1)
            builder += new BlockPos(x, maxY + 0, minZ - 1)
            builder += new BlockPos(x, maxY + 0, maxZ + 1)
            builder += new BlockPos(x, maxY + 4, maxZ + 1)
        }
        for (z <- minZ to maxZ) {
            builder += new BlockPos(minX - 1, maxY + 4, z)
            builder += new BlockPos(minX - 1, maxY + 0, z)
            builder += new BlockPos(maxX + 1, maxY + 0, z)
            builder += new BlockPos(maxX + 1, maxY + 4, z)
        }
        builder.result()
    }
}
