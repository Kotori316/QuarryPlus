package com.yogpc.qp.render

import buildcraft.core.client.BuildCraftLaserManager
import buildcraft.lib.client.render.DetachedRenderer
import buildcraft.lib.client.render.laser.{LaserData_BC8, LaserRenderer_BC8}
import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.tile.TileMarker
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.Loader

class RenderMarker extends TileEntitySpecialRenderer[TileMarker] {

    override def isGlobalRenderer(te: TileMarker): Boolean = true

    override def renderTileEntityAt(te: TileMarker, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int): Unit = {
        super.renderTileEntityAt(te, x, y, z, partialTicks, destroyStage)

        if (Loader isModLoaded QuarryPlus.Optionals.Buildcraft_modID) {
            if (te.laser != null) {
                Minecraft.getMinecraft.mcProfiler.startSection("quarryplus")
                Minecraft.getMinecraft.mcProfiler.startSection("marker")
                Minecraft.getMinecraft.mcProfiler.startSection("laser")

                DetachedRenderer.fromWorldOriginPre(Minecraft.getMinecraft.player, partialTicks)
                RenderHelper.disableStandardItemLighting()
                Minecraft.getMinecraft.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)

                for (boundingBox <- te.laser.boxes) {
                    if (boundingBox != null) {
                        val start = new Vec3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ)
                        val end = new Vec3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ)
                        val data = new LaserData_BC8(BuildCraftLaserManager.MARKER_VOLUME_SIGNAL, start, end, 1d / 16d)
                        LaserRenderer_BC8.renderLaserStatic(data)
                    }
                }

                RenderHelper.enableStandardItemLighting()
                DetachedRenderer.fromWorldOriginPost()

                Minecraft.getMinecraft.mcProfiler.endSection()
                Minecraft.getMinecraft.mcProfiler.endSection()
                Minecraft.getMinecraft.mcProfiler.endSection()
            }
            if (te.link != null) {
                Minecraft.getMinecraft.mcProfiler.startSection("quarryplus")
                Minecraft.getMinecraft.mcProfiler.startSection("marker")
                Minecraft.getMinecraft.mcProfiler.startSection("links")

                DetachedRenderer.fromWorldOriginPre(Minecraft.getMinecraft.player, partialTicks)
                RenderHelper.disableStandardItemLighting()
                Minecraft.getMinecraft.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)

                for (boundingBox <- te.link.boxes) {
                    if (boundingBox != null) {
                        val start = new Vec3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ)
                        val end = new Vec3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ)
                        val data = new LaserData_BC8(BuildCraftLaserManager.MARKER_VOLUME_SIGNAL, start, end, 1d / 16d)
                        LaserRenderer_BC8.renderLaserStatic(data)
                    }
                }

                RenderHelper.enableStandardItemLighting()
                DetachedRenderer.fromWorldOriginPost()

                Minecraft.getMinecraft.mcProfiler.endSection()
                Minecraft.getMinecraft.mcProfiler.endSection()
                Minecraft.getMinecraft.mcProfiler.endSection()
            }
        }
    }
}
