package com.yogpc.qp

import java.io.File

import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.{ConfigElement, Configuration}
import net.minecraftforge.fml.client.config.IConfigElement
import net.minecraftforge.fml.client.event.ConfigChangedEvent
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Config {

    private[this] var mContent: Content = _
    private[this] var configuration: Configuration = _

    @SubscribeEvent
    def onChange(configChangedEvent: ConfigChangedEvent): Unit = {
        if (configChangedEvent.getModID == QuarryPlus.modID) {
            QuarryPlus.LOGGER.info("QuarryPlus Config loaded!")
            sync()
        }
    }

    def sync(): Unit = {
        mContent = new Content
        QuarryPlusI.blockBreaker.setTickRandomly(!mContent.placerOnlyPlaceFront)
        QuarryPlusI.blockPlacer.setTickRandomly(!mContent.placerOnlyPlaceFront)
    }

    def setConfigFile(file: File): Unit = {
        this.configuration = new Configuration(file)
        MinecraftForge.EVENT_BUS.register(this)
        sync()
    }

    def getElements: java.util.List[IConfigElement] = {
        val list = new java.util.LinkedList[IConfigElement]()
        list.addAll(new ConfigElement(configuration.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements)
        list
    }

    def content = mContent

    private val Disables = Set(
        'SpawnerController,
        'ChunkDestroyer,
        'AdvancedPump,
        'BreakerPlus,
        'MiningwellPlus,
        'MagicMirror,
        'EnchantMover,
        'PlacerPlus,
        'PumpPlus,
        'MarkerPlus,
        'QuarryPlus,
        'WorkbenchPlus)
    private val DisableBC = Set(
        'LaserPlus,
        'RefineryPlus
    )

    val DisableFrameChainBreak_key = "DisableFrameChainBreak"
    val DisableRendering_Key = "DisableRendering"
    val EnableChunkDestroyerFluidHander_key = "EnableChunkDestroyerFluidHandler"
    val SpawnerControllerEntityBlackList_key = "SpawnerControllerEntityBlackList"
    val RecipeDifficulty_key = "RecipeDifficulty"
    val Recipe_key = "NewRecipeDifficulty"
    val PlacerOnlyPlaceFront_key = "PlacerOnlyPlaceFront"
    val NoEnergy_key = "NoEnergy"
    val RemoveBedrock_Key = "RemoveBedrock"
    val RemoveOnlySource_Key = "RemoveOnlySource"
    val PumpAutoStart_Key = "PumpAutoStart"
    val WorkbenchplusReceive = "WorkbenchplusReceive="
    val DEBUG_key = "DEBUG"

    class Content {

        import scala.collection.JavaConverters._

        val enableMap = (Disables.map(s => {
            val key = "Disable" + s.name
            (s, !configuration.get(Configuration.CATEGORY_GENERAL, key, false).setRequiresMcRestart(true).setShowInGui(false).getBoolean)
        }) ++ DisableBC.map(s => {
            val key = "Disable" + s.name
            (s, !configuration.get(Configuration.CATEGORY_GENERAL, key, false).setRequiresMcRestart(true).setShowInGui(false).getBoolean &&
              Loader.isModLoaded(QuarryPlus.Optionals.Buildcraft_modID))
        })).toMap
        val disableMapJ = enableMap.map { case (s, b) => (s, Boolean.box(!b)) }.asJava

        val spawnerBlacklist = configuration.get(Configuration.CATEGORY_GENERAL, SpawnerControllerEntityBlackList_key, Array("minecraft:ender_dragon", "minecraft:wither"), "Spawner Blacklist")
          .getStringList.map(new ResourceLocation(_)).toSet.asJava
        val recipeDifficulty = configuration.get(Configuration.CATEGORY_GENERAL, RecipeDifficulty_key, 2d)
        recipeDifficulty.setComment("!!!UNUSED!!! //Default is 2.0")
        recipeDifficulty.setMinValue(1d)
        val recipe = configuration.getInt(Recipe_key, Configuration.CATEGORY_GENERAL, recipeDifficulty.getDouble.toInt, 1, Short.MaxValue, Recipe_key)

        val placerOnlyPlaceFront = configuration.getBoolean(PlacerOnlyPlaceFront_key, Configuration.CATEGORY_GENERAL, true, PlacerOnlyPlaceFront_key)
        val noEnergy = configuration.getBoolean(NoEnergy_key, Configuration.CATEGORY_GENERAL, false, NoEnergy_key)
        PowerManager.loadConfiguration(configuration)

        val removeBedrock = configuration.getBoolean(RemoveBedrock_Key, Configuration.CATEGORY_GENERAL, false, RemoveBedrock_Key)
        val removeOnlySource = configuration.getBoolean(RemoveOnlySource_Key, Configuration.CATEGORY_GENERAL, false, RemoveOnlySource_Key)
        val enableChunkDestroyerFluidHander = configuration.getBoolean(EnableChunkDestroyerFluidHander_key, Configuration.CATEGORY_GENERAL, true, EnableChunkDestroyerFluidHander_key)
        val disableFrameChainBreak = configuration.getBoolean(DisableFrameChainBreak_key, Configuration.CATEGORY_GENERAL, false, DisableFrameChainBreak_key)
        val pumpAutoStart = configuration.getBoolean(PumpAutoStart_Key, Configuration.CATEGORY_GENERAL, false, PumpAutoStart_Key)
        val DisableRendering = configuration.get(Configuration.CATEGORY_CLIENT, DisableRendering_Key, false, "Disable rendering of quarries.")
          .setRequiresMcRestart(true).setShowInGui(false).getBoolean
        val workbenchMaxReceive = configuration.getInt(WorkbenchplusReceive, Configuration.CATEGORY_GENERAL, 250, 1, Int.MaxValue,
            "Amount of enegy WorkbenchPlus can accept in a tick. Unit is MJ and 1 MJ = 10 RF = 10 FE.")
        val debug = configuration.getBoolean(DEBUG_key, Configuration.CATEGORY_GENERAL, false, DEBUG_key)

        if (configuration.hasChanged)
            configuration.save()
    }

}
