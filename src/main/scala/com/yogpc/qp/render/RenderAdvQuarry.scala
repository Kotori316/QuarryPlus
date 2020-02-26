package com.yogpc.qp.render

import com.mojang.blaze3d.matrix.MatrixStack
import com.yogpc.qp.machines.advquarry.AdvQuarryWork.MakeFrame
import com.yogpc.qp.machines.advquarry.{AdvQuarryWork, TileAdvQuarry}
import com.yogpc.qp.machines.base.Area
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.{TileEntityRenderer, TileEntityRendererDispatcher}
import net.minecraft.client.renderer.{IRenderTypeBuffer, RenderType}

object RenderAdvQuarry extends TileEntityRenderer[TileAdvQuarry](TileEntityRendererDispatcher.instance) {
  private[this] final val d = 1d / 16d
  val instance = this
  lazy val sprite = Sprites.getMap(Symbol("yellow"))

  override def render(te: TileAdvQuarry, v: Float, matrixStack: MatrixStack, iRenderTypeBuffer: IRenderTypeBuffer, i: Int, i1: Int): Unit = {

    Minecraft.getInstance.getProfiler.startSection("quarryplus")

    if (te.action.isInstanceOf[MakeFrame] || (te.action == AdvQuarryWork.waiting)) {
      Minecraft.getInstance.getProfiler.startSection("chunkdestroyer")
      val range = te.area
      if (range != Area.zeroArea) {
        val buffer = iRenderTypeBuffer.getBuffer(RenderType.getCutout)
        val pos = te.getPos
        val player = Minecraft.getInstance.player
        val playerX = if (player == null) pos.getX - 0 else player.getPosX //x
        val playerZ = if (player == null) pos.getZ - 0 else player.getPosZ //z
        matrixStack.push()
        matrixStack.translate(-pos.getX, -pos.getY, -pos.getZ) // Offset
        val b1 = (playerZ - range.zMin - 0.5).abs < 256
        val b2 = (playerZ - range.zMax + 1.5).abs < 256
        val b3 = (playerX - range.xMin - 0.5).abs < 256
        val b4 = (playerX - range.xMax + 1.5).abs < 256
        val xMin = math.max(range.xMin - 0.5, playerX - 128)
        val xMax = math.min(range.xMax + 1.5, playerX + 128)
        val zMin = math.max(range.zMin - 0.5, playerZ - 128)
        val zMax = math.min(range.zMax + 1.5, playerZ + 128)
        if (b1) Box(xMin, range.yMin, range.zMin - 0.5, xMax, range.yMax, range.zMin - 0.5, d, d, d, firstSide = false, endSide = false).render(buffer, matrixStack, sprite)
        if (b2) Box(xMin, range.yMin, range.zMax + 1.5, xMax, range.yMax, range.zMax + 1.5, d, d, d, firstSide = false, endSide = false).render(buffer, matrixStack, sprite)
        if (b3) Box(range.xMin - 0.5, range.yMin, zMin, range.xMin - 0.5, range.yMax, zMax, d, d, d, firstSide = false, endSide = false).render(buffer, matrixStack, sprite)
        if (b4) Box(range.xMax + 1.5, range.yMin, zMin, range.xMax + 1.5, range.yMax, zMax, d, d, d, firstSide = false, endSide = false).render(buffer, matrixStack, sprite)
        matrixStack.pop()
      }
      Minecraft.getInstance.getProfiler.endSection()
    }

    Minecraft.getInstance.getProfiler.endSection()
  }

  override def isGlobalRenderer(te: TileAdvQuarry): Boolean = true

}
