package com.yogpc.qp.machines.advquarry

import java.util

import cats.data._
import cats.implicits._
import com.yogpc.qp._
import com.yogpc.qp.machines.advquarry.AdvQuarryWork._
import com.yogpc.qp.machines.base._
import com.yogpc.qp.machines.modules.IModuleItem
import com.yogpc.qp.machines.pump.TilePump
import com.yogpc.qp.machines.quarry.{ContainerQuarryModule, QuarryFakePlayer}
import com.yogpc.qp.machines.{PowerManager, TranslationKeys}
import com.yogpc.qp.packet.PacketHandler
import com.yogpc.qp.packet.advquarry.AdvModeMessage
import com.yogpc.qp.utils.{Holder, NotNullList}
import net.minecraft.block.{Block, BlockState, Blocks}
import net.minecraft.entity.player.{PlayerEntity, PlayerInventory}
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.state.properties.BlockStateProperties
import net.minecraft.tileentity.ITickableTileEntity
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraft.util.text.{ITextComponent, StringTextComponent, TranslationTextComponent}
import net.minecraft.util.{Direction, Hand, IntReferenceHolder, ResourceLocation}
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.event.ForgeEventFactory
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.fluids.{FluidAttributes, FluidStack}

import scala.collection.mutable
import scala.jdk.CollectionConverters._

class TileAdvQuarry extends APowerTile(Holder.advQuarryType)
  with IEnchantableTile
  with HasInv
  with HasStorage
  with ITickableTileEntity
  with IDebugSender
  with IAttachableWithModuleScalaImpl
  with IChunkLoadTile
  with INamedContainerProvider
  with ContainerQuarryModule.HasModuleInventory
  with StatusContainer.StatusProvider
  with EnchantmentHolder.EnchantmentProvider
  with IRemotePowerOn {
  self =>

  var yLevel = 1
  var area = Area.zeroArea
  var enchantments = EnchantmentHolder.noEnch
  var action: AdvQuarryWork = AdvQuarryWork.none
  val storage = new AdvStorage
  val moduleInv = new QuarryModuleInventory(5, this, _ => refreshModules(), TileAdvQuarry.moduleFilter)

  def stickActivated(playerEntity: PlayerEntity): Unit = {
    //Called when block is right clicked with stick (item)
    if (action == AdvQuarryWork.waiting) {
      action = action.next(self)
      startWork()
    } else if (action == AdvQuarryWork.none) {
      action = new MakeFrame(area)
      startWork()
    }
  }

  def noFrameStart(): Unit = {
    if (action == AdvQuarryWork.waiting) {
      action = new BreakBlock(area, None)
      startWork()
    }
  }

  /**
   * Break blocks and gather drop items. This method doesn't try to break bedrock.
   *
   * @return [[cats.data.Ior.Right]] if succeeded, [[cats.data.Ior.Left]] if failed. [[cats.data.Ior.Both]] is returned if block is not breakable.
   */
  def breakBlock(target: BlockPos, searchReplacer: Boolean = true): Ior[Reason, Unit] = {
    val state = getWorld.getBlockState(target)
    if (state.isAir(getWorld, target)) {
      ().rightIor
    } else {
      val hardness = state.getBlockHardness(getWorld, target)
      val energy = PowerManager.calcEnergyBreak(hardness, enchantments)
      if (TileAdvQuarry.isUnbreakable(hardness, state) || energy > getMaxStored) {
        // Not breakable
        Ior.both(Reason.message(s"$state is too hard to break. Requiring $energy."), ())
      } else {
        if (PowerManager.useEnergyBreak(self, target, enchantments, modules.exists(IModule.hasReplaceModule))) {
          val fakePlayer = QuarryFakePlayer.get(world.asInstanceOf[ServerWorld], target)
          val pickaxe = getEnchantedPickaxe()
          fakePlayer.setHeldItem(Hand.MAIN_HAND, pickaxe)

          val ior = gatherItemDrops(target, searchReplacer, state, fakePlayer, pickaxe)
          ior.right.foreach(s => storage.addAll(s, log = true))
          ior.putRight(())
        } else {
          Reason.energy(state, getStoredEnergy).leftIor
        }
      }
    }
  }

  def gatherItemDrops(target: BlockPos, searchReplacer: Boolean, stateBefore: BlockState, fakePlayer: QuarryFakePlayer, pickaxe: ItemStack): Ior[Reason, AdvStorage] = {
    val storage = removeUnbreakable(stateBefore, self.getWorld, target, pickaxe, self.modules.flatMap(IModule.replaceBlocks(target.getY)).headOption.getOrElse(Blocks.AIR.getDefaultState))
    if (getWorld.isAirBlock(target)) {
      return storage.rightIor // Early return for unbreakable blocks that can be broken by above method. (Nether portal)
    }
    val breakEvent = new BlockEvent.BreakEvent(getWorld, target, stateBefore, fakePlayer)
    if (!MinecraftForge.EVENT_BUS.post(breakEvent)) {
      val returnValue = modules.foldMap(m => m.invoke(IModule.BeforeBreak(breakEvent.getExpToDrop, world, target)))
      if (!returnValue.canGoNext) {
        return Reason.message("Module work has not finished yet.").leftIor
      }
      val state = getWorld.getBlockState(target)
      if (TileAdvQuarry.isUnbreakable(state.getBlockHardness(getWorld, target), state)) {
        return storage.rightIor // Early return for unbreakable blocks such as Bedrock.
      }
      if (TilePump.isLiquid(state)) {
        // Pump fluids
        val fluidState = state.getFluidState
        if (fluidState.isSource) {
          storage.insertFluid(new FluidStack(fluidState.getFluid, FluidAttributes.BUCKET_VOLUME), log = false)
        }
      }
      // Gather dropped items
      if (!BlockWrapper.getWrappers.exists(_.contain(state))) {
        val drops = new NotNullList(mutable.Buffer.empty)
        val tile = getWorld.getTileEntity(target)
        drops.addAll(Block.getDrops(state, world.asInstanceOf[ServerWorld], target, tile, fakePlayer, pickaxe))
        ForgeEventFactory.fireBlockHarvesting(drops, getWorld, target, state, self.enchantments.fortune, 1.0f, self.enchantments.silktouch, fakePlayer)
        storage.insertItems(drops.seq)
      }

      if (!TilePump.isLiquid(state) && searchReplacer) {
        val replaced = self.modules.foldMap(_.invoke(IModule.AfterBreak(getWorld, target, state, getWorld.getGameTime)))
        if (!replaced.done) {
          // Not replaced
          getWorld.setBlockState(target, Blocks.AIR.getDefaultState, 0x10 | 0x2)
        }
      } else {
        getWorld.setBlockState(target, Blocks.AIR.getDefaultState, 0x10 | 0x2)
      }
      storage.rightIor
    } else {
      Reason.canceled(target, stateBefore).leftIor
    }
  }

  def removeUnbreakable(state: BlockState, world: World, pos: BlockPos, pickaxe: ItemStack, toReplace: => BlockState): AdvStorage = {
    val storage = new AdvStorage
    state.getBlock match {
      /*case Blocks.BEDROCK =>

        val toRemove = world.getDimension.getType match {
          case DimensionType.THE_NETHER => (pos.getY > 0 && pos.getY < 5) || (pos.getY > 122 && pos.getY < 127)
          case _ => pos.getY > 0 && pos.getY < 5
        }

        if (Config.common.removeBedrock.get() && toRemove) {
          if (Config.common.collectBedrock.get() && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, pickaxe) > 0) {
            storage.insertItem(new ItemStack(state.getBlock))
          }
          world.setBlockState(pos, toReplace, 0x10 | 0x2)
        }*/
      case Blocks.NETHER_PORTAL =>
        // Cause block update to remove other nether portal.
        world.setBlockState(pos, Blocks.AIR.getDefaultState, 3)
      case _ =>
    }
    storage
  }

  // Overrides
  override def remove(): Unit = {
    super[IChunkLoadTile].releaseTicket()
    super.remove()
  }

  override def workInTick(): Unit = {
    modules.foreach(_.invoke(IModule.Tick(self)))

    action.tick(self)
    if (action.goNext(self)) {
      action = action.next(self)
      // Always in server world.
      PacketHandler.sendToAround(AdvModeMessage.create(self), world, pos)
    }

    updateWorkingState()

    storage.pushItem(getWorld, getPos)
    if (getWorld.getGameTime % 10 == 0) {
      storage.pushFluid(getWorld, getPos)
    }
  }

  override def read(nbt: CompoundNBT): Unit = {
    super.read(nbt)
    enchantments = EnchantmentHolder.enchantmentHolderLoad(nbt, "enchantments")
    area = Area.areaLoad(nbt.getCompound("area"))
    storage.deserializeNBT(nbt.getCompound("storage"))
    moduleInv.deserializeNBT(nbt.getCompound("moduleInv"))
    yLevel = nbt.getInt("yLevel")
    action = AdvQuarryWork.load(self)(nbt.getCompound("mode"))
  }

  override def write(nbt: CompoundNBT): CompoundNBT = {
    nbt.put("enchantments", enchantments.toNBT)
    nbt.put("area", area.toNBT)
    nbt.put("mode", action.toNBT)
    nbt.put("storage", storage.toNBT)
    nbt.put("moduleInv", moduleInv.toNBT)
    nbt.put("yLevel", yLevel.toNBT)
    super.write(nbt)
  }

  override def getRenderBoundingBox: AxisAlignedBB = {
    if (area != Area.zeroArea) Area.areaBox(area)
    else super.getRenderBoundingBox
  }

  override def getMaxRenderDistanceSquared: Double = {
    if (area != Area.zeroArea) Area.areaLengthSq(area)
    else super.getMaxRenderDistanceSquared
  }

  override def getCapability[T](cap: Capability[T], side: Direction) = {
    Cap.asJava(Cap.make(cap, this, IRemotePowerOn.Cap.REMOTE_CAPABILITY()) orElse super.getCapability(cap, side).asScala)
  }

  // Interface implementation
  override protected def isWorking = action.target != BlockPos.ZERO && action != AdvQuarryWork.none

  /**
   * Called after enchantment setting.
   */
  override def G_ReInit(): Unit = {
    if (area == Area.zeroArea) {
      val facing = world.getBlockState(pos).get(BlockStateProperties.FACING)
      Area.findAdvQuarryArea(facing, world, pos) match {
        case (newArea, markerOpt) =>
          area = newArea.copy(yMax = newArea.yMin) // Prevent wrong line rendering
          markerOpt.foreach(m => m.removeFromWorldWithItem().forEach(storage.insertItem))
      }
    }
    action = AdvQuarryWork.waiting
    val maxReceive = TileAdvQuarry.maxReceiveEnergy(enchantments)
    configure(maxReceive, maxReceive)
    if (!world.isRemote) {
      PacketHandler.sendToAround(AdvModeMessage.create(self), world, pos)
    }
  }

  override def getEnchantments = {
    EnchantmentHolder.getEnchantmentMap(enchantments).collect(enchantCollector).asJava
  }

  override def setEnchantment(id: ResourceLocation, value: Short): Unit = {
    enchantments = EnchantmentHolder.updateEnchantment(enchantments, id, value)
  }

  override def getStorage = storage

  override def getDebugName = TranslationKeys.advquarry

  override def getDebugMessages: util.List[_ <: ITextComponent] = List(
    s"Mode: ${action.name}",
    s"Target: ${action.target.show}",
    s"Enchantment: ${enchantments.show}",
    s"Area: ${area.show}",
    s"Storage: ${storage.show}",
    s"Digs to y = $yLevel",
    s"Modules: ${modules.mkString(""",""")}",
  ).map(new StringTextComponent(_)).asJava

  override def getDisplayName = super.getDisplayName

  override def getName = new TranslationTextComponent(getDebugName)

  override def createMenu(id: Int, i: PlayerInventory, player: PlayerEntity) = new ContainerAdvQuarry(id, player, getPos)

  override def isValidAttachment(attachments: IAttachment.Attachments[_ <: APacketTile]) =
    attachments == IAttachment.Attachments.EXP_PUMP || attachments == IAttachment.Attachments.REPLACER

  @OnlyIn(Dist.CLIENT)
  override def getStatusStrings(trackIntSeq: Seq[IntReferenceHolder]): Seq[String] = {
    import net.minecraft.client.resources.I18n
    val enchantmentStrings = EnchantmentHolder.getEnchantmentStringSeq(this.enchantments)
    val containItems = if (trackIntSeq(0).get() <= 0) I18n.format(TranslationKeys.EMPTY_ITEM) else I18n.format(TranslationKeys.CONTAIN_ITEM, trackIntSeq(0).get().toString)
    val containFluids = if (trackIntSeq(1).get() <= 0) I18n.format(TranslationKeys.EMPTY_FLUID) else I18n.format(TranslationKeys.CONTAIN_FLUID, trackIntSeq(1).get().toString)
    val modules = if (this.modules.nonEmpty) "Modules" :: this.modules.map("  " + _.toString) else Nil
    enchantmentStrings ++ modules :+ containItems :+ containFluids
  }

  override lazy val tracks: Seq[IntReferenceHolder] = {
    val item = IntReferenceHolder.single()
    val fluid = IntReferenceHolder.single()
    val seq = Seq(item, fluid)
    this.updateIntRef(seq)
    seq
  }

  override def updateIntRef(trackIntSeq: Seq[IntReferenceHolder]): Unit = {
    trackIntSeq(0).set(storage.itemSize)
    trackIntSeq(1).set(storage.fluidSize)
  }

  override def getEnchantmentHolder = enchantments

  override def setArea(area: Area): Unit = this.area = area

  override def startWorking(): Unit = {
    G_ReInit()
  }

  override def getArea = this.area

  override def startWaiting(): Unit = {
    // Just means "stop".
    this.action = AdvQuarryWork.none
  }
}

object TileAdvQuarry {
  final val SYMBOL = Symbol("ChunkDestroyer")
  final val GUI_ID = QuarryPlus.modID + ":gui_" + QuarryPlus.Names.advquarry

  private[this] final val MAX_STORED = 300 * 256

  def maxReceiveEnergy(enchantment: EnchantmentHolder): Long = (enchantment.efficiency + 1) * MAX_STORED * APowerTile.MJToMicroMJ

  def isUnbreakable(hardness: Float, state: BlockState): Boolean = {
    hardness < 0 || hardness.isInfinity
  }


  private[this] final lazy val nonAcceptableModule = Set(
    Holder.itemPumpModule.getSymbol,
    Holder.itemTorchModule.getSymbol,
  )

  val moduleFilter: java.util.function.Predicate[IModuleItem] = item => !nonAcceptableModule.contains(item.getSymbol)
}
