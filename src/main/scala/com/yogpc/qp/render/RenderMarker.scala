package com.yogpc.qp.render

import com.yogpc.qp.tile.TileMarker
import com.yogpc.qp.{Config, QuarryPlus}
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.VertexBuffer
import net.minecraftforge.client.model.animation.FastTESR

object RenderMarker extends FastTESR[TileMarker] {

    val instance = this
    val d = 1d / 16d
    lazy val sprite_B = Sprites.getMap(LaserType.BLUE_LASER.symbol)
    lazy val sprite_R = Sprites.getMap(LaserType.RED_LASER.symbol)

    override def isGlobalRenderer(te: TileMarker): Boolean = true

    override def renderTileEntityFast(te: TileMarker, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, buffer: VertexBuffer): Unit = {
        Minecraft.getMinecraft.mcProfiler.startSection("quarryplus")
        Minecraft.getMinecraft.mcProfiler.startSection("marker")

        val pos = te.getPos
        buffer.setTranslation(x - pos.getX, y - pos.getY, z - pos.getZ)
        if (te.laser != null) {
            Minecraft.getMinecraft.mcProfiler.startSection("laser")
            if (te.laser.boxes != null) {
                te.laser.boxes.foreach(_.render(buffer, sprite_B))
            } else {
                if (Config.content.debug) {
                    QuarryPlus.LOGGER.info("RenderMarker te.laser.boxes == null. " + pos)
                }
            }
            Minecraft.getMinecraft.mcProfiler.endSection()
        }
        if (te.link != null) {
            Minecraft.getMinecraft.mcProfiler.startSection("link")
            if (te.link.boxes != null) {
                te.link.boxes.foreach(_.render(buffer, sprite_R))
            } else {
                if (Config.content.debug) {
                    QuarryPlus.LOGGER.info("RenderMarker te.link.boxes == null. " + pos)
                }
            }
            Minecraft.getMinecraft.mcProfiler.endSection()
        }
        Minecraft.getMinecraft.mcProfiler.endSection()
        Minecraft.getMinecraft.mcProfiler.endSection()
    }

}
