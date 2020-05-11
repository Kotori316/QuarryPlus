package com.yogpc.qp.render

import com.yogpc.qp.Config
import com.yogpc.qp.machines.marker.TileMarker
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraftforge.client.model.animation.TileEntityRendererFast

object RenderMarker extends TileEntityRendererFast[TileMarker] {

  val instance: RenderMarker.type = this
  val d: Double = 1d / 16d
  lazy val sprite_B: TextureAtlasSprite = Sprites.getMap(LaserType.BLUE_LASER.symbol)
  lazy val sprite_R: TextureAtlasSprite = Sprites.getMap(LaserType.RED_LASER.symbol)

  override def isGlobalRenderer(te: TileMarker): Boolean = true

  override def renderTileEntityFast(te: TileMarker, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, buffer: BufferBuilder): Unit = {
    if (Config.client.enableRender.get()) {
      Minecraft.getInstance.getProfiler.startSection("quarryplus")
      Minecraft.getInstance.getProfiler.startSection("marker")

      val pos = te.getPos
      buffer.setTranslation(x - pos.getX, y - pos.getY, z - pos.getZ)
      if (te.laser != null) {
        Minecraft.getInstance.getProfiler.startSection("laser")
        if (te.laser.boxes != null) {
          te.laser.boxes.foreach(_.render(buffer, sprite_B))
        }
        Minecraft.getInstance.getProfiler.endSection()
      }
      if (te.link != null) {
        Minecraft.getInstance.getProfiler.startSection("link")
        if (te.link.boxes != null) {
          te.link.boxes.foreach(_.render(buffer, sprite_R))
        }
        Minecraft.getInstance.getProfiler.endSection()
      }
      Minecraft.getInstance.getProfiler.endSection()
      Minecraft.getInstance.getProfiler.endSection()
    }
    buffer.setTranslation(0, 0, 0)
  }

}
