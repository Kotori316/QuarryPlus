package com.yogpc.qp

import net.minecraft.world.storage.loot.LootTableList._
import net.minecraft.world.storage.loot.conditions.LootCondition
import net.minecraft.world.storage.loot.functions.LootFunction
import net.minecraft.world.storage.loot.{LootEntryEmpty, LootEntryItem, LootPool, RandomValueRange}
import net.minecraftforge.event.LootTableLoadEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Loot {
    val instance = this

    private val NO_FUNCTION = Array.empty[LootFunction]
    private val NO_CONDITION = Array.empty[LootCondition]
    private val mirror = new LootEntryItem(
        QuarryPlusI.magicmirror, 1, 0, NO_FUNCTION, NO_CONDITION, QuarryPlusI.magicmirror.getRegistryName.toString
    )
    private val empty = new LootEntryEmpty(4, 0, NO_CONDITION, "EMPTY")
    private val pool = new LootPool(Array(mirror, empty), NO_CONDITION, new RandomValueRange(1), new RandomValueRange(0), QuarryPlus.Mod_Name)

    @SubscribeEvent
    def addEntry(event: LootTableLoadEvent): Unit = {
        if (event.getName == CHESTS_SIMPLE_DUNGEON) {
            event.getTable.addPool(pool)
        } else if (event.getName == CHESTS_ABANDONED_MINESHAFT) {
            event.getTable.addPool(pool)
        } else if (event.getName == CHESTS_DESERT_PYRAMID) {
            event.getTable.addPool(pool)
        } else if (event.getName == CHESTS_JUNGLE_TEMPLE) {
            event.getTable.addPool(pool)
        } else if (event.getName == CHESTS_STRONGHOLD_CORRIDOR) {
            event.getTable.addPool(pool)
        } else if (event.getName == CHESTS_STRONGHOLD_CROSSING) {
            event.getTable.addPool(pool)
        } else if (event.getName == CHESTS_STRONGHOLD_LIBRARY) {
            event.getTable.addPool(pool)
        } else if (event.getName == CHESTS_VILLAGE_BLACKSMITH) {
            event.getTable.addPool(pool)
        }
    }
}
