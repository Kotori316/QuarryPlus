package com.yogpc.qp.machines.quarry

import cats._
import cats.implicits._
import com.yogpc.qp._
import com.yogpc.qp.machines.base._
import com.yogpc.qp.machines.{PowerManager, TranslationKeys}
import com.yogpc.qp.packet.{PacketHandler, TileMessage}
import com.yogpc.qp.utils.Holder
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagCompound, NBTTagString}
import net.minecraft.state.properties.BlockStateProperties
import net.minecraft.util._
import net.minecraft.util.math.{AxisAlignedBB, BlockPos, Vec3i}
import net.minecraft.util.text.{TextComponentString, TextComponentTranslation}
import net.minecraft.world.{IInteractionObject, World, WorldServer}
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.ForgeEventFactory
import net.minecraftforge.event.world.BlockEvent
import org.apache.logging.log4j.{Marker, MarkerManager}

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
  var attachments: Map[IAttachment.Attachments[_], EnumFacing] = Map.empty
  var enchantments = noEnch
  var area = zeroArea
  var action: QuarryAction = QuarryAction.none
  var target = BlockPos.ORIGIN
  var yLevel = 1
  var frameMode = false
  private val storage = new QuarryStorage
  val moduleInv = new QuarryModuleInventory(new TextComponentString("Modules"), 5, this, _ => refreshModules())

  override def tick(): Unit = {
    super.tick()
    if (!world.isRemote) {
      // Quarry action
      var i = 0
      while (i < enchantments.efficiency + 1) {
        action.action(target)
        if (action.canGoNext(self)) {
          action = action.nextAction(self)
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

  override def write(nbt: NBTTagCompound) = {
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

  override def read(nbt: NBTTagCompound): Unit = {
    super.read(nbt)
    target = BlockPos.fromLong(nbt.getLong("target"))
    enchantments = enchantmentHolderLoad(nbt, "enchantments")
    area = areaLoad(nbt, "area")
    action = QuarryAction.load(self, nbt, "mode")
    storage.deserializeNBT(nbt.getCompound("storage"))
    moduleInv.deserializeNBT(nbt.getCompound("moduleInv"))
    yLevel = nbt.getInt("yLevel")
    frameMode = nbt.getBoolean("frameMode")
  }

  override protected def isWorking = target != BlockPos.ORIGIN && action.mode != none

  def onActivated(player: EntityPlayer): Unit = {
    // Called in server world.
    import com.yogpc.qp.machines.quarry.QuarryAction.BreakInsideFrame
    this.action match {
      case QuarryAction.none | QuarryAction.waiting | _: BreakInsideFrame => frameMode = !frameMode
        player.sendStatusMessage(new TextComponentTranslation(TranslationKeys.CHANGEMODE,
          new TextComponentTranslation(if (frameMode) TranslationKeys.FILLER_MODE else TranslationKeys.QUARRY_MODE)), false)
      case _ => G_ReInit()
        player.sendStatusMessage(new TextComponentString("Quarry Restarted."), false)
    }
  }

  /**
    * Called after enchantment setting.
    */
  override def G_ReInit(): Unit = {
    if (area == zeroArea) {
      val facing = world.getBlockState(pos).get(BlockStateProperties.FACING)
      findArea(facing, world, pos) match {
        case (newArea, markerOpt) =>
          area = newArea
          markerOpt.foreach(m => JavaConverters.asScalaBuffer(m.removeFromWorldWithItem()).foreach(storage.insertItem))
      }
    }
    action = QuarryAction.waiting
    PowerManager.configureQuarryWork(this, enchantments.efficiency, enchantments.unbreaking, 0)
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
  override def connectAttachment(facing: EnumFacing, attachment: IAttachment.Attachments[_ <: APacketTile], simulate: Boolean) = {
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
    val internalModules = JavaConverters.asScalaBuffer(moduleInv.moduleItems()).map { e =>
      e.getKey.getModule(e.getValue).apply(self)
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
  def breakBlock(world: World, pos: BlockPos, state: IBlockState): Boolean = {
    if (pos.getX % 6 == 0 && pos.getZ % 6 == 0) {
      // Gather items
      import scala.collection.JavaConverters._
      val aabb = new AxisAlignedBB(pos.getX - 4, pos.getY, pos.getZ - 4, pos.getX + 4, pos.getY + 5, pos.getZ + 4)
      world.getEntitiesWithinAABB[EntityItem](classOf[EntityItem], aabb, EntitySelectors.IS_ALIVE)
        .asScala.foreach { e =>
        this.storage.addItem(e.getItem)
        QuarryPlus.proxy.removeEntity(e)
      }
      val orbs = world.getEntitiesWithinAABB(classOf[Entity], aabb).asScala.toList
      modules.foreach(_.action(IModule.CollectingItem(orbs)))
    }
    val fakePlayer = QuarryFakePlayer.get(world.asInstanceOf[WorldServer])
    fakePlayer.setHeldItem(EnumHand.MAIN_HAND, getEnchantedPickaxe)
    val event = new BlockEvent.BreakEvent(world, pos, state, fakePlayer)
    MinecraftForge.EVENT_BUS.post(event)
    val drops = if (self.enchantments.silktouch && state.canSilkHarvest(world, pos, fakePlayer)) {
      val list = NonNullList.create[ItemStack]
      list.add(APacketTile.invoke(TileBasic.createStackedBlock, classOf[ItemStack], state.getBlock, state))
      ForgeEventFactory.fireBlockHarvesting(list, world, pos, state, 0, 1.0f, true, fakePlayer)
      list
    } else {
      val list = NonNullList.create[ItemStack]
      state.getBlock.getDrops(state, list, world, pos, self.enchantments.fortune)
      ForgeEventFactory.fireBlockHarvesting(list, world, pos, state, self.enchantments.fortune, 1.0f, false, fakePlayer)
      list
    }
    if (PowerManager.useEnergyBreak(self, state.getBlockHardness(world, pos),
      TileQuarry2.enchantmentMode(enchantments), enchantments.unbreaking, modules.exists(IModule.hasReplaceModule))) {
      val returnValue = modules.foldLeft(true) { case (b, m) => m.invoke(IModule.BeforeBreak(event.getExpToDrop, world, pos)) && b }
      drops.forEach(storage.addItem)
      returnValue
    } else {
      false
    }
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
    s"Modules: ${modules.mkString(comma)}",
    s"Attachments: ${attachments.mkString(comma)}",
  ).map(new TextComponentString(_)))

  def getName = new TextComponentTranslation(getDebugName)

  override def getDisplayName = super.getDisplayName

  override def hasFastRenderer = true

  override def getRenderBoundingBox: AxisAlignedBB = {
    if (area != TileQuarry2.zeroArea) TileQuarry2.areaBox(area)
    else super.getRenderBoundingBox
  }

  override def getMaxRenderDistanceSquared: Double = {
    if (area != TileQuarry2.zeroArea) TileQuarry2.areaLengthSq(area)
    else super.getMaxRenderDistanceSquared
  }

  override def getStorage = storage
}

object TileQuarry2 {
  //---------- Constants ----------
  val SYMBOL = Symbol("NewQuarry")
  final val comma = ","

  val noEnch = EnchantmentHolder(0, 0, 0, silktouch = false)
  val zeroArea = Area(0, 0, 0, 0, 0, 0)

  //---------- Data ----------
  case class EnchantmentHolder(efficiency: Int, unbreaking: Int, fortune: Int, silktouch: Boolean, other: Map[ResourceLocation, Int] = Map.empty)

  implicit val showEnchantmentHolder: Show[EnchantmentHolder] = holder =>
    s"Efficiency=${holder.efficiency} Unbreaking=${holder.unbreaking} Fortune=${holder.fortune} Silktouch=${holder.silktouch} other=${holder.other}"

  case class Area(xMin: Int, yMin: Int, zMin: Int, xMax: Int, yMax: Int, zMax: Int)

  implicit val showArea: Show[Area] = area => s"(${area.xMin}, ${area.yMin}, ${area.zMin}) -> (${area.xMax}, ${area.yMax}, ${area.zMax})"

  sealed class Mode(override val toString: String)

  val none = new Mode("none")
  val waiting = new Mode("waiting")
  val buildFrame = new Mode("BuildFrame")
  val breakInsideFrame = new Mode("BreakInsideFrame")
  val breakBlock = new Mode("BreakBlock")

  class InteractionObject(quarry2: TileQuarry2) extends IInteractionObject {
    override def createContainer(playerInventory: InventoryPlayer, playerIn: EntityPlayer) = new ContainerQuarryModule(quarry2, playerIn)

    override def getGuiID = ContainerQuarryModule.GUI_ID

    override val getName = new TextComponentTranslation(TranslationKeys.quarry)

    override def hasCustomName = false

    override def getCustomName = null
  }

  //---------- Data Functions ----------
  val posToArea: (Vec3i, Vec3i) => Area = {
    case (p1, p2) => Area(Math.min(p1.getX, p2.getX), Math.min(p1.getY, p2.getY), Math.min(p1.getZ, p2.getZ),
      Math.max(p1.getX, p2.getX), Math.max(p1.getY, p2.getY), Math.max(p1.getZ, p2.getZ))
  }

  def defaultArea(pos: BlockPos, facing: EnumFacing): Area = {
    val x = 11
    val y = (x - 1) / 2 //5
    val start = pos.offset(facing, 2)
    val edge1 = start.offset(facing.rotateY(), y).up(3)
    val edge2 = start.offset(facing, x - 1).offset(facing.rotateYCCW(), y)
    posToArea(edge1, edge2)
  }

  def findArea(facing: EnumFacing, world: World, pos: BlockPos) = {
    List(pos.offset(facing.getOpposite), pos.offset(facing.rotateY()), pos.offset(facing.rotateYCCW())).map(world.getTileEntity).collectFirst { case m: IMarker if m.hasLink => m } match {
      case Some(marker) => areaFromMarker(facing, pos, marker)
      case None => defaultArea(pos, facing.getOpposite) -> None
    }
  }

  def areaFromMarker(facing: EnumFacing, pos: BlockPos, marker: IMarker) = {
    if (marker.min().getX <= pos.getX && marker.max().getX >= pos.getX &&
      marker.min().getY <= pos.getY && marker.max().getY >= pos.getY &&
      marker.min().getZ <= pos.getZ && marker.max().getZ >= pos.getZ) {
      defaultArea(pos, facing.getOpposite) -> None
    } else {
      val subs = marker.max().subtract(marker.min())
      if (subs.getX > 1 && subs.getZ > 1) {
        val maxY = if (subs.getY > 1) marker.max().getY else marker.min().getY + 3
        posToArea(marker.min(), marker.max().copy(y = maxY)) -> Some(marker)
      } else {
        defaultArea(pos, facing.getOpposite) -> None
      }
    }
  }

  val areaLengthSq: Area => Double = {
    case Area(xMin, _, zMin, xMax, yMax, zMax) =>
      Math.pow(xMax - xMin, 2) + Math.pow(yMax, 2) + Math.pow(zMax - zMin, 2)
  }
  val areaBox: Area => AxisAlignedBB = area =>
    new AxisAlignedBB(area.xMin, 0, area.zMin, area.xMax, area.yMax, area.zMax)

  val enchantmentMode: EnchantmentHolder => Int = e =>
    if (e.silktouch) -1 else e.fortune

  //---------- NBT ----------
  type NBTLoad[A] = (NBTTagCompound, String) => A
  private[this] final val MARKER: Marker = MarkerManager.getMarker("QUARRY_NBT")
  private[this] final val NBT_X_MIN = "xMin"
  private[this] final val NBT_X_MAX = "xMax"
  private[this] final val NBT_Y_MIN = "yMin"
  private[this] final val NBT_Y_MAX = "yMax"
  private[this] final val NBT_Z_MIN = "zMin"
  private[this] final val NBT_Z_MAX = "zMax"

  private[this] def logTo[T: Show](v: T): Unit = {
    QuarryPlus.LOGGER.debug(MARKER, "To nbt of {}", v.show)
  }

  private[this] def logFrom(name: String, v: Any): Unit = {
    QuarryPlus.LOGGER.debug(MARKER, "From nbt of {} data:{}", name, v)
  }

  implicit val enchantmentHolderToNbt: EnchantmentHolder NBTWrapper NBTTagCompound = enchantments => {
    logTo(enchantments)
    val enchantmentsMap = Map(
      IEnchantableTile.EfficiencyID -> enchantments.efficiency,
      IEnchantableTile.UnbreakingID -> enchantments.unbreaking,
      IEnchantableTile.FortuneID -> enchantments.fortune,
      IEnchantableTile.SilktouchID -> enchantments.silktouch.compare(false),
    ) ++ enchantments.other
    enchantmentsMap.filter(_._2 > 0).foldLeft(new NBTTagCompound) { case (nbt, (id, level)) => nbt.putInt(id.toString, level); nbt }
  }
  implicit val areaToNbt: Area NBTWrapper NBTTagCompound = area => {
    logTo(area)
    val nbt = new NBTTagCompound
    nbt.putInt(NBT_X_MIN, area.xMin)
    nbt.putInt(NBT_X_MAX, area.xMax)
    nbt.putInt(NBT_Y_MIN, area.yMin)
    nbt.putInt(NBT_Y_MAX, area.yMax)
    nbt.putInt(NBT_Z_MIN, area.zMin)
    nbt.putInt(NBT_Z_MAX, area.zMax)
    nbt
  }
  implicit val modeToNbt: Mode NBTWrapper NBTTagString = mode => {
    new NBTTagString(mode.toString)
  }
  val enchantmentHolderLoad: NBTLoad[EnchantmentHolder] = {
    case (tag, name) =>
      val nbt = tag.getCompound(name)
      logFrom("EnchantmentHolder", nbt)
      JavaConverters.asScalaIterator(nbt.keySet().iterator()).map(key => new ResourceLocation(key) -> nbt.getInt(key))
        .foldLeft(noEnch) { case (enchantments, (id, value)) =>
          id match {
            case IEnchantableTile.EfficiencyID => enchantments.copy(efficiency = value)
            case IEnchantableTile.UnbreakingID => enchantments.copy(unbreaking = value)
            case IEnchantableTile.FortuneID => enchantments.copy(fortune = value)
            case IEnchantableTile.SilktouchID => enchantments.copy(silktouch = value > 0)
            case _ => enchantments.copy(other = enchantments.other + (id -> value))
          }
        }
  }
  val areaLoad: NBTLoad[Area] = {
    case (tag, name) =>
      val nbt = tag.getCompound(name)
      logFrom("Area", nbt)
      Area(nbt.getInt(NBT_X_MIN), nbt.getInt(NBT_Y_MIN), nbt.getInt(NBT_Z_MIN), nbt.getInt(NBT_X_MAX), nbt.getInt(NBT_Y_MAX), nbt.getInt(NBT_Z_MAX))
  }
}
