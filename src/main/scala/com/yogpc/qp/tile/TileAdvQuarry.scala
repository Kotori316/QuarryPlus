package com.yogpc.qp.tile

import java.lang.{Boolean => JBool}
import java.util.Collections

import com.yogpc.qp.block.{ADismCBlock, BlockBookMover}
import com.yogpc.qp.compat.InvUtils
import com.yogpc.qp.gui.TranslationKeys
import com.yogpc.qp.packet.PacketHandler
import com.yogpc.qp.packet.advquarry.{AdvContentMessage, AdvModeMessage}
import com.yogpc.qp.tile.IAttachment.Attachments
import com.yogpc.qp.tile.TileAdvQuarry._
import com.yogpc.qp.utils.{INBTReadable, INBTWritable, ItemElement, NotNullList, Reason, ReflectionHelper}
import com.yogpc.qp.version.VersionUtil
import com.yogpc.qp.{Config, PowerManager, QuarryPlus, QuarryPlusI, _}
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
  with IAttachable {
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

        type B_1 = (NonNullList[ItemStack], Seq[Int], Seq[Int], Seq[Int], Seq[Int], Double)
        type C_1 = (NonNullList[ItemStack], Seq[Int], Seq[Int], Seq[Int], Seq[Int])
        type D_1 = (NonNullList[ItemStack], Seq[Reason])
        val dropCheck: Unit => Either[Reason, NonNullList[ItemStack]] = (_: Unit) => {
          if (x % 3 == 0) {
            val list = NonNullList.create[ItemStack]()
            val expPump = facingMap.get(Attachments.EXP_PUMP).map(f => getWorld.getTileEntity(getPos.offset(f)))
              .collect { case pump: TileExpPump => pump }
            val axis = new AxisAlignedBB(new BlockPos(x - 6, 1, z - 6), target.add(6, 0, 6))
            //catch dropped items
            getWorld.getEntitiesWithinAABB(classOf[EntityItem], axis).asScala.filter(nonNull).filter(!_.isDead)
              .filter(_.getItem.getCount > 0).foreach(entity => {
              QuarryPlus.proxy.removeEntity(entity)
              list.add(entity.getItem)
            })
            //remove XPs
            getWorld.getEntitiesWithinAABB(classOf[EntityXPOrb], axis).asScala.filter(nonNull).filter(!_.isDead).foreach(entityXPOrb => {
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
          var y = target.getY - 1
          val pos = new MutableBlockPos(x, y, z)
          val flags = Array(x == digRange.minX, x == digRange.maxX, z == digRange.minZ, z == digRange.maxZ)
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
                  (state.getBlock match {
                    case _ if TileAdvQuarry.noDigBLOCKS.exists(_.contain(state)) => (0, destroy)
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
            if (flags.exists(b => b)) {
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
          Right((list, destroy.result(), dig.result(), drain.result(), shear.result(), requireEnergy * 1.25))
        }

        val cosumeEnergy: B_1 => Either[Reason, C_1] = b => {
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
          val p = new MutableBlockPos(x, 0, z)

          val reasons = new ArrayBuffer[Reason](0)
          dig.foreach { y =>
            p.setY(y)
            val state = getWorld.getBlockState(p)
            breakEvent(p, state, fakePlayer, expPump) { event =>
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
              tempList.clear()
              setBlock(p, toReplace)
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
                TileBasic.getDrops(getWorld, p, state, state.getBlock, 0, nnl)
                nnl.seq.foreach(l.add)
                // adding exp to pump is under.
              }
            } ++=: reasons
          }
          if (collectFurnaceXP) {
            val xp = TileBasic.floorFloat(l.list.map(ie => FurnaceRecipes.instance().getSmeltingResult(ie.toStack) -> ie.count).collect {
              case (s, i) if VersionUtil.nonEmpty(s) => FurnaceRecipes.instance().getSmeltingExperience(s) * i
            }.sum)
            expPump.filter(xpFilter(xp)).foreach(_.addXp(xp))
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
          Right(list, reasons)
        }

        chunkLoad()
        val n = if (chunks.isEmpty) digRange.timeInTick else 1
        var j = 0
        var notEnoughEnergy = false
        while (j < n && (mode is TileAdvQuarry.BREAKBLOCK) && !notEnoughEnergy) {
          ((for (a <- dropCheck().right;
                 b <- calcBreakEnergy(a).right;
                 c <- cosumeEnergy(b).right;
                 d <- digging(c).right) yield d) match {
            case Left(a) =>
              if (a.isEnergyIsuue) notEnoughEnergy = true
              Reason.printNonEnergy(a)
            case Right((l, reasons)) =>
              l.asScala.foreach(cacheItems.add)
              reasons.foreach(Reason.printNonEnergy)

              @tailrec
              def next(stream: Stream[((BlockPos, BlockPos), Int)], reasons: List[Reason] = Nil): (Option[BlockPos], Seq[Reason]) = {
                stream match {
                  case ((pre, newPos), index) #:: rest =>
                    if (pre == newPos) {
                      Some(BlockPos.ORIGIN) -> reasons
                    } else {
                      val energy = PowerManager.calcEnergyAdvSearch(self, ench.unbreaking, newPos.getY)
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
                        } else if (BlockPos.getAllInBoxMutable(new BlockPos(newPos.getX, 1, newPos.getZ), newPos).asScala.exists(p => !getWorld.isAirBlock(p))) {
                          Some(newPos) -> reasons
                        } else {
                          next(rest, Reason(newPos, index) :: reasons)
                        }
                      }
                    }
                }
              }

              next(nextPoses(digRange, target).take(32).zipWithIndex) match {
                case (opt, r) => r.reverse.foreach(Reason.print); opt
              }
          }).foreach { p =>
            target = p
            if (p == BlockPos.ORIGIN) {
              //Finished.
              target = digRange.min
              finishWork()
              mode set TileAdvQuarry.CHECKLIQUID
            }
          }

          j += 1
        }
      } else if (mode is TileAdvQuarry.NOTNEEDBREAK) {
        if (digRange.defined && !Config.content.noEnergy)
          if (getStoredEnergy > getMaxStored * 0.3) {
            mode set TileAdvQuarry.MAKEFRAME
            startWork()
          }
      } else if (mode is TileAdvQuarry.CHECKLIQUID) {
        nextPoses(digRange, target, inclusive = true).take(32 * digRange.timeInTick).foreach { case (_, p) =>
          target = p
          if (p == BlockPos.ORIGIN) {
            mode set TileAdvQuarry.NONE
          } else if (mode is TileAdvQuarry.CHECKLIQUID) {
            Iterator.iterate(p.down())(_.down()).takeWhile(_.getY > 0).filter(p => {
              val state = getWorld.getBlockState(p)
              !state.getBlock.isAir(state, getWorld, p) && TilePump.isLiquid(state)
            }).foreach(getWorld.setBlockToAir)
          }
        }

      } else if (mode is TileAdvQuarry.FILLBLOCKS) {
        val handler = InvUtils.findItemHander(getWorld, getPos.up, EnumFacing.DOWN).orNull
        if (handler != null) {
          val list = Range(0, handler.getSlots).find(i => {
            val stack = handler.getStackInSlot(i)
            VersionUtil.nonEmpty(stack) && stack.getItem.isInstanceOf[ItemBlock]
          }).map(handler.extractItem(_, 1, false)).toList
          val y = if (Config.content.removeBedrock) 1 else 5
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

  def checkandsetFrame(world: World, thatPos: BlockPos): Unit = {
    if (TilePump.isLiquid(world.getBlockState(thatPos))) {
      world.setBlockState(thatPos, QuarryPlusI.blockFrame.getDammingState)
    }
  }

  private def setBlock(pos: BlockPos, state: IBlockState): Unit = {
    val i = if (state.isFullCube) 2 else 3
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
    nbttc.setTag(NBT_QENCH, ench.toNBT)
    nbttc.setTag(NBT_DIGRANGE, digRange.toNBT)
    nbttc.setLong("NBT_TARGET", target.toLong)
    nbttc.setTag(NBT_MODE, mode.toNBT)
    nbttc.setTag(NBT_ITEMLIST, cacheItems.toNBT)
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
      target = digRange.min.add(-1, 0, 0)
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
      nbt.tap(_.setInteger("mode", mode.index))
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
      val itemElements = (new NBTTagList).tap(l => list.map(_.toNBT).foreach(l.appendTag))
      nbt.setTag(NBT_ITEMELEMENTS, itemElements)
      nbt
    }

    override def readFromNBT(tag: NBTTagCompound): ItemList = {
      if (tag.hasKey(NBT_ITEMLIST)) {
        val l = tag.getCompoundTag(NBT_ITEMLIST).getTagList(NBT_ITEMELEMENTS, Constants.NBT.TAG_COMPOUND)
        l.tagIterator.map(VersionUtil.fromNBTTag).foreach(this.add)
      }
      this
    }
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

  private[TileAdvQuarry] class Modes(val index: Int, override val toString: String)

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
