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
    builder.pop()

    def debug = configDebug.get() || inDev
  }

  class ClientContent(builder: ForgeConfigSpec.Builder) {
    builder.comment("Beginning of QuarryPlus client configuration.").push("client")
    //--------- Registering config entries. ---------
    val enableRender = builder.comment("True to enable render of machine effect.").define("enableRender", true)
    builder.pop()
  }

}
