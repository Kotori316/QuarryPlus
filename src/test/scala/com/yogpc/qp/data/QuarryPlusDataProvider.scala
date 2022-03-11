package com.yogpc.qp.data

import com.yogpc.qp.QuarryPlus
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = QuarryPlus.modID)
object QuarryPlusDataProvider {

  @SubscribeEvent
  def gatherData(event: GatherDataEvent): Unit = {
    if (event.includeServer) {
      event.getGenerator.addProvider(new BlockDrop(event.getGenerator))
      event.getGenerator.addProvider(new Recipe(event.getGenerator))
      event.getGenerator.addProvider(new RecipeAdvancement(event.getGenerator))
      event.getGenerator.addProvider(new DefaultMachineConfig(event.getGenerator))
    }
  }

  def location(path: String) = new ResourceLocation(QuarryPlus.modID, path)
}
