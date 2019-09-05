package com.yogpc.qp.machines.quarry

import cats.implicits._
import com.yogpc.qp._
import com.yogpc.qp.machines.base._
import com.yogpc.qp.machines.pump.TilePump
import com.yogpc.qp.machines.{PowerManager, TranslationKeys}
import com.yogpc.qp.packet.{PacketHandler, TileMessage}
import com.yogpc.qp.utils.{Holder, ItemDamage}
import net.minecraft.block.{Block, BlockState}
import net.minecraft.entity.Entity
import net.minecraft.entity.item.ItemEntity
import net.minecraft.entity.player.{PlayerEntity, PlayerInventory}
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{CompoundNBT, StringNBT}
import net.minecraft.state.properties.BlockStateProperties
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraft.util.text.{StringTextComponent, TranslationTextComponent}
import net.minecraft.util.{Unit => _, _}
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.ForgeEventFactory
import net.minecraftforge.event.world.BlockEvent

import scala.collection.JavaConverters

class TileQuarry2 extends APowerTile(Holder.quarry2)
  with IEnchantableTile
  with HasStorage
  with HasInv
  with IAttachable
  with IDebugSender
  with IChunkLoadTile {
  self =>

  import TileQuarry2._

  var modules: List[IModule] = Nil
  var attachments: Map[IAttachment.Attachments[_], Direction] = Map.empty
  var enchantments = EnchantmentHolder.noEnch
  var area = Area.zeroArea
  var action: QuarryAction = QuarryAction.none
  var target = BlockPos.ZERO
  var yLevel = 1
  var frameMode = false
  private val storage = new QuarryStorage
  val moduleInv = new QuarryModuleInventory(5, this, _ => refreshModules())

  override def tick(): Unit = {
    super.tick()
    if (!world.isRemote) {
      // Module Tick Action
      modules.foreach(_.invoke(IModule.Tick(self)))
      // Quarry action
      var i = 0
      while (i < enchantments.efficiency + 1) {
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
      val nowState = world.getBlockState(pos)
      if (nowState.get(QPBlock.WORKING) ^ isWorking) {
        if (isWorking) {
          startWork()
        } else {
          finishWork()
        }
        world.setBlockState(pos, nowState.`with`(QPBlock.WORKING, Boolean.box(isWorking)))
      }
      // Insert items
      storage.pushItem(world, pos)
      if (world.getGameTime % 20 == 0) // Insert fluid every 1 second.
        storage.pushFluid(world, pos)
    }
  }

  override def remove(): Unit = {
    super[IChunkLoadTile].releaseTicket()
    super.remove()
  }

  override def write(nbt: CompoundNBT) = {
    nbt.put("target", target.toLong.toNBT)
    nbt.put("enchantments", enchantments.toNBT)
    nbt.put("area", area.toNBT)
    nbt.put("mode", action.toNBT)
    nbt.put("storage", storage.toNBT)
    nbt.put("moduleInv", moduleInv.toNBT)
    nbt.put("yLevel", yLevel.toNBT)
    nbt.put("frameMode", frameMode.toNBT)
    super.write(nbt)
  }

  override def read(nbt: CompoundNBT): Unit = {
    super.read(nbt)
    target = BlockPos.fromLong(nbt.getLong("target"))
    enchantments = EnchantmentHolder.enchantmentHolderLoad(nbt, "enchantments")
    area = Area.areaLoad(nbt.getCompound("area"))
    action = QuarryAction.load(self, nbt, "mode")
    storage.deserializeNBT(nbt.getCompound("storage"))
    moduleInv.deserializeNBT(nbt.getCompound("moduleInv"))
    yLevel = nbt.getInt("yLevel")
    frameMode = nbt.getBoolean("frameMode")
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
          markerOpt.foreach(m => JavaConverters.asScalaBuffer(m.removeFromWorldWithItem()).foreach(storage.insertItem))
      }
    }
    action = QuarryAction.waiting
    PowerManager.configureQuarryWork(this, enchantments.efficiency, enchantments.unbreaking, 0)
    configure(getMaxStored, getMaxStored)
  }

  override def getEnchantments = {
    val enchantmentsMap = Map(
      IEnchantableTile.EfficiencyID -> enchantments.efficiency,
      IEnchantableTile.UnbreakingID -> enchantments.unbreaking,
      IEnchantableTile.FortuneID -> enchantments.fortune,
      IEnchantableTile.SilktouchID -> enchantments.silktouch.compare(false),
    ) ++ enchantments.other
    JavaConverters.mapAsJavaMap(enchantmentsMap.collect(enchantCollector))
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

  /**
   * @param attachment must have returned true by { @link IAttachable#isValidAttachment(IAttachment.Attachments)}.
   * @param simulate   true to avoid having side effect.
   * @return true if the attachment is (will be) successfully connected.
   */
  override def connectAttachment(facing: Direction, attachment: IAttachment.Attachments[_ <: APacketTile], simulate: Boolean) = {
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

  /**
   * @param attachments that you're trying to add.
   * @return whether this machine can accept the attachment.
   */
  override def isValidAttachment(attachments: IAttachment.Attachments[_ <: APacketTile]) = IAttachment.Attachments.ALL.contains(attachments)

  def refreshModules(): Unit = {
    val attachmentModules = attachments.flatMap { case (kind, facing) => kind.module(world.getTileEntity(pos.offset(facing))).asScala }.toList
    val internalModules = JavaConverters.asScalaBuffer(moduleInv.moduleItems()).toList >>= { e =>
      e.getKey.apply(e.getValue, self).toList
    }
    this.modules = attachmentModules ++ internalModules
  }

  def neighborChanged(): Unit = {
    attachments = attachments.filter { case (kind, facing) => kind.test(world.getTileEntity(pos.offset(facing))) }
    refreshModules()
  }

  /**
   * This method does not place any blocks.
   *
   * @return True if succeeded.
   */
  def breakBlock(world: ServerWorld, pos: BlockPos, state: BlockState): Boolean = {
    import scala.collection.JavaConverters._
    if (pos.getX % 6 == 0 && pos.getZ % 6 == 0) {
      // Gather items
      val aabb = new AxisAlignedBB(pos.getX - 8, pos.getY - 3, pos.getZ - 8, pos.getX + 8, pos.getY + 5, pos.getZ + 8)
      gatherDrops(world, aabb)
    }
    val fakePlayer = QuarryFakePlayer.get(world, pos)
    val pickaxe = getEnchantedPickaxe
    fakePlayer.setHeldItem(Hand.MAIN_HAND, pickaxe)
    val event = new BlockEvent.BreakEvent(world, pos, state, fakePlayer)
    MinecraftForge.EVENT_BUS.post(event)
    if (!event.isCanceled) {
      val drops = NonNullList.create[ItemStack]
      drops.addAll(Block.getDrops(state, world, pos, world.getTileEntity(pos), fakePlayer, pickaxe))
      ForgeEventFactory.fireBlockHarvesting(drops, world, pos, state, self.enchantments.fortune, 1.0f, self.enchantments.silktouch, fakePlayer)
      fakePlayer.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY)

      if (TilePump.isLiquid(state) || PowerManager.useEnergyBreak(self, pos, enchantments, modules.exists(IModule.hasReplaceModule))) {
        val returnValue = modules.foldMap(m => m.invoke(IModule.BeforeBreak(event.getExpToDrop, world, pos)))
        drops.asScala.groupBy(ItemDamage.apply).mapValues(_.map(_.getCount).sum).map { case (damage, i) => damage.toStack(i) }.foreach(storage.addItem)
        returnValue.canGoNext // true means work is finished.
      } else {
        false
      }
    } else {
      true // Once event is canceled, you should think the block is unbreakable.
    }
  }

  def gatherDrops(world: World, aabb: AxisAlignedBB): Unit = {
    import scala.collection.JavaConverters._
    world.getEntitiesWithinAABB[ItemEntity](classOf[ItemEntity], aabb, EntityPredicates.IS_ALIVE)
      .asScala.foreach { e =>
      this.storage.addItem(e.getItem)
      QuarryPlus.proxy.removeEntity(e)
    }
    val orbs = world.getEntitiesWithinAABB(classOf[Entity], aabb).asScala.toList
    modules.foreach(_.invoke(IModule.CollectingItem(orbs)))
  }

  override def getDebugName = TranslationKeys.quarry2

  /**
   * For internal use only.
   *
   * @return debug info of valid machine.
   */
  override def getDebugMessages = JavaConverters.seqAsJavaList(List(
    s"Mode: ${action.mode}",
    s"Target: ${target.show}",
    s"Enchantment: ${enchantments.show}",
    s"Area: ${area.show}",
    s"Storage: ${storage.show}",
    s"FrameMode: $frameMode",
    s"Digs to y = $yLevel",
    s"Modules: ${modules.mkString(comma)}",
    s"Attachments: ${attachments.mkString(comma)}",
  ).map(new StringTextComponent(_)))

  def getName = new TranslationTextComponent(getDebugName)

  override def getDisplayName = super.getDisplayName

  override def hasFastRenderer = true

  override def getRenderBoundingBox: AxisAlignedBB = {
    if (area != Area.zeroArea) Area.areaBox(area)
    else super.getRenderBoundingBox
  }

  override def getMaxRenderDistanceSquared: Double = {
    if (area != Area.zeroArea) Area.areaLengthSq(area)
    else super.getMaxRenderDistanceSquared
  }

  override def getStorage = storage
}

object TileQuarry2 {
  //---------- Constants ----------
  val SYMBOL = Symbol("NewQuarry")
  final val comma = ","

  //---------- Data ----------

  sealed class Mode(override val toString: String)

  val none = new Mode("none")
  val waiting = new Mode("waiting")
  val buildFrame = new Mode("BuildFrame")
  val breakInsideFrame = new Mode("BreakInsideFrame")
  val breakBlock = new Mode("BreakBlock")
  val checkDrops = new Mode("CheckDrops")

  class InteractionObject(quarry2: TileQuarry2) extends INamedContainerProvider {

    override def getDisplayName = new TranslationTextComponent(TranslationKeys.quarry2)

    override def createMenu(id: Int, p_createMenu_2_ : PlayerInventory, player: PlayerEntity) = {
      new ContainerQuarryModule(id, player, quarry2.getPos)
    }

  }

  //---------- NBT ----------
  //  private[this] final val MARKER: Marker = MarkerManager.getMarker("QUARRY_NBT")

  implicit val modeToNbt: Mode NBTWrapper StringNBT = mode => {
    new StringNBT(mode.toString)
  }
}
