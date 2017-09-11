package com.yogpc.qp

import net.minecraft.world.storage.loot.LootEntryItem
import net.minecraft.world.storage.loot.LootTableList._
import net.minecraft.world.storage.loot.conditions.LootCondition
import net.minecraft.world.storage.loot.functions.LootFunction
import net.minecraftforge.event.LootTableLoadEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Loot {
    val instance = this

    private val NO_FUNCTION = Array.empty[LootFunction]
    private val NO_CONDITION = Array.empty[LootCondition]
    private val mirror = new LootEntryItem(
        QuarryPlusI.magicmirror, 9, 1, NO_FUNCTION, NO_CONDITION, QuarryPlusI.magicmirror.getRegistryName.toString
    )

    @SubscribeEvent
    def addEntry(event: LootTableLoadEvent): Unit = {
        if (event.getName == CHESTS_SIMPLE_DUNGEON) {
            event.getTable.getPool("main").addEntry(mirror)
        } else if (event.getName == CHESTS_ABANDONED_MINESHAFT) {
            event.getTable.getPool("main").addEntry(mirror)
        } else if (event.getName == CHESTS_DESERT_PYRAMID) {
            event.getTable.getPool("main").addEntry(mirror)
        } else if (event.getName == CHESTS_JUNGLE_TEMPLE) {
            event.getTable.getPool("main").addEntry(mirror)
        } else if (event.getName == CHESTS_STRONGHOLD_CORRIDOR) {
            event.getTable.getPool("main").addEntry(mirror)
        } else if (event.getName == CHESTS_STRONGHOLD_CROSSING) {
            event.getTable.getPool("main").addEntry(mirror)
        } else if (event.getName == CHESTS_STRONGHOLD_LIBRARY) {
            event.getTable.getPool("main").addEntry(mirror)
        } else if (event.getName == CHESTS_VILLAGE_BLACKSMITH) {
            event.getTable.getPool("main").addEntry(mirror)
        }
    }
}
