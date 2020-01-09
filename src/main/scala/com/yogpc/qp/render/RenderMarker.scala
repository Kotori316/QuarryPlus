package com.yogpc.qp.render

import com.mojang.blaze3d.matrix.MatrixStack
import com.yogpc.qp.Config
import com.yogpc.qp.machines.marker.TileMarker
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.{TileEntityRenderer, TileEntityRendererDispatcher}
import net.minecraft.client.renderer.{IRenderTypeBuffer, RenderType}

object RenderMarker extends TileEntityRenderer[TileMarker](TileEntityRendererDispatcher.instance) {

  val instance = this
  val d = 1d / 16d
  lazy val sprite_B = Sprites.getMap(LaserType.BLUE_LASER.symbol)
  lazy val sprite_R = Sprites.getMap(LaserType.RED_LASER.symbol)

  override def isGlobalRenderer(te: TileMarker): Boolean = true


  override def func_225616_a_(te: TileMarker, v: Float, matrixStack: MatrixStack, iRenderTypeBuffer: IRenderTypeBuffer, i: Int, i1: Int): Unit = {
    if (Config.client.enableRender.get()) {
      Minecraft.getInstance.getProfiler.startSection("quarryplus")
      Minecraft.getInstance.getProfiler.startSection("marker")

      val pos = te.getPos
      val buffer = iRenderTypeBuffer.getBuffer(RenderType.func_228643_e_())
      matrixStack.func_227860_a_()
      matrixStack.func_227861_a_(-pos.getX, -pos.getY, -pos.getZ)
      if (te.laser != null) {
        Minecraft.getInstance.getProfiler.startSection("laser")
        if (te.laser.boxes != null) {
          te.laser.boxes.foreach(_.render(buffer, matrixStack, sprite_B))
        }
        Minecraft.getInstance.getProfiler.endSection()
      }
      if (te.link != null) {
        Minecraft.getInstance.getProfiler.startSection("link")
        if (te.link.boxes != null) {
          te.link.boxes.foreach(_.render(buffer, matrixStack, sprite_R))
        }
        Minecraft.getInstance.getProfiler.endSection()
      }
      matrixStack.func_227865_b_()
      Minecraft.getInstance.getProfiler.endSection()
      Minecraft.getInstance.getProfiler.endSection()
    }
  }

}
