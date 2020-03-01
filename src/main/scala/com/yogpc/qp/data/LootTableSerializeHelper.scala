package com.yogpc.qp.data

import cats.Eval
import com.google.gson.JsonElement
import com.yogpc.qp.machines.base.IEnchantableTile
import net.minecraft.block.Block
import net.minecraft.data.loot.BlockLootTables
import net.minecraft.util.ResourceLocation
import net.minecraft.world.storage.loot._
import net.minecraft.world.storage.loot.functions.ILootFunction

case class LootTableSerializeHelper(block: Block,
                                    functions: List[ILootFunction.IBuilder] = Nil)
  extends BlockLootTables with QuarryPlusDataProvider.DataBuilder {
  override def location: ResourceLocation = block.getRegistryName

  override def build: JsonElement = {
    val value = ItemLootEntry.builder(this.block)
    this.functions.foreach(value.acceptFunction)

    val builder = LootTable.builder().addLootPool(BlockLootTables.withSurvivesExplosion(this.block, LootPool.builder().rolls(ConstantRange.of(1)).addEntry(value)))
    builder.setParameterSet(LootParameterSets.BLOCK)

    LootTableManager.toJson(builder.build())
  }

  def dropWithEnchantments: LootTableSerializeHelper =
    this.copy(functions = LootTableSerializeHelper.dropFunction.value :: this.functions)
}

object LootTableSerializeHelper {
  final val dropFunction = Eval.later(IEnchantableTile.DropFunction.builder())

  def withDrop(block: Block): LootTableSerializeHelper = LootTableSerializeHelper(block)

  def withEnchantedDrop(block: Block): LootTableSerializeHelper = LootTableSerializeHelper(block, List(dropFunction.value))
}
