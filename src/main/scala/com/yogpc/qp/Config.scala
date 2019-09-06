package com.yogpc.qp

import java.nio.file.{Files, Path}

import com.yogpc.qp.machines.PowerManager
import com.yogpc.qp.machines.base.{APowerTile, EnchantmentHolder}
import com.yogpc.qp.utils.Holder
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.ForgeConfigSpec

import scala.collection.JavaConverters

object Config {

  private var mContent: Content = _
  private var mClientContent: ClientContent = _

  def common = mContent

  def client = mClientContent

  def commonBuild(builder: ForgeConfigSpec.Builder): ForgeConfigSpec.Builder = {
    mContent = new Content(builder)
    builder
  }

  def clientBuild(builder: ForgeConfigSpec.Builder): ForgeConfigSpec.Builder = {
    mClientContent = new ClientContent(builder)
    builder
  }

  class Content(builder: ForgeConfigSpec.Builder) {

    import scala.collection.JavaConverters._

    private[this] val inDev = Option(System.getenv("target")).exists(_.contains("dev"))

    builder.comment("Beginning of QuarryPlus configuration.").push("common")
    //--------- Registering config entries. ---------
    private[this] val configDebug = builder.comment("True to enable debug mode.").define("debug", inDev)
    val noEnergy = builder.comment("True to make machines work with no energy.").define("NoEnergy", inDev)
    val workbenchMaxReceive = builder.comment("Amount of energy WorkbenchPlus can receive in a tick. [MJ]")
      .defineInRange("WorkbenchMaxReceive", 250, 0, Int.MaxValue)
    val fastQuarryHeadMove = builder.comment("Fasten quarry's head moving.").define("FastQuarryHeadMove", false)
    val removeBedrock = builder.comment("True to allow machines to remove bedrock. (Just removing. Not collecting)").define("RemoveBedrock", false)
    val collectBedrock = builder.comment("True to enable ChunkDestroyer to collect bedrock as item.").define("CollectBedrock", false)
    val disableFrameChainBreak = builder.comment("DisableFrameChainBreak").define("DisableFrameChainBreak", false)
    val removeOnlySource = builder.comment("Set false to allow PlumPlus to remove non-source fluid block.").define("RemoveOnlyFluidSource", false)
    private[this] final val disabledEntities = Seq(
      "minecraft:ender_dragon",
      "minecraft:wither",
      "minecraft:area_effect_cloud",
      "minecraft:item",
      "minecraft:player",
    )
    private[this] val spawnerBlacklist_internal = builder.comment("Spawner Controller Blacklist")
      .defineList("spawnerBlacklist", disabledEntities.asJava, s => s.isInstanceOf[String])


    val powers = powerConfig(builder)
    val disabled = disableConfig(builder)
    builder.pop()

    def debug = inDev || configDebug.get()

    def spawnerBlacklist = spawnerBlacklist_internal.get().asScala.map(new ResourceLocation(_))

    private def powerConfig(builder: ForgeConfigSpec.Builder): Map[String, ForgeConfigSpec.DoubleValue] = {
      case class Category(path: List[String]) {
        def apply(values: (String, Double)*) = {
          values.map { case (name, defaultValue) =>
            val strings = path :+ name
            strings.mkString(".") -> builder.defineInRange(strings.asJava, defaultValue, 0d, 2e9)
          }.toMap
        }
      }
      builder.comment("Quarry PowerSetting [MJ] (min = 0, Max = 2,000,000,000 = 2 billion)").push("PowerSetting")
      val quarry_break =
        Category("Quarry" :: "BreakBlock" :: Nil)(
          ("BasePower", 40d),
          ("EfficiencyCoefficient", 1.3d),
          ("UnbreakingCoefficient", 1d),
          ("FortuneCoefficient", 1.3d),
          ("SilktouchCoefficient", 2d),
          ("BaseMaxReceive", 300d),
          ("BaseMaxStored", 15000d),
        )
      val quarry_break_head =
        Category("Quarry" :: "BreakBlock" :: "MoveHead" :: Nil)(
          ("BasePower", 200d),
          ("UnbreakingCoefficient", 1d),
        )
      val quarry_makeFrame =
        Category("Quarry" :: "MakeFrame" :: Nil)(
          ("BasePower", 25d),
          ("EfficiencyCoefficient", 1.3d),
          ("UnbreakingCoefficient", 1d),
          ("BaseMaxReceive", 100d),
          ("BaseMaxStored", 15000d),
        )
      val pump_drain =
        Category("Pump" :: "DrainLiquid" :: Nil)(
          ("BasePower", 10d),
          ("UnbreakingCoefficient", 1d),
        )
      val pump_frame =
        Category("Pump" :: "MakeFrame" :: Nil)(
          ("BasePower", 25d),
          ("UnbreakingCoefficient", 1d),
        )
      val mining =
        Category("MiningWell" :: Nil)(
          ("BasePower", 40d),
          ("EfficiencyCoefficient", 1.3d),
          ("UnbreakingCoefficient", 1d),
          ("FortuneCoefficient", 1.3d),
          ("SilktouchCoefficient", 2d),
          ("BaseMaxReceive", 100d),
          ("BaseMaxStored", 1000d),
        )
      val laser =
        Category("Laser" :: Nil)(
          ("BasePower", 4d),
          ("EfficiencyCoefficient", 2d),
          ("UnbreakingCoefficient", 0.1d),
          ("FortuneCoefficient", 1.05d),
          ("SilktouchCoefficient", 1.1d),
          ("BaseMaxReceive", 100d),
          ("BaseMaxStored", 1000d),
        )
      val refinery =
        Category("Refinery" :: Nil)(
          ("EfficiencyCoefficient", 1.2d),
          ("UnbreakingCoefficient", 1d),
          ("BaseMaxReceive", 6d),
          ("BaseMaxStored", 1000d),
        )
      builder.pop()

      quarry_break ++ quarry_break_head ++ quarry_makeFrame ++
        pump_drain ++ pump_frame ++
        mining ++ laser ++ refinery
    }

    private def disableConfig(builder: ForgeConfigSpec.Builder): Map[Symbol, ForgeConfigSpec.BooleanValue] = {
      builder.comment("Setting of enabling or disabling each machine. True to disable.").push("machines")
      val s = Holder.canDisablesSymbols.map { case (symbol, b) => symbol -> builder.define(symbol.name, !(!b || inDev)) }
      builder.pop()
      s.toMap
    }

    def outputPowerDetail(logDirectory: Path): Unit = {
      if (inDev && Files.exists(logDirectory)) {
        def getEnergyMap(ench: EnchantmentHolder): IndexedSeq[String] = {
          BigDecimal("0").to(BigDecimal("100"), BigDecimal("0.1"))
            .map(f => f -> PowerManager.calcEnergyBreak(f.floatValue(), ench))
            .map { case (decimal, l) => s"$decimal,${l.toDouble / APowerTile.MJToMicroMJ}" }
        }

        val seq = Seq(
          "quarryWithNoEnchantment" -> getEnergyMap(EnchantmentHolder.noEnch),
          "quarryWithFortune3" -> getEnergyMap(EnchantmentHolder(0, 0, 3, silktouch = false)),
          "quarryWithSilktouch" -> getEnergyMap(EnchantmentHolder(0, 0, 0, silktouch = true)),
          "quarryWithU3Fortune3" -> getEnergyMap(EnchantmentHolder(0, 3, 3, silktouch = false)),
          "quarryWithU3Silktouch" -> getEnergyMap(EnchantmentHolder(0, 3, 0, silktouch = true)),
        )
        seq.foreach { case (str, strings) => Files.write(logDirectory.resolve(str + ".csv"), JavaConverters.asJavaIterable(strings)) }
      }
    }
  }

  class ClientContent(builder: ForgeConfigSpec.Builder) {
    builder.comment("Beginning of QuarryPlus client configuration.").push("client")

    //--------- Registering config entries. ---------
    val enableRender = builder.comment("True to enable render of machine effect.").define("enableRender", true)
    val dummyTexture = builder.comment("Not used").define("dummyTexture", "minecraft:glass")

    builder.pop()
  }

}
