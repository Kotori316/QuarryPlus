package com.yogpc.qp.common.data

import com.yogpc.qp.QuarryPlus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.data.event.GatherDataEvent

import scala.annotation.static

@EventBusSubscriber(modid = QuarryPlus.modID, bus = EventBusSubscriber.Bus.MOD)
class QuarryDataCommonGenerator {
}

object QuarryDataCommonGenerator {
  @static
  @SubscribeEvent
  def onEvent(event: GatherDataEvent): Unit = {
    QuarryPlus.LOGGER.info("Start common data generation")
    val blockTag = QuarryBlockTagProvider(event.getGenerator.getPackOutput, event.getLookupProvider, event.getExistingFileHelper)
    val itemTag = QuarryItemTagProvider(event.getGenerator.getPackOutput, event.getLookupProvider, event.getExistingFileHelper, blockTag.contentsGetter())
    event.getGenerator.addProvider(event.includeServer(), blockTag)
    event.getGenerator.addProvider(event.includeServer(), itemTag)
  }
}
