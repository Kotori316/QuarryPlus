package com.yogpc.qp.render

import com.yogpc.qp.block.ADismCBlock
import com.yogpc.qp.tile.TileRefinery
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.util.EnumFacing
import net.minecraftforge.client.model.animation.FastTESR

/**
  * @see [[buildcraft.factory.client.render.RenderDistiller]]
  */
object RenderDistiller extends FastTESR[TileRefinery] {
    val instance = this
    private[this] final val d = 1d / 16d
    lazy val sprite = Sprites.getMap('stripes_refinery)
    val spriteUFromStage = (i: Float) => {
        if (i <= 1) 0
        else if (i <= 2.5) 2
        else if (i <= 4.5) 4
        else 6
    }

    override def renderTileEntityFast(te: TileRefinery, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, buffer: VertexBuffer) = {
        Minecraft.getMinecraft.mcProfiler.startSection("quarryplus")
        Minecraft.getMinecraft.mcProfiler.startSection("refinery")
        val pos = te.getPos
        buffer.setTranslation(x, y, z) //set pos to (0,0,0)
        val light = te.getWorld.getCombinedLight(pos, 0)
        val skyLight = (light >> 16) & 0xFFFF
        val blockLight = (light >> 0) & 0xFFFF
        val lightValue = Box.LightValue(skyLight, blockLight)
        val facing = te.getWorld.getBlockState(pos).getValue(ADismCBlock.FACING)

        renderFluid(buffer, lightValue, 1, te.horizontalsTank, 0.5 + facing.offsetX(0.25), 0, 0.5 + facing.offsetZ(0.25),
            x = 0.5, z = 0.5)
        renderFluid(buffer, lightValue, 0.5, te.upTank, 0.5 - facing.offsetX(0.25), 1, 0.5 - facing.offsetZ(0.25),
            x = 0.5 + facing.offsetZAbs(0.5), z = 0.5 + facing.offsetXAbs(0.5))
        renderFluid(buffer, lightValue, 0.5, te.downTank, 0.5 - facing.offsetX(0.25), 0, 0.5 - facing.offsetZ(0.25),
            x = 0.5 + facing.offsetZAbs(0.5), z = 0.5 + facing.offsetXAbs(0.5))
        val stage = te.getAnimationStage
        var trans1 = 0d
        var trans2 = 0d
        if (stage <= 100) {
            trans1 = 12 * d * stage / 100
            trans2 = 0d
        } else if (stage <= 200) {
            trans1 = 12 * d - 12 * d * (stage - 100) / 100
            trans2 = 12 * d * (stage - 100) / 100
        } else {
            trans1 = 12 * d * (stage - 200) / 100
            trans2 = 12 * d - 12 * d * (stage - 200) / 100
        }
        renderBox(buffer, facing, te.animationSpeed, trans1, trans2)
        buffer.setTranslation(0, 0, 0)
        Minecraft.getMinecraft.mcProfiler.endSection()
        Minecraft.getMinecraft.mcProfiler.endSection()
    }

    private def renderFluid(buffer: VertexBuffer, light: Box.LightValue, boxHeight: Double, tank: TileRefinery#DistillerTank,
                            cX: Double, sY: Double, cZ: Double, x: Double, z: Double): Unit = {
        val height = tank.getAA(boxHeight)
        if (height != 0) {
            val texture = Minecraft.getMinecraft.getTextureMapBlocks.getAtlasSprite(tank.getFluid.getFluid.getStill.toString)
            val minY = math.min(sY, sY + height)
            val maxY = math.max(sY, sY + height)
            Box.apply(cX, minY + 0.01, cZ, cX, maxY - 0.01, cZ, x - 0.01, height.abs, z - 0.01, firstSide = true, endSide = true).render(buffer, texture)
        }
    }

    private def renderBox(buffer: VertexBuffer, facing: EnumFacing, speed: Float, y1: Double, y2: Double): Unit = {
        val sx1 = if (facing.getAxis == EnumFacing.Axis.X) 0.5 + facing.offsetX(0.49) else 0 * d
        val sx2 = if (facing.getAxis == EnumFacing.Axis.X) 0.5 + facing.offsetX(0.49) else 12 * d
        val ex1 = if (facing.getAxis == EnumFacing.Axis.X) 0.5 + facing.offsetX(0.01) else 4 * d
        val ex2 = if (facing.getAxis == EnumFacing.Axis.X) 0.5 + facing.offsetX(0.01) else 16 * d

        val sz1 = if (facing.getAxis == EnumFacing.Axis.Z) 0.5 + facing.offsetZ(0.49) else 0 * d
        val sz2 = if (facing.getAxis == EnumFacing.Axis.Z) 0.5 + facing.offsetZ(0.49) else 12 * d
        val ez1 = if (facing.getAxis == EnumFacing.Axis.Z) 0.5 + facing.offsetZ(0.01) else 4 * d
        val ez2 = if (facing.getAxis == EnumFacing.Axis.Z) 0.5 + facing.offsetZ(0.01) else 16 * d

        val sy1 = y1
        val sy2 = y2
        val ey1 = sy1 + 4 * d
        val ey2 = sy2 + 4 * d

        val hminV = sprite.getInterpolatedV(spriteUFromStage(speed))
        val hmaxV = sprite.getInterpolatedV(spriteUFromStage(speed) + 2)
        val hminU = sprite.getInterpolatedU(2)
        val hmaxU = sprite.getInterpolatedU(6)
        val sminV = hminV
        val smaxV = hmaxV
        val sminU = sprite.getInterpolatedU(0)
        val smaxU = sprite.getInterpolatedU(2)
        val wminV = sprite.getInterpolatedV(8)
        val wmaxV = sprite.getInterpolatedV(10)
        val wminU = hminU
        val wmaxU = hmaxU

        if (facing.getAxis == EnumFacing.Axis.X) {
            buffer.pos(sx1, sy1, sz1).colored().tex(hminU, hminV).lightedAndEnd()
            buffer.pos(ex1, sy1, sz1).colored().tex(hmaxU, hminV).lightedAndEnd()
            buffer.pos(ex1, ey1, sz1).colored().tex(hmaxU, hmaxV).lightedAndEnd()
            buffer.pos(sx1, ey1, sz1).colored().tex(hminU, hmaxV).lightedAndEnd()

            buffer.pos(sx1, sy1, ez1).colored().tex(hminU, hminV).lightedAndEnd()
            buffer.pos(ex1, sy1, ez1).colored().tex(hmaxU, hminV).lightedAndEnd()
            buffer.pos(ex1, ey1, ez1).colored().tex(hmaxU, hmaxV).lightedAndEnd()
            buffer.pos(sx1, ey1, ez1).colored().tex(hminU, hmaxV).lightedAndEnd()

            buffer.pos(sx1, ey1, sz1).colored().tex(sminU, smaxV).lightedAndEnd()
            buffer.pos(sx1, ey1, ez1).colored().tex(smaxU, smaxV).lightedAndEnd()
            buffer.pos(sx1, sy1, ez1).colored().tex(smaxU, sminV).lightedAndEnd()
            buffer.pos(sx1, sy1, sz1).colored().tex(sminU, sminV).lightedAndEnd()

            buffer.pos(ex1, ey1, sz1).colored().tex(sminU, smaxV).lightedAndEnd()
            buffer.pos(ex1, ey1, ez1).colored().tex(smaxU, smaxV).lightedAndEnd()
            buffer.pos(ex1, sy1, ez1).colored().tex(smaxU, sminV).lightedAndEnd()
            buffer.pos(ex1, sy1, sz1).colored().tex(sminU, sminV).lightedAndEnd()


            buffer.pos(sx2, sy2, sz2).colored().tex(hminU, hminV).lightedAndEnd()
            buffer.pos(ex2, sy2, sz2).colored().tex(hmaxU, hminV).lightedAndEnd()
            buffer.pos(ex2, ey2, sz2).colored().tex(hmaxU, hmaxV).lightedAndEnd()
            buffer.pos(sx2, ey2, sz2).colored().tex(hminU, hmaxV).lightedAndEnd()

            buffer.pos(sx2, sy2, ez2).colored().tex(hminU, hminV).lightedAndEnd()
            buffer.pos(ex2, sy2, ez2).colored().tex(hmaxU, hminV).lightedAndEnd()
            buffer.pos(ex2, ey2, ez2).colored().tex(hmaxU, hmaxV).lightedAndEnd()
            buffer.pos(sx2, ey2, ez2).colored().tex(hminU, hmaxV).lightedAndEnd()

            buffer.pos(sx2, ey2, sz2).colored().tex(sminU, smaxV).lightedAndEnd()
            buffer.pos(sx2, ey2, ez2).colored().tex(smaxU, smaxV).lightedAndEnd()
            buffer.pos(sx2, sy2, ez2).colored().tex(smaxU, sminV).lightedAndEnd()
            buffer.pos(sx2, sy2, sz2).colored().tex(sminU, sminV).lightedAndEnd()

            buffer.pos(ex2, ey2, sz2).colored().tex(sminU, smaxV).lightedAndEnd()
            buffer.pos(ex2, ey2, ez2).colored().tex(smaxU, smaxV).lightedAndEnd()
            buffer.pos(ex2, sy2, ez2).colored().tex(smaxU, sminV).lightedAndEnd()
            buffer.pos(ex2, sy2, sz2).colored().tex(sminU, sminV).lightedAndEnd()

            //Top
            buffer.pos(sx1, sy1, sz1).colored().tex(wminU, wminV).lightedAndEnd()
            buffer.pos(ex1, sy1, sz1).colored().tex(wmaxU, wminV).lightedAndEnd()
            buffer.pos(ex1, sy1, ez1).colored().tex(wmaxU, wmaxV).lightedAndEnd()
            buffer.pos(sx1, sy1, ez1).colored().tex(wminU, wmaxV).lightedAndEnd()

            buffer.pos(sx1, ey1, sz1).colored().tex(wminU, wminV).lightedAndEnd()
            buffer.pos(ex1, ey1, sz1).colored().tex(wmaxU, wminV).lightedAndEnd()
            buffer.pos(ex1, ey1, ez1).colored().tex(wmaxU, wmaxV).lightedAndEnd()
            buffer.pos(sx1, ey1, ez1).colored().tex(wminU, wmaxV).lightedAndEnd()

            buffer.pos(sx2, sy2, sz2).colored().tex(wminU, wminV).lightedAndEnd()
            buffer.pos(ex2, sy2, sz2).colored().tex(wmaxU, wminV).lightedAndEnd()
            buffer.pos(ex2, sy2, ez2).colored().tex(wmaxU, wmaxV).lightedAndEnd()
            buffer.pos(sx2, sy2, ez2).colored().tex(wminU, wmaxV).lightedAndEnd()

            buffer.pos(sx2, ey2, sz2).colored().tex(wminU, wminV).lightedAndEnd()
            buffer.pos(ex2, ey2, sz2).colored().tex(wmaxU, wminV).lightedAndEnd()
            buffer.pos(ex2, ey2, ez2).colored().tex(wmaxU, wmaxV).lightedAndEnd()
            buffer.pos(sx2, ey2, ez2).colored().tex(wminU, wmaxV).lightedAndEnd()

        } else {
            buffer.pos(sx1, sy1, sz1).colored().tex(sminU, smaxV).lightedAndEnd()
            buffer.pos(ex1, sy1, sz1).colored().tex(smaxU, smaxV).lightedAndEnd()
            buffer.pos(ex1, ey1, sz1).colored().tex(smaxU, sminV).lightedAndEnd()
            buffer.pos(sx1, ey1, sz1).colored().tex(sminU, sminV).lightedAndEnd()

            buffer.pos(sx1, sy1, ez1).colored().tex(sminU, smaxV).lightedAndEnd()
            buffer.pos(ex1, sy1, ez1).colored().tex(smaxU, smaxV).lightedAndEnd()
            buffer.pos(ex1, ey1, ez1).colored().tex(smaxU, sminV).lightedAndEnd()
            buffer.pos(sx1, ey1, ez1).colored().tex(sminU, sminV).lightedAndEnd()

            buffer.pos(sx1, ey1, sz1).colored().tex(hminU, hminV).lightedAndEnd()
            buffer.pos(sx1, ey1, ez1).colored().tex(hmaxU, hminV).lightedAndEnd()
            buffer.pos(sx1, sy1, ez1).colored().tex(hmaxU, hmaxV).lightedAndEnd()
            buffer.pos(sx1, sy1, sz1).colored().tex(hminU, hmaxV).lightedAndEnd()

            buffer.pos(ex1, ey1, sz1).colored().tex(hminU, hminV).lightedAndEnd()
            buffer.pos(ex1, ey1, ez1).colored().tex(hmaxU, hminV).lightedAndEnd()
            buffer.pos(ex1, sy1, ez1).colored().tex(hmaxU, hmaxV).lightedAndEnd()
            buffer.pos(ex1, sy1, sz1).colored().tex(hminU, hmaxV).lightedAndEnd()


            buffer.pos(sx2, sy2, sz2).colored().tex(sminU, smaxV).lightedAndEnd()
            buffer.pos(ex2, sy2, sz2).colored().tex(smaxU, smaxV).lightedAndEnd()
            buffer.pos(ex2, ey2, sz2).colored().tex(smaxU, sminV).lightedAndEnd()
            buffer.pos(sx2, ey2, sz2).colored().tex(sminU, sminV).lightedAndEnd()

            buffer.pos(sx2, sy2, ez2).colored().tex(sminU, smaxV).lightedAndEnd()
            buffer.pos(ex2, sy2, ez2).colored().tex(smaxU, smaxV).lightedAndEnd()
            buffer.pos(ex2, ey2, ez2).colored().tex(smaxU, sminV).lightedAndEnd()
            buffer.pos(sx2, ey2, ez2).colored().tex(sminU, sminV).lightedAndEnd()

            buffer.pos(sx2, ey2, sz2).colored().tex(hminU, hminV).lightedAndEnd()
            buffer.pos(sx2, ey2, ez2).colored().tex(hmaxU, hminV).lightedAndEnd()
            buffer.pos(sx2, sy2, ez2).colored().tex(hmaxU, hmaxV).lightedAndEnd()
            buffer.pos(sx2, sy2, sz2).colored().tex(hminU, hmaxV).lightedAndEnd()

            buffer.pos(ex2, ey2, sz2).colored().tex(hminU, hminV).lightedAndEnd()
            buffer.pos(ex2, ey2, ez2).colored().tex(hmaxU, hminV).lightedAndEnd()
            buffer.pos(ex2, sy2, ez2).colored().tex(hmaxU, hmaxV).lightedAndEnd()
            buffer.pos(ex2, sy2, sz2).colored().tex(hminU, hmaxV).lightedAndEnd()

            //Top
            buffer.pos(sx1, sy1, sz1).colored().tex(wminU, wminV).lightedAndEnd()
            buffer.pos(sx1, sy1, ez1).colored().tex(wmaxU, wminV).lightedAndEnd()
            buffer.pos(ex1, sy1, ez1).colored().tex(wmaxU, wmaxV).lightedAndEnd()
            buffer.pos(ex1, sy1, sz1).colored().tex(wminU, wmaxV).lightedAndEnd()

            buffer.pos(sx1, ey1, sz1).colored().tex(wminU, wminV).lightedAndEnd()
            buffer.pos(sx1, ey1, ez1).colored().tex(wmaxU, wminV).lightedAndEnd()
            buffer.pos(ex1, ey1, ez1).colored().tex(wmaxU, wmaxV).lightedAndEnd()
            buffer.pos(ex1, ey1, sz1).colored().tex(wminU, wmaxV).lightedAndEnd()

            buffer.pos(sx2, sy2, sz2).colored().tex(wminU, wminV).lightedAndEnd()
            buffer.pos(sx2, sy2, ez2).colored().tex(wmaxU, wminV).lightedAndEnd()
            buffer.pos(ex2, sy2, ez2).colored().tex(wmaxU, wmaxV).lightedAndEnd()
            buffer.pos(ex2, sy2, sz2).colored().tex(wminU, wmaxV).lightedAndEnd()

            buffer.pos(sx2, ey2, sz2).colored().tex(wminU, wminV).lightedAndEnd()
            buffer.pos(sx2, ey2, ez2).colored().tex(wmaxU, wminV).lightedAndEnd()
            buffer.pos(ex2, ey2, ez2).colored().tex(wmaxU, wmaxV).lightedAndEnd()
            buffer.pos(ex2, ey2, sz2).colored().tex(wminU, wmaxV).lightedAndEnd()

        }

    }

}
