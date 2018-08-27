package com.yogpc.qp

import java.io.File

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.{ConfigElement, Configuration}
import net.minecraftforge.fml.client.config.{DummyConfigElement, IConfigElement}
import net.minecraftforge.fml.client.event.ConfigChangedEvent
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Config {

    private[this] var mContent: Content = _
    private[this] var configuration: Configuration = _

    @SubscribeEvent
    def onChange(configChangedEvent: ConfigChangedEvent.OnConfigChangedEvent): Unit = {
        if (configChangedEvent.getModID == QuarryPlus.modID) {
            QuarryPlus.LOGGER.info("QuarryPlus Config loaded!")
            if (!content.munuallyDefinedHidden)
                configuration.removeCategory(configuration.getCategory(CATEGORY_HIDDEN))
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
        list.add(new DummyConfigElement.DummyCategoryElement("Machines", s"${QuarryPlus.modID}.$CATEGORY_MACHINES",
            new ConfigElement(configuration.getCategory(CATEGORY_MACHINES)).getChildElements))
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
        'EnchantMoverFromBook,
        'PlacerPlus,
        'PumpPlus,
        'ExpPump,
        'MarkerPlus,
        'QuarryPlus,
        'WorkbenchPlus)
    private val DisableBC = Set(
        'LaserPlus,
        'RefineryPlus
    )

    private val defaultDisables = Set('EnchantMoverFromBook)

    final val CATEGORY_MACHINES = "machines"
    final val CATEGORY_HIDDEN = "hidden"
    final val CollectBedrock_key = "CollectBedrock"
    final val DisableFrameChainBreak_key = "DisableFrameChainBreak"
    final val DisableRendering_Key = "DisableRendering"
    final val DisableDungeonRoot_key = "DisableDungeonLoot"
    final val EnableChunkDestroyerFluidHander_key = "EnableChunkDestroyerFluidHandler"
    final val SpawnerControllerEntityBlackList_key = "SpawnerControllerEntityBlackList"
    final val RecipeDifficulty_key = "RecipeDifficulty"
    final val Recipe_key = "NewRecipeDifficulty"
    final val PlacerOnlyPlaceFront_key = "PlacerOnlyPlaceFront"
    final val NoEnergy_key = "NoEnergy"
    final val RemoveBedrock_Key = "RemoveBedrock"
    final val RemoveOnlySource_Key = "RemoveOnlySource"
    final val PumpAutoStart_Key = "PumpAutoStart"
    final val WorkbenchplusReceive = "WorkbenchplusReceive"
    final val DEBUG_key = "DEBUG"

    class Content {

        import scala.collection.JavaConverters._

        val enableMap = (Disables.map(s => {
            val key = "Disable" + s.name
            (s, !configuration.get(CATEGORY_MACHINES, key, defaultDisables.contains(s)).setRequiresMcRestart(true).getBoolean)
        }) ++ DisableBC.map(s => {
            val key = "Disable" + s.name
            (s, !configuration.get(CATEGORY_MACHINES, key, false).setRequiresMcRestart(true).getBoolean &&
              Loader.isModLoaded(QuarryPlus.Optionals.Buildcraft_modID))
        })).toMap
        val disableMapJ = enableMap.map { case (s, b) => (s, Boolean.box(!b)) }.asJava
        val spawnerBlacklist = configuration.get(Configuration.CATEGORY_GENERAL, SpawnerControllerEntityBlackList_key, Array("EnderDragon", "WitherBoss"))
          .getStringList /*.map(new ResourceLocation(_))*/ .toSet.asJava
        val recipeDifficulty = configuration.get(Configuration.CATEGORY_GENERAL, RecipeDifficulty_key, 2d)
        recipeDifficulty.setComment("!!!UNUSED!!! //Default is 2.0")
        recipeDifficulty.setMinValue(1d)
        val recipe = configuration.getInt(Recipe_key, Configuration.CATEGORY_GENERAL, recipeDifficulty.getDouble.toInt, 1, Short.MaxValue, Recipe_key)

        val placerOnlyPlaceFront = configuration.getBoolean(PlacerOnlyPlaceFront_key, Configuration.CATEGORY_GENERAL, true, PlacerOnlyPlaceFront_key)
        val noEnergy = configuration.getBoolean(NoEnergy_key, Configuration.CATEGORY_GENERAL, false, NoEnergy_key)
        PowerManager.loadConfiguration(configuration)

        val removeBedrock = configuration.getBoolean(RemoveBedrock_Key, Configuration.CATEGORY_GENERAL, false, RemoveBedrock_Key)
        val removeOnlySource = configuration.getBoolean(RemoveOnlySource_Key, Configuration.CATEGORY_GENERAL, false, RemoveOnlySource_Key)
        val enableChunkDestroyerFluidHander = configuration.getBoolean(EnableChunkDestroyerFluidHander_key, Configuration.CATEGORY_GENERAL, false, EnableChunkDestroyerFluidHander_key)
        val disableFrameChainBreak = configuration.getBoolean(DisableFrameChainBreak_key, Configuration.CATEGORY_GENERAL, false, DisableFrameChainBreak_key)
        val pumpAutoStart = configuration.getBoolean(PumpAutoStart_Key, Configuration.CATEGORY_GENERAL, false, PumpAutoStart_Key)
        val DisableRendering = configuration.get(Configuration.CATEGORY_CLIENT, DisableRendering_Key, false, "Disable rendering of quarries.")
          .setRequiresMcRestart(true).setShowInGui(false).getBoolean
        val workbenchMaxReceive = configuration.getInt(WorkbenchplusReceive, Configuration.CATEGORY_GENERAL, 250, 1, Int.MaxValue,
            "Amount of enegy WorkbenchPlus can accept in a tick. Unit is MJ and 1 MJ = 10 RF = 10 FE.")
        val disableDungeonLoot = configuration.get(Configuration.CATEGORY_GENERAL, DisableDungeonRoot_key, false, "Disable adding magic mirror to loot.")
          .setRequiresMcRestart(true).getBoolean
        val debug = configuration.getBoolean(DEBUG_key, Configuration.CATEGORY_GENERAL, false, DEBUG_key)

        if (configuration.hasChanged)
            configuration.save()

        val munuallyDefinedHidden = configuration.hasCategory(CATEGORY_HIDDEN)
        val collectBedrock = configuration.get(CATEGORY_HIDDEN, CollectBedrock_key, false, CollectBedrock_key).setShowInGui(false).getBoolean
    }

}
