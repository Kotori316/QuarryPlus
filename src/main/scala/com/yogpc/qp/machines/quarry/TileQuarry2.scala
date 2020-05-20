package com.yogpc.qp.machines.quarry

import cats.implicits._
import com.yogpc.qp._
import com.yogpc.qp.machines.base._
import com.yogpc.qp.machines.{PowerManager, TranslationKeys}
import com.yogpc.qp.packet.{PacketHandler, TileMessage}
import com.yogpc.qp.utils.{Holder, ItemDamage}
import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.entity.item.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{CompoundNBT, NBTDynamicOps, StringNBT}
import net.minecraft.state.properties.BlockStateProperties
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraft.util.text.{ITextComponent, StringTextComponent, TranslationTextComponent}
import net.minecraft.util.{Unit => _, _}
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.common.{DimensionManager, MinecraftForge}
import net.minecraftforge.event.ForgeEventFactory
import net.minecraftforge.event.world.BlockEvent

import scala.jdk.CollectionConverters._

class TileQuarry2 extends APowerTile(Holder.quarry2)
  with IEnchantableTile
  with HasStorage
  with HasInv
  with IAttachableWithModuleScalaImpl
  with IDebugSender
  with IChunkLoadTile
  with ContainerQuarryModule.HasModuleInventory
  with StatusContainer.StatusProvider
  with EnchantmentHolder.EnchantmentProvider
  with IRemotePowerOn {
  self =>

  import TileQuarry2._

  var enchantments: EnchantmentHolder = EnchantmentHolder.noEnch
  var enchantmentFilter: EnchantmentFilter = EnchantmentFilter.defaultInstance
  var area: Area = Area.zeroArea
  var action: QuarryAction = QuarryAction.none
  var target: BlockPos = BlockPos.ZERO
  var yLevel = 1
  var frameMode = false
  private val storage = new QuarryStorage
  val moduleInv = new QuarryModuleInventory(5, this, _ => refreshModules(), jp.t2v.lab.syntax.MapStreamSyntax.always_true())
  finishListener.add(() => DimensionManager.keepLoaded(getDiggingWorld.getDimension.getType, false))

  def getDiggingWorld: ServerWorld = {
    if (!super.getWorld.isRemote) {
      this.area.getWorld(super.getWorld.asInstanceOf[ServerWorld])
    } else {
      throw new IllegalStateException("Tried to get server world in client.")
    }
  }

  override protected def getEnergyInTick(): Unit = {
    // Module Tick Action
    modules.foreach(_.invoke(IModule.Tick(self)))
  }

  override def workInTick(): Unit = {
    val faster = Boolean.unbox(Config.common.fastQuarryHeadMove.get())
    // Quarry action
    var i = 0
    var broken = false
    while (i < enchantments.efficiency + 1 && !broken) {
      if (!faster) broken = true
      action.action(target)
      if (action.canGoNext(self)) {
        action = action.nextAction(self)
        if (action == QuarryAction.none) {
          PowerManager.configure0(self)
        }
        PacketHandler.sendToClient(TileMessage.create(self), world)
      }
      target = action.nextTarget()
      i += 1
    }
    updateWorkingState()
    // Insert items
    storage.pushItem(world, pos)
    if (world.getGameTime % 20 == 0) { // Insert fluid every 1 second.
      storage.pushFluid(world, pos)
      if (isWorking && getWorld.getDimension.getType != getDiggingWorld.getDimension.getType) {
        DimensionManager.keepLoaded(getDiggingWorld.getDimension.getType, true)
      }
    }
  }

  override def remove(): Unit = {
    super[IChunkLoadTile].releaseTicket()
    super.remove()
  }

  override def write(nbt: CompoundNBT): CompoundNBT = {
    nbt.put("target", target.toLong.toNBT)
    nbt.put("enchantments", enchantments.toNBT)
    nbt.put("area", area.toNBT)
    nbt.put("mode", action.toNBT)
    nbt.put("storage", storage.toNBT)
    nbt.put("moduleInv", moduleInv.toNBT)
    nbt.put("yLevel", yLevel.toNBT)
    nbt.put("frameMode", frameMode.toNBT)
    nbt.put("enchantmentFilter", EnchantmentFilter.write(enchantmentFilter, NBTDynamicOps.INSTANCE))
    super.write(nbt)
  }

  override def read(nbt: CompoundNBT): Unit = {
    super.read(nbt)
    target = BlockPos.fromLong(nbt.getLong("target"))
    enchantments = EnchantmentHolder.enchantmentHolderLoad(nbt, "enchantments")
    area = Area.areaLoad(nbt.getCompound("area"))
    storage.deserializeNBT(nbt.getCompound("storage"))
    moduleInv.deserializeNBT(nbt.getCompound("moduleInv"))
    yLevel = nbt.getInt("yLevel")
    action = QuarryAction.load(self, nbt, "mode")
    frameMode = nbt.getBoolean("frameMode")
    enchantmentFilter = EnchantmentFilter.read(new com.mojang.datafixers.Dynamic(NBTDynamicOps.INSTANCE, nbt.get("enchantmentFilter")))
  }

  override protected def isWorking = target != BlockPos.ZERO && action.mode != none

  def onActivated(player: PlayerEntity): Unit = {
    // Called in server world.
    import com.yogpc.qp.machines.quarry.QuarryAction.BreakInsideFrame
    this.action match {
      case QuarryAction.waiting | _: BreakInsideFrame => frameMode = !frameMode
        player.sendStatusMessage(new TranslationTextComponent(TranslationKeys.CHANGEMODE,
          new TranslationTextComponent(if (frameMode) TranslationKeys.FILLER_MODE else TranslationKeys.QUARRY_MODE)), false)
      case _ => G_ReInit()
        player.sendStatusMessage(new TranslationTextComponent(TranslationKeys.QUARRY_RESTART), false)
    }
  }

  /**
   * Called after enchantment setting.
   */
  override def G_ReInit(): Unit = {
    if (area == Area.zeroArea) {
      val facing = world.getBlockState(pos).get(BlockStateProperties.FACING)
      Area.findQuarryArea(facing, world, pos) match {
        case (newArea, markerOpt) =>
          area = newArea
          markerOpt.foreach(m => m.removeFromWorldWithItem().asScala.foreach(storage.insertItem))
      }
    }
    action = QuarryAction.waiting
    PowerManager.configureQuarryWork(this, enchantments.efficiency, enchantments.unbreaking, 0)
    configure(getMaxStored, getMaxStored)
  }

  override def getEnchantments: java.util.Map[ResourceLocation, Integer] = {
    EnchantmentHolder.getEnchantmentMap(enchantments).collect(enchantCollector).asJava
  }

  override def setEnchantment(id: ResourceLocation, value: Short): Unit = {
    enchantments = EnchantmentHolder.updateEnchantment(enchantments, id, value)
  }

  /**
   * @param attachments that you're trying to add.
   * @return whether this machine can accept the attachment.
   */
  override def isValidAttachment(attachments: IAttachment.Attachments[_ <: APacketTile]): Boolean = IAttachment.Attachments.ALL.contains(attachments)

  /**
   * This method does not place any blocks.
   *
   * @return True if succeeded.
   */
  def breakBlock(world: ServerWorld, pos: BlockPos): (Boolean, Int) = {
    import scala.jdk.CollectionConverters._
    if (pos.getX % 6 == 0 && pos.getZ % 6 == 0) {
      // Gather items
      val aabb = new AxisAlignedBB(pos.getX - 8, pos.getY - 3, pos.getZ - 8, pos.getX + 8, pos.getY + 5, pos.getZ + 8)
      gatherDrops(world, aabb)
    }
    val returnValue = modules.foldMap(m => m.invoke(IModule.BeforeBreak(world, pos)))

    if (returnValue.canGoNext && PowerManager.useEnergyBreak(self, pos, enchantments, modules.exists(IModule.hasReplaceModule), false)) {
      val fakePlayer = QuarryFakePlayer.get(world, pos)
      val state = world.getBlockState(pos)
      val enchantmentPredicate = enchantmentFilter.getEnchantmentPredicate(state, world, pos)
      val pickaxe = getEnchantedPickaxe(enchantmentPredicate)
      fakePlayer.setHeldItem(Hand.MAIN_HAND, pickaxe)
      val event = new BlockEvent.BreakEvent(world, pos, state, fakePlayer)
      MinecraftForge.EVENT_BUS.post(event)
      if (!event.isCanceled) {
        if (!world.isAirBlock(pos)) {
          val drops = NonNullList.create[ItemStack]
          drops.addAll(Block.getDrops(state, world, pos, world.getTileEntity(pos), fakePlayer, pickaxe))
          ForgeEventFactory.fireBlockHarvesting(drops, world, pos, state, self.enchantments.fortune, 1.0f, self.enchantments.silktouch, fakePlayer)
          fakePlayer.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY)

          drops.asScala.groupBy(ItemDamage.apply).view.mapValues(_.map(_.getCount).sum).toList.map { case (damage, i) => damage.toStack(i) }.foreach(storage.addItem)
        }
        true -> event.getExpToDrop // true means work is finished.

      } else {
        true -> 0 // Once event is canceled, you should think the block is unbreakable.
      }
    } else {
      false -> 0 // Not enough energy or Module work is not finished.
    }
  }

  def gatherDrops(world: World, aabb: AxisAlignedBB): Unit = {
    import scala.jdk.CollectionConverters._
    world.getEntitiesWithinAABB[ItemEntity](classOf[ItemEntity], aabb, EntityPredicates.IS_ALIVE)
      .asScala.foreach { e =>
      this.storage.addItem(e.getItem)
      QuarryPlus.proxy.removeEntity(e)
    }
    val orbs = world.getEntitiesWithinAABB(classOf[Entity], aabb).asScala.toList
    modules.foreach(_.invoke(IModule.CollectingItem(orbs)))
  }

  override def getDebugName: String = TranslationKeys.quarry2

  /**
   * For internal use only.
   *
   * @return debug info of valid machine.
   */
  override def getDebugMessages: java.util.List[StringTextComponent] = List(
    s"Mode: ${action.mode}",
    s"Target: ${target.show}",
    s"Enchantment: ${enchantments.show}",
    s"Area: ${area.show}",
    s"Storage: ${storage.show}",
    s"FrameMode: $frameMode",
    s"Digs to y = $yLevel",
    s"Modules: ${modules.mkString(comma)}",
    s"Attachments: ${attachments.mkString(comma)}",
  ).map(new StringTextComponent(_)).asJava

  def getName = new TranslationTextComponent(getDebugName)

  override def getDisplayName: ITextComponent = super.getDisplayName

  override def hasFastRenderer = true

  override def getRenderBoundingBox: AxisAlignedBB = {
    if (area != Area.zeroArea) Area.areaBox(area)
    else super.getRenderBoundingBox
  }

  override def getMaxRenderDistanceSquared: Double = {
    if (area != Area.zeroArea) Area.areaLengthSq(area)
    else super.getMaxRenderDistanceSquared
  }

  override def getCapability[T](cap: Capability[T], side: Direction): LazyOptional[T] = {
    Cap.asJava(Cap.make(cap, this, IRemotePowerOn.Cap.REMOTE_CAPABILITY()) orElse Cap.dummyItemOrFluid(cap) orElse super.getCapability(cap, side).asScala)
  }

  override def getStorage: HasStorage.Storage = storage

  override def getModules: List[IModule] = modules

  @OnlyIn(Dist.CLIENT)
  override def getStatusStrings: Seq[String] = {
    if (clientText == null) return Nil
    import net.minecraft.client.resources.I18n
    val enchantmentStrings = EnchantmentHolder.getEnchantmentStringSeq(this.enchantments)
    val requiresEnergy = (PowerManager.calcEnergyBreak(1.5f, this.enchantments) + PowerManager.calcEnergyQuarryHead(this, 1, enchantments.unbreaking)) /
      APowerTile.FEtoMicroJ * (if (Config.common.fastQuarryHeadMove.get()) enchantments.efficiency + 1 else 1)
    val energyStrings = Seq(I18n.format(TranslationKeys.REQUIRES), s"$requiresEnergy FE/t")
    val containItems = if (clientText.getInt(0) <= 0) I18n.format(TranslationKeys.EMPTY_ITEM) else I18n.format(TranslationKeys.CONTAIN_ITEM, clientText.getInt(0).toString)
    val containFluids = if (clientText.getInt(1) <= 0) I18n.format(TranslationKeys.EMPTY_FLUID) else I18n.format(TranslationKeys.CONTAIN_FLUID, clientText.getInt(1).toString)
    val modules = if (this.modules.nonEmpty) "Modules" :: this.modules.map("  " + _.toString) else Nil
    enchantmentStrings ++ energyStrings ++ modules :+ containItems :+ containFluids
  }

  override def getMessageToSend: TextInClient = TextInClient.create(storage.itemSize, storage.fluidSize)

  override def getEnchantmentHolder: EnchantmentHolder = enchantments

  override def setArea(area: Area): Unit = {
    this.area = if (area.yMin == area.yMax) area.copy(yMax = area.yMin + 3) else area
  }

  override def startWorking(): Unit = {
    G_ReInit()
    if (!getWorld.isRemote) // Send client a packet to notify changes of area.
    PacketHandler.sendToClient(TileMessage.create(self), world)
  }

  override def getArea: Area = this.area

  override def startWaiting(): Unit = {
    this.action = QuarryAction.none
  }
}

object TileQuarry2 {
  //---------- Constants ----------
  val SYMBOL: Symbol = Symbol("NewQuarry")
  final val comma = ","

  //---------- Data ----------

  sealed class Mode(override val toString: String)

  val none = new Mode("none")
  val waiting = new Mode("waiting")
  val buildFrame = new Mode("BuildFrame")
  val breakInsideFrame = new Mode("BreakInsideFrame")
  val breakBlock = new Mode("BreakBlock")
  val checkDrops = new Mode("CheckDrops")

  //---------- NBT ----------
  //  private[this] final val MARKER: Marker = MarkerManager.getMarker("QUARRY_NBT")

  implicit val modeToNbt: Mode NBTWrapper StringNBT = mode => {
    StringNBT.valueOf(mode.toString)
  }
}
