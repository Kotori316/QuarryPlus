package com.yogpc.qp

import net.minecraftforge.common.ForgeConfigSpec

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
    private[this] val inDev = Option(System.getenv("target")).exists(_.contains("dev"))

    builder.comment("Beginning of QuarryPlus configuration.").push("common")
    //--------- Registering config entries. ---------
    private[this] val configDebug = builder.comment("True to enable debug mode.").define("debug", inDev)
    val noEnergy = builder.comment("True to make machines work with no energy.").define("NoEnergy", false)
    val workbenchMaxReceive = builder.comment("Amount of energy WorkbenchPlus can receive in a tick. [MJ]")
      .defineInRange("WorkbenchMaxReceive", 250, 0, Int.MaxValue)
    val fastQuarryHeadMove = builder.comment("Fasten quarry's head moving.").define("FastQuarryHeadMove", false)

    val powers = powerConfig(builder)
    builder.pop()

    def debug = configDebug.get() || inDev

    private def powerConfig(builder: ForgeConfigSpec.Builder): Map[String, ForgeConfigSpec.DoubleValue] = {
      def get(path: List[String], name: String, defaultValue: Double): (String, ForgeConfigSpec.DoubleValue) = {
        import scala.collection.JavaConverters._
        val strings = path :+ name
        strings.mkString(".") -> builder.defineInRange(strings.asJava, defaultValue, 0d, 2e9)
      }

      val pf = (p: List[String]) => (s: (String, Double)) => {
        get(p, s._1, s._2)
      }
      builder.comment("Quarry PowerSetting [Mj] (min = 0, Max = 2,000,000,000 = 2 billion)").push("PowerSetting")
      val quarry_break = Seq(
        ("BasePower", 40d),
        ("EfficiencyCoefficient", 1.3d),
        ("UnbreakingCoefficient", 1d),
        ("FortuneCoefficient", 1.3d),
        ("SilktouchCoefficient", 2d),
        ("BaseMaxReceive", 300d),
        ("BaseMaxStored", 15000d),
      ).map(pf("Quarry" :: "BreakBlock" :: Nil))
      val quarry_break_head = Seq(
        ("BasePower", 200d),
        ("UnbreakingCoefficient", 1d),
      ).map(pf("Quarry" :: "BreakBlock" :: "MoveHead" :: Nil))
      val quarry_makeFrame = Seq(
        ("BasePower", 25d),
        ("EfficiencyCoefficient", 1.3d),
        ("UnbreakingCoefficient", 1d),
        ("BaseMaxReceive", 100d),
        ("BaseMaxStored", 15000d),
      ).map(pf("Quarry" :: "MakeFrame" :: Nil))
      val pump_drain = Seq(
        ("BasePower", 10d),
        ("UnbreakingCoefficient", 1d),
      ).map(pf("Pump" :: "DrainLiquid" :: Nil))
      val pump_frame = Seq(
        ("BasePower", 25d),
        ("UnbreakingCoefficient", 1d),
      ).map(pf("Pump" :: "MakeFrame" :: Nil))
      val mining = Seq(
        ("BasePower", 40d),
        ("EfficiencyCoefficient", 1.3d),
        ("UnbreakingCoefficient", 1d),
        ("FortuneCoefficient", 1.3d),
        ("SilktouchCoefficient", 2d),
        ("BaseMaxReceive", 100d),
        ("BaseMaxStored", 1000d),
      ).map(pf("MiningWell" :: Nil))
      val laser = Seq(
        ("BasePower", 4d),
        ("EfficiencyCoefficient", 2d),
        ("UnbreakingCoefficient", 0.1d),
        ("FortuneCoefficient", 1.05d),
        ("SilktouchCoefficient", 1.1d),
        ("BaseMaxReceive", 100d),
        ("BaseMaxStored", 1000d),
      ).map(pf("Laser" :: Nil))
      val refinery = Seq(
        ("EfficiencyCoefficient", 1.2d),
        ("UnbreakingCoefficient", 1d),
        ("BaseMaxReceive", 6d),
        ("BaseMaxStored", 1000d),
      ).map(pf("Refinery" :: Nil))
      builder.pop()

      (quarry_break ++ quarry_break_head ++ quarry_makeFrame ++
        pump_drain ++ pump_frame ++
        mining ++ laser ++ refinery
        ).toMap
    }
  }

  class ClientContent(builder: ForgeConfigSpec.Builder) {
    builder.comment("Beginning of QuarryPlus client configuration.").push("client")


    //--------- Registering config entries. ---------
    val enableRender = builder.comment("True to enable render of machine effect.").define("enableRender", true)

    builder.pop()
  }

}
