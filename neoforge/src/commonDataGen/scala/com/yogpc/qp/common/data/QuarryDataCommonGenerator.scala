package com.yogpc.qp.common.data

import com.yogpc.qp.QuarryPlus
import net.minecraft.core.registries.Registries
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.data.metadata.PackMetadataGenerator
import net.minecraft.network.chat.Component
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.{EventBusSubscriber, Mod}
import net.neoforged.neoforge.data.event.{GatherDataEvent, GatherDataRegistryEntriesEvent}

import scala.annotation.static

@Mod("quarryplus_common_data")
@EventBusSubscriber
class QuarryDataCommonGenerator {
  QuarryPlus.LOGGER.info("Initialize finished quarryplus_common_data")
}

object QuarryDataCommonGenerator {
  @static
  @SubscribeEvent
  def onRegistryEntries(event: GatherDataRegistryEntriesEvent): Unit = {
    QuarryPlus.LOGGER.info("Start common registry entry data generation")
    val gatherer = event.gatherFor(QuarryPlus.modID)
    gatherer.add(Registries.ENCHANTMENT, EnchantmentProvider)
    gatherer.lootTable(new LootTableProvider.SubProviderEntry(c => new BlockDropProvider(c), LootContextParamSets.BLOCK))
  }

  @static
  @SubscribeEvent
  def onEvent(event: GatherDataEvent.Client): Unit = {
    QuarryPlus.LOGGER.info("Start common data generation")
    event.addProvider(StateAndModelProvider(event.getGenerator.getPackOutput))
    event.addProvider(QuarrySpriteSourceProvider(event.getGenerator.getPackOutput, event.getWorldLookupProvider))
    event.addProvider(PackMetadataGenerator.forFeaturePack(event.getGenerator.getPackOutput, Component.literal("QuarryPlus Resource")))

    val blockTag = QuarryBlockTagProvider(event.getGenerator.getPackOutput, event.getWorldLookupProvider)
    val itemTag = QuarryItemTagProvider(event.getGenerator.getPackOutput, event.getWorldLookupProvider)
    event.addProvider(blockTag)
    event.addProvider(itemTag)
    event.addProvider(QuarryEnchantmentTagProvider(event.getGenerator.getPackOutput, event.getWorldLookupProvider))
  }
}
