package com.yogpc.qp.tile

import java.lang.{Boolean => JBool}
import java.util
import java.util.Collections

import com.yogpc.qp.block.{ADismCBlock, BlockBookMover}
import com.yogpc.qp.compat.InvUtils
import com.yogpc.qp.container.ContainerQuarryModule.HasModuleInventory
import com.yogpc.qp.gui.TranslationKeys
import com.yogpc.qp.modules.IModuleItem
import com.yogpc.qp.packet.PacketHandler
import com.yogpc.qp.packet.advquarry.{AdvContentMessage, AdvModeMessage}
import com.yogpc.qp.tile.HasStorage.Storage
import com.yogpc.qp.tile.IAttachment.Attachments
import com.yogpc.qp.tile.TileAdvQuarry._
import com.yogpc.qp.utils._
import com.yogpc.qp.version.VersionUtil
import com.yogpc.qp.{Config, PowerManager, QuarryPlus, QuarryPlusI, _}
import javax.annotation.Nonnull
import net.minecraft.block.Block
import net.minecraft.block.properties.PropertyHelper
import net.minecraft.block.state.IBlockState
import net.minecraft.enchantment.{Enchantment, EnchantmentHelper}
import net.minecraft.entity.Entity
import net.minecraft.entity.item.{EntityItem, EntityXPOrb}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.crafting.FurnaceRecipes
import net.minecraft.item.{ItemBlock, ItemStack}
import net.minecraft.nbt.{NBTTagCompound, NBTTagLong}
import net.minecraft.util._
import net.minecraft.util.math.BlockPos.MutableBlockPos
import net.minecraft.util.math.{AxisAlignedBB, BlockPos, ChunkPos}
import net.minecraft.util.text.TextComponentString
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
import net.minecraftforge.items.{CapabilityItemHandler, IItemHandlerModifiable}

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

class TileAdvQuarry extends APowerTile
  with IEnchantableTile
  with HasInv
  with ITickable
  with IDebugSender
  with IChunkLoadTile
  with IAttachable
  with HasModuleInventory
  with HasStorage {
  self =>
  private[this] var mDigRange = TileAdvQuarry.defaultRange
  var modules: List[IModule] = Nil
  private[this] var attachments: Map[Attachments[_ <: APacketTile], EnumFacing] = Map.empty
  var ench: QEnch = TileAdvQuarry.defaultEnch
  var target: BlockPos = BlockPos.ORIGIN
  var framePoses = List.empty[BlockPos]
  var chunks = List.empty[ChunkPos]
  var yLevel = 1
  val fluidStacks = scala.collection.mutable.Map.empty[FluidStack, FluidTank]
  val cacheItems = new ItemList
  val itemHandler = new ItemHandler
  val fluidHandlers: Map[EnumFacing, FluidHandler] = EnumFacing.VALUES.map(f => f -> new FluidHandler(facing = f)).toMap.withDefaultValue(new FluidHandler(null))
  val fluidExtractFacings: Map[EnumFacing, mutable.Set[FluidStack]] = EnumFacing.VALUES.map(f => f -> scala.collection.mutable.Set.empty[FluidStack]).toMap
  val mode = new Mode
  val ACTING: PropertyHelper[JBool] = ADismCBlock.ACTING
  val moduleInv = new QuarryModuleInventory(new TextComponentString("Modules"), 5, this, _ => refreshModules(), TileAdvQuarry.moduleFilter)

  override def update(): Unit = {
    super.update()
    if (!getWorld.isRemote && !machineDisabled) {
      modules.foreach(_.invoke(IModule.Tick(self)))
      if (mode is TileAdvQuarry.MAKE_FRAME) {
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
            val axis = new AxisAlignedBB(new BlockPos(x - digRange.dropWidth, 1, z - digRange.dropWidth), target.add(digRange.dropWidth, 4, digRange.dropWidth))
            //catch dropped items
            getWorld.getEntitiesWithinAABB(classOf[EntityItem], axis).asScala.filter(nonNull).filter(!_.isDead)
              .filter(_.getItem.getCount > 0).foreach(entity => {
              QuarryPlus.proxy.removeEntity(entity)
              list.add(entity.getItem)
            })
            //remove XPs
            val orbs = world.getEntitiesWithinAABB(classOf[Entity], axis).asScala.toList
            modules.foreach(_.invoke(IModule.CollectingItem(orbs)))
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
            if (!state.getBlock.isAir(state, getWorld, pos)) {
              if (TilePump.isLiquid(state)) {
                requireEnergy += PowerManager.calcEnergyPumpDrain(ench.unbreaking, 1, 0)
                drain += y
              } else {
                val blockHardness = state.getBlockHardness(getWorld, pos)
                if (blockHardness != -1 && !blockHardness.isInfinity) {
                  (state.getBlock match {
                    case _ if Config.content.noDigBLOCKS.exists(_.contain(state)) => (0, destroy)
                    case leave: IShearable if leave.isLeaves(state, getWorld, pos) && ench.silktouch => (ench.mode, shear)
                    case _ => (ench.mode, dig)
                  }) match {
                    case (m, seq) =>
                      requireEnergy += PowerManager.calcEnergyBreak(blockHardness, m, ench.unbreaking)
                      seq += y
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
          val fakePlayer = QuarryFakePlayer.get(getWorld.asInstanceOf[WorldServer], target)
          fakePlayer.setHeldItem(EnumHand.MAIN_HAND, getEnchantedPickaxe)
          val collectFurnaceXP = InvUtils.hasSmelting(fakePlayer.getHeldItemMainhand) && modules.exists(IModule.hasExpPumpModule)
          val tempList = new NotNullList(new ArrayBuffer[ItemStack]())
          val p = new MutableBlockPos(target.getX, 0, target.getZ)

          val reasons = new ArrayBuffer[Reason](0)
          var additionalExp = 0
          dig.foreach { y =>
            p.setY(y)
            val state = getWorld.getBlockState(p)
            breakEvent(p, state, fakePlayer) { _ =>
              if (ench.silktouch && state.getBlock.canSilkHarvest(getWorld, p, state, fakePlayer)) {
                tempList.add(ReflectionHelper.invoke(TileBasic.createStackedBlock, state.getBlock, state).asInstanceOf[ItemStack])
              } else {
                TileBasic.getDrops(getWorld, p, state, state.getBlock, ench.fortune, tempList)
              }
              tempList.fix = true
              ForgeEventFactory.fireBlockHarvesting(tempList, getWorld, p, state, ench.fortune, 1f, ench.silktouch, fakePlayer)
              list.addAll(tempList)
              if (collectFurnaceXP)
                additionalExp += TileBasic.getSmeltingXp(tempList.fixing.asJava, Collections.emptyList())
              tempList.clear()
              setBlock(p, state)
            } ++=: reasons
          }
          if (shear.nonEmpty) {
            //Enchantment must be Silktouch.
            val itemShear = new ItemStack(net.minecraft.init.Items.SHEARS)
            EnchantmentHelper.setEnchantments(ench.getMap.collect { case (a, b) if b > 0 => (Enchantment.getEnchantmentByID(a), Int.box(b)) }.asJava, itemShear)
            for (y <- shear) {
              p.setY(y)
              val state = getWorld.getBlockState(p)
              val block = state.getBlock.asInstanceOf[Block with IShearable]
              breakEvent(p, state, fakePlayer) { _ =>
                tempList.addAll(block.onSheared(itemShear, getWorld, p, ench.fortune))
                ForgeEventFactory.fireBlockHarvesting(tempList, getWorld, p, state, ench.fortune, 1f, ench.silktouch, fakePlayer)
                list.addAll(tempList)
                tempList.clear()
                setBlock(p, state)
              } ++=: reasons
            }
          }
          val l = new ItemList
          destroy.foreach { y =>
            p.setY(y)
            val state = getWorld.getBlockState(p)
            breakEvent(p, state, fakePlayer) { _ =>
              setBlock(p, state)
              if (collectFurnaceXP) {
                val nnl = new NotNullList(new ArrayBuffer[ItemStack]())
                TileBasic.getDrops(getWorld, p, state, state.getBlock, 0, nnl)
                nnl.seq.foreach(l.add)
                // adding exp to pump is under.
              }
            } ++=: reasons
          }
          if (collectFurnaceXP) {
            additionalExp += TileBasic.floorFloat(l.list.map(ie => FurnaceRecipes.instance().getSmeltingResult(ie.toStack) -> ie.count).collect {
              case (s, i) if VersionUtil.nonEmpty(s) => FurnaceRecipes.instance().getSmeltingExperience(s) * i
            }.sum)
          }
          for (y <- drain) {
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
          modules.collectFirst { case module: ExpPumpModule => module }.foreach(_.addXp(additionalExp))
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
        if (digRange.defined && !Config.content.noEnergy)
          if (getStoredEnergy > getMaxStored * 0.3) {
            mode set TileAdvQuarry.MAKE_FRAME
            startWork()
          }
      } else if (mode is TileAdvQuarry.CHECK_LIQUID) {
        val aabb = new AxisAlignedBB(digRange.minX - digRange.dropWidth, 0, digRange.minZ - digRange.dropWidth, digRange.maxX + digRange.dropWidth, digRange.maxY + 3, digRange.maxZ + digRange.dropWidth)
        val drops = getWorld.getEntitiesWithinAABB(classOf[EntityItem], aabb)
        drops.asScala.filter(_.getItem.getCount > 0).foreach(entity => {
          QuarryPlus.proxy.removeEntity(entity)
          cacheItems.add(entity.getItem)
        })
        val exp = getWorld.getEntitiesWithinAABB(classOf[EntityXPOrb], aabb).asScala.map { e => QuarryPlus.proxy.removeEntity(e); e.xpValue }.sum
        modules.collectFirst { case module: ExpPumpModule => module }.foreach(_.addXp(exp))
        nextPoses(digRange, target, inclusive = true).take(32 * digRange.timeInTick).foreach { case (_, p) =>
          target = p
          if (p == BlockPos.ORIGIN) {
            mode set TileAdvQuarry.NONE
          } else if (mode is TileAdvQuarry.CHECK_LIQUID) {
            Iterator.iterate(p.down())(_.down()).takeWhile(_.getY > yLevel).filter(p => {
              val state = getWorld.getBlockState(p)
              !state.getBlock.isAir(state, getWorld, p) && TilePump.isLiquid(state)
            }).foreach(getWorld.setBlockToAir)
          }
        }

      } else if (mode is TileAdvQuarry.FILL_BLOCKS) {
        val handler = InvUtils.findItemHandler(getWorld, getPos.up, EnumFacing.DOWN).orNull
        if (handler != null) {
          val list = Range(0, handler.getSlots).find(i => {
            val stack = handler.getStackInSlot(i)
            VersionUtil.nonEmpty(stack) && stack.getItem.isInstanceOf[ItemBlock]
          }).map(handler.extractItem(_, 1, false)).toList
          val y = Math.max(if (Config.content.removeBedrock) 1 else 5, yLevel)
          nextPoses(digRange, target).take(list.map(_.getCount).sum).foreach { case (_, p) =>
            target = p
            if (p == BlockPos.ORIGIN) {
              mode set TileAdvQuarry.NONE
            } else {
              val state = InvUtils.getStateFromItem(list.head.getItem.asInstanceOf[ItemBlock], list.head.getItemDamage)
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
      val bool = getWorld.isChunkGeneratedAt(chunkPos.x, chunkPos.z)
      if (Config.content.debug) {
        QuarryPlus.LOGGER.debug("Chunk has already loaded : " + bool + chunkPos.x + chunkPos.z)
      }
      if (!bool)
        getWorld.getChunkFromChunkCoords(chunkPos.x, chunkPos.z)
      chunks = chunks.tail
    }
  }

  def checkAndSetFrame(world: World, thatPos: BlockPos): Unit = {
    if (TilePump.isLiquid(world.getBlockState(thatPos))) {
      world.setBlockState(thatPos, QuarryPlusI.blockFrame.getDammingState)
    }
  }

  private def setBlock(pos: BlockPos, state: IBlockState): Unit = {
    val replaced = modules.foldLeft(IModule.NoAction: IModule.Result) { case (r, m) => IModule.Result.combine(r, m.invoke(IModule.AfterBreak(getWorld, pos, state, self.getWorld.getTotalWorldTime))) }
    if (!replaced.done) {
      val i = 0x10 | 0x2
      getWorld.setBlockState(pos, Blocks.AIR.getDefaultState, i)
    }
  }

  def breakEvent(pos: BlockPos, state: IBlockState, player: EntityPlayer)(action: BlockEvent.BreakEvent => Unit): Seq[Reason] = {
    val event = new BlockEvent.BreakEvent(getWorld, pos, state, player)
    MinecraftForge.EVENT_BUS.post(event)
    if (!event.isCanceled) {
      modules.foldLeft(IModule.NoAction: IModule.Result) { case (r, m) => IModule.Result.combine(r, m.invoke(IModule.BeforeBreak(event.getExpToDrop, world, pos))) }
      action(event)
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

  //noinspection SpellCheckingInspection
  override def readFromNBT(nbt: NBTTagCompound): Unit = {
    super.readFromNBT(nbt)
    ench = QEnch.readFromNBT(nbt.getCompoundTag(NBT_QUARRY_ENCH))
    digRange = DigRange.readFromNBT(nbt.getCompoundTag(NBT_DIG_RANGE))
    target = BlockPos.fromLong(nbt.getLong("NBT_TARGET"))
    mode.readFromNBT(nbt.getCompoundTag(NBT_MODE))
    cacheItems.readFromNBT(nbt.getCompoundTag(NBT_ITEM_LIST))
    nbt.getTagList("NBT_FLUIDLIST", Constants.NBT.TAG_COMPOUND).tagIterator.foreach(tag => {
      val tank = new QuarryTank(null, 0)
      CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.readNBT(tank, null, tag)
      if (tank.getFluid != null) {
        fluidStacks.put(tank.getFluid, tank)
      }
    })
    val l2 = nbt.getTagList("NBT_CHUNKLOADLIST", Constants.NBT.TAG_LONG)
    chunks = Range(0, l2.tagCount()).map(i => new ChunkPos(BlockPos.fromLong(l2.get(i).asInstanceOf[NBTTagLong].getLong))).toList
    yLevel = Math.max(nbt.getInteger("yLevel"), 1)
    moduleInv.deserializeNBT(nbt.getCompoundTag("moduleInv"))
  }

  //noinspection SpellCheckingInspection
  override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
    nbt.setTag(NBT_QUARRY_ENCH, ench.toNBT)
    nbt.setTag(NBT_DIG_RANGE, digRange.toNBT)
    nbt.setLong("NBT_TARGET", target.toLong)
    nbt.setTag(NBT_MODE, mode.toNBT)
    nbt.setTag(NBT_ITEM_LIST, cacheItems.toNBT)
    nbt.setTag("NBT_FLUIDLIST", fluidStacks.map { case (_, tank) =>
      CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.writeNBT(tank, null)
    }.foldLeft(NBTBuilder.empty) { case (l, t) => l.appendTag(t) }.toList)
    nbt.setTag("NBT_CHUNKLOADLIST", chunks.map(_.getBlock(0, 0, 0).toLong.toNBT)
      .foldLeft(NBTBuilder.empty) { case (l, t) => l.appendTag(t) }.toList)
    nbt.setInteger("yLevel", yLevel)
    nbt.setTag("moduleInv", moduleInv.serializeNBT())
    super.writeToNBT(nbt)
  }

  /**
    * @return Map (Enchantment id, level)
    */
  override def getEnchantments: util.Map[Integer, Integer] = ench.getMap.collect(enchantCollector).asJava

  /**
    * @param id    Enchantment id
    * @param value level
    */
  override def setEnchantment(id: Short, value: Short): Unit = ench = ench.set(id, value)

  override def getEnchantedPickaxe: ItemStack = ench.pickaxe

  override def setInventorySlotContents(index: Int, stack: ItemStack): Unit = {
    if (VersionUtil.nonEmpty(stack)) {
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
      "Modules: " + modules.mkString(", "),
      "YLevel = " + yLevel).map(toComponentString).asJava
  }

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = {
    capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ||
      (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && Config.content.enableChunkDestroyerFluidHandler) ||
      super.hasCapability(capability, facing)
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
    if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
      CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler)
    } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && Config.content.enableChunkDestroyerFluidHandler) {
      CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandlers(facing))
    } else
      super.getCapability(capability, facing)
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
    setTileData(this.chunkTicket, getPos)
  }

  override def forceChunkLoading(ticket: ForgeChunkManager.Ticket): Unit = {
    if (this.chunkTicket == null) this.chunkTicket = ticket
    val quarryChunk = new ChunkPos(getPos)
    ForgeChunkManager.forceChunk(ticket, quarryChunk)
  }

  def makeRangeBox(): DigRange = {
    val facing = getWorld.getBlockState(getPos).getValue(ADismCBlock.FACING).getOpposite
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
      VersionUtil.sendMessage(player, new TextComponentString("ChunkDestroyer is disabled."), true)
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

  def openModuleInv(player: EntityPlayer): Unit = {
    if (hasWorld && !world.isRemote) {
      player.openGui(QuarryPlus.instance(), QuarryPlusI.guiIdQuarryModule, world, pos.getX, pos.getY, pos.getZ)
    }
  }

  override def connectAttachment(facing: EnumFacing, attachment: Attachments[_ <: APacketTile], simulate: Boolean): Boolean = {
    if (!attachments.contains(attachment)) {
      if (!simulate) {
        attachments = attachments.updated(attachment, facing)
        refreshModules()
      }
      true
    } else {
      val t = getWorld.getTileEntity(getPos.offset(attachments(attachment)))
      if (!attachment.test(t)) {
        if (!simulate) {
          attachments = attachments.updated(attachment, facing)
          refreshModules()
        }
        true
      } else {
        attachments.get(attachment).contains(facing)
      }
    }
  }

  override def isValidAttachment(attachments: Attachments[_ <: APacketTile]): Boolean = VALID_ATTACHMENTS(attachments)

  def refreshModules(): Unit = {
    val attachmentModules = attachments.flatMap { case (kind, facing) => kind.module(world.getTileEntity(pos.offset(facing))).asScala }.toList
    val internalModules = moduleInv.moduleItems().asScala.flatMap { e =>
      e.getKey.apply(e.getValue, self).toList
    }
    this.modules = attachmentModules ++ internalModules
  }

  def preparedFiller: Boolean = {
    val y = if (Config.content.removeBedrock) 1 else 5
    if (BlockPos.getAllInBoxMutable(new BlockPos(digRange.minX, y, digRange.minZ), new BlockPos(digRange.maxX, y, digRange.maxZ))
      .iterator().asScala.forall(getWorld.isAirBlock)) {
      val need = (digRange.maxX - digRange.minX + 1) * (digRange.maxZ - digRange.minZ + 1)
      val stacks = InvUtils.findItemHandler(getWorld, getPos.up, EnumFacing.DOWN).toList
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
  }

  private[TileAdvQuarry] class QuarryTank(s: FluidStack, a: Int) extends FluidTank(s, a) {
    setTileEntity(TileAdvQuarry.this)

    override def onContentsChanged(): Unit = {
      super.onContentsChanged()
      if (this.getFluidAmount <= 0) {
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

  class Mode extends INBTWritable with INBTReadable[Mode] {

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
      if (state.getValue(ACTING) ^ working) {
        InvUtils.setNewState(getWorld, getPos, self, state.withProperty(ACTING, Boolean.box(working)))
      }
    }

    def is(modes: Modes): Boolean = mode == modes

    def isWorking: Boolean = !is(NONE) && !is(NOT_NEED_BREAK)

    def reduceReceive: Boolean = is(MAKE_FRAME)

    override def toString: String = "ChunkDestroyer mode = " + mode

    override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
      nbt.tap(_.setInteger("mode", mode.index))
    }

    override def readFromNBT(tag: NBTTagCompound): Mode = {
      this.mode = tag.getInteger("mode") match {
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

  override def getName: String = getDebugName

  override protected def getSymbol: Symbol = TileAdvQuarry.SYMBOL

  override lazy val getStorage = new Storage {
    override def insertItem(stack: ItemStack): Unit = cacheItems.add(stack)

    override def insertFluid(fluid: FluidStack, amount: Long): Unit = {
      fluidStacks.get(fluid) match {
        case Some(tank) => tank.fill(fluid, true)
        case None => fluidStacks.put(fluid, new QuarryTank(fluid, Int.MaxValue))
      }
    }
  }
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
  private final val NBT_QUARRY_ENCH = "nbt_qench"
  private final val NBT_DIG_RANGE = "nbt_digrange"
  private final val NBT_MODE = "nbt_quarrymode"
  private final val NBT_ITEM_LIST = "nbt_itemlist"
  private final val NBT_ITEM_ELEMENTS = "nbt_itemelements"
  final val VALID_ATTACHMENTS: Set[Attachments[_]] = Set(Attachments.EXP_PUMP, Attachments.REPLACER)

  val defaultEnch = QEnch(efficiency = 0, unbreaking = 0, fortune = 0, silktouch = false)
  val defaultRange: DigRange = new DigRange(BlockPos.ORIGIN, BlockPos.ORIGIN) {
    override val defined: Boolean = false
    override val toString: String = "Dig Range Not Defined"
    override val timeInTick = 0

    override def min: BlockPos = BlockPos.ORIGIN
  }

  case class QEnch(efficiency: Int, unbreaking: Int, fortune: Int, silktouch: Boolean, other: Map[Int, Int] = Map.empty) extends INBTWritable {

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

    val maxStore = MAX_STORED * (efficiency + 1) * APowerTile.MJToMicroMJ

    val maxReceive = if (efficiency >= 5) maxStore else if (efficiency == 0) maxStore / 1000 else (maxStore * Math.pow(efficiency.toDouble / 5.0, 3)).toLong

    val mode: Int = if (silktouch) -1 else fortune

    override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
      nbt.setInteger("efficiency", efficiency)
      nbt.setInteger("unbreaking", unbreaking)
      nbt.setInteger("fortune", fortune)
      nbt.setBoolean("silktouch", silktouch)
      val o = new NBTTagCompound
      other.map { case (i, l) => Option(Enchantment.getEnchantmentByID(i)) -> l }.foreach {
        case (Some(e), l) => o.setInteger(e.getRegistryName.toString, l)
        case _ =>
      }
      nbt.setTag("other", o)
      nbt
    }
  }

  object QEnch extends INBTReadable[QEnch] {
    override def readFromNBT(tag: NBTTagCompound): QEnch = {
      if (!tag.hasNoTags) {
        val o = tag.getCompoundTag("other")
        val otherMap = o.getKeySet.asScala.map(s => ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(s)) -> o.getInteger(s))
          .collect { case (e, l) if e != null => Enchantment.getEnchantmentID(e) -> l }.toMap
        QEnch(tag.getInteger("efficiency"), tag.getInteger("unbreaking"), tag.getInteger("fortune"), tag.getBoolean("silktouch"), otherMap)
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

    final val renderBox = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ)

    final val lengthSq = min.distanceSq(maxZ, maxY, maxZ)

    val timeInTick: Int = {
      val length = (maxX + maxZ - minX - minZ + 2) / 2
      Math.max(length / 128, 1)
    }

    val dropWidth: Int = Math.max(1, (maxX - minX) / 32)

    def chunkSeq: List[ChunkPos] = {
      val a = for (x <- Range(minX, maxX, 16) :+ maxX;
                   z <- Range(minZ, maxZ, 16) :+ maxZ
                   ) yield new ChunkPos(x >> 4, z >> 4)
      a.toList
    }

    override val toString: String = s"Dig Range from ($minX, $minY, $minZ) to ($maxX, $maxY, $maxZ). $timeInTick times a tick."

    override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
      nbt.setBoolean("defined", defined)
      nbt.setInteger("minX", minX)
      nbt.setInteger("minY", minY)
      nbt.setInteger("minZ", minZ)
      nbt.setInteger("maxX", maxX)
      nbt.setInteger("maxY", maxY)
      nbt.setInteger("maxZ", maxZ)
      nbt
    }
  }

  object DigRange extends INBTReadable[DigRange] {
    override def readFromNBT(tag: NBTTagCompound): DigRange = {
      if (tag.getBoolean("defined")) {
        DigRange(tag.getInteger("minX"), tag.getInteger("minY"), tag.getInteger("minZ"),
          tag.getInteger("maxX"), tag.getInteger("maxY"), tag.getInteger("maxZ"))
      } else {
        defaultRange
      }
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
      nbt.setTag(NBT_ITEM_ELEMENTS,
        list.map(_.toNBT).foldLeft(NBTBuilder.empty) { case (b, t) => b.appendTag(t) }.toList)
      nbt
    }

    override def readFromNBT(tag: NBTTagCompound): ItemList = {
      val l = tag.getTagList(NBT_ITEM_ELEMENTS, Constants.NBT.TAG_COMPOUND)
      l.tagIterator.map(VersionUtil.fromNBTTag).foreach(this.add)
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

  private[this] final lazy val nonAcceptableModule = Set(
    QuarryPlusI.pumpModule.getSymbol
    //    QuarryPlusI.torchModule.getSymbol // In 1.12, the module works fine.
  )

  val moduleFilter: java.util.function.Predicate[IModuleItem] = new java.util.function.Predicate[IModuleItem] {
    override def test(t: IModuleItem) = !nonAcceptableModule.contains(t.getSymbol)
  }

}
