package com.yogpc.qp.tile

import buildcraft.api.core.IAreaProvider
import buildcraft.api.tiles.TilesAPI
import com.yogpc.qp._
import com.yogpc.qp.block.ADismCBlock
import com.yogpc.qp.container.ContainerQuarryModule.HasModuleInventory
import com.yogpc.qp.container.{ContainerQuarryModule, StatusContainer}
import com.yogpc.qp.gui.TranslationKeys
import com.yogpc.qp.packet.{PacketHandler, TileMessage}
import com.yogpc.qp.utils.ReflectionHelper
import net.minecraft.block.state.IBlockState
import net.minecraft.client.resources.I18n
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagCompound, NBTTagString}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util._
import net.minecraft.util.math.{AxisAlignedBB, BlockPos, ChunkPos, Vec3i}
import net.minecraft.util.text.{TextComponentString, TextComponentTranslation}
import net.minecraft.world.{IInteractionObject, World, WorldServer}
import net.minecraftforge.common.ForgeChunkManager.Type
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.{ForgeChunkManager, MinecraftForge}
import net.minecraftforge.event.ForgeEventFactory
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler
import net.minecraftforge.fml.common.Loader
import org.apache.logging.log4j.{Marker, MarkerManager}

import scala.collection.JavaConverters._

class TileQuarry2 extends APowerTile()
  with IEnchantableTile
  with HasStorage
  with HasInv
  with IAttachable
  with IDebugSender
  with IChunkLoadTile
  with HasModuleInventory
  with StatusContainer.StatusProvider {
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
  val moduleInv = new QuarryModuleInventory(new TextComponentString("Modules"), 5, this, _ => refreshModules(), jp.t2v.lab.syntax.MapStreamSyntax.always_true())

  override def update(): Unit = {
    super.update()
    if (!world.isRemote && !machineDisabled) {
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
          PacketHandler.sendToDimension(TileMessage.create(self), world.provider.getDimension)
        }
        target = action.nextTarget()
        i += 1
      }
      val nowState = world.getBlockState(pos)
      if (nowState.getValue(ADismCBlock.ACTING) ^ isWorking) {
        if (isWorking) {
          startWork()
        } else {
          finishWork()
        }
        world.setBlockState(pos, nowState.withProperty(ADismCBlock.ACTING, Boolean.box(isWorking)))
      }
      // Insert items
      storage.pushItem(world, pos)
      if (world.getTotalWorldTime % 20 == 0)
        storage.pushFluid(world, pos) // Push every 1 sec.
    }
  }

  override def onChunkUnload(): Unit = {
    //    super[IChunkLoadTile].releaseTicket()
    ForgeChunkManager.releaseTicket(this.chunkTicket)
    super.onChunkUnload()
  }

  override def writeToNBT(nbt: NBTTagCompound) = {
    nbt.setTag("target", target.toLong.toNBT)
    nbt.setTag("enchantments", enchantments.toNBT)
    nbt.setTag("area", area.toNBT)
    nbt.setTag("mode", action.toNBT)
    nbt.setTag("storage", storage.toNBT)
    nbt.setTag("moduleInv", moduleInv.toNBT)
    nbt.setTag("yLevel", yLevel.toNBT)
    nbt.setTag("frameMode", frameMode.toNBT)
    super.writeToNBT(nbt)
  }

  override def readFromNBT(nbt: NBTTagCompound): Unit = {
    super.readFromNBT(nbt)
    target = BlockPos.fromLong(nbt.getLong("target"))
    enchantments = enchantmentHolderLoad(nbt, "enchantments")
    area = areaLoad(nbt, "area")
    action = QuarryAction.load(self, nbt, "mode")
    storage.deserializeNBT(nbt.getCompoundTag("storage"))
    moduleInv.deserializeNBT(nbt.getCompoundTag("moduleInv"))
    yLevel = nbt.getInteger("yLevel")
    frameMode = nbt.getBoolean("frameMode")
  }

  override protected def isWorking = target != BlockPos.ORIGIN && action.mode != none

  def onActivated(player: EntityPlayer): Unit = {
    // Called in server world.
    import com.yogpc.qp.tile.QuarryAction.BreakInsideFrame
    this.action match {
      case QuarryAction.waiting | _: BreakInsideFrame => frameMode = !frameMode
        player.sendStatusMessage(new TextComponentTranslation(TranslationKeys.CHANGEMODE,
          new TextComponentTranslation(if (frameMode) TranslationKeys.FILLER_MODE else TranslationKeys.QUARRY_MODE)), false)
      case _ => G_ReInit()
        player.sendStatusMessage(new TextComponentTranslation(TranslationKeys.QUARRY_RESTART), false)
    }
  }

  /**
    * Called after enchantment setting.
    */
  override def G_ReInit(): Unit = {
    if (area == zeroArea) {
      val facing = world.getBlockState(pos).getValue(ADismCBlock.FACING)
      findArea(facing, world, pos) match {
        case (newArea, markerOpt) =>
          area = newArea
          markerOpt.foreach(m => m.removeFromWorldWithItem().asScala.foreach(storage.insertItem))
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
      IEnchantableTile.SilktouchID -> enchantments.silktouch.compare(false)
    ) ++ enchantments.other
    enchantmentsMap.collect(enchantCollector).asJava
  }

  override def setEnchantment(id: Short, value: Short): Unit = {
    val newEnch = id match {
      case IEnchantableTile.EfficiencyID => enchantments.copy(efficiency = value)
      case IEnchantableTile.UnbreakingID => enchantments.copy(unbreaking = value)
      case IEnchantableTile.FortuneID => enchantments.copy(fortune = value)
      case IEnchantableTile.SilktouchID => enchantments.copy(silktouch = value > 0)
      case _ => enchantments.copy(other = enchantments.other + (id.toInt -> value))
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
    val internalModules = moduleInv.moduleItems().asScala.flatMap { e =>
      e.getKey.apply(e.getValue, self).toList
    }
    this.modules = IModule.defaultModules(this) ++ attachmentModules ++ internalModules
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
    import scala.collection.JavaConverters._
    if (pos.getX % 6 == 0 && pos.getZ % 6 == 0) {
      // Gather items
      val aabb = new AxisAlignedBB(pos.getX - 8, pos.getY - 3, pos.getZ - 8, pos.getX + 8, pos.getY + 5, pos.getZ + 8)
      gatherDrops(world, aabb)
    }
    val fakePlayer = QuarryFakePlayer.get(world.asInstanceOf[WorldServer], pos)
    fakePlayer.setHeldItem(EnumHand.MAIN_HAND, getEnchantedPickaxe)
    val event = new BlockEvent.BreakEvent(world, pos, state, fakePlayer)
    MinecraftForge.EVENT_BUS.post(event)
    if (!event.isCanceled) {
      val drops = if (self.enchantments.silktouch && state.getBlock.canSilkHarvest(world, pos, state, fakePlayer)) {
        val list = NonNullList.create[ItemStack]
        list.add(ReflectionHelper.invoke(TileBasic.createStackedBlock, state.getBlock, state).asInstanceOf[ItemStack])
        ForgeEventFactory.fireBlockHarvesting(list, world, pos, state, 0, 1.0f, true, fakePlayer)
        list
      } else {
        val list = NonNullList.create[ItemStack]
        TileBasic.getDrops(world, pos, state, state.getBlock, self.enchantments.fortune, list)
        ForgeEventFactory.fireBlockHarvesting(list, world, pos, state, self.enchantments.fortune, 1.0f, false, fakePlayer)
        list
      }
      if (TilePump.isLiquid(state) || PowerManager.useEnergyBreak(self, state.getBlockHardness(world, pos),
        TileQuarry2.enchantmentMode(enchantments), enchantments.unbreaking, modules.exists(IModule.hasReplaceModule))) {
        val returnValue = modules.foldLeft(IModule.NoAction: IModule.Result) { case (r, m) => IModule.Result.combine(r, m.invoke(IModule.BeforeBreak(event.getExpToDrop, world, pos))) }
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
    world.getEntitiesWithinAABB[EntityItem](classOf[EntityItem], aabb, EntitySelectors.IS_ALIVE)
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
  override def getDebugMessages = List(
    s"Mode: ${action.mode}",
    s"Target: $target",
    s"Enchantment: $enchantments",
    s"Area: $area",
    s"Storage: $storage",
    s"FrameMode: $frameMode",
    s"Digs to y = $yLevel",
    s"Modules: ${modules.mkString(comma)}",
    s"Attachments: ${attachments.mkString(comma)}"
  ).map(new TextComponentString(_)).asJava

  def getName = getDebugName

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

  override protected def getSymbol = TileQuarry2.SYMBOL

  override def hasCapability(capability: Capability[_], facing: EnumFacing) =
    capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing)

  override def getCapability[T](capability: Capability[T], facing: EnumFacing) = {
    if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(EmptyFluidHandler.INSTANCE)
    else super.getCapability(capability, facing)
  }

  override def getStatusStrings = {
    val enchantmentStrings = TileQuarry2.getEnchantmentMap(this.enchantments).toList
      .collect { case (location, i) if i > 0 => Option(Enchantment.getEnchantmentByID(location)).map(_.getTranslatedName(i)).getOrElse(s"$location -> $i") } match {
      case Nil => Nil
      case l => I18n.format(TranslationKeys.ENCHANTMENT) :: l
    }
    val requiresEnergy = 2D * PowerManager.calcEnergyBreak(1.5f, TileQuarry2.enchantmentMode(enchantments), enchantments.unbreaking) / APowerTile.MJToMicroMJ * (enchantments.efficiency + 1)
    val energyStrings = Seq(I18n.format(TranslationKeys.REQUIRES), s"$requiresEnergy FE/t")
    val containItems = if (storage.itemSize <= 0) I18n.format(TranslationKeys.EMPTY_ITEM) else I18n.format(TranslationKeys.CONTAIN_ITEM, storage.itemSize.toString)
    val containFluids = if (storage.fluidSize <= 0) I18n.format(TranslationKeys.EMPTY_FLUID) else I18n.format(TranslationKeys.CONTAIN_FLUID, storage.fluidSize.toString)
    enchantmentStrings ++ energyStrings :+ containItems :+ containFluids
  }
}

object TileQuarry2 {
  //---------- Constants ----------
  val SYMBOL = Symbol("NewQuarry")
  final val comma = ","

  val noEnch = EnchantmentHolder(0, 0, 0, silktouch = false)
  val zeroArea = Area(0, 0, 0, 0, 0, 0)

  //---------- Data ----------
  case class EnchantmentHolder(efficiency: Int, unbreaking: Int, fortune: Int, silktouch: Boolean, other: Map[Int, Int] = Map.empty) {
    holder =>
    override def toString = s"Efficiency=${holder.efficiency} Unbreaking=${holder.unbreaking} Fortune=${holder.fortune} Silktouch=${holder.silktouch} other=${holder.other}"
  }

  case class Area(xMin: Int, yMin: Int, zMin: Int, xMax: Int, yMax: Int, zMax: Int) {
    area =>
    override def toString = s"(${area.xMin}, ${area.yMin}, ${area.zMin}) -> (${area.xMax}, ${area.yMax}, ${area.zMax})"
  }

  sealed class Mode(override val toString: String)

  val none = new Mode("none")
  val waiting = new Mode("waiting")
  val buildFrame = new Mode("BuildFrame")
  val breakInsideFrame = new Mode("BreakInsideFrame")
  val breakBlock = new Mode("BreakBlock")
  val checkDrops = new Mode("CheckDrops")

  class InteractionObject(quarry2: TileQuarry2) extends IInteractionObject {
    override def createContainer(playerInventory: InventoryPlayer, playerIn: EntityPlayer) = new ContainerQuarryModule(quarry2, playerIn)

    override def getGuiID = ContainerQuarryModule.GUI_ID

    override val getName = TranslationKeys.quarry2

    override def hasCustomName = false

    override def getDisplayName = new TextComponentTranslation(getName)
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
    val pf: PartialFunction[TileEntity, IMarker] = if (Loader.isModLoaded(QuarryPlus.Optionals.BuildCraft_core)) {
      case provider: IAreaProvider => new IMarker.BCWrapper(provider)
      case m: IMarker if m.hasLink => m
      case t: TileEntity if t.hasCapability(TilesAPI.CAP_TILE_AREA_PROVIDER, facing) => new IMarker.BCWrapper(t.getCapability(TilesAPI.CAP_TILE_AREA_PROVIDER, facing))
    } else {
      case m: IMarker if m.hasLink => m
    }
    List(pos.offset(facing.getOpposite), pos.offset(facing.rotateY()), pos.offset(facing.rotateYCCW())).map(world.getTileEntity).collectFirst(pf) match {
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

  def getEnchantmentMap(enchantments: EnchantmentHolder): Map[Int, Int] = {
    Map(
      IEnchantableTile.EfficiencyID -> enchantments.efficiency,
      IEnchantableTile.UnbreakingID -> enchantments.unbreaking,
      IEnchantableTile.FortuneID -> enchantments.fortune,
      IEnchantableTile.SilktouchID -> enchantments.silktouch.compare(false)
    ) ++ enchantments.other
  }

  implicit val enchantmentHolderToNbt: EnchantmentHolder NBTWrapper NBTTagCompound = enchantments => {
    val enchantmentsMap = getEnchantmentMap(enchantments)
    enchantmentsMap.filter(_._2 > 0).foldLeft(new NBTTagCompound) { case (nbt, (id, level)) => nbt.setInteger(id.toString, level); nbt }
  }
  implicit val areaToNbt: Area NBTWrapper NBTTagCompound = area => {
    val nbt = new NBTTagCompound
    nbt.setInteger(NBT_X_MIN, area.xMin)
    nbt.setInteger(NBT_X_MAX, area.xMax)
    nbt.setInteger(NBT_Y_MIN, area.yMin)
    nbt.setInteger(NBT_Y_MAX, area.yMax)
    nbt.setInteger(NBT_Z_MIN, area.zMin)
    nbt.setInteger(NBT_Z_MAX, area.zMax)
    nbt
  }
  implicit val modeToNbt: Mode NBTWrapper NBTTagString = mode => {
    new NBTTagString(mode.toString)
  }
  val enchantmentHolderLoad: NBTLoad[EnchantmentHolder] = {
    case (tag, name) =>
      val nbt = tag.getCompoundTag(name)
      nbt.getKeySet.iterator().asScala.map(key => key.toInt -> nbt.getInteger(key))
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
      val nbt = tag.getCompoundTag(name)
      Area(nbt.getInteger(NBT_X_MIN), nbt.getInteger(NBT_Y_MIN), nbt.getInteger(NBT_Z_MIN), nbt.getInteger(NBT_X_MAX), nbt.getInteger(NBT_Y_MAX), nbt.getInteger(NBT_Z_MAX))
  }
}
