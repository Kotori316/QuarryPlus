package com.yogpc.qp

import java.io.File
import java.nio.file.{Files, Path}
import java.{lang, util}

import com.yogpc.qp.tile.{TileAdvQuarry, WorkbenchRecipes}
import com.yogpc.qp.utils.{BlockWrapper, ReflectionHelper}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.{ConfigElement, Configuration}
import net.minecraftforge.fml.client.config.{DummyConfigElement, IConfigElement}
import net.minecraftforge.fml.client.event.ConfigChangedEvent
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

import scala.util.Try
import scala.util.control.NonFatal

object Config {
  final val NO_DIG_BLOCK_PATH = "quarryplus_noDigBlocks.json"
  final val RECIPE_DIRECTORY = "recipes"

  private[this] var mContent: Content = _
  private[this] var configuration: Configuration = _

  @SubscribeEvent
  def onChange(configChangedEvent: ConfigChangedEvent.OnConfigChangedEvent): Unit = {
    if (configChangedEvent.getModID == QuarryPlus.modID) {
      QuarryPlus.LOGGER.info("QuarryPlus Config loaded!")
      if (!content.manuallyDefinedHidden)
        configuration.removeCategory(configuration.getCategory(CATEGORY_HIDDEN))
      sync()
      recipeSync()
      // Moved here to avoid reloading in preInit.
      QuarryPlus.proxy.setDummyTexture(mContent.dummyBlockTextureName)
    }
  }

  def sync(): Unit = {
    mContent = new Content
    QuarryPlusI.blockBreaker.setTickRandomly(!mContent.placerOnlyPlaceFront)
    QuarryPlusI.blockPlacer.setTickRandomly(!mContent.placerOnlyPlaceFront)
  }

  def recipeSync(): Unit = {
    val recipePath = configuration.getConfigFile.toPath.getParent.resolve(RECIPE_DIRECTORY)
    if (Files.notExists(recipePath)) Files.createDirectory(recipePath)
    WorkbenchRecipes.registerJsonRecipe(ReflectionHelper.paths(recipePath))
  }

  def setConfigFile(pre: File, file: File): Unit = {
    if (Files.exists(pre.toPath)) {
      if (Files.notExists(file.toPath)) {
        if (Files.notExists(file.toPath.getParent)) {
          Files.createDirectories(file.toPath.getParent)
        }
        Files.move(pre.toPath, file.toPath)
      }
      val path: Path = pre.toPath.getParent.resolve(NO_DIG_BLOCK_PATH)
      if (Files.exists(path)) {
        val path2 = file.toPath.getParent.resolve(NO_DIG_BLOCK_PATH)
        if (Files.notExists(path2)) {
          Files.move(path, path2)
        }
        Files.deleteIfExists(path)
      }
      Files.deleteIfExists(pre.toPath)
    }

    this.configuration = new Configuration(file)
    MinecraftForge.EVENT_BUS.register(this)
    sync()
  }

  def getElements: java.util.List[IConfigElement] = {
    val list = new java.util.LinkedList[IConfigElement]()
    list.addAll(new ConfigElement(configuration.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements)
    list.add(new DummyConfigElement.DummyCategoryElement("Machines", QuarryPlus.modID + "." + CATEGORY_MACHINES,
      new ConfigElement(configuration.getCategory(CATEGORY_MACHINES)).getChildElements))
    list.add(new DummyConfigElement.DummyCategoryElement("Client", QuarryPlus.modID + "." + Configuration.CATEGORY_CLIENT,
      new ConfigElement(configuration.getCategory(Configuration.CATEGORY_CLIENT)).getChildElements))
    list
  }

  def content: Content = mContent

  private val Disables = Set(
    'SpawnerController,
    'ChunkDestroyer,
    'AdvancedPump,
    'BreakerPlus,
    'MiningwellPlus,
    'MagicMirror,
    'EnchantMover,
    'EnchantMoverFromBook,
    'PlacerPlus,
    'PumpPlus,
    'ExpPump,
    'MarkerPlus,
    'QuarryPlus,
    'WorkbenchPlus,
    'SolidFuleQuarry,
    'Replacer)
  private val DisableBC = Set(
    'LaserPlus,
    'RefineryPlus
  )

  private val defaultDisables = Set('EnchantMoverFromBook, 'Replacer)

  final val CATEGORY_MACHINES = "machines"
  final val CATEGORY_HIDDEN = "hidden"
  final val CollectBedrock_key = "CollectBedrock"
  final val DisableFrameChainBreak_key = "DisableFrameChainBreak"
  final val DisableRendering_Key = "DisableRendering"
  final val DisableDungeonRoot_key = "DisableDungeonLoot"
  final val DummyBlockTextureName_key = "DummyBlockTextureName"
  final val EnableChunkDestroyerFluidHandler_key = "EnableChunkDestroyerFluidHandler"
  final val SpawnerControllerEntityBlackList_key = "SpawnerControllerEntityBlackList"
  final val RecipeDifficulty_key = "RecipeDifficulty"
  final val Recipe_key = "NewRecipeDifficulty"
  final val PlacerOnlyPlaceFront_key = "PlacerOnlyPlaceFront"
  final val NoEnergy_key = "NoEnergy"
  final val RemoveBedrock_Key = "RemoveBedrock"
  final val RemoveOnlySource_Key = "RemoveOnlySource"
  final val PumpAutoStart_Key = "PumpAutoStart"
  final val UseHardCodedRecipe_Key = "UseHardCodedRecipe"
  final val WorkbenchplusReceive = "WorkbenchplusReceive"
  final val DEBUG_key = "DEBUG"

  class Content {

    import scala.collection.JavaConverters._

    val enableMap: Map[Symbol, Boolean] = (Disables.map(s => {
      val key = "Disable" + s.name
      (s, !configuration.get(CATEGORY_MACHINES, key, defaultDisables.contains(s)).setRequiresMcRestart(true).getBoolean)
    }) ++ DisableBC.map(s => {
      val key = "Disable" + s.name
      (s, !configuration.get(CATEGORY_MACHINES, key, false).setRequiresMcRestart(true).getBoolean &&
        Loader.isModLoaded(QuarryPlus.Optionals.Buildcraft_modID))
    })).toMap
    val disableMapJ: util.Map[Symbol, lang.Boolean] = enableMap.map { case (s, b) => (s, Boolean.box(!b)) }.asJava

    val spawnerBlacklist: util.Set[ResourceLocation] = configuration.get(Configuration.CATEGORY_GENERAL, SpawnerControllerEntityBlackList_key, Array("minecraft:ender_dragon", "minecraft:wither"), "Spawner Blacklist")
      .getStringList.map(new ResourceLocation(_)).toSet.asJava
    configuration.getCategory(Configuration.CATEGORY_GENERAL).remove(RecipeDifficulty_key)
    val recipe: Int = configuration.getInt(Recipe_key, Configuration.CATEGORY_GENERAL, 2, 1, Short.MaxValue, Recipe_key)

    val placerOnlyPlaceFront: Boolean = configuration.getBoolean(PlacerOnlyPlaceFront_key, Configuration.CATEGORY_GENERAL, true, PlacerOnlyPlaceFront_key)
    val noEnergy: Boolean = configuration.getBoolean(NoEnergy_key, Configuration.CATEGORY_GENERAL, false, NoEnergy_key)
    PowerManager.loadConfiguration(configuration)

    val removeBedrock: Boolean = configuration.getBoolean(RemoveBedrock_Key, Configuration.CATEGORY_GENERAL, false, RemoveBedrock_Key)
    val removeOnlySource: Boolean = configuration.getBoolean(RemoveOnlySource_Key, Configuration.CATEGORY_GENERAL, false, RemoveOnlySource_Key)
    val enableChunkDestroyerFluidHandler: Boolean = configuration.getBoolean(EnableChunkDestroyerFluidHandler_key, Configuration.CATEGORY_GENERAL, true, EnableChunkDestroyerFluidHandler_key)
    val disableFrameChainBreak: Boolean = configuration.getBoolean(DisableFrameChainBreak_key, Configuration.CATEGORY_GENERAL, false, DisableFrameChainBreak_key)
    val pumpAutoStart: Boolean = configuration.getBoolean(PumpAutoStart_Key, Configuration.CATEGORY_GENERAL, false, PumpAutoStart_Key)
    val disableRendering: Boolean = configuration.get(Configuration.CATEGORY_CLIENT, DisableRendering_Key, false, "Disable rendering of quarries.")
      .setRequiresMcRestart(true).setShowInGui(false).getBoolean
    val workbenchMaxReceive: Int = configuration.getInt(WorkbenchplusReceive, Configuration.CATEGORY_GENERAL, 250, 1, Int.MaxValue,
      "Amount of energy WorkbenchPlus can accept in a tick. Unit is MJ and 1 MJ = 10 RF = 10 FE.")
    val disableDungeonLoot: Boolean = configuration.get(Configuration.CATEGORY_GENERAL, DisableDungeonRoot_key, false, "Disable adding magic mirror to loot.")
      .setRequiresMcRestart(true).getBoolean
    val debug: Boolean = configuration.getBoolean(DEBUG_key, Configuration.CATEGORY_GENERAL, false, DEBUG_key)
    val dummyBlockTextureName: String = configuration.getString(DummyBlockTextureName_key, Configuration.CATEGORY_CLIENT, "minecraft:glass",
      "The name of block whose texture is used for dummy block placed by Replacer.")
    val useHardCodedRecipe=configuration.getBoolean(UseHardCodedRecipe_Key,Configuration.CATEGORY_GENERAL, true,
      "False to disable default workbench recipe. You can add recipe with json file.")

    (Disables ++ DisableBC).map("Disable" + _.name).foreach(s => configuration.getCategory(Configuration.CATEGORY_GENERAL).remove(s))

    if (configuration.hasChanged)
      configuration.save()

    val manuallyDefinedHidden: Boolean = configuration.hasCategory(CATEGORY_HIDDEN)
    val collectBedrock: Boolean = configuration.get(CATEGORY_HIDDEN, CollectBedrock_key, false, CollectBedrock_key).setShowInGui(false).getBoolean

    import scala.collection.JavaConverters._

    private val path: Path = configuration.getConfigFile.toPath.getParent.resolve(NO_DIG_BLOCK_PATH)
    val noDigBLOCKS: Set[BlockWrapper] = if (Files.exists(path)) {
      val str = Files.readAllLines(path).asScala.reduce(_ + _)
      Try(BlockWrapper.getWrapper(str)).recover { case NonFatal(e) => e.printStackTrace(); TileAdvQuarry.noDigBLOCKS }.getOrElse(TileAdvQuarry.noDigBLOCKS)
    } else {
      Files.write(path, BlockWrapper.getString(TileAdvQuarry.noDigBLOCKS.toSeq).split(System.lineSeparator()).toSeq.asJava)
      TileAdvQuarry.noDigBLOCKS
    }

    def outputRecipeJson(): Unit = {
      val recipePath = configuration.getConfigFile.toPath.getParent.resolve("default" + RECIPE_DIRECTORY)
      if (Files.notExists(recipePath)) {
        Files.createDirectory(recipePath)
      }
      WorkbenchRecipes.outputDefaultRecipe(recipePath)
    }
  }

}
