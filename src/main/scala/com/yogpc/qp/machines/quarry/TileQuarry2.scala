package com.yogpc.qp.machines.quarry

import cats._
import cats.implicits._
import com.yogpc.qp._
import com.yogpc.qp.machines.base._
import com.yogpc.qp.machines.{PowerManager, TranslationKeys}
import com.yogpc.qp.utils.Holder
import net.minecraft.nbt.{NBTTagCompound, NBTTagString}
import net.minecraft.state.properties.BlockStateProperties
import net.minecraft.util.math.{BlockPos, Vec3i}
import net.minecraft.util.text.{TextComponentString, TextComponentTranslation}
import net.minecraft.util.{EnumFacing, ResourceLocation}
import org.apache.logging.log4j.{Marker, MarkerManager}

import scala.collection.JavaConverters

class TileQuarry2 extends APowerTile(Holder.quarry2)
  with IEnchantableTile
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
  var mode = none
  var target = BlockPos.ORIGIN
  val storage = new QuarryStorage

  override def tick(): Unit = {
    super.tick()
    // Quarry action
    // Insert items
    storage.pushItem(world, pos)
    storage.pushFluid(world, pos)
  }

  override def remove(): Unit = {
    super[IChunkLoadTile].releaseTicket()
    super.remove()
  }

  override def write(nbt: NBTTagCompound) = {
    nbt.put("enchantments", enchantments.toNBT)
    nbt.put("area", area.toNBT)
    nbt.put("mode", mode.toNBT)
    nbt.put("storage", storage.serializeNBT())
    super.write(nbt)
  }

  override def read(nbt: NBTTagCompound): Unit = {
    super.read(nbt)
    enchantments = enchantmentHolderLoad(nbt, "enchantments")
    area = areaLoad(nbt, "area")
    mode = modeLoad(nbt, "mode")
    storage.deserializeNBT(nbt.getCompound("storage"))
  }

  override protected def isWorking = target != BlockPos.ORIGIN && mode != none

  /**
    * Called after enchantment setting.
    */
  override def G_ReInit(): Unit = {
    if (area == zeroArea) {
      area = defaultArea(pos, world.getBlockState(pos).get(BlockStateProperties.FACING).getOpposite)
    }
    mode = waiting
    PowerManager.configureQuarryWork(this, enchantments.efficiency, enchantments.unbreaking, 0)
  }

  /**
    * @return Map (Enchantment id, level)
    */
  override def getEnchantments = {
    val enchantmentsMap = Map(
      IEnchantableTile.EfficiencyID -> enchantments.efficiency,
      IEnchantableTile.UnbreakingID -> enchantments.unbreaking,
      IEnchantableTile.FortuneID -> enchantments.fortune,
      IEnchantableTile.SilktouchID -> enchantments.silktouch.compare(false),
    ) ++ enchantments.other
    JavaConverters.mapAsJavaMap(enchantmentsMap.collect(enchantCollector))
  }

  /**
    * @param id    Enchantment id
    * @param value level
    */
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
    val internalModules = Nil
    this.modules = attachmentModules ++ internalModules
  }

  override def getDebugName = TranslationKeys.quarry

  /**
    * For internal use only.
    *
    * @return debug info of valid machine.
    */
  override def getDebugMessages = JavaConverters.seqAsJavaList(List(
    s"Mode: $mode",
    s"Target: ${target.show}",
    s"Enchantment: $enchantments",
    s"Area: ${area.show}",
  ).map(new TextComponentString(_)))

  override def getName = new TextComponentTranslation(getDebugName)

  override def getDisplayName = super.getDisplayName
}

object TileQuarry2 {
  //---------- Constants ----------
  val SYMBOL = Symbol("quarry2")

  val noEnch = EnchantmentHolder(0, 0, 0, silktouch = false)
  val zeroArea = Area(0, 0, 0, 0, 0, 0)

  //---------- Data ----------
  case class EnchantmentHolder(efficiency: Int, unbreaking: Int, fortune: Int, silktouch: Boolean, other: Map[ResourceLocation, Int] = Map.empty)

  case class Area(xMin: Int, yMin: Int, zMin: Int, xMax: Int, yMax: Int, zMax: Int)

  implicit val showArea: Show[Area] = area => s"(${area.xMin}, ${area.yMin}, ${area.zMin}) -> (${area.xMax}, ${area.yMax}, ${area.zMax})"

  sealed class Mode(override val toString: String)

  val none = new Mode("none")
  val waiting = new Mode("waiting")

  val posToArea: (Vec3i, Vec3i) => Area = {
    case (p1, p2) => Area(Math.min(p1.getX, p2.getX), Math.min(p1.getY, p2.getY), Math.min(p1.getZ, p2.getZ),
      Math.max(p1.getX, p2.getX), Math.max(p1.getY, p2.getY), Math.max(p1.getZ, p2.getZ))
  }

  def defaultArea(pos: BlockPos, facing: EnumFacing): Area = {
    val x = 11
    val y = (x - 1) / 2 //5
    val start = pos.offset(facing)
    val edge1 = start.offset(facing.rotateY(), y).up(3)
    val edge2 = start.offset(facing, x).offset(facing.rotateYCCW(), y)
    posToArea(edge1, edge2)
  }

  //---------- NBT ----------
  type NBTLoad[A] = (NBTTagCompound, String) => A
  private[this] final val marker: Marker = MarkerManager.getMarker("QUARRY_NBT")
  private[this] final val NBT_X_MIN = "xMin"
  private[this] final val NBT_X_MAX = "xMax"
  private[this] final val NBT_Y_MIN = "yMin"
  private[this] final val NBT_Y_MAX = "yMax"
  private[this] final val NBT_Z_MIN = "zMin"
  private[this] final val NBT_Z_MAX = "zMax"
  private[this] final val MODES = Set(none, waiting)

  private[this] def logTo(v: Any): Unit = {
    QuarryPlus.LOGGER.debug(marker, "To nbt of {}", v)
  }

  private[this] def logFrom(name: String, v: Any): Unit = {
    QuarryPlus.LOGGER.debug(marker, "From nbt of {} data:{}", name, v)
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
    logTo(mode)
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
  val modeLoad: NBTLoad[Mode] = {
    case (tag, name) =>
      val s = tag.getString(name)
      logFrom("Mode", s)
      MODES.collectFirst { case mode if mode.toString == s => mode }.get
  }
}
