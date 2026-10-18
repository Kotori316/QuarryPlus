package com.yogpc.qp.forge.data

import com.yogpc.qp.QuarryPlus
import net.minecraft.core.RegistrySetBuilder
import net.minecraftforge.common.data.RegistryDataBuilder
import net.minecraftforge.data.event.GatherDataEvent
import net.minecraftforge.eventbus.api.listener.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber

import scala.annotation.static

object QuarryDataGenerator {
  @static
  @SubscribeEvent
  def onEvent(event: GatherDataEvent): Unit = {
    val builder = RegistryDataBuilder.of().modid(QuarryPlus.modID)
    builder.reloadable(new RegistrySetBuilder().add(new RecipeForge()))
    event.getGenerator.addProvider(event.includeServer(), builder.reloadableGenerator(event.getGenerator.getPackOutput))
  }
}

@EventBusSubscriber(modid = QuarryPlus.modID, bus = EventBusSubscriber.Bus.MOD)
class QuarryDataGenerator {

}
