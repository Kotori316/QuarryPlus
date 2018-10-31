package com.yogpc.qp.tile

import java.lang.{Boolean => JBool}
import java.util.{Collections, Objects}

import com.yogpc.qp.block.{ADismCBlock, BlockBookMover}
import com.yogpc.qp.compat.{INBTReadable, INBTWritable, InvUtils}
import com.yogpc.qp.gui.TranslationKeys
import com.yogpc.qp.packet.PacketHandler
import com.yogpc.qp.packet.advquarry.{AdvContentMessage, AdvModeMessage}
import com.yogpc.qp.tile.IAttachment.Attachments
import com.yogpc.qp.tile.TileAdvQuarry._
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
import net.minecraft.item.crafting.FurnaceRecipes
import net.minecraft.item.{ItemBlock, ItemStack}
import net.minecraft.nbt.{NBTTagCompound, NBTTagList, NBTTagLong}
import net.minecraft.util.math.BlockPos.MutableBlockPos
import net.minecraft.util.math.{AxisAlignedBB, BlockPos, ChunkPos}
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.{EnumFacing, EnumHand, ITickable, NonNullList, ResourceLocation}
import net.minecraft.world.{World, WorldServer}
import net.minecraftforge.common.ForgeChunkManager.Type
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.Constants
import net.minecraftforge.common.{ForgeChunkManager, IShearable, MinecraftForge}
import net.minecraftforge.event.ForgeEventFactory
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.fluids.capability.{CapabilityFluidHandler, FluidTankProperties, IFluidHandler, IFluidTankProperties}
import net.minecraftforge.fluids.{FluidStack, FluidTank, FluidUtil}
import net.minecraftforge.fml.common.registry.ForgeRegistries
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import net.minecraftforge.items.{CapabilityItemHandler, IItemHandlerModifiable}

import scala.collection.JavaConverters._
import scala.collection.generic.Clearable
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

class TileAdvQuarry extends APowerTile with IEnchantableTile with HasInv with ITickable with IDebugSender with IChunkLoadTile with IAttachable {
    self =>
    private[this] var mDigRange = TileAdvQuarry.defaultRange
    private[this] var facingMap: Map[Attachments[_ <: APacketTile], EnumFacing] = Map.empty
    var ench = TileAdvQuarry.defaultEnch
    var target = BlockPos.ORIGIN
    var framePoses = List.empty[BlockPos]
    var chunks = List.empty[ChunkPos]
    val fluidStacks = scala.collection.mutable.Map.empty[FluidStack, FluidTank]
    val cacheItems = new ItemList
    val itemHandler = new ItemHandler
    val fluidHandlers = EnumFacing.VALUES.map(f => f -> new FluidHandler(facing = f)).toMap.withDefaultValue(new FluidHandler(null))
    val fluidExtractFacings = EnumFacing.VALUES.map(f => f -> scala.collection.mutable.Set.empty[FluidStack]).toMap
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
                            if (useEnergy(energy, energy, false, EnergyUsage.ADV_BREAK_BLOCK) == energy) {
                                useEnergy(energy, energy, true, EnergyUsage.ADV_BREAK_BLOCK)
                                list.add(ReflectionHelper.invoke(TileBasic.createStackedBlock, state.getBlock, state).asInstanceOf[ItemStack])
                                getWorld.setBlockToAir(target)
                            } else {
                                return
                            }
                        } else {
                            val energy = PowerManager.calcEnergyBreak(state.getBlockHardness(getWorld, target), ench.fortune, ench.unbreaking)
                            if (useEnergy(energy, energy, false, EnergyUsage.ADV_BREAK_BLOCK) == energy) {
                                useEnergy(energy, energy, true, EnergyUsage.ADV_BREAK_BLOCK)
                                TileBasic.getDrops(getWorld, target, state, state.getBlock, ench.fortune, list)
                                getWorld.setBlockToAir(target)
                            } else {
                                return
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

                var i = 0
                while (i < 4) {
                    if (mode is TileAdvQuarry.MAKEFRAME)
                        makeFrame()
                    i += 1
                }
            } else if (mode is TileAdvQuarry.BREAKBLOCK) {
                val x = target.getX
                val z = target.getZ

                @inline
                def breakBlocks(): Boolean = {
                    val list = NonNullList.create[ItemStack]()
                    val expPump = facingMap.get(Attachments.EXP_PUMP).map(f => getWorld.getTileEntity(getPos.offset(f)))
                      .collect { case pump: TileExpPump => pump }

                    if (x % 3 == 0) {
                        val axis = new AxisAlignedBB(new BlockPos(x - 6, 1, z - 6), target.add(6, 0, 6))
                        //catch dropped items
                        getWorld.getEntitiesWithinAABB(classOf[EntityItem], axis).asScala.filter(nonNull).filter(!_.isDead).foreach(entity => {
                            val drop = entity.getItem
                            if (drop.getCount > 0) {
                                entity.getEntityWorld.removeEntity(entity)
                                list.add(drop)
                            }
                        })
                        //remove XPs
                        getWorld.getEntitiesWithinAABB(classOf[EntityXPOrb], axis).asScala.filter(nonNull).filter(!_.isDead).foreach(entityXPOrb => {
                            expPump.foreach(_.addXp(entityXPOrb.xpValue))
                            entityXPOrb.getEntityWorld.removeEntity(entityXPOrb)
                        })
                    }

                    var destroy, dig, drain, shear = new mutable.WrappedArrayBuilder[Int](ClassTag.Int)
                    val flags = Array(x == digRange.minX, x == digRange.maxX, z == digRange.minZ, z == digRange.maxZ)
                    var requireEnergy = 0d
                    var y = target.getY - 1
                    val pos = new MutableBlockPos(x, y, z)
                    while (y > 0) {
                        pos.setY(y)
                        val state = getWorld.getBlockState(pos)
                        if (!state.getBlock.isAir(state, getWorld, pos)) {
                            if (TilePump.isLiquid(state)) {
                                requireEnergy += PowerManager.calcEnergyPumpDrain(ench.unbreaking, 1, 0)
                                drain += y
                            } else {
                                val blockHardness = state.getBlockHardness(getWorld, pos)
                                if (blockHardness != -1 && !blockHardness.isInfinity) {
                                    state.getBlock match {
                                        case _ if TileAdvQuarry.noDigBLOCKS.exists(_.contain(state)) =>
                                            requireEnergy += PowerManager.calcEnergyBreak(blockHardness, 0, ench.unbreaking)
                                            destroy += y
                                        case leave: IShearable if leave.isLeaves(state, getWorld, pos) =>
                                            requireEnergy += PowerManager.calcEnergyBreak(blockHardness, ench.mode, ench.unbreaking)
                                            if (ench.silktouch)
                                                shear += y
                                            else
                                                dig += y
                                        case _ => requireEnergy += PowerManager.calcEnergyBreak(blockHardness, ench.mode, ench.unbreaking)
                                            dig += y
                                    }
                                } else if (Config.content.removeBedrock && (state.getBlock == Blocks.BEDROCK) &&
                                  ((pos.getY > 0 && pos.getY <= 5) || (pos.getY > 122 && pos.getY < 127))) {
                                    if (Config.content.collectBedrock) {
                                        requireEnergy += 600
                                        dig += y
                                    } else {
                                        requireEnergy += 200
                                        destroy += y
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
                                    checkandsetFrame(getWorld, pos.offset(EnumFacing.NORTH, EnumFacing.WEST))
                                }
                                else if (flags(3)) { //+z, -x
                                    checkandsetFrame(getWorld, pos.offset(EnumFacing.SOUTH, EnumFacing.WEST))
                                }
                            }
                            else if (flags(1)) { //+x
                                checkandsetFrame(getWorld, pos.offset(EnumFacing.EAST))
                                if (flags(2)) { //-z, +x
                                    checkandsetFrame(getWorld, pos.offset(EnumFacing.NORTH, EnumFacing.EAST))
                                }
                                else if (flags(3)) { //+z, +x
                                    checkandsetFrame(getWorld, pos.offset(EnumFacing.SOUTH, EnumFacing.EAST))
                                }
                            }
                            if (flags(2)) { //-z
                                checkandsetFrame(getWorld, pos.offset(EnumFacing.NORTH))
                            }
                            else if (flags(3)) { //+z
                                checkandsetFrame(getWorld, pos.offset(EnumFacing.SOUTH))
                            }
                        }

                        y -= 1
                    }

                    requireEnergy *= 1.25
                    if (useEnergy(requireEnergy, requireEnergy, false, EnergyUsage.ADV_BREAK_BLOCK) == requireEnergy) {
                        useEnergy(requireEnergy, requireEnergy, true, EnergyUsage.ADV_BREAK_BLOCK)
                        val fakePlayer = QuarryFakePlayer.get(getWorld.asInstanceOf[WorldServer])
                        fakePlayer.setHeldItem(EnumHand.MAIN_HAND, getEnchantedPickaxe)
                        val collectFurnaceXP = InvUtils.hasSmelting(fakePlayer.getHeldItemMainhand) && expPump.isDefined
                        val tempList = new NotNullList(new ArrayBuffer[ItemStack]())
                        val toReplace = getFillBlock
                        val p = new MutableBlockPos(x, 0, z)
                        dig.result().foreach(y => {
                            p.setY(y)
                            val state = getWorld.getBlockState(p)
                            val event = new BlockEvent.BreakEvent(getWorld, p, state, fakePlayer)
                            MinecraftForge.EVENT_BUS.post(event)
                            if (!event.isCanceled) {
                                if (ench.silktouch && state.getBlock.canSilkHarvest(getWorld, p, state, fakePlayer)) {
                                    tempList.add(ReflectionHelper.invoke(TileBasic.createStackedBlock, state.getBlock, state).asInstanceOf[ItemStack])
                                } else {
                                    TileBasic.getDrops(getWorld, p, state, state.getBlock, ench.fortune, tempList)
                                }
                                tempList.fix = true
                                ForgeEventFactory.fireBlockHarvesting(tempList, getWorld, p, state, ench.fortune, 1f, ench.silktouch, fakePlayer)
                                list.addAll(tempList)
                                if (collectFurnaceXP)
                                    event.setExpToDrop(event.getExpToDrop + TileBasic.getSmeltingXp(tempList.fixing.asJava, Collections.emptyList()))
                                expPump.filter(xpFilter(event.getExpToDrop)).foreach(_.addXp(event.getExpToDrop))
                                tempList.clear()
                                setBlock(p, toReplace)
                            }
                        })
                        if (shear.result().nonEmpty) {
                            //Enchantment must be Silktouch.
                            val itemShear = new ItemStack(net.minecraft.init.Items.SHEARS)
                            EnchantmentHelper.setEnchantments(ench.getMap.collect { case (a, b) if b > 0 => (Enchantment.getEnchantmentByID(a), Int.box(b)) }.asJava, itemShear)
                            for (y <- shear.result()) {
                                p.setY(y)
                                val state = getWorld.getBlockState(p)
                                val block = state.getBlock.asInstanceOf[Block with IShearable]
                                val event = new BlockEvent.BreakEvent(getWorld, p, state, fakePlayer)
                                MinecraftForge.EVENT_BUS.post(event)
                                if (!event.isCanceled) {
                                    tempList.addAll(block.onSheared(itemShear, getWorld, p, ench.fortune))
                                    ForgeEventFactory.fireBlockHarvesting(tempList, getWorld, p, state, ench.fortune, 1f, ench.silktouch, fakePlayer)
                                    list.addAll(tempList)
                                    tempList.clear()
                                    setBlock(p, toReplace)
                                    expPump.filter(xpFilter(event.getExpToDrop)).foreach(_.addXp(event.getExpToDrop))
                                }
                            }
                        }
                        val l = new ItemList
                        destroy.result().foreach(y => {
                            p.setY(y)
                            val state = getWorld.getBlockState(p)
                            val event = new BlockEvent.BreakEvent(getWorld, p, state, fakePlayer)
                            MinecraftForge.EVENT_BUS.post(event)
                            if (!event.isCanceled) {
                                setBlock(p, toReplace)
                                if (collectFurnaceXP) {
                                    val nnl = new NotNullList(new ArrayBuffer[ItemStack]())
                                    TileBasic.getDrops(getWorld, p, state, state.getBlock, 0, nnl)
                                    nnl.seq.foreach(l.add)
                                }
                                expPump.filter(xpFilter(event.getExpToDrop)).foreach(_.addXp(event.getExpToDrop))
                            }
                        })
                        if (collectFurnaceXP) {
                            val xp = TileBasic.floorFloat(l.list.map(ie => FurnaceRecipes.instance().getSmeltingResult(ie.toStack) -> ie.count).collect {
                                case (s, i) if VersionUtil.nonEmpty(s) => FurnaceRecipes.instance().getSmeltingExperience(s) * i
                            }.sum)
                            expPump.filter(xpFilter(xp)).foreach(_.addXp(xp))
                        }
                        for (y <- drain.result()) {
                            p.setY(y)
                            val handler = Option(FluidUtil.getFluidHandler(getWorld, p, EnumFacing.UP))
                            val fluidOp = handler.flatMap(_.getTankProperties.apply(0).nnMap(_.getContents))
                            fluidOp match {
                                case Some(fluidStack) => handler.flatMap(_.drain(fluidStack.amount, false).toOption).foreach(s => fluidStacks.get(fluidStack) match {
                                    case Some(tank) => tank.fill(s, true)
                                    case None => fluidStacks.put(fluidStack, new QuarryTank(s, Int.MaxValue))
                                })
                                case None => //QuarryPlus.LOGGER.error(s"Adv Fluid null, ${getWorld.getBlockState(p)}, ${FluidUtil.getFluidHandler(getWorld, p, EnumFacing.UP)}")
                            }
                            getWorld.setBlockState(p, Blocks.AIR.getDefaultState, 2)
                        }
                        fakePlayer.setHeldItem(EnumHand.MAIN_HAND, VersionUtil.empty())
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
                            if (PowerManager.useEnergyAdvSearch(this, ench.unbreaking, target.getY)) {
                                val x = target.getX + 1
                                if (x > digRange.maxX) {
                                    val z = target.getZ + 1
                                    if (z > digRange.maxZ) {
                                        //Finished.
                                        target = digRange.min
                                        finishWork()
                                        mode set TileAdvQuarry.CHECKLIQUID
                                    } else {
                                        target = new BlockPos(digRange.minX, target.getY, z)
                                    }
                                } else {
                                    target = new BlockPos(x, target.getY, target.getZ)
                                }
                            } else i = 33 //Finish searching.
                        } while (i < 32 && {
                            val p = new MutableBlockPos(target)
                            Range.inclusive(1, target.getY).forall { i => p.setY(i); getWorld.isAirBlock(p) }
                        })
                    }

            } else if (mode is TileAdvQuarry.NOTNEEDBREAK) {
                if (digRange.defined && !Config.content.noEnergy)
                    if (getStoredEnergy > getMaxStored * 0.3) {
                        mode set TileAdvQuarry.MAKEFRAME
                        startWork()
                    }
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

    private def setBlock(pos: BlockPos, state: IBlockState): Unit = {
        val i = if (state.isFullCube) 2 else 3
        getWorld.setBlockState(pos, state, i)
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
            configure(0, getMaxStored)
        } else if (mode.reduceRecieve) {
            configure(ench.maxRecieve / 128, ench.maxStore)
        } else {
            configure(ench.maxRecieve, ench.maxStore)
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
        val l2 = nbttc.getTagList("NBT_CHUNKLOADLIST", Constants.NBT.TAG_LONG)
        chunks = Range(0, l2.tagCount()).map(i => new ChunkPos(BlockPos.fromLong(l2.get(i).asInstanceOf[NBTTagLong].getLong))).toList
    }

    override def writeToNBT(nbttc: NBTTagCompound) = {
        ench.writeToNBT(nbttc)
        digRange.writeToNBT(nbttc)
        nbttc.setLong("NBT_TARGET", target.toLong)
        mode.writeToNBT(nbttc)
        cacheItems.writeToNBT(nbttc)
        nbttc.setTag("NBT_FLUIDLIST", (new NBTTagList).tap(tagList => fluidStacks.foreach {
            case (_, tank) => tagList.appendTag(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.writeNBT(tank, null))
        }))
        nbttc.setTag("NBT_CHUNKLOADLIST", (new NBTTagList).tap(tagList => chunks.foreach(c => tagList.appendTag(c.getBlock(0, 0, 0).toLong.toNBT))))
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
            digRange.toString,
            ench.toString).map(toComponentString).asJava
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
            startWork()
        }
    }

    def startFillMode(): Unit = {
        if ((mode is TileAdvQuarry.NONE) && digRange.defined && preparedFiller) {
            mode set TileAdvQuarry.FILLBLOCKS
            target = digRange.min
        }
    }

    override def connectAttachment(facing: EnumFacing, attachments: Attachments[_ <: APacketTile]): Boolean = {
        if (!facingMap.contains(attachments)) {
            facingMap = facingMap.updated(attachments, facing)
            true
        } else {
            val t = getWorld.getTileEntity(getPos.offset(facingMap(attachments)))
            if (!attachments.test(t)) {
                facingMap = facingMap.updated(attachments, facing)
                true
            } else {
                facingMap(attachments) == facing
            }
        }
    }

    override def isValidAttachment(attachments: Attachments[_ <: APacketTile]): Boolean = VALID_ATTACHMENTS(attachments)

    private def getFillBlock: IBlockState = {
        facingMap.get(Attachments.REPLACER)
          .flatMap(f => getWorld.getTileEntity(getPos.offset(f)).toOption)
          .collect { case t if Attachments.REPLACER.test(t) => Attachments.REPLACER.apply(t) }
          .fold(Blocks.AIR.getDefaultState)(_.getReplaceState)
    }

    @SideOnly(Side.CLIENT)
    def recieveModeMessage(modeTag: NBTTagCompound): Runnable = new Runnable {
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
                cacheItems.list.applyOrElse(slot, (_: Int) => ItemElement(ItemDamage.invalid, 1)) match {
                    case ItemElement(i, size) => i.toStack(Math.min(amount, Math.min(size, i.itemStackLimit)))
                }
            } else {
                self.decrStackSize(slot, amount)
            }
        }

        override def getSlotLimit(slot: Int): Int = 1

        override def getSlots: Int = self.getSizeInventory

        override def insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack = stack

        override def isItemValid(slot: Int, stack: ItemStack): Boolean = isItemValidForSlot(slot, stack)
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
                self.fluidStacks.retain { case (_, v) => v != this }
                if (!tile.getWorld.isRemote) {
                    PacketHandler.sendToAround(AdvContentMessage.create(self), getWorld, getPos)
                }
            }
        }

        override def toString: String = {
            if (fluid == null) {
                "QuarryTank(null, 0)"
            } else {
                "QuarryTank(" + fluid.getLocalizedName + ", " + getFluidAmount + ")"
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
            nbt.setTag(NBT_MODE, (new NBTTagCompound).tap(_.setInteger("mode", mode.index)))
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

    private final val MAX_STORED = 300 * 256
    final val noDigBLOCKS = Set(
        BlockWrapper(Blocks.STONE.getDefaultState, ignoreMeta = true),
        BlockWrapper(Blocks.COBBLESTONE.getDefaultState),
        BlockWrapper(Blocks.DIRT.getDefaultState, ignoreProperty = true),
        BlockWrapper(Blocks.GRASS.getDefaultState, ignoreProperty = true),
        BlockWrapper(Blocks.NETHERRACK.getDefaultState),
        BlockWrapper(Blocks.SANDSTONE.getDefaultState, ignoreMeta = true),
        BlockWrapper(Blocks.RED_SANDSTONE.getDefaultState, ignoreMeta = true))
    private final val NBT_QENCH = "nbt_qench"
    private final val NBT_DIGRANGE = "nbt_digrange"
    private final val NBT_MODE = "nbt_quarrymode"
    private final val NBT_ITEMLIST = "nbt_itemlist"
    private final val NBT_ITEMELEMENTS = "nbt_itemelements"
    final val VALID_ATTACHMENTS: Set[Attachments[_]] = Set(Attachments.EXP_PUMP, Attachments.REPLACER)

    val defaultEnch = QEnch(efficiency = 0, unbreaking = 0, fortune = 0, silktouch = false)
    val defaultRange: DigRange = new DigRange(BlockPos.ORIGIN, BlockPos.ORIGIN) {
        override val defined: Boolean = false
        override val toString: String = "Dig Range Not Defined"
        override val timeInTick = 0

        override def min: BlockPos = BlockPos.ORIGIN
    }

    private[TileAdvQuarry] case class QEnch(efficiency: Int, unbreaking: Int, fortune: Int, silktouch: Boolean, other: Map[Int, Int] = Map.empty) extends INBTWritable {

        require(efficiency >= 0 && unbreaking >= 0 && fortune >= 0,
            s"Chunk Destroyer Enchantment error with Efficiency $efficiency, Unbreaking $unbreaking, Fortune $fortune, Silktouch $silktouch, other $other")
        val pickaxe = new ItemStack(net.minecraft.init.Items.DIAMOND_PICKAXE)
        private val pf: PartialFunction[(Int, Int), (Enchantment, Integer)] = {
            case (id, level) if level > 0 && (Config.content.enableMap(BlockBookMover.SYMBOL) || IEnchantableTile.isValidEnch.test(id, level))
            => (Enchantment.getEnchantmentByID(id), Int.box(level))
        }
        EnchantmentHelper.setEnchantments((getMap collect pf).asJava, pickaxe)

        import IEnchantableTile._

        def set(id: Short, level: Int): QEnch = {
            id match {
                case EfficiencyID => this.copy(efficiency = level)
                case UnbreakingID => this.copy(unbreaking = level)
                case FortuneID => this.copy(fortune = level)
                case SilktouchID => this.copy(silktouch = level > 0)
                case _ => this.copy(other = other + (id.toInt -> level))
            }
        }

        def getMap: Map[Int, Int] = Map(EfficiencyID -> efficiency, UnbreakingID -> unbreaking,
            FortuneID -> fortune, SilktouchID -> silktouch.compare(false)) ++ other

        val maxStore = MAX_STORED * (efficiency + 1)

        val maxRecieve = if (efficiency >= 5) maxStore else if (efficiency == 0) maxStore * 0.001 else maxStore * Math.pow(efficiency.toDouble / 5.0, 3)

        val mode: Byte = if (silktouch) -1 else fortune.toByte

        override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
            val t = new NBTTagCompound
            t.setInteger("efficiency", efficiency)
            t.setInteger("unbreaking", unbreaking)
            t.setInteger("fortune", fortune)
            t.setBoolean("silktouch", silktouch)
            val o = new NBTTagCompound
            other.map { case (i, l) => Option(Enchantment.getEnchantmentByID(i)) -> l }.foreach {
                case (Some(e), l) => o.setInteger(e.getRegistryName.toString, l)
                case _ =>
            }
            t.setTag("other", o)
            nbt.setTag(NBT_QENCH, t)
            nbt
        }
    }

    object QEnch extends INBTReadable[QEnch] {
        override def readFromNBT(tag: NBTTagCompound): QEnch = {
            if (tag.hasKey(NBT_QENCH)) {
                val t = tag.getCompoundTag(NBT_QENCH)
                val o = t.getCompoundTag("other")
                val otherMap = o.getKeySet.asScala.map(s => ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(s)) -> o.getInteger(s))
                  .collect { case (e, l) if e != null => Enchantment.getEnchantmentID(e) -> l }.toMap
                QEnch(t.getInteger("efficiency"), t.getInteger("unbreaking"), t.getInteger("fortune"), t.getBoolean("silktouch"), otherMap)
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
        val list = ArrayBuffer.empty[ItemElement]

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
            if (list.isDefinedAt(index)) {
                val t = list(index)
                val min = Math.min(count, t.itemDamage.itemStackLimit)
                if (t.count <= min) {
                    list.remove(index)
                    t.itemDamage.toStack(t.count)
                } else {
                    list(index) = ItemElement(t.itemDamage, t.count - min)
                    t.itemDamage.toStack(min)
                }
            } else {
                VersionUtil.empty()
            }
        }

        def getStack(index: Int): ItemStack = {
            if (list.isDefinedAt(index))
                list(index).toStack
            else
                VersionUtil.empty()
        }

        def remove(index: Int): ItemStack = {
            if (list.isDefinedAt(index))
                list.remove(index).toStack
            else
                VersionUtil.empty()
        }

        override def toString: String = "ItemList size = " + list.size

        override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
            val t = new NBTTagCompound
            val l = (new NBTTagList).tap(l => list.map(_.toNBT).foreach(l.appendTag))
            t.setTag(NBT_ITEMELEMENTS, l)
            val l2 = (new NBTTagList).tap(l => list.foreach(i => l.appendTag(i.toNBT)))
            nbt.setTag(NBT_ITEMLIST, (new NBTTagCompound).tap(t => t.setTag(NBT_ITEMELEMENTS, l2)))
            nbt
        }

        override def readFromNBT(tag: NBTTagCompound): ItemList = {
            if (tag.hasKey(NBT_ITEMLIST)) {
                val l = tag.getCompoundTag(NBT_ITEMLIST).getTagList(NBT_ITEMELEMENTS, Constants.NBT.TAG_COMPOUND)
                l.tagIterator.foreach(t => add(VersionUtil.fromNBTTag(t)))
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

    private[TileAdvQuarry] class NotNullList(val seq: mutable.Buffer[ItemStack] with Clearable) extends NonNullList[ItemStack](seq.asJava, null) {
        var fix = false
        val fixing = ArrayBuffer.empty[ItemStack]

        override def clear(): Unit = {
            seq.clear()
            fix = false
            fixing.clear()
        }

        override def add(e: ItemStack): Boolean = {
            seq.append(Objects.requireNonNull(e))
            if (fix) fixing += e
            true
        }

        override def add(i: Int, e: ItemStack): Unit = {
            seq.insert(i, Objects.requireNonNull(e))
            if (fix) fixing += e
        }

        override def set(i: Int, e: ItemStack): ItemStack = {
            if (fix) fixing += e
            super.set(i, e)
        }
    }

    private class Modes(val index: Int, override val toString: String)

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

    def xpFilter(i: Int): Any => Boolean = _ => i > 0
}
