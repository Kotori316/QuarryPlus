package com.yogpc.qp.common.data

import com.yogpc.qp.QuarryPlus
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.data.metadata.PackMetadataGenerator
import net.minecraft.network.chat.Component
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.{EventBusSubscriber, Mod}
import net.neoforged.neoforge.data.event.GatherDataEvent

import java.util.Collections
import scala.annotation.static
import scala.jdk.javaapi.CollectionConverters

@Mod("quarryplus_common_data")
@EventBusSubscriber
class QuarryDataCommonGenerator {
  QuarryPlus.LOGGER.info("Initialize finished quarryplus_common_data")
}

object QuarryDataCommonGenerator {
  @static
  @SubscribeEvent
  def onEvent(event: GatherDataEvent.Client): Unit = {
    QuarryPlus.LOGGER.info("Start common data generation")
    val enchantmentProvider = new EnchantmentProvider(event.getGenerator.getPackOutput, event.getLookupProvider)
    event.addProvider(enchantmentProvider)
    event.addProvider(new LootTableProvider(event.getGenerator.getPackOutput, Collections.emptySet(),
      CollectionConverters.asJava(Seq(new LootTableProvider.SubProviderEntry(r => new BlockDropProvider(r), LootContextParamSets.BLOCK))),
      event.getLookupProvider
    ))
    event.addProvider(StateAndModelProvider(event.getGenerator.getPackOutput))
    event.addProvider(QuarrySpriteSourceProvider(event.getGenerator.getPackOutput, event.getLookupProvider))
    event.addProvider(PackMetadataGenerator.forFeaturePack(event.getGenerator.getPackOutput, Component.literal("QuarryPlus Resource")))

    val blockTag = QuarryBlockTagProvider(event.getGenerator.getPackOutput, event.getLookupProvider)
    val itemTag = QuarryItemTagProvider(event.getGenerator.getPackOutput, event.getLookupProvider)
    event.addProvider(blockTag)
    event.addProvider(itemTag)
    event.addProvider(QuarryEnchantmentTagProvider(event.getGenerator.getPackOutput, enchantmentProvider))
  }
}
