package com.yogpc.qp.forge.data

import com.yogpc.qp.QuarryPlus
import net.minecraftforge.data.event.GatherDataEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber

import scala.annotation.static

object QuarryDataGenerator {
  @static
  @SubscribeEvent
  def onEvent(event: GatherDataEvent): Unit = {
    event.getGenerator.addProvider(event.includeServer, RecipeForge(event.getGenerator.getPackOutput, event.getLookupProvider))
  }
}

@EventBusSubscriber(modid = QuarryPlus.modID, bus = EventBusSubscriber.Bus.MOD)
class QuarryDataGenerator {

}
