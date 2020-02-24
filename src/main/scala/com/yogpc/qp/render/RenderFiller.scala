package com.yogpc.qp.render

import com.yogpc.qp.tile.TileFiller
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BufferBuilder
import net.minecraftforge.client.model.animation.FastTESR

object RenderFiller extends FastTESR[TileFiller] {
  private[this] final val d = 2d / 16d
  lazy val sprite = Sprites.getMap('yellow)
  val instance = this

  override def renderTileEntityFast(te: TileFiller, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, partial: Float, buffer: BufferBuilder): Unit = {
    Minecraft.getMinecraft.mcProfiler.startSection("quarryplus")
    Minecraft.getMinecraft.mcProfiler.startSection("filler")
    val areaOpt = te.getWorkingArea
    if (areaOpt.isDefined) {
      val min = areaOpt.get._1
      val max = areaOpt.get._2
      val pos = te.getPos
      val playerX = pos.getX - x
      val playerY = pos.getY - y
      val playerZ = pos.getZ - z
      buffer.setTranslation(-playerX, -playerY, -playerZ)
      val b1 = (playerZ - min.getZ + 0.5).abs < 256
      val b2 = (playerZ - max.getZ + 0.5).abs < 256
      val b3 = (playerX - min.getX + 0.5).abs < 256
      val b4 = (playerX - max.getX + 0.5).abs < 256
      val minX = math.max(min.getX + 0.5, playerX - 128)
      val maxX = math.min(max.getX + 0.5, playerX + 128)
      val minZ = math.max(min.getZ + 0.5, playerZ - 128)
      val maxZ = math.min(max.getZ + 0.5, playerZ + 128)
      if (b1) Box(minX, min.getY + 0.5, min.getZ + 0.5, maxX, min.getY + 0.5, min.getZ + 0.5, d, d, d, firstSide = false, endSide = false).render(buffer, sprite)
      if (b1) Box(minX, max.getY + 0.5, min.getZ + 0.5, maxX, max.getY + 0.5, min.getZ + 0.5, d, d, d, firstSide = false, endSide = false).render(buffer, sprite)
      if (b2) Box(minX, min.getY + 0.5, max.getZ + 0.5, maxX, min.getY + 0.5, max.getZ + 0.5, d, d, d, firstSide = false, endSide = false).render(buffer, sprite)
      if (b2) Box(minX, max.getY + 0.5, max.getZ + 0.5, maxX, max.getY + 0.5, max.getZ + 0.5, d, d, d, firstSide = false, endSide = false).render(buffer, sprite)
      if (b3) Box(min.getX + 0.5, min.getY + 0.5, minZ, min.getX + 0.5, min.getY + 0.5, maxZ, d, d, d, firstSide = false, endSide = false).render(buffer, sprite)
      if (b3) Box(min.getX + 0.5, max.getY + 0.5, minZ, min.getX + 0.5, max.getY + 0.5, maxZ, d, d, d, firstSide = false, endSide = false).render(buffer, sprite)
      if (b4) Box(max.getX + 0.5, min.getY + 0.5, minZ, max.getX + 0.5, min.getY + 0.5, maxZ, d, d, d, firstSide = false, endSide = false).render(buffer, sprite)
      if (b4) Box(max.getX + 0.5, max.getY + 0.5, minZ, max.getX + 0.5, max.getY + 0.5, maxZ, d, d, d, firstSide = false, endSide = false).render(buffer, sprite)

      if (b1 && b3) Box(min.getX + 0.5, min.getY + 0.5, min.getZ + 0.5, min.getX + 0.5, max.getY + 0.5, min.getZ + 0.5, d, d, d, firstSide = false, endSide = false).render(buffer, sprite)
      if (b1 && b4) Box(max.getX + 0.5, min.getY + 0.5, min.getZ + 0.5, max.getX + 0.5, max.getY + 0.5, min.getZ + 0.5, d, d, d, firstSide = false, endSide = false).render(buffer, sprite)
      if (b2 && b3) Box(min.getX + 0.5, min.getY + 0.5, max.getZ + 0.5, min.getX + 0.5, max.getY + 0.5, max.getZ + 0.5, d, d, d, firstSide = false, endSide = false).render(buffer, sprite)
      if (b2 && b4) Box(max.getX + 0.5, min.getY + 0.5, max.getZ + 0.5, max.getX + 0.5, max.getY + 0.5, max.getZ + 0.5, d, d, d, firstSide = false, endSide = false).render(buffer, sprite)
      buffer.setTranslation(0, 0, 0)
    }
    Minecraft.getMinecraft.mcProfiler.endSection()
    Minecraft.getMinecraft.mcProfiler.endSection()
  }

}
