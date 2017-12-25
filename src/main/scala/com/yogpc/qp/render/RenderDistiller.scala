package com.yogpc.qp.render

import com.yogpc.qp.block.ADismCBlock
import com.yogpc.qp.tile.TileRefinery
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.util.EnumFacing

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
        buffer.setTranslation(x, y, z) //set pos to (0,0,0)
        val light = te.getWorld.getCombinedLight(pos, 0)
        val skyLight = (light >> 16) & 0xFFFF
        val blockLight = (light >> 0) & 0xFFFF
        val lightValue = Box.LightValue(skyLight, blockLight)
        val facing = te.getWorld.getBlockState(pos).getValue(ADismCBlock.FACING)
        facing match {
            case EnumFacing.NORTH =>
                renderFluid(buffer, lightValue, 1, te.horizontalsTank, 0.5, 0, 0.25, 0.5, 0.5)
                renderFluid(buffer, lightValue, 0.5, te.upTank, 0.5, 1, 0.75, 1, 0.5)
                renderFluid(buffer, lightValue, 0.5, te.downTank, 0.5, 0, 0.75, 1, 0.5)
            case EnumFacing.SOUTH =>
                renderFluid(buffer, lightValue, 1, te.horizontalsTank, 0.5, 0, 0.75, 0.5, 0.5)
                renderFluid(buffer, lightValue, 0.5, te.upTank, 0.5, 1, 0.25, 1, 0.5)
                renderFluid(buffer, lightValue, 0.5, te.downTank, 0.5, 0, 0.25, 1, 0.5)
            case EnumFacing.EAST =>
                renderFluid(buffer, lightValue, 1, te.horizontalsTank, 0.75, 0, 0.5, 0.5, 0.5)
                renderFluid(buffer, lightValue, 0.5, te.upTank, 0.25, 1, 0.5, 0.5, 1)
                renderFluid(buffer, lightValue, 0.5, te.downTank, 0.25, 0, 0.5, 0.5, 1)
            case EnumFacing.WEST =>
                renderFluid(buffer, lightValue, 1, te.horizontalsTank, 0.25, 0, 0.5, 0.5, 0.5)
                renderFluid(buffer, lightValue, 0.5, te.upTank, 0.75, 1, 0.5, 0.5, 1)
                renderFluid(buffer, lightValue, 0.5, te.downTank, 0.75, 0, 0.5, 0.5, 1)
            case _ =>
        }

        buffer.setTranslation(0, 0, 0)
        Minecraft.getMinecraft.mcProfiler.endSection()
        Minecraft.getMinecraft.mcProfiler.endSection()
    }

    def renderFluid(buffer: BufferBuilder, light: Box.LightValue, boxHeight: Double, tank: TileRefinery#DistillerTank,
                    cX: Double, sY: Double, cZ: Double, x: Double, z: Double): Unit = {
        val height = tank.getAA(boxHeight)
        if (height != 0) {
            val texture = Minecraft.getMinecraft.getTextureMapBlocks.getAtlasSprite(tank.getFluid.getFluid.getStill.toString)
            val minY = math.min(sY, sY + height)
            val maxY = math.max(sY, sY + height)
            Box.apply(cX, minY, cZ, cX, maxY, cZ, x - 0.01, height.abs - 0.01, z - 0.01, firstSide = true, endSide = true).render(buffer, texture)
        }
    }
}
