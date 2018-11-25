package com.yogpc.qp.render

import com.yogpc.qp.tile.TileAdvQuarry
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BufferBuilder
import net.minecraftforge.client.model.animation.FastTESR

object RenderAdvQuarry extends FastTESR[TileAdvQuarry] {
  private[this] final val d = 1d / 16d
  val instance = this
  lazy val sprite = Sprites.getMap('yellow)

  override def renderTileEntityFast(te: TileAdvQuarry, x: Double, y: Double, z: Double,
                                    partialTicks: Float, destroyStage: Int, partial: Float, buffer: BufferBuilder) = {

    Minecraft.getMinecraft.mcProfiler.startSection("quarryplus")

    if ((te.mode is TileAdvQuarry.MAKEFRAME) || (te.mode is TileAdvQuarry.NOTNEEDBREAK)) {
      Minecraft.getMinecraft.mcProfiler.startSection("advquarry")
      val range = te.digRange
      if (range.defined) {
        val pos = te.getPos
        val playerX = pos.getX - x
        val playerY = pos.getY - y
        val playerZ = pos.getZ - z
        buffer.setTranslation(-playerX, -playerY, -playerZ)
        val b1 = (playerZ - range.minZ - 0.5).abs < 256
        val b2 = (playerZ - range.maxZ + 1.5).abs < 256
        val b3 = (playerX - range.minX - 0.5).abs < 256
        val b4 = (playerX - range.maxX + 1.5).abs < 256
        val minX = math.max(range.minX - 0.5, playerX - 128)
        val maxX = math.min(range.maxX + 1.5, playerX + 128)
        val minZ = math.max(range.minZ - 0.5, playerZ - 128)
        val maxZ = math.min(range.maxZ + 1.5, playerZ + 128)
        if (b1) Box(minX, range.minY, range.minZ - 0.5, maxX, range.maxY, range.minZ - 0.5, d, d, d, firstSide = false, endSide = false).render(buffer, sprite)
        if (b2) Box(minX, range.minY, range.maxZ + 1.5, maxX, range.maxY, range.maxZ + 1.5, d, d, d, firstSide = false, endSide = false).render(buffer, sprite)
        if (b3) Box(range.minX - 0.5, range.minY, minZ, range.minX - 0.5, range.maxY, maxZ, d, d, d, firstSide = false, endSide = false).render(buffer, sprite)
        if (b4) Box(range.maxX + 1.5, range.minY, minZ, range.maxX + 1.5, range.maxY, maxZ, d, d, d, firstSide = false, endSide = false).render(buffer, sprite)
      }
      Minecraft.getMinecraft.mcProfiler.endSection()
    }

    Minecraft.getMinecraft.mcProfiler.endSection()
  }

  override def isGlobalRenderer(te: TileAdvQuarry): Boolean = true

}
