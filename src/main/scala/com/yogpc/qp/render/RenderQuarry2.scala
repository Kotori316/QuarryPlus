package com.yogpc.qp.render

import com.yogpc.qp.tile.TileQuarry
import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.util.math.Vec3d
import net.minecraftforge.client.model.animation.FastTESR

object RenderQuarry2 extends FastTESR[TileQuarry] {

    val halfVec = new Vec3d(.5, .5, .5)
    val d = 1d / 16d
    private lazy val spriteV = Sprites.getMap('stripes_v)
    private lazy val spriteH = Sprites.getMap('stripes_h)

    override def renderTileEntityFast(quarry: TileQuarry, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, buffer: VertexBuffer) = {
        val pos = quarry.getPos
        buffer.setTranslation(x - pos.getX, y - pos.getY, z - pos.getZ)

        if ((quarry.G_getNow == TileQuarry.Mode.NOTNEEDBREAK || quarry.G_getNow == TileQuarry.Mode.MAKEFRAME) && quarry.yMax != Integer.MIN_VALUE) {
            buffer.setTranslation(x - pos.getX + .5, y - pos.getY + .5, z - pos.getZ + .5)
            val minPos = quarry.getMinPos
            val maxPos = quarry.getMaxPos
            val subtract = quarry.getMaxPos subtract quarry.getMinPos

            //X 4 LINES
            for (i <- 0 until subtract.getX) {
                val n = if (i == subtract.getX - 1) 1 - d * 2 else 1d

                def tempY(y: Double): Unit = {
                    buffer.pos(minPos.getX + d + i, y - d, minPos.getZ - d).colored().tex(spriteV.getMinU, spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d + i + n, y - d, minPos.getZ - d).colored().tex(spriteV.getInterpolatedU(8), spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d + i + n, y - d, minPos.getZ + d).colored().tex(spriteV.getInterpolatedU(8), spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d + i, y - d, minPos.getZ + d).colored().tex(spriteV.getMinU, spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                    buffer.pos(minPos.getX + d + i, y - d, maxPos.getZ - d).colored().tex(spriteV.getMinU, spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d + i + n, y - d, maxPos.getZ - d).colored().tex(spriteV.getInterpolatedU(8), spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d + i + n, y - d, maxPos.getZ + d).colored().tex(spriteV.getInterpolatedU(8), spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d + i, y - d, maxPos.getZ + d).colored().tex(spriteV.getMinU, spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                    buffer.pos(minPos.getX + d + i, y + d, minPos.getZ - d).colored().tex(spriteV.getMinU, spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d + i + n, y + d, minPos.getZ - d).colored().tex(spriteV.getInterpolatedU(8), spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d + i + n, y + d, minPos.getZ + d).colored().tex(spriteV.getInterpolatedU(8), spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d + i, y + d, minPos.getZ + d).colored().tex(spriteV.getMinU, spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                    buffer.pos(minPos.getX + d + i, y + d, maxPos.getZ - d).colored().tex(spriteV.getMinU, spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d + i + n, y + d, maxPos.getZ - d).colored().tex(spriteV.getInterpolatedU(8), spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d + i + n, y + d, maxPos.getZ + d).colored().tex(spriteV.getInterpolatedU(8), spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d + i, y + d, maxPos.getZ + d).colored().tex(spriteV.getMinU, spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                }

                def tempZ(z: Double): Unit = {
                    buffer.pos(minPos.getX + d + i, minPos.getY + d, z - d).colored().tex(spriteV.getMinU, spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d + i + n, minPos.getY + d, z - d).colored().tex(spriteV.getInterpolatedU(8), spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d + i + n, minPos.getY - d, z - d).colored().tex(spriteV.getInterpolatedU(8), spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d + i, minPos.getY - d, z - d).colored().tex(spriteV.getMinU, spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                    buffer.pos(minPos.getX + d + i, maxPos.getY + d, z - d).colored().tex(spriteV.getMinU, spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d + i + n, maxPos.getY + d, z - d).colored().tex(spriteV.getInterpolatedU(8), spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d + i + n, maxPos.getY - d, z - d).colored().tex(spriteV.getInterpolatedU(8), spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d + i, maxPos.getY - d, z - d).colored().tex(spriteV.getMinU, spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                    buffer.pos(minPos.getX + d + i, minPos.getY + d, z + d).colored().tex(spriteV.getMinU, spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d + i + n, minPos.getY + d, z + d).colored().tex(spriteV.getInterpolatedU(8), spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d + i + n, minPos.getY - d, z + d).colored().tex(spriteV.getInterpolatedU(8), spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d + i, minPos.getY - d, z + d).colored().tex(spriteV.getMinU, spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                    buffer.pos(minPos.getX + d + i, maxPos.getY + d, z + d).colored().tex(spriteV.getMinU, spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d + i + n, maxPos.getY + d, z + d).colored().tex(spriteV.getInterpolatedU(8), spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d + i + n, maxPos.getY - d, z + d).colored().tex(spriteV.getInterpolatedU(8), spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d + i, maxPos.getY - d, z + d).colored().tex(spriteV.getMinU, spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                }

                tempY(minPos.getY)
                tempY(maxPos.getY)
                tempZ(minPos.getZ)
                tempZ(maxPos.getZ)
            }
            //Y 4 LINES
            for (i <- 0 until subtract.getY) {
                val n = if (i == subtract.getY - 1) 1 - d * 2 else 1d

                // => Z
                buffer.pos(minPos.getX - d, minPos.getY + d + i, minPos.getZ - d).colored().tex(spriteH.getMinU, spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(minPos.getX + d, minPos.getY + d + i, minPos.getZ - d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(minPos.getX + d, minPos.getY + d + i + n, minPos.getZ - d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                buffer.pos(minPos.getX - d, minPos.getY + d + i + n, minPos.getZ - d).colored().tex(spriteH.getMinU, spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                buffer.pos(maxPos.getX - d, minPos.getY + d + i, minPos.getZ - d).colored().tex(spriteH.getMinU, spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(maxPos.getX + d, minPos.getY + d + i, minPos.getZ - d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(maxPos.getX + d, minPos.getY + d + i + n, minPos.getZ - d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                buffer.pos(maxPos.getX - d, minPos.getY + d + i + n, minPos.getZ - d).colored().tex(spriteH.getMinU, spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                buffer.pos(minPos.getX - d, minPos.getY + d + i, maxPos.getZ - d).colored().tex(spriteH.getMinU, spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(minPos.getX + d, minPos.getY + d + i, maxPos.getZ - d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(minPos.getX + d, minPos.getY + d + i + n, maxPos.getZ - d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                buffer.pos(minPos.getX - d, minPos.getY + d + i + n, maxPos.getZ - d).colored().tex(spriteH.getMinU, spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                buffer.pos(maxPos.getX - d, minPos.getY + d + i, maxPos.getZ - d).colored().tex(spriteH.getMinU, spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(maxPos.getX + d, minPos.getY + d + i, maxPos.getZ - d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(maxPos.getX + d, minPos.getY + d + i + n, maxPos.getZ - d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                buffer.pos(maxPos.getX - d, minPos.getY + d + i + n, maxPos.getZ - d).colored().tex(spriteH.getMinU, spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                buffer.pos(minPos.getX - d, minPos.getY + d + i, minPos.getZ + d).colored().tex(spriteH.getMinU, spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(minPos.getX + d, minPos.getY + d + i, minPos.getZ + d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(minPos.getX + d, minPos.getY + d + i + n, minPos.getZ + d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                buffer.pos(minPos.getX - d, minPos.getY + d + i + n, minPos.getZ + d).colored().tex(spriteH.getMinU, spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                buffer.pos(maxPos.getX - d, minPos.getY + d + i, minPos.getZ + d).colored().tex(spriteH.getMinU, spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(maxPos.getX + d, minPos.getY + d + i, minPos.getZ + d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(maxPos.getX + d, minPos.getY + d + i + n, minPos.getZ + d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                buffer.pos(maxPos.getX - d, minPos.getY + d + i + n, minPos.getZ + d).colored().tex(spriteH.getMinU, spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                buffer.pos(minPos.getX - d, minPos.getY + d + i, maxPos.getZ + d).colored().tex(spriteH.getMinU, spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(minPos.getX + d, minPos.getY + d + i, maxPos.getZ + d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(minPos.getX + d, minPos.getY + d + i + n, maxPos.getZ + d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                buffer.pos(minPos.getX - d, minPos.getY + d + i + n, maxPos.getZ + d).colored().tex(spriteH.getMinU, spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                buffer.pos(maxPos.getX - d, minPos.getY + d + i, maxPos.getZ + d).colored().tex(spriteH.getMinU, spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(maxPos.getX + d, minPos.getY + d + i, maxPos.getZ + d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(maxPos.getX + d, minPos.getY + d + i + n, maxPos.getZ + d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                buffer.pos(maxPos.getX - d, minPos.getY + d + i + n, maxPos.getZ + d).colored().tex(spriteH.getMinU, spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                // => X
                buffer.pos(minPos.getX - d, minPos.getY + d + i, minPos.getZ - d).colored().tex(spriteH.getMinU, spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(minPos.getX - d, minPos.getY + d + i, minPos.getZ + d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(minPos.getX - d, minPos.getY + d + i + n, minPos.getZ + d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                buffer.pos(minPos.getX - d, minPos.getY + d + i + n, minPos.getZ - d).colored().tex(spriteH.getMinU, spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                buffer.pos(maxPos.getX - d, minPos.getY + d + i, minPos.getZ - d).colored().tex(spriteH.getMinU, spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(maxPos.getX - d, minPos.getY + d + i, minPos.getZ + d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(maxPos.getX - d, minPos.getY + d + i + n, minPos.getZ + d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                buffer.pos(maxPos.getX - d, minPos.getY + d + i + n, minPos.getZ - d).colored().tex(spriteH.getMinU, spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                buffer.pos(minPos.getX - d, minPos.getY + d + i, maxPos.getZ - d).colored().tex(spriteH.getMinU, spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(minPos.getX - d, minPos.getY + d + i, maxPos.getZ + d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(minPos.getX - d, minPos.getY + d + i + n, maxPos.getZ + d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                buffer.pos(minPos.getX - d, minPos.getY + d + i + n, maxPos.getZ - d).colored().tex(spriteH.getMinU, spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                buffer.pos(maxPos.getX - d, minPos.getY + d + i, maxPos.getZ - d).colored().tex(spriteH.getMinU, spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(maxPos.getX - d, minPos.getY + d + i, maxPos.getZ + d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(maxPos.getX - d, minPos.getY + d + i + n, maxPos.getZ + d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                buffer.pos(maxPos.getX - d, minPos.getY + d + i + n, maxPos.getZ - d).colored().tex(spriteH.getMinU, spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()


                buffer.pos(minPos.getX + d, minPos.getY + d + i, minPos.getZ - d).colored().tex(spriteH.getMinU, spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(minPos.getX + d, minPos.getY + d + i, minPos.getZ + d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(minPos.getX + d, minPos.getY + d + i + n, minPos.getZ + d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                buffer.pos(minPos.getX + d, minPos.getY + d + i + n, minPos.getZ - d).colored().tex(spriteH.getMinU, spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                buffer.pos(maxPos.getX + d, minPos.getY + d + i, minPos.getZ - d).colored().tex(spriteH.getMinU, spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(maxPos.getX + d, minPos.getY + d + i, minPos.getZ + d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(maxPos.getX + d, minPos.getY + d + i + n, minPos.getZ + d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                buffer.pos(maxPos.getX + d, minPos.getY + d + i + n, minPos.getZ - d).colored().tex(spriteH.getMinU, spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                buffer.pos(minPos.getX + d, minPos.getY + d + i, maxPos.getZ - d).colored().tex(spriteH.getMinU, spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(minPos.getX + d, minPos.getY + d + i, maxPos.getZ + d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(minPos.getX + d, minPos.getY + d + i + n, maxPos.getZ + d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                buffer.pos(minPos.getX + d, minPos.getY + d + i + n, maxPos.getZ - d).colored().tex(spriteH.getMinU, spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                buffer.pos(maxPos.getX + d, minPos.getY + d + i, maxPos.getZ - d).colored().tex(spriteH.getMinU, spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(maxPos.getX + d, minPos.getY + d + i, maxPos.getZ + d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getMinV).lightmap(240, 0).endVertex()
                buffer.pos(maxPos.getX + d, minPos.getY + d + i + n, maxPos.getZ + d).colored().tex(spriteH.getInterpolatedU(8), spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                buffer.pos(maxPos.getX + d, minPos.getY + d + i + n, maxPos.getZ - d).colored().tex(spriteH.getMinU, spriteH.getInterpolatedV(8)).lightmap(240, 0).endVertex()
            }
            //Z 4 LINES
            for (i <- 0 until subtract.getZ) {
                val n = if (i == subtract.getZ - 1) 1 - d * 2 else 1d

                def tempY(y: Double): Unit = {
                    buffer.pos(minPos.getX - d, y - d, minPos.getZ + d + i).colored().tex(spriteV.getMinU, spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX - d, y - d, minPos.getZ + d + i + n).colored().tex(spriteV.getInterpolatedU(8), spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d, y - d, minPos.getZ + d + i + n).colored().tex(spriteV.getInterpolatedU(8), spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d, y - d, minPos.getZ + d + i).colored().tex(spriteV.getMinU, spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                    buffer.pos(maxPos.getX - d, y - d, minPos.getZ + d + i).colored().tex(spriteV.getMinU, spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(maxPos.getX - d, y - d, minPos.getZ + d + i + n).colored().tex(spriteV.getInterpolatedU(8), spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(maxPos.getX + d, y - d, minPos.getZ + d + i + n).colored().tex(spriteV.getInterpolatedU(8), spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                    buffer.pos(maxPos.getX + d, y - d, minPos.getZ + d + i).colored().tex(spriteV.getMinU, spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                    buffer.pos(minPos.getX - d, y + d, minPos.getZ + d + i).colored().tex(spriteV.getMinU, spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX - d, y + d, minPos.getZ + d + i + n).colored().tex(spriteV.getInterpolatedU(8), spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d, y + d, minPos.getZ + d + i + n).colored().tex(spriteV.getInterpolatedU(8), spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                    buffer.pos(minPos.getX + d, y + d, minPos.getZ + d + i).colored().tex(spriteV.getMinU, spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                    buffer.pos(maxPos.getX - d, y + d, minPos.getZ + d + i).colored().tex(spriteV.getMinU, spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(maxPos.getX - d, y + d, minPos.getZ + d + i + n).colored().tex(spriteV.getInterpolatedU(8), spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(maxPos.getX + d, y + d, minPos.getZ + d + i + n).colored().tex(spriteV.getInterpolatedU(8), spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                    buffer.pos(maxPos.getX + d, y + d, minPos.getZ + d + i).colored().tex(spriteV.getMinU, spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                }

                def tempX(x: Double): Unit = {
                    buffer.pos(x - d, minPos.getY + d, minPos.getZ + d + i).colored().tex(spriteV.getMinU, spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(x - d, minPos.getY + d, minPos.getZ + d + i + n).colored().tex(spriteV.getInterpolatedU(8), spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(x - d, minPos.getY - d, minPos.getZ + d + i + n).colored().tex(spriteV.getInterpolatedU(8), spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                    buffer.pos(x - d, minPos.getY - d, minPos.getZ + d + i).colored().tex(spriteV.getMinU, spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                    buffer.pos(x - d, maxPos.getY + d, minPos.getZ + d + i).colored().tex(spriteV.getMinU, spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(x - d, maxPos.getY + d, minPos.getZ + d + i + n).colored().tex(spriteV.getInterpolatedU(8), spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(x - d, maxPos.getY - d, minPos.getZ + d + i + n).colored().tex(spriteV.getInterpolatedU(8), spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                    buffer.pos(x - d, maxPos.getY - d, minPos.getZ + d + i).colored().tex(spriteV.getMinU, spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                    buffer.pos(x + d, minPos.getY + d, minPos.getZ + d + i).colored().tex(spriteV.getMinU, spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(x + d, minPos.getY + d, minPos.getZ + d + i + n).colored().tex(spriteV.getInterpolatedU(8), spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(x + d, minPos.getY - d, minPos.getZ + d + i + n).colored().tex(spriteV.getInterpolatedU(8), spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                    buffer.pos(x + d, minPos.getY - d, minPos.getZ + d + i).colored().tex(spriteV.getMinU, spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()

                    buffer.pos(x + d, maxPos.getY + d, minPos.getZ + d + i).colored().tex(spriteV.getMinU, spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(x + d, maxPos.getY + d, minPos.getZ + d + i + n).colored().tex(spriteV.getInterpolatedU(8), spriteV.getMinV).lightmap(240, 0).endVertex()
                    buffer.pos(x + d, maxPos.getY - d, minPos.getZ + d + i + n).colored().tex(spriteV.getInterpolatedU(8), spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                    buffer.pos(x + d, maxPos.getY - d, minPos.getZ + d + i).colored().tex(spriteV.getMinU, spriteV.getInterpolatedV(8)).lightmap(240, 0).endVertex()
                }

                tempY(minPos.getY)
                tempY(maxPos.getY)
                tempX(minPos.getX)
                tempX(maxPos.getX)
            }
        }
    }

    override def isGlobalRenderer(te: TileQuarry): Boolean = true
}
