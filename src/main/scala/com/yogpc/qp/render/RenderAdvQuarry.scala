package com.yogpc.qp.render

import com.yogpc.qp.machines.advquarry.AdvQuarryWork.MakeFrame
import com.yogpc.qp.machines.advquarry.{AdvQuarryWork, TileAdvQuarry}
import com.yogpc.qp.machines.base.Area
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraftforge.client.model.animation.TileEntityRendererFast

object RenderAdvQuarry extends TileEntityRendererFast[TileAdvQuarry] {
  private[this] final val d = 1d / 16d
  val instance: RenderAdvQuarry.type = this
  lazy val sprite: TextureAtlasSprite = Sprites.getMap(Symbol("yellow"))

  override def renderTileEntityFast(te: TileAdvQuarry, x: Double, y: Double, z: Double,
                                    partialTicks: Float, destroyStage: Int, buffer: BufferBuilder): Unit = {

    Minecraft.getInstance.getProfiler.startSection("quarryplus")

    if (te.action.isInstanceOf[MakeFrame] || (te.action == AdvQuarryWork.waiting)) {
      Minecraft.getInstance.getProfiler.startSection("chunkdestroyer")
      val range = te.area
      if (range != Area.zeroArea) {
        val pos = te.getPos
        val playerX = pos.getX - x
        val playerY = pos.getY - y
        val playerZ = pos.getZ - z
        buffer.setTranslation(-playerX, -playerY, -playerZ)
        val b1 = (playerZ - range.zMin - 0.5).abs < 256
        val b2 = (playerZ - range.zMax + 1.5).abs < 256
        val b3 = (playerX - range.xMin - 0.5).abs < 256
        val b4 = (playerX - range.xMax + 1.5).abs < 256
        val xMin = math.max(range.xMin - 0.5, playerX - 128)
        val xMax = math.min(range.xMax + 1.5, playerX + 128)
        val zMin = math.max(range.zMin - 0.5, playerZ - 128)
        val zMax = math.min(range.zMax + 1.5, playerZ + 128)
        if (b1) Box(xMin, range.yMin, range.zMin - 0.5, xMax, range.yMax, range.zMin - 0.5, d, d, d, firstSide = false, endSide = false).render(buffer, sprite)
        if (b2) Box(xMin, range.yMin, range.zMax + 1.5, xMax, range.yMax, range.zMax + 1.5, d, d, d, firstSide = false, endSide = false).render(buffer, sprite)
        if (b3) Box(range.xMin - 0.5, range.yMin, zMin, range.xMin - 0.5, range.yMax, zMax, d, d, d, firstSide = false, endSide = false).render(buffer, sprite)
        if (b4) Box(range.xMax + 1.5, range.yMin, zMin, range.xMax + 1.5, range.yMax, zMax, d, d, d, firstSide = false, endSide = false).render(buffer, sprite)
        buffer.setTranslation(0, 0, 0)
      }
      Minecraft.getInstance.getProfiler.endSection()
    }

    Minecraft.getInstance.getProfiler.endSection()
  }

  override def isGlobalRenderer(te: TileAdvQuarry): Boolean = true

}
