package com.yogpc.qp.data

import com.yogpc.qp.QuarryPlus
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.data.event.GatherDataEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = QuarryPlus.modID)
object QuarryPlusDataProvider {

  @SubscribeEvent
  def gatherData(event: GatherDataEvent): Unit = {
    event.getGenerator.addProvider(event.includeServer, new BlockDrop(event.getGenerator))
    event.getGenerator.addProvider(event.includeServer, new Recipe(event.getGenerator))
    event.getGenerator.addProvider(event.includeServer, new RecipeAdvancement(event.getGenerator))
    event.getGenerator.addProvider(event.includeServer, new DefaultMachineConfig(event.getGenerator))
    event.getGenerator.addProvider(event.includeServer, new MineableTag(event.getGenerator, event.getExistingFileHelper))
    event.getGenerator.addProvider(event.includeServer, new ItemTag(event.getGenerator, event.getExistingFileHelper))
  }

  def location(path: String) = new ResourceLocation(QuarryPlus.modID, path)
}
