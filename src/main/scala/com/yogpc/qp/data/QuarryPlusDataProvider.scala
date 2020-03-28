package com.yogpc.qp.data

import java.io.IOException

import com.google.gson.{GsonBuilder, JsonElement}
import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.machines.advquarry.BlockWrapper
import com.yogpc.qp.machines.base.QuarryBlackList
import com.yogpc.qp.machines.pb.PlacerTile
import net.minecraft.data.{DataGenerator, DirectoryCache, IDataProvider}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.conditions.NotCondition
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent

object QuarryPlusDataProvider {
  def gatherData(event: GatherDataEvent): Unit = {
    if (event.includeServer()) {
      event.getGenerator.addProvider(new Recipe(event.getGenerator))
      event.getGenerator.addProvider(new Wrapper(event.getGenerator))
      event.getGenerator.addProvider(new Advancements(event.getGenerator))
      event.getGenerator.addProvider(new BlockDrop(event.getGenerator))
      event.getGenerator.addProvider(new BlackList(event.getGenerator))
    }
  }

  def location(name: String) = new ResourceLocation(QuarryPlus.modID, name)

  trait DataBuilder {
    def location: ResourceLocation

    def build: JsonElement
  }

  abstract class DataProvider(generatorIn: DataGenerator) extends IDataProvider {
    override def act(cache: DirectoryCache): Unit = {
      val path = generatorIn.getOutputFolder
      val GSON = (new GsonBuilder).setPrettyPrinting().create

      for (builder <- data) {
        val out = path.resolve(s"data/${builder.location.getNamespace}/$directory/${builder.location.getPath}.json")
        try {
          IDataProvider.save(GSON, cache, builder.build, out)
        } catch {
          case e: IOException => QuarryPlus.LOGGER.error(s"Failed to save recipe ${builder.location}.", e)
        }
      }
    }

    override def getName: String = getClass.getName

    def directory: String

    def data: Seq[DataBuilder]
  }

  class Wrapper(g: DataGenerator) extends QuarryPlusDataProvider.DataProvider(g) {
    override def data: Seq[DataBuilder] = Seq(new DataBuilder {
      override def location: ResourceLocation = QuarryPlusDataProvider.location("default")

      override def build: JsonElement = BlockWrapper.GSON.toJsonTree(BlockWrapper.example)
    })

    override def directory: String = QuarryPlus.modID + "/adv_quarry"
  }

  class Advancements(g: DataGenerator) extends QuarryPlusDataProvider.DataProvider(g) {
    override def directory: String = "advancements"

    override def data: Seq[DataBuilder] = {
      import com.yogpc.qp.machines.quarry.BlockSolidQuarry
      import com.yogpc.qp.machines.workbench.TileWorkbench
      import com.yogpc.qp.utils.{EnableCondition, Holder}
      import net.minecraft.item.Items
      import net.minecraftforge.common.Tags

      val workbench = AdvancementSerializeHelper(Holder.blockWorkbench.getRegistryName, saveName = location("recipe/workbench"))
        .addItemCriterion(Tags.Items.STORAGE_BLOCKS_IRON)
        .addItemCriterion(Tags.Items.STORAGE_BLOCKS_GOLD)
        .addCondition(new EnableCondition(TileWorkbench.SYMBOL))

      val solidQuarry = AdvancementSerializeHelper(Holder.blockSolidQuarry.getRegistryName, saveName = location("recipe/solid_quarry"))
        .addItemCriterion(Items.DIAMOND_PICKAXE)
        .addCondition(new EnableCondition(BlockSolidQuarry.SYMBOL))

      val placer = AdvancementSerializeHelper(Holder.blockPlacer.getRegistryName, saveName = location("recipe/placer_plus_crafting"))
        .addItemCriterion(Items.MOSSY_COBBLESTONE)
        .addItemCriterion(Items.DISPENSER)
        .addCondition(new NotCondition(new EnableCondition(TileWorkbench.SYMBOL)))
        .addCondition(new EnableCondition(PlacerTile.SYMBOL))

      workbench :: solidQuarry :: placer :: Nil
    }
  }

  class BlockDrop(g: DataGenerator) extends QuarryPlusDataProvider.DataProvider(g) {
    override def directory: String = "loot_tables/blocks"

    override def data: Seq[DataBuilder] = {
      import com.yogpc.qp.utils.Holder
      val notMachines = Set(
        Holder.blockMover,
        Holder.blockBookMover,
        Holder.blockMarker,
        Holder.blockReplacer,
        Holder.blockSolidQuarry,
        Holder.blockController,
        Holder.blockWorkbench,
        Holder.blockPlacer,
      ).map(LootTableSerializeHelper.withDrop)
      val enchantedMachines = Set(
        Holder.blockAdvQuarry,
        Holder.blockExpPump,
        Holder.blockMiningWell,
        Holder.blockPump,
        Holder.blockQuarry,
        Holder.blockQuarry2,
        Holder.blockAdvPump,
      ).map(LootTableSerializeHelper.withEnchantedDrop)
      (notMachines ++ enchantedMachines).toSeq
    }
  }

  class BlackList(g: DataGenerator) extends QuarryPlusDataProvider.DataProvider(g) {
    override def directory: String = QuarryPlus.modID + "/blacklist"

    override def data: Seq[DataBuilder] = Seq(
      new DataBuilder {
        override def location = QuarryPlusDataProvider.location("blacklist")

        override def build: JsonElement = QuarryBlackList.GSON.toJsonTree(QuarryBlackList.example1)
      },
      new DataBuilder {
        override def location = QuarryPlusDataProvider.location("blacklist2")

        override def build: JsonElement = QuarryBlackList.GSON.toJsonTree(QuarryBlackList.example2)
      }
    )
  }

}
