package com.yogpc.qp.machines.quarry

import com.yogpc.qp._
import com.yogpc.qp.machines.base._
import com.yogpc.qp.machines.{PowerManager, TranslationKeys}
import com.yogpc.qp.utils.Holder
import net.minecraft.state.properties.BlockStateProperties
import net.minecraft.util.math.{BlockPos, Vec3i}
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.util.{EnumFacing, ResourceLocation}

import scala.collection.JavaConverters

class TileQuarry2 extends APowerTile(Holder.quarry2)
  with IEnchantableTile
  with HasInv
  with IAttachable
  with IDebugSender
  with IChunkLoadTile {

  var modules: List[IModule] = Nil
  var enchantments = TileQuarry2.noEnch
  var area = TileQuarry2.zeroArea
  var mode = TileQuarry2.none
  var target = BlockPos.ORIGIN

  override def remove(): Unit = {
    super[IChunkLoadTile].releaseTicket()
    super.remove()
  }
  override protected def isWorking = target != BlockPos.ORIGIN && mode != TileQuarry2.none

  /**
    * Called after enchantment setting.
    */
  override def G_ReInit(): Unit = {
    if (area == TileQuarry2.zeroArea) {
      area = TileQuarry2.defaultArea(pos, world.getBlockState(pos).get(BlockStateProperties.FACING).getOpposite)
    }
    mode = TileQuarry2.waiting
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
    * @param attachments must have returned true by { @link IAttachable#isValidAttachment(IAttachment.Attachments)}.
    * @param simulate    true to avoid having side effect.
    * @return true if the attachment is (will be) successfully connected.
    */
  override def connectAttachment(facing: EnumFacing, attachments: IAttachment.Attachments[_ <: APacketTile], simulate: Boolean) = {
    val tile = world.getTileEntity(pos.offset(facing))
    modules = modules ++ attachments.module(tile).asScala
    true
  }

  /**
    * @param attachments that you're trying to add.
    * @return whether this machine can accept the attachment.
    */
  override def isValidAttachment(attachments: IAttachment.Attachments[_ <: APacketTile]) = IAttachment.Attachments.ALL.contains(attachments)

  override def getDebugName = TranslationKeys.quarry

  /**
    * For internal use only.
    *
    * @return debug info of valid machine.
    */
  override def getDebugMessages = JavaConverters.seqAsJavaList(Nil)

  override def getName = new TextComponentTranslation(getDebugName)

  override def getDisplayName = super.getDisplayName
}

object TileQuarry2 {
  val SYMBOL = Symbol("quarry2")

  val noEnch = EnchantmentHolder(0, 0, 0, silktouch = false)
  val zeroArea = Area(0, 0, 0, 0, 0, 0)

  case class EnchantmentHolder(efficiency: Int, unbreaking: Int, fortune: Int, silktouch: Boolean, other: Map[ResourceLocation, Int] = Map.empty)

  case class Area(xMin: Int, yMin: Int, zMin: Int, xMax: Int, yMax: Int, zMax: Int)

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
}
