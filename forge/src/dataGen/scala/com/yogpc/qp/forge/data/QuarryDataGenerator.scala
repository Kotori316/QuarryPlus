package com.yogpc.qp.forge.data

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.data.Recipe
import net.minecraftforge.data.event.GatherDataEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber

import scala.annotation.static

object QuarryDataGenerator {
  @static
  @SubscribeEvent
  def onEvent(event: GatherDataEvent): Unit = {
    val ingredientProvider = IngredientProviderForge()
    event.getGenerator.addProvider(event.includeServer, new Recipe(ingredientProvider, event.getGenerator.getPackOutput, event.getLookupProvider))
  }
}

@EventBusSubscriber(modid = QuarryPlus.modID, bus = EventBusSubscriber.Bus.MOD)
class QuarryDataGenerator {

}
