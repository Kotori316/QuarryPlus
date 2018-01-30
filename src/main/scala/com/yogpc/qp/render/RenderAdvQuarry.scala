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
                buffer.setTranslation(x - pos.getX, y - pos.getY, z - pos.getZ)
                Box(range.minX - 0.5, range.minY, range.minZ - 0.5, range.maxX + 1.5, range.maxY, range.minZ - 0.5, d, d, d, firstSide = false, endSide = false).render(buffer, sprite)
                Box(range.minX - 0.5, range.minY, range.maxZ + 1.5, range.maxX + 1.5, range.maxY, range.maxZ + 1.5, d, d, d, firstSide = false, endSide = false).render(buffer, sprite)
                Box(range.minX - 0.5, range.minY, range.minZ - 0.5, range.minX - 0.5, range.maxY, range.maxZ + 1.5, d, d, d, firstSide = false, endSide = false).render(buffer, sprite)
                Box(range.maxX + 1.5, range.minY, range.minZ - 0.5, range.maxX + 1.5, range.maxY, range.maxZ + 1.5, d, d, d, firstSide = false, endSide = false).render(buffer, sprite)
            }
            Minecraft.getMinecraft.mcProfiler.endSection()
        }

        Minecraft.getMinecraft.mcProfiler.endSection()
    }

    override def isGlobalRenderer(te: TileAdvQuarry): Boolean = true
}
