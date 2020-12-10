package com.yogpc.qp.render

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import com.yogpc.qp.machines.mini_quarry.MiniQuarryTile
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.tileentity.{TileEntityRenderer, TileEntityRendererDispatcher}
import net.minecraft.client.renderer.{IRenderTypeBuffer, RenderType}

object RenderMiniQuarry extends TileEntityRenderer[MiniQuarryTile](TileEntityRendererDispatcher.instance) {
  private[this] final val d = 1d / 16d
  val instance: RenderMiniQuarry.type = this
  lazy val sprite: TextureAtlasSprite = Sprites.getMap(Symbol("white"))

  override def isGlobalRenderer(te: MiniQuarryTile): Boolean = true

  override def render(tileEntityIn: MiniQuarryTile, partialTicks: Float, matrixStackIn: MatrixStack, bufferIn: IRenderTypeBuffer, combinedLightIn: Int, combinedOverlayIn: Int): Unit = {
    Minecraft.getInstance.getProfiler.startSection("quarryplus")
    if (tileEntityIn.renderAreaBox) {
      Minecraft.getInstance.getProfiler.startSection("mini_quarry")
      matrixStackIn.push()
      val pos = tileEntityIn.getPos
      matrixStackIn.translate(-pos.getX, -pos.getY, -pos.getZ) // Offset
      val a = tileEntityIn.getArea
      val buffer = bufferIn.getBuffer(RenderType.getTranslucent)
      val xMin = a.xMin - d
      val zMin = a.zMin - d
      val xMax = a.xMax + 1 + d
      val zMax = a.zMax + 1 + d
      Box(
        (xMin + xMax) / 2, a.yMin - d, (zMin + zMax) / 2,
        (xMin + xMax) / 2, a.yMax + 1 + d, (zMin + zMax) / 2,
        xMax - xMin, 1, zMax - zMin, firstSide = true, endSide = true
      ).render(buffer, matrixStackIn, sprite, alpha = 128)

      drawOutline(matrixStackIn, buffer, xMin, a.yMin, zMin, xMax, a.yMax, zMax)

      matrixStackIn.pop()
      Minecraft.getInstance.getProfiler.endSection()
    }
    Minecraft.getInstance.getProfiler.endSection()
  }

  //noinspection DuplicatedCode
  private def drawOutline(matrixStackIn: MatrixStack, buffer: IVertexBuilder, xMin: Double, yMin: Double, zMin: Double, xMax: Double, yMax: Double, zMax: Double): Unit = {
    // Outline
    // x
    val blue = 64
    Box(xMin, yMax + 1 + d, zMin, xMin, yMax + 1 + d, zMax, d, d, zMax - zMin, firstSide = false, endSide = false).render(buffer, matrixStackIn, sprite,
      alpha = 192, red = 0, green = 0, blue = blue)
    Box(xMin, yMin - d, zMin, xMin, yMin - d, zMax, d, d, zMax - zMin, firstSide = false, endSide = false).render(buffer, matrixStackIn, sprite,
      alpha = 192, red = 0, green = 0, blue = blue)
    Box(xMax, yMax + 1 + d, zMin, xMax, yMax + 1 + d, zMax, d, d, zMax - zMin, firstSide = false, endSide = false).render(buffer, matrixStackIn, sprite,
      alpha = 192, red = 0, green = 0, blue = blue)
    Box(xMax, yMin - d, zMin, xMax, yMin - d, zMax, d, d, zMax - zMin, firstSide = false, endSide = false).render(buffer, matrixStackIn, sprite,
      alpha = 192, red = 0, green = 0, blue = blue)
    // y
    Box(xMin, yMin - d, zMin, xMin, yMax + 1 + d, zMin, d, yMax - yMin + 1d / 8, d, firstSide = false, endSide = false).render(buffer, matrixStackIn, sprite,
      alpha = 192, red = 0, green = 0, blue = blue)
    Box(xMin, yMin - d, zMax, xMin, yMax + 1 + d, zMax, d, yMax - yMin + 1d / 8, d, firstSide = false, endSide = false).render(buffer, matrixStackIn, sprite,
      alpha = 192, red = 0, green = 0, blue = blue)
    Box(xMax, yMin - d, zMin, xMax, yMax + 1 + d, zMin, d, yMax - yMin + 1d / 8, d, firstSide = false, endSide = false).render(buffer, matrixStackIn, sprite,
      alpha = 192, red = 0, green = 0, blue = blue)
    Box(xMax, yMin - d, zMax, xMax, yMax + 1 + d, zMax, d, yMax - yMin + 1d / 8, d, firstSide = false, endSide = false).render(buffer, matrixStackIn, sprite,
      alpha = 192, red = 0, green = 0, blue = blue)
    // z
    Box(xMin, yMax + 1 + d, zMin, xMax, yMax + 1 + d, zMin, xMax - xMin, d, d, firstSide = false, endSide = false).render(buffer, matrixStackIn, sprite,
      alpha = 192, red = 0, green = 0, blue = blue)
    Box(xMin, yMin - d, zMin, xMax, yMin - d, zMin, xMax - xMin, d, d, firstSide = false, endSide = false).render(buffer, matrixStackIn, sprite,
      alpha = 192, red = 0, green = 0, blue = blue)
    Box(xMin, yMax + 1 + d, zMax, xMax, yMax + 1 + d, zMax, xMax - xMin, d, d, firstSide = false, endSide = false).render(buffer, matrixStackIn, sprite,
      alpha = 192, red = 0, green = 0, blue = blue)
    Box(xMin, yMin - d, zMax, xMax, yMin - d, zMax, xMax - xMin, d, d, firstSide = false, endSide = false).render(buffer, matrixStackIn, sprite,
      alpha = 192, red = 0, green = 0, blue = blue)
  }
}
