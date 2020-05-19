package com.yogpc.qp.machines.base

import java.util.function.Predicate

import com.google.gson.JsonArray
import com.mojang.datafixers.Dynamic
import com.mojang.datafixers.types.{DynamicOps, JsonOps}
import com.yogpc.qp._
import com.yogpc.qp.machines.base.QuarryBlackList.Entry
import com.yogpc.qp.machines.workbench.BlockData
import jp.t2v.lab.syntax.MapStreamSyntax
import net.minecraft.block.BlockState
import net.minecraft.enchantment.Enchantment
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraftforge.common.util.Constants

import scala.jdk.javaapi.CollectionConverters

case class EnchantmentFilter(fortuneInclude: Boolean,
                             silktouchInclude: Boolean,
                             fortuneList: Set[Entry],
                             silktouchList: Set[Entry]) {

  def getEnchantmentPredicate(state: BlockState, world: IBlockReader, pos: BlockPos): Predicate[Enchantment] = {
    val fortune = canApplyFortune(state, world, pos)
    val silk = canApplySilktouch(state, world, pos)
    EnchantmentFilter.excludePredicate(fortune, silk)
  }

  def canApplyFortune(state: BlockState, world: IBlockReader, pos: BlockPos): Boolean = {
    fortuneList.exists(_.test(state, world, pos)) == fortuneInclude
  }

  def toggleFortune: EnchantmentFilter = this.copy(fortuneInclude = !fortuneInclude)

  def addFortune(name: ResourceLocation): EnchantmentFilter = this.copy(fortuneList = fortuneList + QuarryBlackList.Name(name))

  def removeFortune(entry: Entry): EnchantmentFilter = this.copy(fortuneList = fortuneList - entry)

  def canApplySilktouch(state: BlockState, world: IBlockReader, pos: BlockPos): Boolean = {
    silktouchList.exists(_.test(state, world, pos)) == silktouchInclude
  }

  def toggleSilktouch: EnchantmentFilter = this.copy(silktouchInclude = !silktouchInclude)

  def addSilktouch(name: ResourceLocation): EnchantmentFilter = this.copy(silktouchList = silktouchList + QuarryBlackList.Name(name))

  def removeSilktouch(entry: Entry): EnchantmentFilter = this.copy(silktouchList = silktouchList - entry)
}

object EnchantmentFilter {
  val defaultInstance: EnchantmentFilter = EnchantmentFilter(fortuneInclude = false, silktouchInclude = false, Set.empty, Set.empty)
  val NOT_SILK: Predicate[Enchantment] = IEnchantableItem.SILKTOUCH.negate()
  val NOT_FORTUNE: Predicate[Enchantment] = IEnchantableItem.FORTUNE.negate()
  val NOT_SILK_AND_FORTUNE: Predicate[Enchantment] = NOT_SILK and NOT_FORTUNE

  def excludePredicate(fortuneOK: Boolean, silktouchOK: Boolean): Predicate[Enchantment] = {
    if (fortuneOK && silktouchOK) {
      MapStreamSyntax.always_true()
    } else if (fortuneOK && !silktouchOK) { // Exclude Silktouch
      NOT_SILK
    } else if (!fortuneOK && silktouchOK) { // Exclude Fortune
      NOT_FORTUNE
    } else { // Exclude Silktouch and Fortune
      NOT_SILK_AND_FORTUNE
    }
  }

  def fromLegacyTag(nbt: CompoundNBT): EnchantmentFilter = {
    import scala.jdk.CollectionConverters._
    val fortuneInclude = nbt.getBoolean("fortuneInclude")
    val silktouchInclude = nbt.getBoolean("silktouchInclude")
    val fortuneList: Set[Entry] = nbt.getList("fortuneList", Constants.NBT.TAG_COMPOUND).asScala
      .map(i => BlockData.read(i.asInstanceOf[CompoundNBT]))
      .map(d => QuarryBlackList.Name(d.name))
      .toSet
    val silktouchList: Set[Entry] = nbt.getList("silktouchList", Constants.NBT.TAG_COMPOUND).asScala
      .map(i => BlockData.read(i.asInstanceOf[CompoundNBT]))
      .map(d => QuarryBlackList.Name(d.name))
      .toSet
    new EnchantmentFilter(fortuneInclude, silktouchInclude, fortuneList, silktouchList)
  }

  def read[T](tagLike: Dynamic[T]): EnchantmentFilter = {
    import scala.jdk.CollectionConverters._
    val mapOpt: Option[Map[String, Dynamic[T]]] = tagLike.asMapOpt[String, Dynamic[T]](k => k.asString(""), v => v).asScala.map(m => m.asScala.toMap)
    mapOpt.map { m =>
      val fortuneInclude = m.get("fortuneInclude").exists(_.asBoolean(false))
      val silktouchInclude = m.get("silktouchInclude").exists(_.asBoolean(false))
      val fortuneList = m.get("fortuneList").map(_.convert(JsonOps.INSTANCE).getValue).map(j => QuarryBlackList.GSON.fromJson(j, classOf[Array[Entry]])).map(_.toSet).getOrElse(Set.empty)
      val silktouchList = m.get("silktouchList").map(_.convert(JsonOps.INSTANCE).getValue).map(j => QuarryBlackList.GSON.fromJson(j, classOf[Array[Entry]])).map(_.toSet).getOrElse(Set.empty)
      new EnchantmentFilter(fortuneInclude, silktouchInclude, fortuneList, silktouchList)
    }.getOrElse(defaultInstance)
  }

  def write[T](filter: EnchantmentFilter, ops: DynamicOps[T]): T = {
    val map = Map(
      "fortuneInclude" -> ops.createBoolean(filter.fortuneInclude),
      "silktouchInclude" -> ops.createBoolean(filter.silktouchInclude),
      "fortuneList" -> Dynamic.convert(JsonOps.INSTANCE, ops, convertToJsonArray(filter.fortuneList)),
      "silktouchList" -> Dynamic.convert(JsonOps.INSTANCE, ops, convertToJsonArray(filter.silktouchList))
    ).map { case (str, t) => ops.createString(str) -> t }
    ops.createMap(CollectionConverters.asJava(map))
  }

  private def convertToJsonArray(s: Set[Entry]): JsonArray = {
    val a = new JsonArray
    s.map(e => QuarryBlackList.GSON.toJsonTree(e)).foreach(a.add)
    a
  }
}
