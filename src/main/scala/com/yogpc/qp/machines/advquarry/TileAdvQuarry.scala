package com.yogpc.qp.machines.advquarry

import java.util
import java.util.Collections

import com.yogpc.qp.compat.{FluidStore, InvUtils}
import com.yogpc.qp.machines.base.IAttachment.Attachments
import com.yogpc.qp.machines.base.{APacketTile, APowerTile, EnergyUsage, HasInv, IAttachable, IChunkLoadTile, IDebugSender, IEnchantableTile, IMarker, QPBlock}
import com.yogpc.qp.machines.bookmover.BlockBookMover
import com.yogpc.qp.machines.exppump.TileExpPump
import com.yogpc.qp.machines.pump.TilePump
import com.yogpc.qp.machines.quarry.{QuarryFakePlayer, TileBasic}
import com.yogpc.qp.machines.{PowerManager, TranslationKeys}
import com.yogpc.qp.packet.PacketHandler
import com.yogpc.qp.packet.advquarry.AdvModeMessage
import com.yogpc.qp.utils._
import com.yogpc.qp.{Config, QuarryPlus, _}
import javax.annotation.Nonnull
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.enchantment.{Enchantment, EnchantmentHelper}
import net.minecraft.entity.item.{EntityItem, EntityXPOrb}
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.init.Blocks
import net.minecraft.inventory.Container
import net.minecraft.item.{ItemBlock, ItemStack}
import net.minecraft.nbt.{NBTTagCompound, NBTTagList, NBTTagLong}
import net.minecraft.state.properties.BlockStateProperties
import net.minecraft.util._
import net.minecraft.util.math.BlockPos.MutableBlockPos
import net.minecraft.util.math.{AxisAlignedBB, BlockPos, ChunkPos}
import net.minecraft.util.text.{ITextComponent, TextComponentString, TextComponentTranslation}
import net.minecraft.world.{IInteractionObject, World, WorldServer}
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.{Constants, LazyOptional}
import net.minecraftforge.common.{IShearable, MinecraftForge}
import net.minecraftforge.event.ForgeEventFactory
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.fluids.{FluidStack, FluidTank}
import net.minecraftforge.items.wrapper.EmptyHandler
import net.minecraftforge.items.{CapabilityItemHandler, IItemHandlerModifiable}
import net.minecraftforge.registries.ForgeRegistries

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

class TileAdvQuarry extends APowerTile(Holder.advQuarryType)
  with IEnchantableTile
  with HasInv
  with ITickable
  with IDebugSender
  with IChunkLoadTile
  with IAttachable
  with IInteractionObject {
  self =>

  import TileAdvQuarry._

  private[this] var mDigRange = TileAdvQuarry.defaultRange
  private[this] var facingMap: Map[Attachments[_ <: APacketTile], EnumFacing] = Map.empty
  var ench: QEnch = TileAdvQuarry.defaultEnch
  var target: BlockPos = BlockPos.ORIGIN
  var framePoses = List.empty[BlockPos]
  var chunks = List.empty[ChunkPos]
  var yLevel = 1
  val fluidStacks = scala.collection.mutable.Map.empty[FluidStack, FluidTank]
  val cacheItems = new ItemList
  val itemHandler = new ItemHandler
  //  val fluidHandlers: Map[EnumFacing, FluidHandler] = EnumFacing.values().map(f => f -> new FluidHandler(facing = f)).toMap.withDefaultValue(new FluidHandler(null))
  val fluidExtractFacings: Map[EnumFacing, mutable.Set[FluidStack]] = EnumFacing.values().map(f => f -> scala.collection.mutable.Set.empty[FluidStack]).toMap
  val mode = new Mode

  override def tick(): Unit = {
    super.tick()
    if (!getWorld.isRemote && !machineDisabled) {
      if (mode is TileAdvQuarry.MAKE_FRAME) {
        @inline
        def makeFrame(): Unit = {
          if (target == getPos) {
            target = nextFrameTarget
            return
          } else if (!getWorld.isAirBlock(target)) {
            val list = NonNullList.create[ItemStack]
            val state = getWorld.getBlockState(target)

            if (state.getBlock == Holder.blockFrame) {
              target = nextFrameTarget
              return
            }

            if (ench.silktouch && state.getBlock.canSilkHarvest(state, getWorld, target, null)) {
              val energy = PowerManager.calcEnergyBreak(state.getBlockHardness(getWorld, target), -1, ench.unbreaking)
              if (useEnergy(energy, energy, false, EnergyUsage.ADV_BREAK_BLOCK) == energy) {
                useEnergy(energy, energy, true, EnergyUsage.ADV_BREAK_BLOCK)
                list.add(APacketTile.invoke(TileBasic.createStackedBlock, classOf[ItemStack], state.getBlock, state))
                getWorld.removeBlock(target)
              } else {
                return
              }
            } else {
              val energy = PowerManager.calcEnergyBreak(state.getBlockHardness(getWorld, target), ench.fortune, ench.unbreaking)
              if (useEnergy(energy, energy, false, EnergyUsage.ADV_BREAK_BLOCK) == energy) {
                useEnergy(energy, energy, true, EnergyUsage.ADV_BREAK_BLOCK)
                state.getDrops(list, getWorld, target, ench.fortune)
                getWorld.removeBlock(target)
              } else {
                return
              }
            }
            list.asScala.foreach(cacheItems.add)
          }

          if (PowerManager.useEnergyFrameBuild(self, ench.unbreaking)) {
            getWorld.setBlockState(target, Holder.blockFrame.getDefaultState)
            target = nextFrameTarget
          }
        }

        @inline
        def nextFrameTarget: BlockPos = {
          framePoses match {
            case p :: rest => framePoses = rest; p
            case Nil => mode set TileAdvQuarry.BREAK_BLOCK; digRange.min
          }
        }

        if (framePoses.isEmpty) {
          val headTail = TileAdvQuarry.getFramePoses(digRange)
          target = headTail.head
          framePoses = headTail.tail
          chunks = digRange.chunkSeq
        }
        chunkLoad()

        var i = 0
        while (i < 4 * digRange.timeInTick) {
          if (mode is TileAdvQuarry.MAKE_FRAME)
            makeFrame()
          i += 1
        }
      } else if (mode is TileAdvQuarry.BREAK_BLOCK) {

        type B_1 = (NonNullList[ItemStack], Seq[Int], Seq[Int], Seq[Int], Seq[Int], Long)
        type C_1 = (NonNullList[ItemStack], Seq[Int], Seq[Int], Seq[Int], Seq[Int])
        type D_1 = (NonNullList[ItemStack], Seq[Reason])
        val dropCheck: () => Either[Reason, NonNullList[ItemStack]] = () => {
          val x = target.getX
          val z = target.getZ
          if (x % 3 == 0) {
            val list = NonNullList.create[ItemStack]()
            val expPump = facingMap.get(Attachments.EXP_PUMP).map(f => getWorld.getTileEntity(getPos.offset(f)))
              .collect { case pump: TileExpPump => pump }
            val axis = new AxisAlignedBB(new BlockPos(x - 6, 1, z - 6), target.add(6, 0, 6))
            //catch dropped items
            getWorld.getEntitiesWithinAABB(classOf[EntityItem], axis).asScala.filter(nonNull).filter(_.isAlive)
              .filter(_.getItem.getCount > 0).foreach(entity => {
              QuarryPlus.proxy.removeEntity(entity)
              list.add(entity.getItem)
            })
            //remove XPs
            getWorld.getEntitiesWithinAABB(classOf[EntityXPOrb], axis).asScala.filter(nonNull).filter(_.isAlive).foreach(entityXPOrb => {
              expPump.foreach(_.addXp(entityXPOrb.xpValue))
              QuarryPlus.proxy.removeEntity(entityXPOrb)
            })
            Right(list)
          } else {
            Right(NonNullList.create())
          }
        }

        val calcBreakEnergy: NonNullList[ItemStack] => Either[Reason, B_1] = list => {
          val destroy, dig, drain, shear = new mutable.WrappedArrayBuilder[Int](ClassTag.Int)
          var requireEnergy = 0d
          val x = target.getX
          var y = target.getY - 1
          val z = target.getZ
          val pos = new MutableBlockPos(x, y, z)
          val flags = Array(x == digRange.minX, x == digRange.maxX, z == digRange.minZ, z == digRange.maxZ)
          while (y >= yLevel) {
            pos.setY(y)

            val state = getWorld.getBlockState(pos)
            if (!state.isAir(getWorld, pos)) {
              if (TilePump.isLiquid(state)) {
                requireEnergy += PowerManager.calcEnergyPumpDrain(ench.unbreaking, 1, 0)
                drain += y
              } else {
                val blockHardness = state.getBlockHardness(getWorld, pos)
                if (blockHardness != -1 && !blockHardness.isInfinity) {
                  (state.getBlock match {
                    case _ if Config.common.noDigBLOCKS.exists(_.contain(state)) => (0, destroy)
                    //                    case leave: IShearable if leave.isLeaves(state, getWorld, pos) && ench.silktouch => (ench.mode, shear)
                    case _ => (ench.mode, dig)
                  }) match {
                    case (m, seq) =>
                      requireEnergy += PowerManager.calcEnergyBreak(blockHardness, m, ench.unbreaking)
                      seq += y
                  }
                } else if (Config.common.removeBedrock.get() && (state.getBlock == Blocks.BEDROCK) &&
                  ((pos.getY > 0 && pos.getY <= 5) || (pos.getY > 122 && pos.getY < 127))) {
                  if (Config.common.collectBedrock.get()) {
                    requireEnergy += 600
                    dig += y
                  } else {
                    requireEnergy += 200
                    destroy += y
                  }
                } else if (state.getBlock == Blocks.NETHER_PORTAL) {
                  getWorld.removeBlock(pos)
                  requireEnergy += 20
                }
              }
            }
            if (flags.exists(identity)) {
              if (flags(0)) { //-x
                checkAndSetFrame(getWorld, pos.offset(EnumFacing.WEST))
                if (flags(2)) { //-z, -x
                  checkAndSetFrame(getWorld, pos.offset(EnumFacing.NORTH, EnumFacing.WEST))
                }
                else if (flags(3)) { //+z, -x
                  checkAndSetFrame(getWorld, pos.offset(EnumFacing.SOUTH, EnumFacing.WEST))
                }
              }
              else if (flags(1)) { //+x
                checkAndSetFrame(getWorld, pos.offset(EnumFacing.EAST))
                if (flags(2)) { //-z, +x
                  checkAndSetFrame(getWorld, pos.offset(EnumFacing.NORTH, EnumFacing.EAST))
                }
                else if (flags(3)) { //+z, +x
                  checkAndSetFrame(getWorld, pos.offset(EnumFacing.SOUTH, EnumFacing.EAST))
                }
              }
              if (flags(2)) { //-z
                checkAndSetFrame(getWorld, pos.offset(EnumFacing.NORTH))
              }
              else if (flags(3)) { //+z
                checkAndSetFrame(getWorld, pos.offset(EnumFacing.SOUTH))
              }
            }
            y -= 1
          }
          Right((list, destroy.result(), dig.result(), drain.result(), shear.result(), (requireEnergy * 1.25).toLong))
        }

        val consumeEnergy: B_1 => Either[Reason, C_1] = b => {
          val (list, destroy, dig, drain, rest, energy) = b
          if (useEnergy(energy, energy, false, EnergyUsage.ADV_BREAK_BLOCK) == energy) {
            useEnergy(energy, energy, true, EnergyUsage.ADV_BREAK_BLOCK)
            Right(list, destroy, dig, drain, rest)
          } else {
            Left(Reason(EnergyUsage.ADV_BREAK_BLOCK, energy, getStoredEnergy))
          }
        }

        val digging: C_1 => Either[Reason, D_1] = c => {
          val (list, destroy, dig, drain, shear) = c
          val expPump = facingMap.get(Attachments.EXP_PUMP).map(f => getWorld.getTileEntity(getPos.offset(f)))
            .collect { case pump: TileExpPump => pump }
          val fakePlayer = QuarryFakePlayer.get(getWorld.asInstanceOf[WorldServer])
          fakePlayer.setHeldItem(EnumHand.MAIN_HAND, getEnchantedPickaxe)
          val collectFurnaceXP = InvUtils.hasSmelting(fakePlayer.getHeldItemMainhand) && expPump.isDefined
          val tempList = new NotNullList(new ArrayBuffer[ItemStack]())
          val toReplace = getFillBlock
          val p = new MutableBlockPos(target.getX, 0, target.getZ)

          val reasons = new ArrayBuffer[Reason](0)
          dig.foreach { y =>
            p.setY(y)
            val state = getWorld.getBlockState(p)
            breakEvent(p, state, fakePlayer, expPump) { event =>
              if (ench.silktouch && state.getBlock.canSilkHarvest(state, getWorld, p, fakePlayer)) {
                tempList.add(APacketTile.invoke(TileBasic.createStackedBlock, classOf[ItemStack], state.getBlock, state))
              } else {
                state.getDrops(tempList, getWorld, p, ench.fortune)
              }
              tempList.fix = true
              ForgeEventFactory.fireBlockHarvesting(tempList, getWorld, p, state, ench.fortune, 1f, ench.silktouch, fakePlayer)
              list.addAll(tempList)
              if (collectFurnaceXP)
                event.setExpToDrop(event.getExpToDrop + TileBasic.getSmeltingXp(tempList.fixing.asJava, Collections.emptyList(), world))
              tempList.clear()
              setBlock(p, toReplace)
            } ++=: reasons
          }
          if (shear.nonEmpty) {
            //Enchantment must be Silktouch.
            val itemShear = new ItemStack(net.minecraft.init.Items.SHEARS)
            EnchantmentHelper.setEnchantments(ench.getMap.collect { case (a, b) if b > 0 => (ForgeRegistries.ENCHANTMENTS.getValue(a), Int.box(b)) }.asJava, itemShear)
            for (y <- shear) {
              p.setY(y)
              val state = getWorld.getBlockState(p)
              val block = state.getBlock.asInstanceOf[Block with IShearable]
              breakEvent(p, state, fakePlayer, expPump) { _ =>
                tempList.addAll(block.onSheared(itemShear, getWorld, p, ench.fortune))
                ForgeEventFactory.fireBlockHarvesting(tempList, getWorld, p, state, ench.fortune, 1f, ench.silktouch, fakePlayer)
                list.addAll(tempList)
                tempList.clear()
                setBlock(p, toReplace)
              } ++=: reasons
            }
          }
          val l = new ItemList
          destroy.foreach { y =>
            p.setY(y)
            val state = getWorld.getBlockState(p)
            breakEvent(p, state, fakePlayer, expPump) { _ =>
              setBlock(p, toReplace)
              if (collectFurnaceXP) {
                val nnl = new NotNullList(new ArrayBuffer[ItemStack]())
                state.getDrops(nnl, getWorld, p, 0)
                nnl.seq.foreach(l.add)
                // adding exp to pump is under.
              }
            } ++=: reasons
          }
          if (collectFurnaceXP) {
            val xp = TileBasic.getSmeltingXp(l.list.map(_.toStack).asJavaCollection, Collections.emptyList(), getWorld)
            expPump.filter(xpFilter(xp)).foreach(_.addXp(xp))
          }
          for (y <- drain) {
            p.setY(y)
            //            val handler = Option(FluidUtil.getFluidHandler(getWorld, p, EnumFacing.UP).orElse(EmptyFluidHandler.INSTANCE))
            //            val fluidOp = handler.flatMap(h => Option(h.getTankProperties.apply(0))).map(_.getContents)
            //            fluidOp match {
            //              case Some(fluidStack) => handler.flatMap(s => Option(s.drain(fluidStack.amount, false))).foreach(s => fluidStacks.get(fluidStack) match {
            //                case Some(tank) => tank.fill(s, true)
            //                case None => fluidStacks.put(fluidStack, new QuarryTank(s, Int.MaxValue))
            //              })
            //              case None => //QuarryPlus.LOGGER.error(s"Adv Fluid null, ${getWorld.getBlockState(p)}, ${FluidUtil.getFluidHandler(getWorld, p, EnumFacing.UP)}")
            //            }
            val fluidState = getWorld.getFluidState(p)
            if (fluidState.isSource) FluidStore.injectToNearTile(getWorld, getPos, fluidState.getFluid)
            getWorld.setBlockState(p, Blocks.AIR.getDefaultState, 0x10 | 0x2)
          }
          fakePlayer.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY)
          Right(list, reasons)
        }

        chunkLoad()
        val n = if (chunks.isEmpty) digRange.timeInTick else 1
        var j = 0
        var notEnoughEnergy = false
        while (j < n && (mode is TileAdvQuarry.BREAK_BLOCK) && !notEnoughEnergy) {
          (for (a <- dropCheck().right;
                b <- calcBreakEnergy(a).right;
                c <- consumeEnergy(b).right;
                d <- digging(c).right) yield d)
            .right.map { case (l, reasons) =>
            @tailrec
            def next(stream: Stream[((BlockPos, BlockPos), Int)], reasons: List[Reason] = Nil): (Option[BlockPos], Seq[Reason]) = {
              stream match {
                case ((pre, newPos), index) #:: rest =>
                  if (pre == newPos) {
                    Some(BlockPos.ORIGIN) -> reasons
                  } else {
                    val energy = PowerManager.calcEnergyAdvSearch(ench.unbreaking, newPos.getY - yLevel + 1)
                    if (notEnoughEnergy || useEnergy(energy, energy, false, EnergyUsage.ADV_CHECK_BLOCK) != energy) {
                      notEnoughEnergy = true
                      if (index == 0)
                        None -> Seq(Reason(EnergyUsage.ADV_CHECK_BLOCK, energy, getStoredEnergy, index))
                      else
                        Some(pre) -> (Reason(EnergyUsage.ADV_CHECK_BLOCK, energy, getStoredEnergy, index) :: reasons)
                    } else {
                      useEnergy(energy, energy, true, EnergyUsage.ADV_CHECK_BLOCK)
                      if (index == 31) {
                        Some(newPos) -> reasons
                      } else if (BlockPos.getAllInBoxMutable(new BlockPos(newPos.getX, yLevel, newPos.getZ), newPos).asScala.exists(p => !getWorld.isAirBlock(p))) {
                        Some(newPos) -> reasons
                      } else {
                        next(rest, Reason(newPos, index) :: reasons)
                      }
                    }
                  }
              }
            }

            val (opt, r) = next(nextPoses(digRange, target).take(32).zipWithIndex)
            (l, opt, reasons ++ r.reverse)
          } match {
            case Left(a) => Reason.printNonEnergy(a)
            case Right((drops, nextPos, reasons)) =>
              drops.asScala.foreach(cacheItems.add)
              reasons.foreach { r =>
                r.usage match {
                  case Some(x) if x == EnergyUsage.ADV_BREAK_BLOCK => Reason.print(r)
                  case None => Reason.print(r)
                  case _ =>
                }
              }
              nextPos.foreach { p =>
                target = p
                if (p == BlockPos.ORIGIN) {
                  //Finished.
                  target = digRange.min
                  finishWork()
                  mode set TileAdvQuarry.CHECK_LIQUID
                }
              }
          }

          j += 1
        }
      } else if (mode is TileAdvQuarry.NOT_NEED_BREAK) {
        if (digRange.defined && !Config.common.noEnergy.get())
          if (getStoredEnergy > getMaxStored * 0.3) {
            mode set TileAdvQuarry.MAKE_FRAME
            startWork()
          }
      } else if (mode is TileAdvQuarry.CHECK_LIQUID) {
        nextPoses(digRange, target, inclusive = true).take(32 * digRange.timeInTick).foreach { case (_, p) =>
          target = p
          if (p == BlockPos.ORIGIN) {
            mode set TileAdvQuarry.NONE
          } else if (mode is TileAdvQuarry.CHECK_LIQUID) {
            Iterator.iterate(p.down())(_.down()).takeWhile(_.getY > yLevel).filter(p => {
              val state = getWorld.getBlockState(p)
              !state.getBlock.isAir(state, getWorld, p) && TilePump.isLiquid(state)
            }).foreach(pos => getWorld.setBlockState(pos, Blocks.AIR.getDefaultState))
          }
        }

      } else if (mode is TileAdvQuarry.FILL_BLOCKS) {
        import cats.implicits._
        val handlerOpt = getWorld.getTileEntity(getPos.up).pure[Option] >>= { t => t.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN).orElse(null).pure[Option] }
        if (handlerOpt.isDefined) {
          val handler = handlerOpt.get
          val list = Range(0, handler.getSlots).find(i => {
            val stack = handler.getStackInSlot(i)
            !stack.isEmpty && stack.getItem.isInstanceOf[ItemBlock]
          }).map(handler.extractItem(_, 1, false)).toList
          val y = Math.max(if (Config.common.removeBedrock.get()) 1 else 5, yLevel)
          nextPoses(digRange, target).take(list.map(_.getCount).sum).foreach { case (_, p) =>
            target = p
            if (p == BlockPos.ORIGIN) {
              mode set TileAdvQuarry.NONE
            } else {
              val state = InvUtils.getStateFromItem(list.head.getItem.asInstanceOf[ItemBlock])
              getWorld.setBlockState(new BlockPos(p.getX, y, p.getZ), state)
            }
          }
        } else {
          target = BlockPos.ORIGIN
          mode set TileAdvQuarry.NONE
        }
      }
      if (!isEmpty) {
        @tailrec
        def inject(out: ItemStack): Unit = {
          val stack = InvUtils.injectToNearTile(getWorld, getPos, out)
          if (stack.getCount > 0) {
            cacheItems.add(stack)
          } else if (!isEmpty) {
            inject(cacheItems.remove(0))
          }
        }

        inject(cacheItems.remove(0))
      }
    }
  }

  private def chunkLoad(): Unit = {
    if (chunks.nonEmpty) {
      val chunkPos = chunks.head
      val bool = getWorld.isChunkLoaded(chunkPos.x, chunkPos.z, false)
      if (Config.common.debug) {
        QuarryPlus.LOGGER.debug("Chunk has already loaded : " + bool + chunkPos.x + chunkPos.z)
      }
      if (!bool)
        getWorld.getChunk(chunkPos.x, chunkPos.z)
      chunks = chunks.tail
    }
  }

  def checkAndSetFrame(world: World, thatPos: BlockPos): Unit = {
    if (TilePump.isLiquid(world.getBlockState(thatPos))) {
      world.setBlockState(thatPos, Holder.blockFrame.getDammingState)
    }
  }

  private def setBlock(pos: BlockPos, state: IBlockState): Unit = {
    val i = if (state == Blocks.AIR.getDefaultState || state.isFullCube) 0x10 | 0x2 else 3
    getWorld.setBlockState(pos, state, i)
  }

  def breakEvent(pos: BlockPos, state: IBlockState, player: EntityPlayer, expPump: Option[TileExpPump])(action: BlockEvent.BreakEvent => Unit): Seq[Reason] = {
    val event = new BlockEvent.BreakEvent(getWorld, pos, state, player)
    MinecraftForge.EVENT_BUS.post(event)
    if (!event.isCanceled) {
      action(event)
      expPump.filter(xpFilter(event.getExpToDrop)).foreach(_.addXp(event.getExpToDrop))
      Nil
    } else {
      Seq(Reason(pos, state))
    }
  }

  override protected def isWorking: Boolean = mode.isWorking

  override def G_ReInit(): Unit = {
    mode.set(TileAdvQuarry.NOT_NEED_BREAK)
    if (!digRange.defined) {
      digRange = makeRangeBox()
    }
  }

  def energyConfigure(): Unit = {
    if (mode is NONE) {
      configure(0, getMaxStored)
    } else if (mode.reduceReceive) {
      configure(ench.maxReceive / 128, ench.maxStore)
    } else {
      configure(ench.maxReceive, ench.maxStore)
    }
  }

  override def read(nbt: NBTTagCompound): Unit = {
    super.read(nbt)
    ench = QEnch.readFromNBT(nbt.getCompound(NBT_QUARRY_ENCH))
    digRange = DigRange.readFromNBT(nbt.getCompound(NBT_DIG_RANGE))
    target = BlockPos.fromLong(nbt.getLong("NBT_TARGET"))
    mode.readFromNBT(nbt.getCompound(NBT_MODE))
    cacheItems.readFromNBT(nbt.getCompound(NBT_ITEM_LIST))
    //    nbt.getList("NBT_FLUID_LIST", Constants.NBT.TAG_COMPOUND).forEach(tag => {
    //      val tank = new QuarryTank(null, 0)
    //      CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.readNBT(tank, null, tag)
    //      if (tank.getFluid != null) {
    //        fluidStacks.put(tank.getFluid, tank)
    //      }
    //    })
    val l2 = nbt.getList("NBT_CHUNK_LOADING_LIST", Constants.NBT.TAG_LONG)
    chunks = Range(0, l2.size()).map(i => new ChunkPos(BlockPos.fromLong(l2.get(i).asInstanceOf[NBTTagLong].getLong))).toList
    yLevel = Math.max(nbt.getInt("yLevel"), 1)
  }

  override def write(nbt: NBTTagCompound): NBTTagCompound = {
    nbt.put(NBT_QUARRY_ENCH, ench.toNBT)
    nbt.put(NBT_DIG_RANGE, digRange.toNBT)
    nbt.putLong("NBT_TARGET", target.toLong)
    nbt.put(NBT_MODE, mode.toNBT)
    nbt.put(NBT_ITEM_LIST, cacheItems.toNBT)
    //    nbt.put("NBT_FLUID_LIST", fluidStacks.map { case (_, tank) =>
    //      CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.writeNBT(tank, null)
    //    }.foldLeft(new NBTTagList) { case (l, t) => l.add(t); l })
    nbt.put("NBT_CHUNK_LOADING_LIST", chunks.map(_.getBlock(0, 0, 0).toLong.toNBT)
      .foldLeft(new NBTTagList) { case (l, t) => l.add(t); l })
    nbt.putInt("yLevel", yLevel)
    super.write(nbt)
  }

  /**
    * @return Map (Enchantment id, level)
    */
  override def getEnchantments: util.Map[ResourceLocation, Integer] = ench.getMap.collect(enchantCollector).asJava

  /**
    * @param id    Enchantment id
    * @param value level
    */
  override def setEnchantment(id: ResourceLocation, value: Short): Unit = ench = ench.set(id, value)

  override def getEnchantedPickaxe: ItemStack = ench.pickaxe

  override def setInventorySlotContents(index: Int, stack: ItemStack): Unit = {
    if (!stack.isEmpty) {
      QuarryPlus.LOGGER.warn("QuarryPlus WARN: call setInventorySlotContents with non empty ItemStack.")
    } else {
      removeStackFromSlot(index)
    }
  }

  override def decrStackSize(index: Int, count: Int): ItemStack = cacheItems.decrease(index, count)

  override def getSizeInventory: Int = Math.max(cacheItems.list.size, 1)

  override def removeStackFromSlot(index: Int): ItemStack = cacheItems.remove(index)

  override val getInventoryStackLimit = 1

  override def clear(): Unit = cacheItems.list.clear()

  override def isEmpty: Boolean = cacheItems.list.isEmpty

  override def getStackInSlot(index: Int): ItemStack = cacheItems.getStack(index)

  override def getDebugName: String = TranslationKeys.advquarry

  override def isUsableByPlayer(player: EntityPlayer): Boolean = self.getWorld.getTileEntity(self.getPos) eq this

  override def getDebugMessages: util.List[TextComponentString] = {
    import scala.collection.JavaConverters._
    List("Items to extract = " + cacheItems.list.size,
      "Liquid to extract = " + fluidStacks.size,
      "Next target = " + target.toString,
      mode.toString,
      digRange.toString,
      ench.toString,
      "YLevel = " + yLevel).map(toComponentString).asJava
  }

  override def getCapability[T](cap: Capability[T], side: EnumFacing): LazyOptional[T] = {
    if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
      CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() => itemHandler))
    //    else if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
    //      CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap , LazyOptional.of(() => fluidHandlers(side)))
    else super.getCapability(cap, side)
  }

  override def hasFastRenderer: Boolean = true

  override def getRenderBoundingBox: AxisAlignedBB = {
    if (digRange.defined) digRange.renderBox
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

  override def remove(): Unit = {
    super[IChunkLoadTile].releaseTicket()
    super.remove()
  }

  override def onChunkUnloaded(): Unit = {
    if (!getWorld.isRemote)
      mode set TileAdvQuarry.NONE
    super.onChunkUnloaded()
  }

  override def createContainer(playerInventory: InventoryPlayer, playerIn: EntityPlayer): Container = new ContainerAdvQuarry(this, playerIn)

  override def getGuiID: String = GUI_ID

  override def getDisplayName: ITextComponent = super[APowerTile].getDisplayName

  def makeRangeBox(): DigRange = {
    val facing = getWorld.getBlockState(getPos).get(BlockStateProperties.FACING).getOpposite
    val link = getNeighbors(facing).map(getWorld.getTileEntity(_))
      .collectFirst { case m: IMarker if m.hasLink =>
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

  def digRange: DigRange = mDigRange

  def digRange_=(@Nonnull digRange: TileAdvQuarry.DigRange): Unit = {
    require(digRange != null, "DigRange must not be null.")
    mDigRange = digRange
  }

  def stickActivated(player: EntityPlayer): Unit = {
    //Called when noEnergy is true and block is right clicked with stick (item)
    if (machineDisabled) {
      player.sendStatusMessage(new TextComponentString("ChunkDestroyer is disabled."), true)
    } else if (mode is TileAdvQuarry.NOT_NEED_BREAK) {
      mode set TileAdvQuarry.MAKE_FRAME
      startWork()
    }
  }

  def startFillMode(): Unit = {
    if ((mode is TileAdvQuarry.NONE) && digRange.defined && preparedFiller) {
      mode set TileAdvQuarry.FILL_BLOCKS
      target = digRange.min.add(-1, 0, 0)
    }
  }

  def noFrameStart(): Unit = {
    if (mode is TileAdvQuarry.NOT_NEED_BREAK) {
      mode set TileAdvQuarry.BREAK_BLOCK
      target = digRange.min
      startWork()
    }
  }

  override def connectAttachment(facing: EnumFacing, attachments: Attachments[_ <: APacketTile], simulate: Boolean): Boolean = {
    if (!facingMap.contains(attachments)) {
      if (!simulate) facingMap = facingMap.updated(attachments, facing)
      true
    } else {
      val t = getWorld.getTileEntity(getPos.offset(facingMap(attachments)))
      if (!attachments.test(t)) {
        if (!simulate) facingMap = facingMap.updated(attachments, facing)
        true
      } else {
        facingMap(attachments) == facing
      }
    }
  }

  override def isValidAttachment(attachments: Attachments[_ <: APacketTile]): Boolean = TileAdvQuarry.VALID_ATTACHMENTS(attachments)

  private def getFillBlock: IBlockState = {
    facingMap.get(Attachments.REPLACER)
      .flatMap(f => Option(getWorld.getTileEntity(getPos.offset(f))))
      .flatMap(t => Attachments.REPLACER.apply(t).asScala)
      .fold(Blocks.AIR.getDefaultState)(_.getReplaceState)
  }

  def preparedFiller: Boolean = {
    val y = if (Config.common.removeBedrock.get()) 1 else 5
    if (BlockPos.getAllInBoxMutable(new BlockPos(digRange.minX, y, digRange.minZ), new BlockPos(digRange.maxX, y, digRange.maxZ))
      .iterator().asScala.forall(getWorld.isAirBlock)) {
      val need = (digRange.maxX - digRange.minX + 1) * (digRange.maxZ - digRange.minZ + 1)
      import cats.implicits._
      val stacks = for (tile <- getWorld.getTileEntity(getPos.up).pure[Option].toList;
                        handler <- tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN)
                          .orElse(EmptyHandler.INSTANCE).pure[List];
                        i <- Range(0, handler.getSlots)) yield handler.getStackInSlot(i)

      val blocks = stacks.filter(s => !s.isEmpty && s.getItem.isInstanceOf[ItemBlock])
      blocks.nonEmpty &&
        stacks.forall(stack => !stack.isEmpty || !stack.getItem.isInstanceOf[ItemBlock] || stack.isItemEqual(blocks.head)) &&
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

  /*private[TileAdvQuarry] class FluidHandler(val facing: EnumFacing) extends IFluidHandler {
    //FluidHandlerFluidMap(fluidStacks.asJava) {

    def fluids = if (facing != null) fluidStacks.filterKeys(fluidExtractFacings(facing)) else fluidStacks.toMap

    /**
      * Not fill-able.
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
          case None => ("No liquid", 0)
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
  }*/

  /*private[TileAdvQuarry] class QuarryTank(s: FluidStack, a: Int) extends FluidTank(s, a) {
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
  }*/

  class Mode {

    import TileAdvQuarry._

    private[this] var mode: Modes = NONE

    def set(newMode: Modes): Unit = {
      mode = newMode
      if (!getWorld.isRemote) {
        energyConfigure()
        PacketHandler.sendToAround(AdvModeMessage.create(self), getWorld, getPos)
      }
      val state = getWorld.getBlockState(getPos)
      val working = newMode != NONE && newMode != NOT_NEED_BREAK
      if (state.get(QPBlock.WORKING) ^ working) {
        getWorld.setBlockState(getPos, state.`with`(QPBlock.WORKING, Boolean.box(working)))
      }
    }

    def is(modes: Modes): Boolean = mode == modes

    def isWorking: Boolean = !is(NONE) && !is(NOT_NEED_BREAK)

    def reduceReceive: Boolean = is(MAKE_FRAME)

    override def toString: String = "ChunkDestroyer mode = " + mode

    def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
      nbt.putInt("mode", mode.index)
      nbt
    }

    def readFromNBT(tag: NBTTagCompound): Mode = {
      this.mode = tag.getInt("mode") match {
        case 0 => NONE
        case 1 => NOT_NEED_BREAK
        case 2 => MAKE_FRAME
        case 3 => BREAK_BLOCK
        case 4 => CHECK_LIQUID
        case 5 => FILL_BLOCKS
        case _ => throw new IllegalStateException("Invalid mode")
      }
      this
    }
  }

  override def getName = new TextComponentTranslation(getDebugName)
}

object TileAdvQuarry {
  final val SYMBOL = Symbol("ChunkDestroyer")
  final val GUI_ID = QuarryPlus.modID + ":gui_" + QuarryPlus.Names.advquarry

  private final val MAX_STORED = 300 * 256
  final val noDigBLOCKS = Set(
    BlockWrapper(Blocks.STONE.getDefaultState),
    BlockWrapper(Blocks.GRANITE.getDefaultState),
    BlockWrapper(Blocks.POLISHED_GRANITE.getDefaultState),
    BlockWrapper(Blocks.DIORITE.getDefaultState),
    BlockWrapper(Blocks.POLISHED_DIORITE.getDefaultState),
    BlockWrapper(Blocks.ANDESITE.getDefaultState),
    BlockWrapper(Blocks.POLISHED_ANDESITE.getDefaultState),
    BlockWrapper(Blocks.COBBLESTONE.getDefaultState),
    BlockWrapper(Blocks.DIRT.getDefaultState, ignoreProperty = true),
    BlockWrapper(Blocks.GRASS.getDefaultState, ignoreProperty = true),
    BlockWrapper(Blocks.NETHERRACK.getDefaultState),
    BlockWrapper(Blocks.SANDSTONE.getDefaultState),
    BlockWrapper(Blocks.CHISELED_SANDSTONE.getDefaultState),
    BlockWrapper(Blocks.SMOOTH_SANDSTONE.getDefaultState),
    BlockWrapper(Blocks.CUT_SANDSTONE.getDefaultState),
    BlockWrapper(Blocks.RED_SANDSTONE.getDefaultState),
    BlockWrapper(Blocks.CHISELED_RED_SANDSTONE.getDefaultState),
    BlockWrapper(Blocks.SMOOTH_RED_SANDSTONE.getDefaultState),
    BlockWrapper(Blocks.CUT_RED_SANDSTONE.getDefaultState),
  )
  private final val NBT_QUARRY_ENCH = "nbt_q_ench"
  private final val NBT_DIG_RANGE = "nbt_dig_range"
  private final val NBT_MODE = "nbt_quarry_mode"
  private final val NBT_ITEM_LIST = "nbt_item_list"
  private final val NBT_ITEM_ELEMENTS = "nbt_item_elements"
  final val VALID_ATTACHMENTS: Set[Attachments[_]] = Set(Attachments.EXP_PUMP, Attachments.REPLACER)

  val defaultEnch = QEnch(efficiency = 0, unbreaking = 0, fortune = 0, silktouch = false)
  val defaultRange: DigRange = new DigRange(BlockPos.ORIGIN, BlockPos.ORIGIN) {
    override val defined: Boolean = false
    override val toString: String = "Dig Range Not Defined"
    override val timeInTick = 0

    override def min: BlockPos = BlockPos.ORIGIN
  }

  implicit val digRangeNbt: NBTWrapper[DigRange, NBTTagCompound] = _.writeToNBT(new NBTTagCompound)
  implicit val qEnchNbt: NBTWrapper[QEnch, NBTTagCompound] = _.writeToNBT(new NBTTagCompound)
  implicit val itemListNbt: NBTWrapper[ItemList, NBTTagCompound] = _.writeToNBT(new NBTTagCompound)
  implicit val modeNbt: NBTWrapper[TileAdvQuarry#Mode, NBTTagCompound] = _.writeToNBT(new NBTTagCompound)

  case class QEnch(efficiency: Int, unbreaking: Int, fortune: Int, silktouch: Boolean, other: Map[ResourceLocation, Int] = Map.empty) {

    import IEnchantableTile._

    require(efficiency >= 0 && unbreaking >= 0 && fortune >= 0,
      s"Chunk Destroyer Enchantment error with Efficiency $efficiency, Unbreaking $unbreaking, Fortune $fortune, Silktouch $silktouch, other $other")
    val pickaxe = new ItemStack(net.minecraft.init.Items.DIAMOND_PICKAXE)
    private val pf: PartialFunction[(ResourceLocation, Int), (Enchantment, Integer)] = {
      case (id, level) if level > 0 && (!Config.common.disabled(BlockBookMover.SYMBOL).get() || IEnchantableTile.isValidEnch.test(id, level))
      => (ForgeRegistries.ENCHANTMENTS.getValue(id), Int.box(level))
    }
    EnchantmentHelper.setEnchantments((getMap collect pf).asJava, pickaxe)

    def set(id: ResourceLocation, level: Int): QEnch = {
      id match {
        case EfficiencyID => this.copy(efficiency = level)
        case UnbreakingID => this.copy(unbreaking = level)
        case FortuneID => this.copy(fortune = level)
        case SilktouchID => this.copy(silktouch = level > 0)
        case _ => this.copy(other = other + (id -> level))
      }
    }

    def getMap: Map[ResourceLocation, Int] = Map(EfficiencyID -> efficiency, UnbreakingID -> unbreaking,
      FortuneID -> fortune, SilktouchID -> silktouch.compare(false)) ++ other

    val maxStore = MAX_STORED * (efficiency + 1) * APowerTile.MicroJtoMJ

    val maxReceive = if (efficiency >= 5) maxStore else if (efficiency == 0) maxStore / 1000 else (maxStore * Math.pow(efficiency.toDouble / 5.0, 3)).toLong

    val mode: Int = if (silktouch) -1 else fortune

    def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
      nbt.putInt("efficiency", efficiency)
      nbt.putInt("unbreaking", unbreaking)
      nbt.putInt("fortune", fortune)
      nbt.putBoolean("silktouch", silktouch)
      val o = new NBTTagCompound
      other.map { case (id, l) => Option(ForgeRegistries.ENCHANTMENTS.getValue(id)) -> l }.foreach {
        case (Some(e), l) => o.putInt(e.getRegistryName.toString, l)
        case _ =>
      }
      nbt.put("other", o)
      nbt
    }
  }

  object QEnch {
    def readFromNBT(tag: NBTTagCompound): QEnch = {
      if (!tag.isEmpty) {
        val o = tag.getCompound("other")
        val otherMap = o.keySet().asScala
          .map(new ResourceLocation(_))
          .collect { case s if ForgeRegistries.ENCHANTMENTS.containsKey(s) => s -> o.getInt(s.toString) }
          .toMap
        QEnch(tag.getInt("efficiency"), tag.getInt("unbreaking"), tag.getInt("fortune"), tag.getBoolean("silktouch"), otherMap)
      } else
        QEnch(efficiency = 0, unbreaking = 0, fortune = 0, silktouch = false)
    }
  }

  case class DigRange(minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int) {
    def this(minPos: BlockPos, maxPos: BlockPos) {
      this(minPos.getX, minPos.getY, minPos.getZ, maxPos.getX, maxPos.getY, maxPos.getZ)
    }

    val defined = true

    def min: BlockPos = new BlockPos(minX, minY, minZ)

    final val renderBox = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ)

    final val lengthSq = min.distanceSq(maxZ, maxY, maxZ)

    val timeInTick: Int = {
      val length = (maxX + maxZ - minX - minZ + 2) / 2
      Math.max(length / 128, 1)
    }

    def chunkSeq: List[ChunkPos] = {
      val a = for (x <- Range(minX, maxX, 16) :+ maxX;
                   z <- Range(minZ, maxZ, 16) :+ maxZ
      ) yield new ChunkPos(x >> 4, z >> 4)
      a.toList
    }

    override val toString: String = s"Dig Range from ($minX, $minY, $minZ) to ($maxX, $maxY, $maxZ). $timeInTick times a tick."

    def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
      nbt.putBoolean("defined", defined)
      nbt.putInt("minX", minX)
      nbt.putInt("minY", minY)
      nbt.putInt("minZ", minZ)
      nbt.putInt("maxX", maxX)
      nbt.putInt("maxY", maxY)
      nbt.putInt("maxZ", maxZ)
      nbt
    }
  }

  object DigRange {
    def readFromNBT(tag: NBTTagCompound): DigRange = {
      if (tag.getBoolean("defined")) {
        DigRange(tag.getInt("minX"), tag.getInt("minY"), tag.getInt("minZ"),
          tag.getInt("maxX"), tag.getInt("maxY"), tag.getInt("maxZ"))
      } else {
        defaultRange
      }
    }
  }

  class ItemList {
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
        ItemStack.EMPTY
      }
    }

    def getStack(index: Int): ItemStack = {
      if (list.isDefinedAt(index))
        list(index).toStack
      else
        ItemStack.EMPTY
    }

    def remove(index: Int): ItemStack = {
      if (list.isDefinedAt(index))
        list.remove(index).toStack
      else
        ItemStack.EMPTY
    }

    override def toString: String = "ItemList size = " + list.size

    def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
      nbt.put(NBT_ITEM_ELEMENTS,
        list.map(_.toNBT).foldLeft(new NBTTagList) { case (b, t) => b.add(t); b })
      nbt
    }

    def readFromNBT(tag: NBTTagCompound): ItemList = {
      val l = tag.getList(NBT_ITEM_ELEMENTS, Constants.NBT.TAG_COMPOUND)
      l.asScala.map(_.asInstanceOf[NBTTagCompound]).map(ItemStack.read).foreach(this.add)
      this
    }
  }

  private[TileAdvQuarry] class Modes(val index: Int, override val toString: String)

  val NONE = new Modes(0, "NONE")

  val NOT_NEED_BREAK = new Modes(1, "NOT_NEED_BREAK")

  val MAKE_FRAME = new Modes(2, "MAKE_FRAME")

  val BREAK_BLOCK = new Modes(3, "BREAK_BLOCK")

  val CHECK_LIQUID = new Modes(4, "CHECK_LIQUID")

  val FILL_BLOCKS = new Modes(5, "FILL_BLOCKS")

  def getFramePoses(digRange: DigRange): List[BlockPos] = {
    val builder = List.newBuilder[BlockPos]
    val minX = digRange.minX
    val maxX = digRange.maxX
    val maxY = digRange.maxY
    val minZ = digRange.minZ
    val maxZ = digRange.maxZ
    var i = 0
    while (i <= 4) {
      builder += new BlockPos(minX - 1, maxY + 4 - i, minZ - 1)
      builder += new BlockPos(minX - 1, maxY + 4 - i, maxZ + 1)
      builder += new BlockPos(maxX + 1, maxY + 4 - i, maxZ + 1)
      builder += new BlockPos(maxX + 1, maxY + 4 - i, minZ - 1)
      i += 1
    }
    var x = minX
    while (x <= maxX) {
      builder += new BlockPos(x, maxY + 4, minZ - 1)
      builder += new BlockPos(x, maxY + 0, minZ - 1)
      builder += new BlockPos(x, maxY + 0, maxZ + 1)
      builder += new BlockPos(x, maxY + 4, maxZ + 1)
      x += 1
    }
    var z = minZ
    while (z <= maxZ) {
      builder += new BlockPos(minX - 1, maxY + 4, z)
      builder += new BlockPos(minX - 1, maxY + 0, z)
      builder += new BlockPos(maxX + 1, maxY + 0, z)
      builder += new BlockPos(maxX + 1, maxY + 4, z)
      z += 1
    }
    builder.result()
  }

  def nextPoses(range: DigRange, previous: BlockPos, inclusive: Boolean = false): Stream[(BlockPos, BlockPos)] = {
    val getNext: ((BlockPos, BlockPos)) => (BlockPos, BlockPos) = (t: (BlockPos, BlockPos)) => {
      val (_, pos) = t
      if (pos == BlockPos.ORIGIN)
        pos -> BlockPos.ORIGIN
      else {
        val x = pos.getX + 1
        if (x > range.maxX) {
          val z = pos.getZ + 1
          if (z > range.maxZ) {
            // Finished. Update Status
            pos -> BlockPos.ORIGIN
          } else {
            pos -> new BlockPos(range.minX, pos.getY, z)
          }
        } else {
          pos -> new BlockPos(x, pos.getY, pos.getZ)
        }
      }
    }
    if (inclusive) {
      Stream.iterate((previous, previous))(getNext)
    } else {
      Stream.iterate(getNext(previous, previous))(getNext)
    }
  }

  def xpFilter(i: Int): Any => Boolean = _ => i > 0
}
