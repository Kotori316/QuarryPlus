package com.yogpc.qp.neoforge.data

import com.yogpc.qp.QuarryPlus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.{EventBusSubscriber, Mod}
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent

import scala.annotation.static

object QuarryDataGenerator {
  @static
  @SubscribeEvent
  def onEvent(event: GatherDataRegistryEntriesEvent): Unit = {
    QuarryPlus.LOGGER.info("Start NeoForge registry entry data generation")
    event.gatherFor(QuarryPlus.modID).add(RecipeNeoForge)
  }
}

@Mod("quarryplus_data")
@EventBusSubscriber
class QuarryDataGenerator {

}
