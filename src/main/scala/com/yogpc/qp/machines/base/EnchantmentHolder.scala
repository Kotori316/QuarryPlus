package com.yogpc.qp.machines.base

import cats.Show
import com.yogpc.qp.NBTWrapper
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.ResourceLocation

import scala.jdk.CollectionConverters._

case class EnchantmentHolder(efficiency: Int, unbreaking: Int, fortune: Int, silktouch: Boolean, other: Map[ResourceLocation, Int] = Map.empty)

object EnchantmentHolder {
  val noEnch = EnchantmentHolder(0, 0, 0, silktouch = false)

  def enchantmentMode(e: EnchantmentHolder): Int = if (e.silktouch) -1 else e.fortune

  def getEnchantmentMap(enchantments: EnchantmentHolder): Map[ResourceLocation, Int] = {
    val enchantmentsMap = Map(
      IEnchantableTile.EfficiencyID -> enchantments.efficiency,
      IEnchantableTile.UnbreakingID -> enchantments.unbreaking,
      IEnchantableTile.FortuneID -> enchantments.fortune,
      IEnchantableTile.SilktouchID -> enchantments.silktouch.compare(false),
    ) ++ enchantments.other
    enchantmentsMap
  }

  implicit val showEnchantmentHolder: Show[EnchantmentHolder] = holder =>
    s"Efficiency=${holder.efficiency} Unbreaking=${holder.unbreaking} Fortune=${holder.fortune} Silktouch=${holder.silktouch} other=${holder.other}"

  implicit val enchantmentHolderToNbt: EnchantmentHolder NBTWrapper CompoundNBT = enchantments => {
    getEnchantmentMap(enchantments).filter(_._2 > 0).foldLeft(new CompoundNBT) { case (nbt, (id, level)) => nbt.putInt(id.toString, level); nbt }
  }

  def enchantmentHolderLoad(tag: CompoundNBT, name: String): EnchantmentHolder = {
    val nbt = tag.getCompound(name)
    nbt.keySet().iterator().asScala
      .map(key => new ResourceLocation(key) -> nbt.getInt(key))
      .foldLeft(noEnch) { case (enchantments, (id, value)) =>
        updateEnchantment(enchantments, id, value)
      }
  }

  def updateEnchantment(enchantments: EnchantmentHolder, id: ResourceLocation, value: Int): EnchantmentHolder = {
    id match {
      case IEnchantableTile.EfficiencyID => enchantments.copy(efficiency = value)
      case IEnchantableTile.UnbreakingID => enchantments.copy(unbreaking = value)
      case IEnchantableTile.FortuneID => enchantments.copy(fortune = value)
      case IEnchantableTile.SilktouchID => enchantments.copy(silktouch = value > 0)
      case _ => enchantments.copy(other = enchantments.other + (id -> value))
    }
  }
}
