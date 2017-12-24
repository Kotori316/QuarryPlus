package com.yogpc.qp.render

import com.yogpc.qp.block.ADismCBlock
import com.yogpc.qp.tile.TileRefinery
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer

/**
  * @see [[buildcraft.factory.client.render.RenderDistiller]]
  */
object RenderDistiller extends TileEntitySpecialRenderer[TileRefinery] {
    val instance = this
    val d = 1d / 16d

    override def renderTileEntityFast(te: TileRefinery, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, partial: Float, buffer: BufferBuilder) = {
        Minecraft.getMinecraft.mcProfiler.startSection("quarryplus")
        Minecraft.getMinecraft.mcProfiler.startSection("refinery")
        val pos = te.getPos
        buffer.setTranslation(x - pos.getX, y - pos.getY, z - pos.getZ)
        val light = te.getWorld.getCombinedLight(pos, 0)
        val skyLight = (light >> 16) & 0xFFFF
        val blockLight = (light >> 0) & 0xFFFF
        val facing = te.getWorld.getBlockState(pos).getValue(ADismCBlock.FACING)

        buffer.setTranslation(0, 0, 0)
        Minecraft.getMinecraft.mcProfiler.endSection()
        Minecraft.getMinecraft.mcProfiler.endSection()
    }

    def renderFluid(buffer: BufferBuilder, light: Box.LightValue, tank: TileRefinery#DistillerTank,
                    height: Double, sx: Double, sz: Double, sy: Double, ex: Double, ez: Double): Unit = {
        val d1 = tank.getAA(height)
        if (d1 != 0) {
            val texture = Minecraft.getMinecraft.getTextureMapBlocks.getAtlasSprite(tank.getFluid.getFluid.getStill.toString)
            val minY = math.min(sy, sy + d1)
            val maxY = math.max(sy, sy + d1)
            Box.apply(sx, minY, sz, ex, maxY, ez, .5, .5, .5, firstSide = true, endSide = true).render(buffer, texture)(light)
        }
    }
}
