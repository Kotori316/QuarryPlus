package com.yogpc.qp.neoforge.data

import com.yogpc.qp.QuarryPlus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.{EventBusSubscriber, Mod}
import net.neoforged.neoforge.data.event.GatherDataEvent

import scala.annotation.static

object QuarryDataGenerator {
  @static
  @SubscribeEvent
  def onEvent(event: GatherDataEvent.Client): Unit = {
    QuarryPlus.LOGGER.info("Start NeoForge data generation")
    event.addProvider(new RecipeNeoForge(event.getGenerator.getPackOutput, event.getLookupProvider))
  }
}

@Mod("quarryplus_data")
@EventBusSubscriber
class QuarryDataGenerator {

}
