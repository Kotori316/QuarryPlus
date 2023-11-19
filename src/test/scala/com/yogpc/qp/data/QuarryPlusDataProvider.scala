package com.yogpc.qp.data

import com.yogpc.qp.QuarryPlus
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.data.event.GatherDataEvent

import java.util.Collections
import scala.jdk.javaapi.CollectionConverters

object QuarryPlusDataProvider {

  @SubscribeEvent
  def gatherData(event: GatherDataEvent): Unit = {
    event.getGenerator.addProvider(event.includeServer, new LootTableProvider(event.getGenerator.getPackOutput, Collections.emptySet(),
      CollectionConverters.asJava(Seq(new LootTableProvider.SubProviderEntry(() => new BlockDropProvider, LootContextParamSets.BLOCK)))
    ))
    event.getGenerator.addProvider(event.includeServer, new Recipe(event.getGenerator))
    event.getGenerator.addProvider(event.includeServer, new RecipeAdvancement(event.getGenerator))
    event.getGenerator.addProvider(event.includeServer, new DefaultMachineConfig(event.getGenerator))
    event.getGenerator.addProvider(event.includeServer, new MineableTag(event.getGenerator, event.getLookupProvider, event.getExistingFileHelper))
    event.getGenerator.addProvider(event.includeServer, new ItemTag(event.getGenerator, event.getLookupProvider, event.getExistingFileHelper))
    event.getGenerator.addProvider(event.includeClient, new StateAndModelProvider(event.getGenerator, event.getExistingFileHelper))
  }

  def location(path: String) = new ResourceLocation(QuarryPlus.modID, path)
}

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = QuarryPlus.modID)
class QuarryPlusDataProvider
