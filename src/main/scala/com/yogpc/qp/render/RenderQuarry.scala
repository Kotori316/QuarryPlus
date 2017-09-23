package com.yogpc.qp.render

import com.yogpc.qp.tile.TileQuarry
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.util.math.MathHelper
import net.minecraftforge.client.model.animation.FastTESR

object RenderQuarry extends FastTESR[TileQuarry] {
    val instance = this

    val d1 = 1d / 16d
    val d4 = 4d / 16d
    private lazy val spriteV = Sprites.getMap('stripes_v)
    private lazy val spriteH = Sprites.getMap('stripes_h)
    private lazy val boxStripe = Sprites.getMap('stripes_b)
    private lazy val drillStripe = Sprites.getMap(LaserType.DRILL.symbol)
    private lazy val headSprite = Sprites.getMap(LaserType.DRILL_HEAD.symbol)
    val plusF: (Double, Double) => Double = (double1, double2) => double1 + double2
    val minusF: (Double, Double) => Double = (double1, double2) => double1 - double2

    override def renderTileEntityFast(quarry: TileQuarry, distanceX: Double, distanceY: Double, distanceZ: Double,
                                      partialTicks: Float, destroyStage: Int, buffer: VertexBuffer) = {
        val pos = quarry.getPos

        Minecraft.getMinecraft.mcProfiler.startSection("quarryplus")
        Minecraft.getMinecraft.mcProfiler.startSection("quarry")
        if ((quarry.G_getNow == TileQuarry.Mode.NOTNEEDBREAK || quarry.G_getNow == TileQuarry.Mode.MAKEFRAME) && quarry.yMax != Integer.MIN_VALUE) {
            Minecraft.getMinecraft.mcProfiler.startSection("frame")
            buffer.setTranslation(distanceX - pos.getX + .5, distanceY - pos.getY + .5, distanceZ - pos.getZ + .5)
            val minPos = quarry.getMinPos
            val maxPos = quarry.getMaxPos
            val subtract = quarry.getMaxPos subtract quarry.getMinPos

            //X 4 LINES
            val V_minU = spriteV.getMinU
            val V_minV = spriteV.getMinV
            val V_maxU = spriteV.getInterpolatedU(8)
            val V_maxV = spriteV.getInterpolatedV(8)
            for (i <- 0 until subtract.getX) {
                val n = if (i == subtract.getX - 1) 1 - d1 * 2 else 1d
                val di = d1 + i
                val dn = d1 + i + n

                def tempY(y: Double): Unit = {
                    buffer.pos(minPos.getX + di, y - d1, minPos.getZ - d1).colored().tex(V_minU, V_minV).lightedAndEnd()
                    buffer.pos(minPos.getX + dn, y - d1, minPos.getZ - d1).colored().tex(V_maxU, V_minV).lightedAndEnd()
                    buffer.pos(minPos.getX + dn, y - d1, minPos.getZ + d1).colored().tex(V_maxU, V_maxV).lightedAndEnd()
                    buffer.pos(minPos.getX + di, y - d1, minPos.getZ + d1).colored().tex(V_minU, V_maxV).lightedAndEnd()

                    buffer.pos(minPos.getX + di, y - d1, maxPos.getZ - d1).colored().tex(V_minU, V_minV).lightedAndEnd()
                    buffer.pos(minPos.getX + dn, y - d1, maxPos.getZ - d1).colored().tex(V_maxU, V_minV).lightedAndEnd()
                    buffer.pos(minPos.getX + dn, y - d1, maxPos.getZ + d1).colored().tex(V_maxU, V_maxV).lightedAndEnd()
                    buffer.pos(minPos.getX + di, y - d1, maxPos.getZ + d1).colored().tex(V_minU, V_maxV).lightedAndEnd()

                    buffer.pos(minPos.getX + di, y + d1, minPos.getZ - d1).colored().tex(V_minU, V_minV).lightedAndEnd()
                    buffer.pos(minPos.getX + dn, y + d1, minPos.getZ - d1).colored().tex(V_maxU, V_minV).lightedAndEnd()
                    buffer.pos(minPos.getX + dn, y + d1, minPos.getZ + d1).colored().tex(V_maxU, V_maxV).lightedAndEnd()
                    buffer.pos(minPos.getX + di, y + d1, minPos.getZ + d1).colored().tex(V_minU, V_maxV).lightedAndEnd()

                    buffer.pos(minPos.getX + di, y + d1, maxPos.getZ - d1).colored().tex(V_minU, V_minV).lightedAndEnd()
                    buffer.pos(minPos.getX + dn, y + d1, maxPos.getZ - d1).colored().tex(V_maxU, V_minV).lightedAndEnd()
                    buffer.pos(minPos.getX + dn, y + d1, maxPos.getZ + d1).colored().tex(V_maxU, V_maxV).lightedAndEnd()
                    buffer.pos(minPos.getX + di, y + d1, maxPos.getZ + d1).colored().tex(V_minU, V_maxV).lightedAndEnd()
                }

                def tempZ(z: Double): Unit = {
                    buffer.pos(minPos.getX + di, minPos.getY + d1, z - d1).colored().tex(V_minU, V_minV).lightedAndEnd()
                    buffer.pos(minPos.getX + dn, minPos.getY + d1, z - d1).colored().tex(V_maxU, V_minV).lightedAndEnd()
                    buffer.pos(minPos.getX + dn, minPos.getY - d1, z - d1).colored().tex(V_maxU, V_maxV).lightedAndEnd()
                    buffer.pos(minPos.getX + di, minPos.getY - d1, z - d1).colored().tex(V_minU, V_maxV).lightedAndEnd()

                    buffer.pos(minPos.getX + di, maxPos.getY + d1, z - d1).colored().tex(V_minU, V_minV).lightedAndEnd()
                    buffer.pos(minPos.getX + dn, maxPos.getY + d1, z - d1).colored().tex(V_maxU, V_minV).lightedAndEnd()
                    buffer.pos(minPos.getX + dn, maxPos.getY - d1, z - d1).colored().tex(V_maxU, V_maxV).lightedAndEnd()
                    buffer.pos(minPos.getX + di, maxPos.getY - d1, z - d1).colored().tex(V_minU, V_maxV).lightedAndEnd()

                    buffer.pos(minPos.getX + di, minPos.getY + d1, z + d1).colored().tex(V_minU, V_minV).lightedAndEnd()
                    buffer.pos(minPos.getX + dn, minPos.getY + d1, z + d1).colored().tex(V_maxU, V_minV).lightedAndEnd()
                    buffer.pos(minPos.getX + dn, minPos.getY - d1, z + d1).colored().tex(V_maxU, V_maxV).lightedAndEnd()
                    buffer.pos(minPos.getX + di, minPos.getY - d1, z + d1).colored().tex(V_minU, V_maxV).lightedAndEnd()

                    buffer.pos(minPos.getX + di, maxPos.getY + d1, z + d1).colored().tex(V_minU, V_minV).lightedAndEnd()
                    buffer.pos(minPos.getX + dn, maxPos.getY + d1, z + d1).colored().tex(V_maxU, V_minV).lightedAndEnd()
                    buffer.pos(minPos.getX + dn, maxPos.getY - d1, z + d1).colored().tex(V_maxU, V_maxV).lightedAndEnd()
                    buffer.pos(minPos.getX + di, maxPos.getY - d1, z + d1).colored().tex(V_minU, V_maxV).lightedAndEnd()
                }

                tempY(minPos.getY)
                tempY(maxPos.getY)
                tempZ(minPos.getZ)
                tempZ(maxPos.getZ)
            }
            //Y 4 LINES
            for (i <- 0 until subtract.getY) {
                val n = if (i == subtract.getY - 1) 1 - d1 * 2 else 1d

                // => Z
                val H_minU = spriteH.getMinU
                val H_minV = spriteH.getMinV
                val H_maxU = spriteH.getInterpolatedU(8)
                val H_maxV = spriteH.getInterpolatedV(8)

                buffer.pos(minPos.getX - d1, minPos.getY + d1 + i, minPos.getZ - d1).colored().tex(H_minU, H_minV).lightedAndEnd()
                buffer.pos(minPos.getX + d1, minPos.getY + d1 + i, minPos.getZ - d1).colored().tex(H_maxU, H_minV).lightedAndEnd()
                buffer.pos(minPos.getX + d1, minPos.getY + d1 + i + n, minPos.getZ - d1).colored().tex(H_maxU, H_maxV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, minPos.getY + d1 + i + n, minPos.getZ - d1).colored().tex(H_minU, H_maxV).lightedAndEnd()

                buffer.pos(maxPos.getX - d1, minPos.getY + d1 + i, minPos.getZ - d1).colored().tex(H_minU, H_minV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, minPos.getY + d1 + i, minPos.getZ - d1).colored().tex(H_maxU, H_minV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, minPos.getY + d1 + i + n, minPos.getZ - d1).colored().tex(H_maxU, H_maxV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, minPos.getY + d1 + i + n, minPos.getZ - d1).colored().tex(H_minU, H_maxV).lightedAndEnd()

                buffer.pos(minPos.getX - d1, minPos.getY + d1 + i, maxPos.getZ - d1).colored().tex(H_minU, H_minV).lightedAndEnd()
                buffer.pos(minPos.getX + d1, minPos.getY + d1 + i, maxPos.getZ - d1).colored().tex(H_maxU, H_minV).lightedAndEnd()
                buffer.pos(minPos.getX + d1, minPos.getY + d1 + i + n, maxPos.getZ - d1).colored().tex(H_maxU, H_maxV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, minPos.getY + d1 + i + n, maxPos.getZ - d1).colored().tex(H_minU, H_maxV).lightedAndEnd()

                buffer.pos(maxPos.getX - d1, minPos.getY + d1 + i, maxPos.getZ - d1).colored().tex(H_minU, H_minV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, minPos.getY + d1 + i, maxPos.getZ - d1).colored().tex(H_maxU, H_minV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, minPos.getY + d1 + i + n, maxPos.getZ - d1).colored().tex(H_maxU, H_maxV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, minPos.getY + d1 + i + n, maxPos.getZ - d1).colored().tex(H_minU, H_maxV).lightedAndEnd()

                buffer.pos(minPos.getX - d1, minPos.getY + d1 + i, minPos.getZ + d1).colored().tex(H_minU, H_minV).lightedAndEnd()
                buffer.pos(minPos.getX + d1, minPos.getY + d1 + i, minPos.getZ + d1).colored().tex(H_maxU, H_minV).lightedAndEnd()
                buffer.pos(minPos.getX + d1, minPos.getY + d1 + i + n, minPos.getZ + d1).colored().tex(H_maxU, H_maxV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, minPos.getY + d1 + i + n, minPos.getZ + d1).colored().tex(H_minU, H_maxV).lightedAndEnd()

                buffer.pos(maxPos.getX - d1, minPos.getY + d1 + i, minPos.getZ + d1).colored().tex(H_minU, H_minV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, minPos.getY + d1 + i, minPos.getZ + d1).colored().tex(H_maxU, H_minV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, minPos.getY + d1 + i + n, minPos.getZ + d1).colored().tex(H_maxU, H_maxV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, minPos.getY + d1 + i + n, minPos.getZ + d1).colored().tex(H_minU, H_maxV).lightedAndEnd()

                buffer.pos(minPos.getX - d1, minPos.getY + d1 + i, maxPos.getZ + d1).colored().tex(H_minU, H_minV).lightedAndEnd()
                buffer.pos(minPos.getX + d1, minPos.getY + d1 + i, maxPos.getZ + d1).colored().tex(H_maxU, H_minV).lightedAndEnd()
                buffer.pos(minPos.getX + d1, minPos.getY + d1 + i + n, maxPos.getZ + d1).colored().tex(H_maxU, H_maxV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, minPos.getY + d1 + i + n, maxPos.getZ + d1).colored().tex(H_minU, H_maxV).lightedAndEnd()

                buffer.pos(maxPos.getX - d1, minPos.getY + d1 + i, maxPos.getZ + d1).colored().tex(H_minU, H_minV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, minPos.getY + d1 + i, maxPos.getZ + d1).colored().tex(H_maxU, H_minV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, minPos.getY + d1 + i + n, maxPos.getZ + d1).colored().tex(H_maxU, H_maxV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, minPos.getY + d1 + i + n, maxPos.getZ + d1).colored().tex(H_minU, H_maxV).lightedAndEnd()

                // => X
                buffer.pos(minPos.getX - d1, minPos.getY + d1 + i, minPos.getZ - d1).colored().tex(H_minU, H_minV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, minPos.getY + d1 + i, minPos.getZ + d1).colored().tex(H_maxU, H_minV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, minPos.getY + d1 + i + n, minPos.getZ + d1).colored().tex(H_maxU, H_maxV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, minPos.getY + d1 + i + n, minPos.getZ - d1).colored().tex(H_minU, H_maxV).lightedAndEnd()

                buffer.pos(maxPos.getX - d1, minPos.getY + d1 + i, minPos.getZ - d1).colored().tex(H_minU, H_minV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, minPos.getY + d1 + i, minPos.getZ + d1).colored().tex(H_maxU, H_minV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, minPos.getY + d1 + i + n, minPos.getZ + d1).colored().tex(H_maxU, H_maxV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, minPos.getY + d1 + i + n, minPos.getZ - d1).colored().tex(H_minU, H_maxV).lightedAndEnd()

                buffer.pos(minPos.getX - d1, minPos.getY + d1 + i, maxPos.getZ - d1).colored().tex(H_minU, H_minV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, minPos.getY + d1 + i, maxPos.getZ + d1).colored().tex(H_maxU, H_minV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, minPos.getY + d1 + i + n, maxPos.getZ + d1).colored().tex(H_maxU, H_maxV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, minPos.getY + d1 + i + n, maxPos.getZ - d1).colored().tex(H_minU, H_maxV).lightedAndEnd()

                buffer.pos(maxPos.getX - d1, minPos.getY + d1 + i, maxPos.getZ - d1).colored().tex(H_minU, H_minV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, minPos.getY + d1 + i, maxPos.getZ + d1).colored().tex(H_maxU, H_minV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, minPos.getY + d1 + i + n, maxPos.getZ + d1).colored().tex(H_maxU, H_maxV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, minPos.getY + d1 + i + n, maxPos.getZ - d1).colored().tex(H_minU, H_maxV).lightedAndEnd()


                buffer.pos(minPos.getX + d1, minPos.getY + d1 + i, minPos.getZ - d1).colored().tex(H_minU, H_minV).lightedAndEnd()
                buffer.pos(minPos.getX + d1, minPos.getY + d1 + i, minPos.getZ + d1).colored().tex(H_maxU, H_minV).lightedAndEnd()
                buffer.pos(minPos.getX + d1, minPos.getY + d1 + i + n, minPos.getZ + d1).colored().tex(H_maxU, H_maxV).lightedAndEnd()
                buffer.pos(minPos.getX + d1, minPos.getY + d1 + i + n, minPos.getZ - d1).colored().tex(H_minU, H_maxV).lightedAndEnd()

                buffer.pos(maxPos.getX + d1, minPos.getY + d1 + i, minPos.getZ - d1).colored().tex(H_minU, H_minV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, minPos.getY + d1 + i, minPos.getZ + d1).colored().tex(H_maxU, H_minV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, minPos.getY + d1 + i + n, minPos.getZ + d1).colored().tex(H_maxU, H_maxV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, minPos.getY + d1 + i + n, minPos.getZ - d1).colored().tex(H_minU, H_maxV).lightedAndEnd()

                buffer.pos(minPos.getX + d1, minPos.getY + d1 + i, maxPos.getZ - d1).colored().tex(H_minU, H_minV).lightedAndEnd()
                buffer.pos(minPos.getX + d1, minPos.getY + d1 + i, maxPos.getZ + d1).colored().tex(H_maxU, H_minV).lightedAndEnd()
                buffer.pos(minPos.getX + d1, minPos.getY + d1 + i + n, maxPos.getZ + d1).colored().tex(H_maxU, H_maxV).lightedAndEnd()
                buffer.pos(minPos.getX + d1, minPos.getY + d1 + i + n, maxPos.getZ - d1).colored().tex(H_minU, H_maxV).lightedAndEnd()

                buffer.pos(maxPos.getX + d1, minPos.getY + d1 + i, maxPos.getZ - d1).colored().tex(H_minU, H_minV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, minPos.getY + d1 + i, maxPos.getZ + d1).colored().tex(H_maxU, H_minV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, minPos.getY + d1 + i + n, maxPos.getZ + d1).colored().tex(H_maxU, H_maxV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, minPos.getY + d1 + i + n, maxPos.getZ - d1).colored().tex(H_minU, H_maxV).lightedAndEnd()
            }
            //Z 4 LINES
            for (i <- 0 until subtract.getZ) {
                val n = if (i == subtract.getZ - 1) 1 - d1 * 2 else 1d

                def tempY(y: Double): Unit = {
                    buffer.pos(minPos.getX - d1, y - d1, minPos.getZ + d1 + i).colored().tex(V_minU, V_minV).lightedAndEnd()
                    buffer.pos(minPos.getX - d1, y - d1, minPos.getZ + d1 + i + n).colored().tex(V_maxU, V_minV).lightedAndEnd()
                    buffer.pos(minPos.getX + d1, y - d1, minPos.getZ + d1 + i + n).colored().tex(V_maxU, V_maxV).lightedAndEnd()
                    buffer.pos(minPos.getX + d1, y - d1, minPos.getZ + d1 + i).colored().tex(V_minU, V_maxV).lightedAndEnd()

                    buffer.pos(maxPos.getX - d1, y - d1, minPos.getZ + d1 + i).colored().tex(V_minU, V_minV).lightedAndEnd()
                    buffer.pos(maxPos.getX - d1, y - d1, minPos.getZ + d1 + i + n).colored().tex(V_maxU, V_minV).lightedAndEnd()
                    buffer.pos(maxPos.getX + d1, y - d1, minPos.getZ + d1 + i + n).colored().tex(V_maxU, V_maxV).lightedAndEnd()
                    buffer.pos(maxPos.getX + d1, y - d1, minPos.getZ + d1 + i).colored().tex(V_minU, V_maxV).lightedAndEnd()

                    buffer.pos(minPos.getX - d1, y + d1, minPos.getZ + d1 + i).colored().tex(V_minU, V_minV).lightedAndEnd()
                    buffer.pos(minPos.getX - d1, y + d1, minPos.getZ + d1 + i + n).colored().tex(V_maxU, V_minV).lightedAndEnd()
                    buffer.pos(minPos.getX + d1, y + d1, minPos.getZ + d1 + i + n).colored().tex(V_maxU, V_maxV).lightedAndEnd()
                    buffer.pos(minPos.getX + d1, y + d1, minPos.getZ + d1 + i).colored().tex(V_minU, V_maxV).lightedAndEnd()

                    buffer.pos(maxPos.getX - d1, y + d1, minPos.getZ + d1 + i).colored().tex(V_minU, V_minV).lightedAndEnd()
                    buffer.pos(maxPos.getX - d1, y + d1, minPos.getZ + d1 + i + n).colored().tex(V_maxU, V_minV).lightedAndEnd()
                    buffer.pos(maxPos.getX + d1, y + d1, minPos.getZ + d1 + i + n).colored().tex(V_maxU, V_maxV).lightedAndEnd()
                    buffer.pos(maxPos.getX + d1, y + d1, minPos.getZ + d1 + i).colored().tex(V_minU, V_maxV).lightedAndEnd()
                }

                def tempX(x: Double): Unit = {
                    buffer.pos(x - d1, minPos.getY + d1, minPos.getZ + d1 + i).colored().tex(V_minU, V_minV).lightedAndEnd()
                    buffer.pos(x - d1, minPos.getY + d1, minPos.getZ + d1 + i + n).colored().tex(V_maxU, V_minV).lightedAndEnd()
                    buffer.pos(x - d1, minPos.getY - d1, minPos.getZ + d1 + i + n).colored().tex(V_maxU, V_maxV).lightedAndEnd()
                    buffer.pos(x - d1, minPos.getY - d1, minPos.getZ + d1 + i).colored().tex(V_minU, V_maxV).lightedAndEnd()

                    buffer.pos(x - d1, maxPos.getY + d1, minPos.getZ + d1 + i).colored().tex(V_minU, V_minV).lightedAndEnd()
                    buffer.pos(x - d1, maxPos.getY + d1, minPos.getZ + d1 + i + n).colored().tex(V_maxU, V_minV).lightedAndEnd()
                    buffer.pos(x - d1, maxPos.getY - d1, minPos.getZ + d1 + i + n).colored().tex(V_maxU, V_maxV).lightedAndEnd()
                    buffer.pos(x - d1, maxPos.getY - d1, minPos.getZ + d1 + i).colored().tex(V_minU, V_maxV).lightedAndEnd()

                    buffer.pos(x + d1, minPos.getY + d1, minPos.getZ + d1 + i).colored().tex(V_minU, V_minV).lightedAndEnd()
                    buffer.pos(x + d1, minPos.getY + d1, minPos.getZ + d1 + i + n).colored().tex(V_maxU, V_minV).lightedAndEnd()
                    buffer.pos(x + d1, minPos.getY - d1, minPos.getZ + d1 + i + n).colored().tex(V_maxU, V_maxV).lightedAndEnd()
                    buffer.pos(x + d1, minPos.getY - d1, minPos.getZ + d1 + i).colored().tex(V_minU, V_maxV).lightedAndEnd()

                    buffer.pos(x + d1, maxPos.getY + d1, minPos.getZ + d1 + i).colored().tex(V_minU, V_minV).lightedAndEnd()
                    buffer.pos(x + d1, maxPos.getY + d1, minPos.getZ + d1 + i + n).colored().tex(V_maxU, V_minV).lightedAndEnd()
                    buffer.pos(x + d1, maxPos.getY - d1, minPos.getZ + d1 + i + n).colored().tex(V_maxU, V_maxV).lightedAndEnd()
                    buffer.pos(x + d1, maxPos.getY - d1, minPos.getZ + d1 + i).colored().tex(V_minU, V_maxV).lightedAndEnd()
                }

                tempY(minPos.getY)
                tempY(maxPos.getY)
                tempX(minPos.getX)
                tempX(maxPos.getX)
            }

            def renderBox(): Unit = {
                val B_minU = boxStripe.getMinU
                val B_minV = boxStripe.getMinV
                val B_maxU = boxStripe.getMaxU
                val B_maxV = boxStripe.getMaxV
                //z
                buffer.pos(minPos.getX + d1, minPos.getY + d1, minPos.getZ - d1).colored().tex(B_minU, B_minV).lightedAndEnd()
                buffer.pos(minPos.getX + d1, minPos.getY - d1, minPos.getZ - d1).colored().tex(B_maxU, B_minV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, minPos.getY - d1, minPos.getZ - d1).colored().tex(B_maxU, B_maxV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, minPos.getY + d1, minPos.getZ - d1).colored().tex(B_minU, B_maxV).lightedAndEnd()

                buffer.pos(maxPos.getX + d1, minPos.getY + d1, minPos.getZ - d1).colored().tex(B_minU, B_minV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, minPos.getY - d1, minPos.getZ - d1).colored().tex(B_maxU, B_minV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, minPos.getY - d1, minPos.getZ - d1).colored().tex(B_maxU, B_maxV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, minPos.getY + d1, minPos.getZ - d1).colored().tex(B_minU, B_maxV).lightedAndEnd()

                buffer.pos(minPos.getX + d1, minPos.getY + d1, maxPos.getZ + d1).colored().tex(B_minU, B_minV).lightedAndEnd()
                buffer.pos(minPos.getX + d1, minPos.getY - d1, maxPos.getZ + d1).colored().tex(B_maxU, B_minV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, minPos.getY - d1, maxPos.getZ + d1).colored().tex(B_maxU, B_maxV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, minPos.getY + d1, maxPos.getZ + d1).colored().tex(B_minU, B_maxV).lightedAndEnd()

                buffer.pos(maxPos.getX + d1, minPos.getY + d1, maxPos.getZ + d1).colored().tex(B_minU, B_minV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, minPos.getY - d1, maxPos.getZ + d1).colored().tex(B_maxU, B_minV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, minPos.getY - d1, maxPos.getZ + d1).colored().tex(B_maxU, B_maxV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, minPos.getY + d1, maxPos.getZ + d1).colored().tex(B_minU, B_maxV).lightedAndEnd()

                buffer.pos(minPos.getX + d1, maxPos.getY + d1, minPos.getZ - d1).colored().tex(B_minU, B_minV).lightedAndEnd()
                buffer.pos(minPos.getX + d1, maxPos.getY - d1, minPos.getZ - d1).colored().tex(B_maxU, B_minV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, maxPos.getY - d1, minPos.getZ - d1).colored().tex(B_maxU, B_maxV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, maxPos.getY + d1, minPos.getZ - d1).colored().tex(B_minU, B_maxV).lightedAndEnd()

                buffer.pos(maxPos.getX + d1, maxPos.getY + d1, minPos.getZ - d1).colored().tex(B_minU, B_minV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, maxPos.getY - d1, minPos.getZ - d1).colored().tex(B_maxU, B_minV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, maxPos.getY - d1, minPos.getZ - d1).colored().tex(B_maxU, B_maxV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, maxPos.getY + d1, minPos.getZ - d1).colored().tex(B_minU, B_maxV).lightedAndEnd()

                buffer.pos(minPos.getX + d1, maxPos.getY + d1, maxPos.getZ + d1).colored().tex(B_minU, B_minV).lightedAndEnd()
                buffer.pos(minPos.getX + d1, maxPos.getY - d1, maxPos.getZ + d1).colored().tex(B_maxU, B_minV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, maxPos.getY - d1, maxPos.getZ + d1).colored().tex(B_maxU, B_maxV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, maxPos.getY + d1, maxPos.getZ + d1).colored().tex(B_minU, B_maxV).lightedAndEnd()

                buffer.pos(maxPos.getX + d1, maxPos.getY + d1, maxPos.getZ + d1).colored().tex(B_minU, B_minV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, maxPos.getY - d1, maxPos.getZ + d1).colored().tex(B_maxU, B_minV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, maxPos.getY - d1, maxPos.getZ + d1).colored().tex(B_maxU, B_maxV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, maxPos.getY + d1, maxPos.getZ + d1).colored().tex(B_minU, B_maxV).lightedAndEnd()

                //x
                buffer.pos(minPos.getX - d1, minPos.getY + d1, minPos.getZ + d1).colored().tex(B_minU, B_minV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, minPos.getY - d1, minPos.getZ + d1).colored().tex(B_maxU, B_minV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, minPos.getY - d1, minPos.getZ - d1).colored().tex(B_maxU, B_maxV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, minPos.getY + d1, minPos.getZ - d1).colored().tex(B_minU, B_maxV).lightedAndEnd()

                buffer.pos(minPos.getX - d1, minPos.getY + d1, maxPos.getZ + d1).colored().tex(B_minU, B_minV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, minPos.getY - d1, maxPos.getZ + d1).colored().tex(B_maxU, B_minV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, minPos.getY - d1, maxPos.getZ - d1).colored().tex(B_maxU, B_maxV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, minPos.getY + d1, maxPos.getZ - d1).colored().tex(B_minU, B_maxV).lightedAndEnd()

                buffer.pos(minPos.getX - d1, maxPos.getY + d1, minPos.getZ + d1).colored().tex(B_minU, B_minV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, maxPos.getY - d1, minPos.getZ + d1).colored().tex(B_maxU, B_minV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, maxPos.getY - d1, minPos.getZ - d1).colored().tex(B_maxU, B_maxV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, maxPos.getY + d1, minPos.getZ - d1).colored().tex(B_minU, B_maxV).lightedAndEnd()

                buffer.pos(minPos.getX - d1, maxPos.getY + d1, maxPos.getZ + d1).colored().tex(B_minU, B_minV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, maxPos.getY - d1, maxPos.getZ + d1).colored().tex(B_maxU, B_minV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, maxPos.getY - d1, maxPos.getZ - d1).colored().tex(B_maxU, B_maxV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, maxPos.getY + d1, maxPos.getZ - d1).colored().tex(B_minU, B_maxV).lightedAndEnd()

                buffer.pos(maxPos.getX + d1, minPos.getY + d1, minPos.getZ + d1).colored().tex(B_minU, B_minV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, minPos.getY - d1, minPos.getZ + d1).colored().tex(B_maxU, B_minV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, minPos.getY - d1, minPos.getZ - d1).colored().tex(B_maxU, B_maxV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, minPos.getY + d1, minPos.getZ - d1).colored().tex(B_minU, B_maxV).lightedAndEnd()

                buffer.pos(maxPos.getX + d1, minPos.getY + d1, maxPos.getZ + d1).colored().tex(B_minU, B_minV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, minPos.getY - d1, maxPos.getZ + d1).colored().tex(B_maxU, B_minV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, minPos.getY - d1, maxPos.getZ - d1).colored().tex(B_maxU, B_maxV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, minPos.getY + d1, maxPos.getZ - d1).colored().tex(B_minU, B_maxV).lightedAndEnd()

                buffer.pos(maxPos.getX + d1, maxPos.getY + d1, minPos.getZ + d1).colored().tex(B_minU, B_minV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, maxPos.getY - d1, minPos.getZ + d1).colored().tex(B_maxU, B_minV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, maxPos.getY - d1, minPos.getZ - d1).colored().tex(B_maxU, B_maxV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, maxPos.getY + d1, minPos.getZ - d1).colored().tex(B_minU, B_maxV).lightedAndEnd()

                buffer.pos(maxPos.getX + d1, maxPos.getY + d1, maxPos.getZ + d1).colored().tex(B_minU, B_minV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, maxPos.getY - d1, maxPos.getZ + d1).colored().tex(B_maxU, B_minV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, maxPos.getY - d1, maxPos.getZ - d1).colored().tex(B_maxU, B_maxV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, maxPos.getY + d1, maxPos.getZ - d1).colored().tex(B_minU, B_maxV).lightedAndEnd()

                //y
                buffer.pos(minPos.getX + d1, minPos.getY - d1, minPos.getZ + d1).colored().tex(B_minU, B_minV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, minPos.getY - d1, minPos.getZ + d1).colored().tex(B_maxU, B_minV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, minPos.getY - d1, minPos.getZ - d1).colored().tex(B_maxU, B_maxV).lightedAndEnd()
                buffer.pos(minPos.getX + d1, minPos.getY - d1, minPos.getZ - d1).colored().tex(B_minU, B_maxV).lightedAndEnd()

                buffer.pos(maxPos.getX + d1, minPos.getY - d1, minPos.getZ + d1).colored().tex(B_minU, B_minV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, minPos.getY - d1, minPos.getZ + d1).colored().tex(B_maxU, B_minV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, minPos.getY - d1, minPos.getZ - d1).colored().tex(B_maxU, B_maxV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, minPos.getY - d1, minPos.getZ - d1).colored().tex(B_minU, B_maxV).lightedAndEnd()

                buffer.pos(minPos.getX + d1, minPos.getY - d1, maxPos.getZ + d1).colored().tex(B_minU, B_minV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, minPos.getY - d1, maxPos.getZ + d1).colored().tex(B_maxU, B_minV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, minPos.getY - d1, maxPos.getZ - d1).colored().tex(B_maxU, B_maxV).lightedAndEnd()
                buffer.pos(minPos.getX + d1, minPos.getY - d1, maxPos.getZ - d1).colored().tex(B_minU, B_maxV).lightedAndEnd()

                buffer.pos(maxPos.getX + d1, minPos.getY - d1, maxPos.getZ + d1).colored().tex(B_minU, B_minV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, minPos.getY - d1, maxPos.getZ + d1).colored().tex(B_maxU, B_minV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, minPos.getY - d1, maxPos.getZ - d1).colored().tex(B_maxU, B_maxV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, minPos.getY - d1, maxPos.getZ - d1).colored().tex(B_minU, B_maxV).lightedAndEnd()

                buffer.pos(minPos.getX + d1, maxPos.getY + d1, minPos.getZ + d1).colored().tex(B_minU, B_minV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, maxPos.getY + d1, minPos.getZ + d1).colored().tex(B_maxU, B_minV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, maxPos.getY + d1, minPos.getZ - d1).colored().tex(B_maxU, B_maxV).lightedAndEnd()
                buffer.pos(minPos.getX + d1, maxPos.getY + d1, minPos.getZ - d1).colored().tex(B_minU, B_maxV).lightedAndEnd()

                buffer.pos(maxPos.getX + d1, maxPos.getY + d1, minPos.getZ + d1).colored().tex(B_minU, B_minV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, maxPos.getY + d1, minPos.getZ + d1).colored().tex(B_maxU, B_minV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, maxPos.getY + d1, minPos.getZ - d1).colored().tex(B_maxU, B_maxV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, maxPos.getY + d1, minPos.getZ - d1).colored().tex(B_minU, B_maxV).lightedAndEnd()

                buffer.pos(minPos.getX + d1, maxPos.getY + d1, maxPos.getZ + d1).colored().tex(B_minU, B_minV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, maxPos.getY + d1, maxPos.getZ + d1).colored().tex(B_maxU, B_minV).lightedAndEnd()
                buffer.pos(minPos.getX - d1, maxPos.getY + d1, maxPos.getZ - d1).colored().tex(B_maxU, B_maxV).lightedAndEnd()
                buffer.pos(minPos.getX + d1, maxPos.getY + d1, maxPos.getZ - d1).colored().tex(B_minU, B_maxV).lightedAndEnd()

                buffer.pos(maxPos.getX + d1, maxPos.getY + d1, maxPos.getZ + d1).colored().tex(B_minU, B_minV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, maxPos.getY + d1, maxPos.getZ + d1).colored().tex(B_maxU, B_minV).lightedAndEnd()
                buffer.pos(maxPos.getX - d1, maxPos.getY + d1, maxPos.getZ - d1).colored().tex(B_maxU, B_maxV).lightedAndEnd()
                buffer.pos(maxPos.getX + d1, maxPos.getY + d1, maxPos.getZ - d1).colored().tex(B_minU, B_maxV).lightedAndEnd()
            }

            renderBox()
            Minecraft.getMinecraft.mcProfiler.endSection()
        }

        if (quarry.G_getNow() == TileQuarry.Mode.BREAKBLOCK || quarry.G_getNow() == TileQuarry.Mode.MOVEHEAD) {
            Minecraft.getMinecraft.mcProfiler.startSection("drill")
            buffer.setTranslation(distanceX - pos.getX + .5, distanceY - pos.getY + .5, distanceZ - pos.getZ + .5)
            //render crossed frame
            val D_minU = drillStripe.getMinU
            val D_minV = drillStripe.getMinV
            val D_maxU = drillStripe.getInterpolatedU(8)
            val D_maxV = drillStripe.getMaxV

            def xLine(plus: Boolean, floor: Int, length: Double): Unit = {
                val f = if (plus) plusF else minusF

                for (i1 <- 0 until floor) {
                    val i2 = i1 + 1
                    buffer.pos(f(quarry.headPosX, d4 + i1), quarry.yMax + d4, quarry.headPosZ - d4).colored().tex(D_minU, D_minV).lightedAndEnd()
                    buffer.pos(f(quarry.headPosX, d4 + i1), quarry.yMax + d4, quarry.headPosZ + d4).colored().tex(D_maxU, D_minV).lightedAndEnd()
                    buffer.pos(f(quarry.headPosX, d4 + i2), quarry.yMax + d4, quarry.headPosZ + d4).colored().tex(D_maxU, D_maxV).lightedAndEnd()
                    buffer.pos(f(quarry.headPosX, d4 + i2), quarry.yMax + d4, quarry.headPosZ - d4).colored().tex(D_minU, D_maxV).lightedAndEnd()

                    buffer.pos(f(quarry.headPosX, d4 + i1), quarry.yMax - d4, quarry.headPosZ - d4).colored().tex(D_minU, D_minV).lightedAndEnd()
                    buffer.pos(f(quarry.headPosX, d4 + i1), quarry.yMax - d4, quarry.headPosZ + d4).colored().tex(D_maxU, D_minV).lightedAndEnd()
                    buffer.pos(f(quarry.headPosX, d4 + i2), quarry.yMax - d4, quarry.headPosZ + d4).colored().tex(D_maxU, D_maxV).lightedAndEnd()
                    buffer.pos(f(quarry.headPosX, d4 + i2), quarry.yMax - d4, quarry.headPosZ - d4).colored().tex(D_minU, D_maxV).lightedAndEnd()

                    buffer.pos(f(quarry.headPosX, d4 + i2), quarry.yMax + d4, quarry.headPosZ - d4).colored().tex(D_maxU, D_maxV).lightedAndEnd()
                    buffer.pos(f(quarry.headPosX, d4 + i2), quarry.yMax - d4, quarry.headPosZ - d4).colored().tex(D_minU, D_maxV).lightedAndEnd()
                    buffer.pos(f(quarry.headPosX, d4 + i1), quarry.yMax - d4, quarry.headPosZ - d4).colored().tex(D_minU, D_minV).lightedAndEnd()
                    buffer.pos(f(quarry.headPosX, d4 + i1), quarry.yMax + d4, quarry.headPosZ - d4).colored().tex(D_maxU, D_minV).lightedAndEnd()

                    buffer.pos(f(quarry.headPosX, d4 + i2), quarry.yMax + d4, quarry.headPosZ + d4).colored().tex(D_maxU, D_maxV).lightedAndEnd()
                    buffer.pos(f(quarry.headPosX, d4 + i2), quarry.yMax - d4, quarry.headPosZ + d4).colored().tex(D_minU, D_maxV).lightedAndEnd()
                    buffer.pos(f(quarry.headPosX, d4 + i1), quarry.yMax - d4, quarry.headPosZ + d4).colored().tex(D_minU, D_minV).lightedAndEnd()
                    buffer.pos(f(quarry.headPosX, d4 + i1), quarry.yMax + d4, quarry.headPosZ + d4).colored().tex(D_maxU, D_minV).lightedAndEnd()
                }
                val fixedV = drillStripe.getInterpolatedV((length - floor) * 16)
                buffer.pos(f(quarry.headPosX, d4 + floor), quarry.yMax + d4, quarry.headPosZ - d4).colored().tex(D_minU, D_minV).lightedAndEnd()
                buffer.pos(f(quarry.headPosX, d4 + floor), quarry.yMax + d4, quarry.headPosZ + d4).colored().tex(D_maxU, D_minV).lightedAndEnd()
                buffer.pos(f(quarry.headPosX, d4 + length), quarry.yMax + d4, quarry.headPosZ + d4).colored().tex(D_maxU, fixedV).lightedAndEnd()
                buffer.pos(f(quarry.headPosX, d4 + length), quarry.yMax + d4, quarry.headPosZ - d4).colored().tex(D_minU, fixedV).lightedAndEnd()

                buffer.pos(f(quarry.headPosX, d4 + floor), quarry.yMax - d4, quarry.headPosZ - d4).colored().tex(D_minU, D_minV).lightedAndEnd()
                buffer.pos(f(quarry.headPosX, d4 + floor), quarry.yMax - d4, quarry.headPosZ + d4).colored().tex(D_maxU, D_minV).lightedAndEnd()
                buffer.pos(f(quarry.headPosX, d4 + length), quarry.yMax - d4, quarry.headPosZ + d4).colored().tex(D_maxU, fixedV).lightedAndEnd()
                buffer.pos(f(quarry.headPosX, d4 + length), quarry.yMax - d4, quarry.headPosZ - d4).colored().tex(D_minU, fixedV).lightedAndEnd()

                buffer.pos(f(quarry.headPosX, d4 + length), quarry.yMax + d4, quarry.headPosZ - d4).colored().tex(D_maxU, fixedV).lightedAndEnd()
                buffer.pos(f(quarry.headPosX, d4 + length), quarry.yMax - d4, quarry.headPosZ - d4).colored().tex(D_minU, fixedV).lightedAndEnd()
                buffer.pos(f(quarry.headPosX, d4 + floor), quarry.yMax - d4, quarry.headPosZ - d4).colored().tex(D_minU, D_minV).lightedAndEnd()
                buffer.pos(f(quarry.headPosX, d4 + floor), quarry.yMax + d4, quarry.headPosZ - d4).colored().tex(D_maxU, D_minV).lightedAndEnd()

                buffer.pos(f(quarry.headPosX, d4 + length), quarry.yMax + d4, quarry.headPosZ + d4).colored().tex(D_maxU, fixedV).lightedAndEnd()
                buffer.pos(f(quarry.headPosX, d4 + length), quarry.yMax - d4, quarry.headPosZ + d4).colored().tex(D_minU, fixedV).lightedAndEnd()
                buffer.pos(f(quarry.headPosX, d4 + floor), quarry.yMax - d4, quarry.headPosZ + d4).colored().tex(D_minU, D_minV).lightedAndEnd()
                buffer.pos(f(quarry.headPosX, d4 + floor), quarry.yMax + d4, quarry.headPosZ + d4).colored().tex(D_maxU, D_minV).lightedAndEnd()

            }
            //X lines

            //positive(East)
            val xp_length = quarry.xMax - quarry.headPosX - d4 * 2
            val xp_floor = MathHelper.floor(xp_length)
            xLine(plus = true, xp_floor, xp_length)

            //negative(West)
            val xn_length = quarry.headPosX - quarry.xMin - d4 * 2
            val xn_floor = MathHelper.floor(xn_length)
            xLine(plus = false, xn_floor, xn_length)


            def zLine(plus: Boolean, floor: Int, length: Double): Unit = {
                val f = if (plus) plusF else minusF
                for (i1 <- Range(0, floor)) {
                    val i2 = i1 + 1
                    buffer.pos(quarry.headPosX - d4, quarry.yMax + d4, f(quarry.headPosZ, d4 + i1)).colored().tex(D_minU, D_minV).lightedAndEnd()
                    buffer.pos(quarry.headPosX + d4, quarry.yMax + d4, f(quarry.headPosZ, d4 + i1)).colored().tex(D_maxU, D_minV).lightedAndEnd()
                    buffer.pos(quarry.headPosX + d4, quarry.yMax + d4, f(quarry.headPosZ, d4 + i2)).colored().tex(D_maxU, D_maxV).lightedAndEnd()
                    buffer.pos(quarry.headPosX - d4, quarry.yMax + d4, f(quarry.headPosZ, d4 + i2)).colored().tex(D_minU, D_maxV).lightedAndEnd()

                    buffer.pos(quarry.headPosX - d4, quarry.yMax - d4, f(quarry.headPosZ, d4 + i1)).colored().tex(D_minU, D_minV).lightedAndEnd()
                    buffer.pos(quarry.headPosX + d4, quarry.yMax - d4, f(quarry.headPosZ, d4 + i1)).colored().tex(D_maxU, D_minV).lightedAndEnd()
                    buffer.pos(quarry.headPosX + d4, quarry.yMax - d4, f(quarry.headPosZ, d4 + i2)).colored().tex(D_maxU, D_maxV).lightedAndEnd()
                    buffer.pos(quarry.headPosX - d4, quarry.yMax - d4, f(quarry.headPosZ, d4 + i2)).colored().tex(D_minU, D_maxV).lightedAndEnd()

                    buffer.pos(quarry.headPosX - d4, quarry.yMax + d4, f(quarry.headPosZ, d4 + i2)).colored().tex(D_maxU, D_maxV).lightedAndEnd()
                    buffer.pos(quarry.headPosX - d4, quarry.yMax - d4, f(quarry.headPosZ, d4 + i2)).colored().tex(D_minU, D_maxV).lightedAndEnd()
                    buffer.pos(quarry.headPosX - d4, quarry.yMax - d4, f(quarry.headPosZ, d4 + i1)).colored().tex(D_minU, D_minV).lightedAndEnd()
                    buffer.pos(quarry.headPosX - d4, quarry.yMax + d4, f(quarry.headPosZ, d4 + i1)).colored().tex(D_maxU, D_minV).lightedAndEnd()

                    buffer.pos(quarry.headPosX + d4, quarry.yMax + d4, f(quarry.headPosZ, d4 + i2)).colored().tex(D_maxU, D_maxV).lightedAndEnd()
                    buffer.pos(quarry.headPosX + d4, quarry.yMax - d4, f(quarry.headPosZ, d4 + i2)).colored().tex(D_minU, D_maxV).lightedAndEnd()
                    buffer.pos(quarry.headPosX + d4, quarry.yMax - d4, f(quarry.headPosZ, d4 + i1)).colored().tex(D_minU, D_minV).lightedAndEnd()
                    buffer.pos(quarry.headPosX + d4, quarry.yMax + d4, f(quarry.headPosZ, d4 + i1)).colored().tex(D_maxU, D_minV).lightedAndEnd()
                }
                val fixedV = drillStripe.getInterpolatedV((length - floor) * 16)
                buffer.pos(quarry.headPosX - d4, quarry.yMax + d4, f(quarry.headPosZ, d4 + floor)).colored().tex(D_minU, D_minV).lightedAndEnd()
                buffer.pos(quarry.headPosX + d4, quarry.yMax + d4, f(quarry.headPosZ, d4 + floor)).colored().tex(D_maxU, D_minV).lightedAndEnd()
                buffer.pos(quarry.headPosX + d4, quarry.yMax + d4, f(quarry.headPosZ, d4 + length)).colored().tex(D_maxU, fixedV).lightedAndEnd()
                buffer.pos(quarry.headPosX - d4, quarry.yMax + d4, f(quarry.headPosZ, d4 + length)).colored().tex(D_minU, fixedV).lightedAndEnd()

                buffer.pos(quarry.headPosX - d4, quarry.yMax - d4, f(quarry.headPosZ, d4 + floor)).colored().tex(D_minU, D_minV).lightedAndEnd()
                buffer.pos(quarry.headPosX + d4, quarry.yMax - d4, f(quarry.headPosZ, d4 + floor)).colored().tex(D_maxU, D_minV).lightedAndEnd()
                buffer.pos(quarry.headPosX + d4, quarry.yMax - d4, f(quarry.headPosZ, d4 + length)).colored().tex(D_maxU, fixedV).lightedAndEnd()
                buffer.pos(quarry.headPosX - d4, quarry.yMax - d4, f(quarry.headPosZ, d4 + length)).colored().tex(D_minU, fixedV).lightedAndEnd()

                buffer.pos(quarry.headPosX - d4, quarry.yMax + d4, f(quarry.headPosZ, d4 + length)).colored().tex(D_maxU, fixedV).lightedAndEnd()
                buffer.pos(quarry.headPosX - d4, quarry.yMax - d4, f(quarry.headPosZ, d4 + length)).colored().tex(D_minU, fixedV).lightedAndEnd()
                buffer.pos(quarry.headPosX - d4, quarry.yMax - d4, f(quarry.headPosZ, d4 + floor)).colored().tex(D_minU, D_minV).lightedAndEnd()
                buffer.pos(quarry.headPosX - d4, quarry.yMax + d4, f(quarry.headPosZ, d4 + floor)).colored().tex(D_maxU, D_minV).lightedAndEnd()

                buffer.pos(quarry.headPosX + d4, quarry.yMax + d4, f(quarry.headPosZ, d4 + length)).colored().tex(D_maxU, fixedV).lightedAndEnd()
                buffer.pos(quarry.headPosX + d4, quarry.yMax - d4, f(quarry.headPosZ, d4 + length)).colored().tex(D_minU, fixedV).lightedAndEnd()
                buffer.pos(quarry.headPosX + d4, quarry.yMax - d4, f(quarry.headPosZ, d4 + floor)).colored().tex(D_minU, D_minV).lightedAndEnd()
                buffer.pos(quarry.headPosX + d4, quarry.yMax + d4, f(quarry.headPosZ, d4 + floor)).colored().tex(D_maxU, D_minV).lightedAndEnd()

            }
            //Z lines

            //positive(South)
            val zp_length = quarry.zMax - quarry.headPosZ - d4 * 2
            val zp_floor = MathHelper.floor(zp_length)
            zLine(plus = true, zp_floor, zp_length)
            //negative(North)
            val zn_length = quarry.headPosZ - quarry.zMin - d4 * 2
            val zn_floor = MathHelper.floor(zn_length)
            zLine(plus = false, zn_floor, zn_length)

            //render drill
            def yLine(floor: Int, length: Double): Unit = {
                val D_I8dV = drillStripe.getInterpolatedV(8d)
                val D_16dU = drillStripe.getMaxU
                //Top
                buffer.pos(quarry.headPosX - d4, quarry.yMax + d4 + .5, quarry.headPosZ - d4).colored().tex(D_minU, D_I8dV).lightedAndEnd()
                buffer.pos(quarry.headPosX - d4, quarry.yMax + d4 + .5, quarry.headPosZ + d4).colored().tex(D_maxU, D_I8dV).lightedAndEnd()
                buffer.pos(quarry.headPosX + d4, quarry.yMax + d4 + .5, quarry.headPosZ + d4).colored().tex(D_maxU, drillStripe.getMaxV).lightedAndEnd()
                buffer.pos(quarry.headPosX + d4, quarry.yMax + d4 + .5, quarry.headPosZ - d4).colored().tex(D_minU, drillStripe.getMaxV).lightedAndEnd()

                for (i1 <- 0 until floor) {
                    val i2 = i1 + 1
                    buffer.pos(quarry.headPosX + d4, quarry.yMax + d4 - i1, quarry.headPosZ - d4).colored().tex(D_minU, D_minV).lightedAndEnd()
                    buffer.pos(quarry.headPosX + d4, quarry.yMax + d4 - i2, quarry.headPosZ - d4).colored().tex(D_16dU, D_minV).lightedAndEnd()
                    buffer.pos(quarry.headPosX - d4, quarry.yMax + d4 - i2, quarry.headPosZ - d4).colored().tex(D_16dU, D_I8dV).lightedAndEnd()
                    buffer.pos(quarry.headPosX - d4, quarry.yMax + d4 - i1, quarry.headPosZ - d4).colored().tex(D_minU, D_I8dV).lightedAndEnd()

                    buffer.pos(quarry.headPosX + d4, quarry.yMax + d4 - i1, quarry.headPosZ + d4).colored().tex(D_minU, D_minV).lightedAndEnd()
                    buffer.pos(quarry.headPosX + d4, quarry.yMax + d4 - i2, quarry.headPosZ + d4).colored().tex(D_16dU, D_minV).lightedAndEnd()
                    buffer.pos(quarry.headPosX - d4, quarry.yMax + d4 - i2, quarry.headPosZ + d4).colored().tex(D_16dU, D_I8dV).lightedAndEnd()
                    buffer.pos(quarry.headPosX - d4, quarry.yMax + d4 - i1, quarry.headPosZ + d4).colored().tex(D_minU, D_I8dV).lightedAndEnd()

                    buffer.pos(quarry.headPosX + d4, quarry.yMax + d4 - i1, quarry.headPosZ + d4).colored().tex(D_minU, D_minV).lightedAndEnd()
                    buffer.pos(quarry.headPosX + d4, quarry.yMax + d4 - i2, quarry.headPosZ + d4).colored().tex(D_16dU, D_minV).lightedAndEnd()
                    buffer.pos(quarry.headPosX + d4, quarry.yMax + d4 - i2, quarry.headPosZ - d4).colored().tex(D_16dU, D_I8dV).lightedAndEnd()
                    buffer.pos(quarry.headPosX + d4, quarry.yMax + d4 - i1, quarry.headPosZ - d4).colored().tex(D_minU, D_I8dV).lightedAndEnd()

                    buffer.pos(quarry.headPosX - d4, quarry.yMax + d4 - i1, quarry.headPosZ + d4).colored().tex(D_minU, D_minV).lightedAndEnd()
                    buffer.pos(quarry.headPosX - d4, quarry.yMax + d4 - i2, quarry.headPosZ + d4).colored().tex(D_16dU, D_minV).lightedAndEnd()
                    buffer.pos(quarry.headPosX - d4, quarry.yMax + d4 - i2, quarry.headPosZ - d4).colored().tex(D_16dU, D_I8dV).lightedAndEnd()
                    buffer.pos(quarry.headPosX - d4, quarry.yMax + d4 - i1, quarry.headPosZ - d4).colored().tex(D_minU, D_I8dV).lightedAndEnd()
                }
                val fixedU = drillStripe.getInterpolatedU((length - floor) * 16)
                buffer.pos(quarry.headPosX + d4, quarry.yMax + d4 - floor, quarry.headPosZ - d4).colored().tex(D_minU, D_minV).lightedAndEnd()
                buffer.pos(quarry.headPosX + d4, quarry.yMax + d4 - length, quarry.headPosZ - d4).colored().tex(fixedU, D_minV).lightedAndEnd()
                buffer.pos(quarry.headPosX - d4, quarry.yMax + d4 - length, quarry.headPosZ - d4).colored().tex(fixedU, D_I8dV).lightedAndEnd()
                buffer.pos(quarry.headPosX - d4, quarry.yMax + d4 - floor, quarry.headPosZ - d4).colored().tex(D_minU, D_I8dV).lightedAndEnd()

                buffer.pos(quarry.headPosX + d4, quarry.yMax + d4 - floor, quarry.headPosZ + d4).colored().tex(D_minU, D_minV).lightedAndEnd()
                buffer.pos(quarry.headPosX + d4, quarry.yMax + d4 - length, quarry.headPosZ + d4).colored().tex(fixedU, D_minV).lightedAndEnd()
                buffer.pos(quarry.headPosX - d4, quarry.yMax + d4 - length, quarry.headPosZ + d4).colored().tex(fixedU, D_I8dV).lightedAndEnd()
                buffer.pos(quarry.headPosX - d4, quarry.yMax + d4 - floor, quarry.headPosZ + d4).colored().tex(D_minU, D_I8dV).lightedAndEnd()

                buffer.pos(quarry.headPosX + d4, quarry.yMax + d4 - floor, quarry.headPosZ + d4).colored().tex(D_minU, D_minV).lightedAndEnd()
                buffer.pos(quarry.headPosX + d4, quarry.yMax + d4 - length, quarry.headPosZ + d4).colored().tex(fixedU, D_minV).lightedAndEnd()
                buffer.pos(quarry.headPosX + d4, quarry.yMax + d4 - length, quarry.headPosZ - d4).colored().tex(fixedU, D_I8dV).lightedAndEnd()
                buffer.pos(quarry.headPosX + d4, quarry.yMax + d4 - floor, quarry.headPosZ - d4).colored().tex(D_minU, D_I8dV).lightedAndEnd()

                buffer.pos(quarry.headPosX - d4, quarry.yMax + d4 - floor, quarry.headPosZ + d4).colored().tex(D_minU, D_minV).lightedAndEnd()
                buffer.pos(quarry.headPosX - d4, quarry.yMax + d4 - length, quarry.headPosZ + d4).colored().tex(fixedU, D_minV).lightedAndEnd()
                buffer.pos(quarry.headPosX - d4, quarry.yMax + d4 - length, quarry.headPosZ - d4).colored().tex(fixedU, D_I8dV).lightedAndEnd()
                buffer.pos(quarry.headPosX - d4, quarry.yMax + d4 - floor, quarry.headPosZ - d4).colored().tex(D_minU, D_I8dV).lightedAndEnd()

                //Bottom
                buffer.pos(quarry.headPosX - d4, quarry.yMax + d4 - length, quarry.headPosZ - d4).colored().tex(D_minU, D_I8dV).lightedAndEnd()
                buffer.pos(quarry.headPosX - d4, quarry.yMax + d4 - length, quarry.headPosZ + d4).colored().tex(D_maxU, D_I8dV).lightedAndEnd()
                buffer.pos(quarry.headPosX + d4, quarry.yMax + d4 - length, quarry.headPosZ + d4).colored().tex(D_maxU, drillStripe.getMaxV).lightedAndEnd()
                buffer.pos(quarry.headPosX + d4, quarry.yMax + d4 - length, quarry.headPosZ - d4).colored().tex(D_minU, drillStripe.getMaxV).lightedAndEnd()

                //Drill
                buffer.pos(quarry.headPosX + d4 / 2, quarry.headPosY + 1, quarry.headPosZ - d4 / 2).colored().tex(headSprite.getMinU, headSprite.getMinV).lightedAndEnd()
                buffer.pos(quarry.headPosX + d4 / 2, quarry.headPosY + 0, quarry.headPosZ - d4 / 2).colored().tex(headSprite.getMaxU, headSprite.getMinV).lightedAndEnd()
                buffer.pos(quarry.headPosX - d4 / 2, quarry.headPosY + 0, quarry.headPosZ - d4 / 2).colored().tex(headSprite.getMaxU, headSprite.getInterpolatedV(4)).lightedAndEnd()
                buffer.pos(quarry.headPosX - d4 / 2, quarry.headPosY + 1, quarry.headPosZ - d4 / 2).colored().tex(headSprite.getMinU, headSprite.getInterpolatedV(4)).lightedAndEnd()

                buffer.pos(quarry.headPosX - d4 / 2, quarry.headPosY + 1, quarry.headPosZ + d4 / 2).colored().tex(headSprite.getMinU, headSprite.getMinV).lightedAndEnd()
                buffer.pos(quarry.headPosX - d4 / 2, quarry.headPosY + 0, quarry.headPosZ + d4 / 2).colored().tex(headSprite.getMaxU, headSprite.getMinV).lightedAndEnd()
                buffer.pos(quarry.headPosX + d4 / 2, quarry.headPosY + 0, quarry.headPosZ + d4 / 2).colored().tex(headSprite.getMaxU, headSprite.getInterpolatedV(4)).lightedAndEnd()
                buffer.pos(quarry.headPosX + d4 / 2, quarry.headPosY + 1, quarry.headPosZ + d4 / 2).colored().tex(headSprite.getMinU, headSprite.getInterpolatedV(4)).lightedAndEnd()

                buffer.pos(quarry.headPosX + d4 / 2, quarry.headPosY + 1, quarry.headPosZ + d4 / 2).colored().tex(headSprite.getMinU, headSprite.getMinV).lightedAndEnd()
                buffer.pos(quarry.headPosX + d4 / 2, quarry.headPosY + 0, quarry.headPosZ + d4 / 2).colored().tex(headSprite.getMaxU, headSprite.getMinV).lightedAndEnd()
                buffer.pos(quarry.headPosX + d4 / 2, quarry.headPosY + 0, quarry.headPosZ - d4 / 2).colored().tex(headSprite.getMaxU, headSprite.getInterpolatedV(4)).lightedAndEnd()
                buffer.pos(quarry.headPosX + d4 / 2, quarry.headPosY + 1, quarry.headPosZ - d4 / 2).colored().tex(headSprite.getMinU, headSprite.getInterpolatedV(4)).lightedAndEnd()

                buffer.pos(quarry.headPosX - d4 / 2, quarry.headPosY + 1, quarry.headPosZ - d4 / 2).colored().tex(headSprite.getMinU, headSprite.getMinV).lightedAndEnd()
                buffer.pos(quarry.headPosX - d4 / 2, quarry.headPosY + 0, quarry.headPosZ - d4 / 2).colored().tex(headSprite.getMaxU, headSprite.getMinV).lightedAndEnd()
                buffer.pos(quarry.headPosX - d4 / 2, quarry.headPosY + 0, quarry.headPosZ + d4 / 2).colored().tex(headSprite.getMaxU, headSprite.getInterpolatedV(4)).lightedAndEnd()
                buffer.pos(quarry.headPosX - d4 / 2, quarry.headPosY + 1, quarry.headPosZ + d4 / 2).colored().tex(headSprite.getMinU, headSprite.getInterpolatedV(4)).lightedAndEnd()
            }

            val y_length = quarry.yMax - quarry.headPosY - 0.75
            val y_floor = MathHelper.floor(y_length)
            buffer.setTranslation(distanceX - pos.getX + .5, distanceY - pos.getY, distanceZ - pos.getZ + .5)
            yLine(y_floor, y_length)

            Minecraft.getMinecraft.mcProfiler.endSection()
        }

        Minecraft.getMinecraft.mcProfiler.endSection()
        Minecraft.getMinecraft.mcProfiler.endSection()
    }

    override def isGlobalRenderer(te: TileQuarry): Boolean = true
}
