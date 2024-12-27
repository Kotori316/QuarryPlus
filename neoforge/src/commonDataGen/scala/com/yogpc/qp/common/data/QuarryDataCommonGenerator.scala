package com.yogpc.qp.common.data

import com.yogpc.qp.QuarryPlus
import net.minecraft.DetectedVersion
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.data.metadata.PackMetadataGenerator
import net.minecraft.network.chat.Component
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.metadata.pack.PackMetadataSection
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.data.event.GatherDataEvent

import java.util.Collections
import scala.annotation.static
import scala.jdk.javaapi.CollectionConverters

@EventBusSubscriber(modid = QuarryPlus.modID, bus = EventBusSubscriber.Bus.MOD)
class QuarryDataCommonGenerator {
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
    event.addProvider(QuarrySpriteSourceProvider(event.getGenerator.getPackOutput, event.getLookupProvider, event.getExistingFileHelper))
    event.addProvider(PackMetadataGenerator(event.getGenerator.getPackOutput)
      .add(PackMetadataSection.TYPE, PackMetadataSection(Component.literal("QuarryPlus Resource"), DetectedVersion.BUILT_IN.getPackVersion(PackType.CLIENT_RESOURCES)))
    )

    val blockTag = QuarryBlockTagProvider(event.getGenerator.getPackOutput, event.getLookupProvider, event.getExistingFileHelper)
    val itemTag = QuarryItemTagProvider(event.getGenerator.getPackOutput, event.getLookupProvider, event.getExistingFileHelper, blockTag.contentsGetter())
    event.addProvider(blockTag)
    event.addProvider(itemTag)
    event.addProvider(QuarryEnchantmentTagProvider(event.getGenerator.getPackOutput, enchantmentProvider, event.getExistingFileHelper))
  }
}
