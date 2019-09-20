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
import net.minecraft.entity.player.{PlayerEntity, PlayerInventory, ServerPlayerEntity}
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.state.properties.BlockStateProperties
import net.minecraft.tileentity.ITickableTileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.{ITextComponent, StringTextComponent, TranslationTextComponent}
import net.minecraft.util.{Direction, Hand, ResourceLocation}
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.ForgeEventFactory
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.fluids.{FluidAttributes, FluidStack}
import net.minecraftforge.fml.network.NetworkHooks

import scala.collection.mutable
import scala.jdk.CollectionConverters._

class TileAdvQuarry extends APowerTile(Holder.advQuarryType)
  with IEnchantableTile
  with HasInv
  with HasStorage
  with ITickableTileEntity
  with IDebugSender
  with IAttachable
  with IChunkLoadTile
  with INamedContainerProvider
  with ContainerQuarryModule.HasModuleInventory {
  self =>

  var yLevel = 1
  var area = Area.zeroArea
  var enchantments = EnchantmentHolder.noEnch
  var action: AdvQuarryWork = AdvQuarryWork.none
  var modules: List[IModule] = List.empty
  var attachments: Map[IAttachment.Attachments[_], Direction] = Map.empty
  val storage = new AdvStorage
  val moduleInv = new QuarryModuleInventory(5, this, _ => refreshModules(), TileAdvQuarry.moduleFilter)

  def stickActivated(playerEntity: PlayerEntity): Unit = {
    //Called when noEnergy is true and block is right clicked with stick (item)
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

  def openModuleInv(player: ServerPlayerEntity): Unit = {
    if (hasWorld && !world.isRemote) {
      NetworkHooks.openGui(player, new ContainerQuarryModule.InteractionObject(getPos, TranslationKeys.advquarry), getPos)
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
      if (hardness < 0 || hardness.isInfinity || energy > getMaxStored) {
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

  def gatherItemDrops(target: BlockPos, searchReplacer: Boolean, state: BlockState, fakePlayer: QuarryFakePlayer, pickaxe: ItemStack): Ior[Reason, AdvStorage] = {
    val storage = new AdvStorage
    if (state.isAir(getWorld, target)) {
      return storage.rightIor
    }
    val breakEvent = new BlockEvent.BreakEvent(getWorld, target, state, fakePlayer)
    if (!MinecraftForge.EVENT_BUS.post(breakEvent)) {
      val returnValue = modules.foldMap(m => m.invoke(IModule.BeforeBreak(breakEvent.getExpToDrop, world, pos)))
      if (!returnValue.canGoNext) {
        return Reason.message("Module work has not finished yet.").leftIor
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
      Reason.canceled(target, state).leftIor
    }
  }

  def neighborChanged(): Unit = {
    attachments = attachments.filter { case (kind, facing) => kind.test(world.getTileEntity(pos.offset(facing))) }
    refreshModules()
  }

  def refreshModules(): Unit = {
    val attachmentModules = attachments.toList >>= { case (kind, facing) => kind.module(world.getTileEntity(pos.offset(facing))).toList }
    val internalModules = moduleInv.moduleItems().asScala.toList >>= (e => e.getKey.apply(e.getValue, self).toList)
    this.modules = attachmentModules ++ internalModules
  }

  // Overrides
  override def remove(): Unit = {
    super[IChunkLoadTile].releaseTicket()
    super.remove()
  }

  override def tick(): Unit = {
    super.tick()
    if (!world.isRemote && !machineDisabled) {
      modules.foreach(_.invoke(IModule.Tick(self)))

      action.tick(self)
      if (action.goNext(self)) {
        action = action.next(self)
        if (!world.isRemote) {
          PacketHandler.sendToAround(AdvModeMessage.create(self), world, pos)
        }
      }

      val nowState = world.getBlockState(pos)
      if (nowState.get(QPBlock.WORKING) ^ isWorking) {
        if (isWorking) {
          startWork()
        } else {
          finishWork()
        }
        world.setBlockState(pos, nowState.`with`(QPBlock.WORKING, Boolean.box(isWorking)))
      }

      storage.pushItem(getWorld, getPos)
      if (getWorld.getGameTime % 20 == 0) {
        storage.pushFluid(getWorld, getPos)
      }
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
          area = newArea
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
    val enchantmentsMap = Map(
      IEnchantableTile.EfficiencyID -> enchantments.efficiency,
      IEnchantableTile.UnbreakingID -> enchantments.unbreaking,
      IEnchantableTile.FortuneID -> enchantments.fortune,
      IEnchantableTile.SilktouchID -> enchantments.silktouch.compare(false),
    ) ++ enchantments.other
    enchantmentsMap.collect(enchantCollector).asJava
  }

  override def setEnchantment(id: ResourceLocation, value: Short): Unit = {
    val newEnch = id match {
      case IEnchantableTile.EfficiencyID => enchantments.copy(efficiency = value)
      case IEnchantableTile.UnbreakingID => enchantments.copy(unbreaking = value)
      case IEnchantableTile.FortuneID => enchantments.copy(fortune = value)
      case IEnchantableTile.SilktouchID => enchantments.copy(silktouch = value > 0)
      case _ => enchantments.copy(other = enchantments.other + (id -> value))
    }
    enchantments = newEnch
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

  override def connectAttachment(facing: Direction, attachment: IAttachment.Attachments[_ <: APacketTile], simulate: Boolean): Boolean = {
    val tile = world.getTileEntity(pos.offset(facing))
    if (!attachments.get(attachment).exists(_ != facing) && attachment.test(tile)) {
      if (!simulate) {
        attachments = attachments.updated(attachment, facing)
        refreshModules()
      }
      true
    } else {
      false
    }
  }

  override def isValidAttachment(attachments: IAttachment.Attachments[_ <: APacketTile]) =
    attachments == IAttachment.Attachments.EXP_PUMP || attachments == IAttachment.Attachments.REPLACER
}

object TileAdvQuarry {
  final val SYMBOL = Symbol("ChunkDestroyer")
  final val GUI_ID = QuarryPlus.modID + ":gui_" + QuarryPlus.Names.advquarry

  private[this] final val MAX_STORED = 300 * 256

  def maxReceiveEnergy(enchantment: EnchantmentHolder): Long = (enchantment.efficiency + 1) * MAX_STORED * APowerTile.MJToMicroMJ

  def isUnbreakable(hardness: Float, state: BlockState): Boolean = {
    hardness < 0 || hardness.isInfinity
  }

  def calcUnbreakableEnergy(state: BlockState): Long = {
    if (state.getBlock == Blocks.BEDROCK) {
      if (Config.common.removeBedrock.get()) {
        if (Config.common.collectBedrock.get()) {
          PowerManager.calcEnergyBreak(50f, EnchantmentHolder.noEnch)
        } else {
          PowerManager.calcEnergyBreak(100f, EnchantmentHolder.noEnch) * 2
        }
      } else {
        0L
      }
    } else {
      0L
    }
  }

  private[this] final lazy val nonAcceptableModule = Set(
    Holder.itemPumpModule.getSymbol,
    Holder.itemTorchModule.getSymbol,
  )

  val moduleFilter: java.util.function.Predicate[IModuleItem] = item => !nonAcceptableModule.contains(item.getSymbol)
}
