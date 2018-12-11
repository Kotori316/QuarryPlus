package com.yogpc.qp

import net.minecraft.world.storage.loot.LootTableList._
import net.minecraft.world.storage.loot.conditions.LootCondition
import net.minecraft.world.storage.loot.functions.LootFunction
import net.minecraft.world.storage.loot.{LootEntryEmpty, LootEntryItem, LootPool, RandomValueRange}
import net.minecraftforge.event.LootTableLoadEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Loot {
  final val instance = this

  private[this] final val NO_FUNCTION = Array.empty[LootFunction]
  private[this] final val NO_CONDITION = Array.empty[LootCondition]
  private[this] final val mirror = new LootEntryItem(
    QuarryPlusI.magicMirror, 1, 0, NO_FUNCTION, NO_CONDITION, QuarryPlusI.magicMirror.getRegistryName.toString
  )
  private[this] final val empty = new LootEntryEmpty(9, 0, NO_CONDITION, "EMPTY")
  private[this] final val pool = new LootPool(Array(mirror, empty), NO_CONDITION, new RandomValueRange(1), new RandomValueRange(0), QuarryPlus.Mod_Name)
  private[this] final val nameSet = Set(CHESTS_SIMPLE_DUNGEON, CHESTS_ABANDONED_MINESHAFT, CHESTS_DESERT_PYRAMID, CHESTS_JUNGLE_TEMPLE,
    CHESTS_STRONGHOLD_CORRIDOR, CHESTS_STRONGHOLD_CROSSING, CHESTS_STRONGHOLD_LIBRARY, CHESTS_VILLAGE_BLACKSMITH)

  @SubscribeEvent
  def addEntry(event: LootTableLoadEvent): Unit = {
    if (nameSet contains event.getName) {
      event.getTable.addPool(pool)
    }
  }

  /* Test codes
  /setblock ~ ~ ~ minecraft:chest 0 replace {LootTable:"minecraft:chests/simple_dungeon"}
  /setblock ~ ~ ~ minecraft:chest 0 replace {LootTable:"minecraft:chests/abandoned_mineshaft"}
  /setblock ~ ~ ~ minecraft:chest 0 replace {LootTable:"minecraft:chests/desert_pyramid"}
  /setblock ~ ~ ~ minecraft:chest 0 replace {LootTable:"minecraft:chests/jungle_temple"}
  /setblock ~ ~ ~ minecraft:chest 0 replace {LootTable:"minecraft:chests/stronghold_corridor"}
  /setblock ~ ~ ~ minecraft:chest 0 replace {LootTable:"minecraft:chests/stronghold_crossing"}
  /setblock ~ ~ ~ minecraft:chest 0 replace {LootTable:"minecraft:chests/stronghold_library"}
  /setblock ~ ~ ~ minecraft:chest 0 replace {LootTable:"minecraft:chests/village_blacksmith"}
   */
}
