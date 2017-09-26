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

    /*
        private def render(box: AxisAlignedBB, buffer: VertexBuffer, minU: Double, minV: Double, maxU: Double, maxV: Double): Unit = {
            val subtract = new Vec3d(box.maxX - box.minX, box.maxY - box.minY, box.maxZ - box.minZ)
            val xFloor = MathHelper.floor(subtract.xCoord * 8d)
            val yFloor = MathHelper.floor(subtract.yCoord * 8d)
            val zFloor = MathHelper.floor(subtract.zCoord * 8d)

            if (xFloor != 0) {
                val y = box.maxY
                val z = box.maxZ
                new Range(0, xFloor, 1).foreach(i => {
                    //X line
                    val od = i * d * 2
                    val td = (i + 1) * d * 2
                    buffer.pos(box.minX + od, y + d, z + d).color(255, 255, 255, 255).tex(minU, minV).lightmap(240, 0).endVertex()
                    buffer.pos(box.minX + td, y + d, z + d).color(255, 255, 255, 255).tex(maxU, minV).lightmap(240, 0).endVertex()
                    buffer.pos(box.minX + td, y - d, z + d).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(240, 0).endVertex()
                    buffer.pos(box.minX + od, y - d, z + d).color(255, 255, 255, 255).tex(minU, maxV).lightmap(240, 0).endVertex()

                    buffer.pos(box.minX + od, y + d, z - d).color(255, 255, 255, 255).tex(minU, minV).lightmap(240, 0).endVertex()
                    buffer.pos(box.minX + td, y + d, z - d).color(255, 255, 255, 255).tex(maxU, minV).lightmap(240, 0).endVertex()
                    buffer.pos(box.minX + td, y - d, z - d).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(240, 0).endVertex()
                    buffer.pos(box.minX + od, y - d, z - d).color(255, 255, 255, 255).tex(minU, maxV).lightmap(240, 0).endVertex()

                    buffer.pos(box.minX + od, y + d, z - d).color(255, 255, 255, 255).tex(minU, minV).lightmap(240, 0).endVertex()
                    buffer.pos(box.minX + td, y + d, z - d).color(255, 255, 255, 255).tex(maxU, minV).lightmap(240, 0).endVertex()
                    buffer.pos(box.minX + td, y + d, z + d).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(240, 0).endVertex()
                    buffer.pos(box.minX + od, y + d, z + d).color(255, 255, 255, 255).tex(minU, maxV).lightmap(240, 0).endVertex()

                    buffer.pos(box.minX + od, y - d, z - d).color(255, 255, 255, 255).tex(minU, minV).lightmap(240, 0).endVertex()
                    buffer.pos(box.minX + td, y - d, z - d).color(255, 255, 255, 255).tex(maxU, minV).lightmap(240, 0).endVertex()
                    buffer.pos(box.minX + td, y - d, z + d).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(240, 0).endVertex()
                    buffer.pos(box.minX + od, y - d, z + d).color(255, 255, 255, 255).tex(minU, maxV).lightmap(240, 0).endVertex()
                })
                val rest = subtract.xCoord - xFloor.toDouble / 8d
                assert(rest < 1, "Marker laser x rest")
                val od = xFloor * d * 2
                val td = od + rest
                buffer.pos(box.minX + od, y + d, z + d).color(255, 255, 255, 255).tex(minU, minV).lightmap(240, 0).endVertex()
                buffer.pos(box.minX + td, y + d, z + d).color(255, 255, 255, 255).tex(maxU, minV).lightmap(240, 0).endVertex()
                buffer.pos(box.minX + td, y - d, z + d).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(240, 0).endVertex()
                buffer.pos(box.minX + od, y - d, z + d).color(255, 255, 255, 255).tex(minU, maxV).lightmap(240, 0).endVertex()

                buffer.pos(box.minX + od, y + d, z - d).color(255, 255, 255, 255).tex(minU, minV).lightmap(240, 0).endVertex()
                buffer.pos(box.minX + td, y + d, z - d).color(255, 255, 255, 255).tex(maxU, minV).lightmap(240, 0).endVertex()
                buffer.pos(box.minX + td, y - d, z - d).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(240, 0).endVertex()
                buffer.pos(box.minX + od, y - d, z - d).color(255, 255, 255, 255).tex(minU, maxV).lightmap(240, 0).endVertex()

                buffer.pos(box.minX + od, y + d, z - d).color(255, 255, 255, 255).tex(minU, minV).lightmap(240, 0).endVertex()
                buffer.pos(box.minX + td, y + d, z - d).color(255, 255, 255, 255).tex(maxU, minV).lightmap(240, 0).endVertex()
                buffer.pos(box.minX + td, y + d, z + d).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(240, 0).endVertex()
                buffer.pos(box.minX + od, y + d, z + d).color(255, 255, 255, 255).tex(minU, maxV).lightmap(240, 0).endVertex()

                buffer.pos(box.minX + od, y - d, z - d).color(255, 255, 255, 255).tex(minU, minV).lightmap(240, 0).endVertex()
                buffer.pos(box.minX + td, y - d, z - d).color(255, 255, 255, 255).tex(maxU, minV).lightmap(240, 0).endVertex()
                buffer.pos(box.minX + td, y - d, z + d).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(240, 0).endVertex()
                buffer.pos(box.minX + od, y - d, z + d).color(255, 255, 255, 255).tex(minU, maxV).lightmap(240, 0).endVertex()
            }
            if (yFloor != 0) {
                val x = box.maxX
                val z = box.maxZ
                for (i <- 0 until yFloor) {
                    //Y line
                    val od = i * d * 2
                    val td = od + d * 2
                    buffer.pos(x - d, box.minY + od, z + d).color(255, 255, 255, 255).tex(minU, minV).lightmap(240, 0).endVertex()
                    buffer.pos(x - d, box.minY + td, z + d).color(255, 255, 255, 255).tex(maxU, minV).lightmap(240, 0).endVertex()
                    buffer.pos(x + d, box.minY + td, z + d).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(240, 0).endVertex()
                    buffer.pos(x + d, box.minY + od, z + d).color(255, 255, 255, 255).tex(minU, maxV).lightmap(240, 0).endVertex()

                    buffer.pos(x - d, box.minY + od, z - d).color(255, 255, 255, 255).tex(minU, minV).lightmap(240, 0).endVertex()
                    buffer.pos(x - d, box.minY + td, z - d).color(255, 255, 255, 255).tex(maxU, minV).lightmap(240, 0).endVertex()
                    buffer.pos(x + d, box.minY + td, z - d).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(240, 0).endVertex()
                    buffer.pos(x + d, box.minY + od, z - d).color(255, 255, 255, 255).tex(minU, maxV).lightmap(240, 0).endVertex()

                    buffer.pos(x + d, box.minY + od, z - d).color(255, 255, 255, 255).tex(minU, minV).lightmap(240, 0).endVertex()
                    buffer.pos(x + d, box.minY + td, z - d).color(255, 255, 255, 255).tex(maxU, minV).lightmap(240, 0).endVertex()
                    buffer.pos(x + d, box.minY + td, z + d).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(240, 0).endVertex()
                    buffer.pos(x + d, box.minY + od, z + d).color(255, 255, 255, 255).tex(minU, maxV).lightmap(240, 0).endVertex()

                    buffer.pos(x - d, box.minY + od, z - d).color(255, 255, 255, 255).tex(minU, minV).lightmap(240, 0).endVertex()
                    buffer.pos(x - d, box.minY + td, z - d).color(255, 255, 255, 255).tex(maxU, minV).lightmap(240, 0).endVertex()
                    buffer.pos(x - d, box.minY + td, z + d).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(240, 0).endVertex()
                    buffer.pos(x - d, box.minY + od, z + d).color(255, 255, 255, 255).tex(minU, maxV).lightmap(240, 0).endVertex()
                }
                val rest = subtract.yCoord - yFloor.toDouble / 8d
                assert(rest < 1, "Marker laser y rest")
                val od = yFloor * d * 2
                val td = od + rest
                buffer.pos(x - d, box.minY + od, z + d).color(255, 255, 255, 255).tex(minU, minV).lightmap(240, 0).endVertex()
                buffer.pos(x - d, box.minY + td, z + d).color(255, 255, 255, 255).tex(maxU, minV).lightmap(240, 0).endVertex()
                buffer.pos(x + d, box.minY + td, z + d).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(240, 0).endVertex()
                buffer.pos(x + d, box.minY + od, z + d).color(255, 255, 255, 255).tex(minU, maxV).lightmap(240, 0).endVertex()

                buffer.pos(x - d, box.minY + od, z - d).color(255, 255, 255, 255).tex(minU, minV).lightmap(240, 0).endVertex()
                buffer.pos(x - d, box.minY + td, z - d).color(255, 255, 255, 255).tex(maxU, minV).lightmap(240, 0).endVertex()
                buffer.pos(x + d, box.minY + td, z - d).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(240, 0).endVertex()
                buffer.pos(x + d, box.minY + od, z - d).color(255, 255, 255, 255).tex(minU, maxV).lightmap(240, 0).endVertex()

                buffer.pos(x + d, box.minY + od, z - d).color(255, 255, 255, 255).tex(minU, minV).lightmap(240, 0).endVertex()
                buffer.pos(x + d, box.minY + td, z - d).color(255, 255, 255, 255).tex(maxU, minV).lightmap(240, 0).endVertex()
                buffer.pos(x + d, box.minY + td, z + d).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(240, 0).endVertex()
                buffer.pos(x + d, box.minY + od, z + d).color(255, 255, 255, 255).tex(minU, maxV).lightmap(240, 0).endVertex()

                buffer.pos(x - d, box.minY + od, z - d).color(255, 255, 255, 255).tex(minU, minV).lightmap(240, 0).endVertex()
                buffer.pos(x - d, box.minY + td, z - d).color(255, 255, 255, 255).tex(maxU, minV).lightmap(240, 0).endVertex()
                buffer.pos(x - d, box.minY + td, z + d).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(240, 0).endVertex()
                buffer.pos(x - d, box.minY + od, z + d).color(255, 255, 255, 255).tex(minU, maxV).lightmap(240, 0).endVertex()
            }
            if (zFloor != 0) {
                val x = box.maxX
                val y = box.maxY
                for (i <- Range(0, zFloor)) {
                    //Z line
                    val od = i * d * 2
                    val td = od + d * 2
                    buffer.pos(x + d, y + d, box.minZ + od).color(255, 255, 255, 255).tex(minU, minV).lightmap(240, 0).endVertex()
                    buffer.pos(x + d, y + d, box.minZ + td).color(255, 255, 255, 255).tex(maxU, minV).lightmap(240, 0).endVertex()
                    buffer.pos(x + d, y - d, box.minZ + td).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(240, 0).endVertex()
                    buffer.pos(x + d, y - d, box.minZ + od).color(255, 255, 255, 255).tex(minU, maxV).lightmap(240, 0).endVertex()

                    buffer.pos(x - d, y + d, box.minZ + od).color(255, 255, 255, 255).tex(minU, minV).lightmap(240, 0).endVertex()
                    buffer.pos(x - d, y + d, box.minZ + td).color(255, 255, 255, 255).tex(maxU, minV).lightmap(240, 0).endVertex()
                    buffer.pos(x - d, y - d, box.minZ + td).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(240, 0).endVertex()
                    buffer.pos(x - d, y - d, box.minZ + od).color(255, 255, 255, 255).tex(minU, maxV).lightmap(240, 0).endVertex()

                    buffer.pos(x - d, y + d, box.minZ + od).color(255, 255, 255, 255).tex(minU, minV).lightmap(240, 0).endVertex()
                    buffer.pos(x - d, y + d, box.minZ + td).color(255, 255, 255, 255).tex(maxU, minV).lightmap(240, 0).endVertex()
                    buffer.pos(x + d, y + d, box.minZ + td).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(240, 0).endVertex()
                    buffer.pos(x + d, y + d, box.minZ + od).color(255, 255, 255, 255).tex(minU, maxV).lightmap(240, 0).endVertex()

                    buffer.pos(x - d, y - d, box.minZ + od).color(255, 255, 255, 255).tex(minU, minV).lightmap(240, 0).endVertex()
                    buffer.pos(x - d, y - d, box.minZ + td).color(255, 255, 255, 255).tex(maxU, minV).lightmap(240, 0).endVertex()
                    buffer.pos(x + d, y - d, box.minZ + td).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(240, 0).endVertex()
                    buffer.pos(x + d, y - d, box.minZ + od).color(255, 255, 255, 255).tex(minU, maxV).lightmap(240, 0).endVertex()
                }
                val rest = subtract.zCoord - zFloor.toDouble / 8d
                assert(rest < 1, "Marker laser z rest")
                val od = zFloor * d * 2
                val td = od + rest
                buffer.pos(x + d, y + d, box.minZ + od).color(255, 255, 255, 255).tex(minU, minV).lightmap(240, 0).endVertex()
                buffer.pos(x + d, y + d, box.minZ + td).color(255, 255, 255, 255).tex(maxU, minV).lightmap(240, 0).endVertex()
                buffer.pos(x + d, y - d, box.minZ + td).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(240, 0).endVertex()
                buffer.pos(x + d, y - d, box.minZ + od).color(255, 255, 255, 255).tex(minU, maxV).lightmap(240, 0).endVertex()

                buffer.pos(x - d, y + d, box.minZ + od).color(255, 255, 255, 255).tex(minU, minV).lightmap(240, 0).endVertex()
                buffer.pos(x - d, y + d, box.minZ + td).color(255, 255, 255, 255).tex(maxU, minV).lightmap(240, 0).endVertex()
                buffer.pos(x - d, y - d, box.minZ + td).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(240, 0).endVertex()
                buffer.pos(x - d, y - d, box.minZ + od).color(255, 255, 255, 255).tex(minU, maxV).lightmap(240, 0).endVertex()

                buffer.pos(x - d, y + d, box.minZ + od).color(255, 255, 255, 255).tex(minU, minV).lightmap(240, 0).endVertex()
                buffer.pos(x - d, y + d, box.minZ + td).color(255, 255, 255, 255).tex(maxU, minV).lightmap(240, 0).endVertex()
                buffer.pos(x + d, y + d, box.minZ + td).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(240, 0).endVertex()
                buffer.pos(x + d, y + d, box.minZ + od).color(255, 255, 255, 255).tex(minU, maxV).lightmap(240, 0).endVertex()

                buffer.pos(x - d, y - d, box.minZ + od).color(255, 255, 255, 255).tex(minU, minV).lightmap(240, 0).endVertex()
                buffer.pos(x - d, y - d, box.minZ + td).color(255, 255, 255, 255).tex(maxU, minV).lightmap(240, 0).endVertex()
                buffer.pos(x + d, y - d, box.minZ + td).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(240, 0).endVertex()
                buffer.pos(x + d, y - d, box.minZ + od).color(255, 255, 255, 255).tex(minU, maxV).lightmap(240, 0).endVertex()
            }
        }

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

                     for (boundingBox <- te.laser.lineBoxes) {
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

                     for (boundingBox <- te.link.lineBoxes) {
                         if (boundingBox != null) {
                             val start = new Vec3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ)
                             val end = new Vec3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ)
                             val data = new LaserData_BC8(BuildCraftLaserManager.MARKER_VOLUME_CONNECTED, start, end, 1d / 16d)
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
         }*/
}
