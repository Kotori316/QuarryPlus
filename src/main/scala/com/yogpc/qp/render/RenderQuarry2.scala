package com.yogpc.qp.render

import com.mojang.blaze3d.matrix.MatrixStack
import com.yogpc.qp.machines.base.Area
import com.yogpc.qp.machines.quarry.{QuarryAction, TileQuarry2}
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.{TileEntityRenderer, TileEntityRendererDispatcher}
import net.minecraft.client.renderer.{IRenderTypeBuffer, RenderType}
import net.minecraft.util.math.{MathHelper, Vec3i}

object RenderQuarry2 extends TileEntityRenderer[TileQuarry2](TileEntityRendererDispatcher.instance) {
  val instance = this

  private[this] final val d1 = 1d / 16d
  private[this] final val d4 = 4d / 16d
  private lazy val spriteV = Sprites.getMap(Symbol("stripes_v"))
  private lazy val spriteH = Sprites.getMap(Symbol("stripes_h"))
  private lazy val boxStripe = Sprites.getMap(Symbol("stripes_b"))
  private lazy val drillStripe = Sprites.getMap(LaserType.DRILL.symbol)
  private lazy val headSprite = Sprites.getMap(LaserType.DRILL_HEAD.symbol)
  private[this] final val plusF: (Double, Double) => Double = (double1, double2) => double1 + double2
  private[this] final val minusF: (Double, Double) => Double = (double1, double2) => double1 - double2
  private[this] var bufferInstance = new Buffer(null)

  override def func_225616_a_(quarry: TileQuarry2, v: Float, matrixStack: MatrixStack, iRenderTypeBuffer: IRenderTypeBuffer, i: Int, i1: Int): Unit = {
    Minecraft.getInstance.getProfiler.startSection("quarryplus")
    Minecraft.getInstance.getProfiler.startSection("quarry")
    if (!quarry.area.dimID.forall(id => Minecraft.getInstance.world.dimension.getType.getId == id)) return
    val pos = quarry.getPos
    val bufferBuilder = iRenderTypeBuffer.getBuffer(RenderType.func_228643_e_())
    if (!(bufferInstance bufferEq bufferBuilder)) {
      bufferInstance = new Buffer(bufferBuilder)
    }
    val buffer = bufferInstance

    if ((quarry.action.mode == TileQuarry2.waiting || quarry.action.mode == TileQuarry2.buildFrame || quarry.action.mode == TileQuarry2.breakInsideFrame)
      && quarry.area != Area.zeroArea) {
      Minecraft.getInstance.getProfiler.startSection("frame")
      matrixStack.func_227860_a_()
      matrixStack.func_227861_a_(-pos.getX + .5, -pos.getY + .5, -pos.getZ + .5)
      val minX = quarry.area.xMin
      val minY = quarry.area.yMin
      val minZ = quarry.area.zMin
      val maxX = quarry.area.xMax
      val maxY = quarry.area.yMax
      val maxZ = quarry.area.zMax

      val subtract = new Vec3i(maxX - minX, maxY - minY, maxZ - minZ)

      val mXm = minX - d1
      val mXP = minX + d1
      val mYm = minY - d1
      val mYP = minY + d1
      val mZm = minZ - d1
      val mZP = minZ + d1

      val MXm = maxX - d1
      val MXP = maxX + d1
      val MYm = maxY - d1
      val MYP = maxY + d1
      val MZm = maxZ - d1
      val MZP = maxZ + d1

      //X 4 LINES
      val V_minU = spriteV.getMinU
      val V_minV = spriteV.getMinV
      val V_maxU = spriteV.getInterpolatedU(8)
      val V_maxV = spriteV.getInterpolatedV(8)

      var i = 0
      while (i < subtract.getX) {
        val n = if (i == subtract.getX - 1) 1 - d1 * 2 else 1d
        val mXi = minX + d1 + i
        val mXn = minX + d1 + i + n

        def tempY(y: Double): Unit = {
          val ymd = y - d1
          val yPd = y + d1
          buffer.pos(mXi, ymd, mZm, matrixStack).colored().tex(V_minU, V_minV).lightedAndEnd()
          buffer.pos(mXn, ymd, mZm, matrixStack).colored().tex(V_maxU, V_minV).lightedAndEnd()
          buffer.pos(mXn, ymd, mZP, matrixStack).colored().tex(V_maxU, V_maxV).lightedAndEnd()
          buffer.pos(mXi, ymd, mZP, matrixStack).colored().tex(V_minU, V_maxV).lightedAndEnd()

          buffer.pos(mXi, ymd, MZm, matrixStack).colored().tex(V_minU, V_minV).lightedAndEnd()
          buffer.pos(mXn, ymd, MZm, matrixStack).colored().tex(V_maxU, V_minV).lightedAndEnd()
          buffer.pos(mXn, ymd, MZP, matrixStack).colored().tex(V_maxU, V_maxV).lightedAndEnd()
          buffer.pos(mXi, ymd, MZP, matrixStack).colored().tex(V_minU, V_maxV).lightedAndEnd()

          buffer.pos(mXi, yPd, mZm, matrixStack).colored().tex(V_minU, V_minV).lightedAndEnd()
          buffer.pos(mXi, yPd, mZP, matrixStack).colored().tex(V_minU, V_maxV).lightedAndEnd()
          buffer.pos(mXn, yPd, mZP, matrixStack).colored().tex(V_maxU, V_maxV).lightedAndEnd()
          buffer.pos(mXn, yPd, mZm, matrixStack).colored().tex(V_maxU, V_minV).lightedAndEnd()

          buffer.pos(mXi, yPd, MZm, matrixStack).colored().tex(V_minU, V_minV).lightedAndEnd()
          buffer.pos(mXi, yPd, MZP, matrixStack).colored().tex(V_minU, V_maxV).lightedAndEnd()
          buffer.pos(mXn, yPd, MZP, matrixStack).colored().tex(V_maxU, V_maxV).lightedAndEnd()
          buffer.pos(mXn, yPd, MZm, matrixStack).colored().tex(V_maxU, V_minV).lightedAndEnd()
        }

        def tempZ(z: Double): Unit = {
          val zmd = z - d1
          val zPd = z + d1
          buffer.pos(mXi, mYP, zmd, matrixStack).colored().tex(V_minU, V_minV).lightedAndEnd()
          buffer.pos(mXn, mYP, zmd, matrixStack).colored().tex(V_maxU, V_minV).lightedAndEnd()
          buffer.pos(mXn, mYm, zmd, matrixStack).colored().tex(V_maxU, V_maxV).lightedAndEnd()
          buffer.pos(mXi, mYm, zmd, matrixStack).colored().tex(V_minU, V_maxV).lightedAndEnd()

          buffer.pos(mXi, MYP, zmd, matrixStack).colored().tex(V_minU, V_minV).lightedAndEnd()
          buffer.pos(mXn, MYP, zmd, matrixStack).colored().tex(V_maxU, V_minV).lightedAndEnd()
          buffer.pos(mXn, MYm, zmd, matrixStack).colored().tex(V_maxU, V_maxV).lightedAndEnd()
          buffer.pos(mXi, MYm, zmd, matrixStack).colored().tex(V_minU, V_maxV).lightedAndEnd()

          buffer.pos(mXi, mYP, zPd, matrixStack).colored().tex(V_minU, V_minV).lightedAndEnd()
          buffer.pos(mXi, mYm, zPd, matrixStack).colored().tex(V_minU, V_maxV).lightedAndEnd()
          buffer.pos(mXn, mYm, zPd, matrixStack).colored().tex(V_maxU, V_maxV).lightedAndEnd()
          buffer.pos(mXn, mYP, zPd, matrixStack).colored().tex(V_maxU, V_minV).lightedAndEnd()

          buffer.pos(mXi, MYP, zPd, matrixStack).colored().tex(V_minU, V_minV).lightedAndEnd()
          buffer.pos(mXi, MYm, zPd, matrixStack).colored().tex(V_minU, V_maxV).lightedAndEnd()
          buffer.pos(mXn, MYm, zPd, matrixStack).colored().tex(V_maxU, V_maxV).lightedAndEnd()
          buffer.pos(mXn, MYP, zPd, matrixStack).colored().tex(V_maxU, V_minV).lightedAndEnd()
        }

        tempY(minY)
        tempY(maxY)
        tempZ(minZ)
        tempZ(maxZ)
        i += 1
      }
      //Y 4 LINES
      i = 0
      while (i < subtract.getY) {
        val n = if (i == subtract.getY - 1) 1 - d1 * 2 else 1d

        // => Z
        val H_minU = spriteH.getMinU
        val H_minV = spriteH.getMinV
        val H_maxU = spriteH.getInterpolatedU(8)
        val H_maxV = spriteH.getInterpolatedV(8)

        val y0 = mYP + i + 0
        val yn = mYP + i + n
        buffer.pos(mXm, y0, mZm, matrixStack).colored().tex(H_minU, H_minV).lightedAndEnd()
        buffer.pos(mXm, yn, mZm, matrixStack).colored().tex(H_minU, H_maxV).lightedAndEnd()
        buffer.pos(mXP, yn, mZm, matrixStack).colored().tex(H_maxU, H_maxV).lightedAndEnd()
        buffer.pos(mXP, y0, mZm, matrixStack).colored().tex(H_maxU, H_minV).lightedAndEnd()

        buffer.pos(MXm, y0, mZm, matrixStack).colored().tex(H_minU, H_minV).lightedAndEnd()
        buffer.pos(MXm, yn, mZm, matrixStack).colored().tex(H_minU, H_maxV).lightedAndEnd()
        buffer.pos(MXP, yn, mZm, matrixStack).colored().tex(H_maxU, H_maxV).lightedAndEnd()
        buffer.pos(MXP, y0, mZm, matrixStack).colored().tex(H_maxU, H_minV).lightedAndEnd()

        buffer.pos(mXm, y0, MZm, matrixStack).colored().tex(H_minU, H_minV).lightedAndEnd()
        buffer.pos(mXm, yn, MZm, matrixStack).colored().tex(H_minU, H_maxV).lightedAndEnd()
        buffer.pos(mXP, yn, MZm, matrixStack).colored().tex(H_maxU, H_maxV).lightedAndEnd()
        buffer.pos(mXP, y0, MZm, matrixStack).colored().tex(H_maxU, H_minV).lightedAndEnd()

        buffer.pos(MXm, y0, MZm, matrixStack).colored().tex(H_minU, H_minV).lightedAndEnd()
        buffer.pos(MXm, yn, MZm, matrixStack).colored().tex(H_minU, H_maxV).lightedAndEnd()
        buffer.pos(MXP, yn, MZm, matrixStack).colored().tex(H_maxU, H_maxV).lightedAndEnd()
        buffer.pos(MXP, y0, MZm, matrixStack).colored().tex(H_maxU, H_minV).lightedAndEnd()

        buffer.pos(mXm, y0, mZP, matrixStack).colored().tex(H_minU, H_minV).lightedAndEnd()
        buffer.pos(mXP, y0, mZP, matrixStack).colored().tex(H_maxU, H_minV).lightedAndEnd()
        buffer.pos(mXP, yn, mZP, matrixStack).colored().tex(H_maxU, H_maxV).lightedAndEnd()
        buffer.pos(mXm, yn, mZP, matrixStack).colored().tex(H_minU, H_maxV).lightedAndEnd()

        buffer.pos(MXm, y0, mZP, matrixStack).colored().tex(H_minU, H_minV).lightedAndEnd()
        buffer.pos(MXP, y0, mZP, matrixStack).colored().tex(H_maxU, H_minV).lightedAndEnd()
        buffer.pos(MXP, yn, mZP, matrixStack).colored().tex(H_maxU, H_maxV).lightedAndEnd()
        buffer.pos(MXm, yn, mZP, matrixStack).colored().tex(H_minU, H_maxV).lightedAndEnd()

        buffer.pos(mXm, y0, MZP, matrixStack).colored().tex(H_minU, H_minV).lightedAndEnd()
        buffer.pos(mXP, y0, MZP, matrixStack).colored().tex(H_maxU, H_minV).lightedAndEnd()
        buffer.pos(mXP, yn, MZP, matrixStack).colored().tex(H_maxU, H_maxV).lightedAndEnd()
        buffer.pos(mXm, yn, MZP, matrixStack).colored().tex(H_minU, H_maxV).lightedAndEnd()

        buffer.pos(MXm, y0, MZP, matrixStack).colored().tex(H_minU, H_minV).lightedAndEnd()
        buffer.pos(MXP, y0, MZP, matrixStack).colored().tex(H_maxU, H_minV).lightedAndEnd()
        buffer.pos(MXP, yn, MZP, matrixStack).colored().tex(H_maxU, H_maxV).lightedAndEnd()
        buffer.pos(MXm, yn, MZP, matrixStack).colored().tex(H_minU, H_maxV).lightedAndEnd()

        // => X
        buffer.pos(mXm, y0, mZm, matrixStack).colored().tex(H_minU, H_minV).lightedAndEnd()
        buffer.pos(mXm, y0, mZP, matrixStack).colored().tex(H_maxU, H_minV).lightedAndEnd()
        buffer.pos(mXm, yn, mZP, matrixStack).colored().tex(H_maxU, H_maxV).lightedAndEnd()
        buffer.pos(mXm, yn, mZm, matrixStack).colored().tex(H_minU, H_maxV).lightedAndEnd()

        buffer.pos(MXm, y0, mZm, matrixStack).colored().tex(H_minU, H_minV).lightedAndEnd()
        buffer.pos(MXm, y0, mZP, matrixStack).colored().tex(H_maxU, H_minV).lightedAndEnd()
        buffer.pos(MXm, yn, mZP, matrixStack).colored().tex(H_maxU, H_maxV).lightedAndEnd()
        buffer.pos(MXm, yn, mZm, matrixStack).colored().tex(H_minU, H_maxV).lightedAndEnd()

        buffer.pos(mXm, y0, MZm, matrixStack).colored().tex(H_minU, H_minV).lightedAndEnd()
        buffer.pos(mXm, y0, MZP, matrixStack).colored().tex(H_maxU, H_minV).lightedAndEnd()
        buffer.pos(mXm, yn, MZP, matrixStack).colored().tex(H_maxU, H_maxV).lightedAndEnd()
        buffer.pos(mXm, yn, MZm, matrixStack).colored().tex(H_minU, H_maxV).lightedAndEnd()

        buffer.pos(MXm, y0, MZm, matrixStack).colored().tex(H_minU, H_minV).lightedAndEnd()
        buffer.pos(MXm, y0, MZP, matrixStack).colored().tex(H_maxU, H_minV).lightedAndEnd()
        buffer.pos(MXm, yn, MZP, matrixStack).colored().tex(H_maxU, H_maxV).lightedAndEnd()
        buffer.pos(MXm, yn, MZm, matrixStack).colored().tex(H_minU, H_maxV).lightedAndEnd()


        buffer.pos(mXP, y0, mZm, matrixStack).colored().tex(H_minU, H_minV).lightedAndEnd()
        buffer.pos(mXP, yn, mZm, matrixStack).colored().tex(H_minU, H_maxV).lightedAndEnd()
        buffer.pos(mXP, yn, mZP, matrixStack).colored().tex(H_maxU, H_maxV).lightedAndEnd()
        buffer.pos(mXP, y0, mZP, matrixStack).colored().tex(H_maxU, H_minV).lightedAndEnd()

        buffer.pos(MXP, y0, mZm, matrixStack).colored().tex(H_minU, H_minV).lightedAndEnd()
        buffer.pos(MXP, yn, mZm, matrixStack).colored().tex(H_minU, H_maxV).lightedAndEnd()
        buffer.pos(MXP, yn, mZP, matrixStack).colored().tex(H_maxU, H_maxV).lightedAndEnd()
        buffer.pos(MXP, y0, mZP, matrixStack).colored().tex(H_maxU, H_minV).lightedAndEnd()

        buffer.pos(mXP, y0, MZm, matrixStack).colored().tex(H_minU, H_minV).lightedAndEnd()
        buffer.pos(mXP, yn, MZm, matrixStack).colored().tex(H_minU, H_maxV).lightedAndEnd()
        buffer.pos(mXP, yn, MZP, matrixStack).colored().tex(H_maxU, H_maxV).lightedAndEnd()
        buffer.pos(mXP, y0, MZP, matrixStack).colored().tex(H_maxU, H_minV).lightedAndEnd()

        buffer.pos(MXP, y0, MZm, matrixStack).colored().tex(H_minU, H_minV).lightedAndEnd()
        buffer.pos(MXP, yn, MZm, matrixStack).colored().tex(H_minU, H_maxV).lightedAndEnd()
        buffer.pos(MXP, yn, MZP, matrixStack).colored().tex(H_maxU, H_maxV).lightedAndEnd()
        buffer.pos(MXP, y0, MZP, matrixStack).colored().tex(H_maxU, H_minV).lightedAndEnd()
        i += 1
      }
      //Z 4 LINES
      i = 0
      while (i < subtract.getZ) {
        val n = if (i == subtract.getZ - 1) 1 - d1 * 2 else 1d

        val mZi = mZP + i + 0
        val mZn = mZP + i + n

        def tempY(y: Double): Unit = {
          val ymd = y - d1
          val yPd = y + d1
          buffer.pos(mXm, ymd, mZi, matrixStack).colored().tex(V_minU, V_minV).lightedAndEnd()
          buffer.pos(mXP, ymd, mZi, matrixStack).colored().tex(V_minU, V_maxV).lightedAndEnd()
          buffer.pos(mXP, ymd, mZn, matrixStack).colored().tex(V_maxU, V_maxV).lightedAndEnd()
          buffer.pos(mXm, ymd, mZn, matrixStack).colored().tex(V_maxU, V_minV).lightedAndEnd()

          buffer.pos(MXm, ymd, mZi, matrixStack).colored().tex(V_minU, V_minV).lightedAndEnd()
          buffer.pos(MXP, ymd, mZi, matrixStack).colored().tex(V_minU, V_maxV).lightedAndEnd()
          buffer.pos(MXP, ymd, mZn, matrixStack).colored().tex(V_maxU, V_maxV).lightedAndEnd()
          buffer.pos(MXm, ymd, mZn, matrixStack).colored().tex(V_maxU, V_minV).lightedAndEnd()

          buffer.pos(mXm, yPd, mZi, matrixStack).colored().tex(V_minU, V_minV).lightedAndEnd()
          buffer.pos(mXm, yPd, mZn, matrixStack).colored().tex(V_maxU, V_minV).lightedAndEnd()
          buffer.pos(mXP, yPd, mZn, matrixStack).colored().tex(V_maxU, V_maxV).lightedAndEnd()
          buffer.pos(mXP, yPd, mZi, matrixStack).colored().tex(V_minU, V_maxV).lightedAndEnd()

          buffer.pos(MXm, yPd, mZi, matrixStack).colored().tex(V_minU, V_minV).lightedAndEnd()
          buffer.pos(MXm, yPd, mZn, matrixStack).colored().tex(V_maxU, V_minV).lightedAndEnd()
          buffer.pos(MXP, yPd, mZn, matrixStack).colored().tex(V_maxU, V_maxV).lightedAndEnd()
          buffer.pos(MXP, yPd, mZi, matrixStack).colored().tex(V_minU, V_maxV).lightedAndEnd()
        }

        def tempX(x: Double): Unit = {
          val xmd = x - d1
          val xPd = x + d1
          buffer.pos(xmd, mYP, mZi, matrixStack).colored().tex(V_minU, V_minV).lightedAndEnd()
          buffer.pos(xmd, mYm, mZi, matrixStack).colored().tex(V_minU, V_maxV).lightedAndEnd()
          buffer.pos(xmd, mYm, mZn, matrixStack).colored().tex(V_maxU, V_maxV).lightedAndEnd()
          buffer.pos(xmd, mYP, mZn, matrixStack).colored().tex(V_maxU, V_minV).lightedAndEnd()

          buffer.pos(xmd, MYP, mZi, matrixStack).colored().tex(V_minU, V_minV).lightedAndEnd()
          buffer.pos(xmd, MYm, mZi, matrixStack).colored().tex(V_minU, V_maxV).lightedAndEnd()
          buffer.pos(xmd, MYm, mZn, matrixStack).colored().tex(V_maxU, V_maxV).lightedAndEnd()
          buffer.pos(xmd, MYP, mZn, matrixStack).colored().tex(V_maxU, V_minV).lightedAndEnd()

          buffer.pos(xPd, mYP, mZi, matrixStack).colored().tex(V_minU, V_minV).lightedAndEnd()
          buffer.pos(xPd, mYP, mZn, matrixStack).colored().tex(V_maxU, V_minV).lightedAndEnd()
          buffer.pos(xPd, mYm, mZn, matrixStack).colored().tex(V_maxU, V_maxV).lightedAndEnd()
          buffer.pos(xPd, mYm, mZi, matrixStack).colored().tex(V_minU, V_maxV).lightedAndEnd()

          buffer.pos(xPd, MYP, mZi, matrixStack).colored().tex(V_minU, V_minV).lightedAndEnd()
          buffer.pos(xPd, MYP, mZn, matrixStack).colored().tex(V_maxU, V_minV).lightedAndEnd()
          buffer.pos(xPd, MYm, mZn, matrixStack).colored().tex(V_maxU, V_maxV).lightedAndEnd()
          buffer.pos(xPd, MYm, mZi, matrixStack).colored().tex(V_minU, V_maxV).lightedAndEnd()
        }

        tempY(minY)
        tempY(maxY)
        tempX(minX)
        tempX(maxX)
        i += 1
      }

      val B_minU = boxStripe.getMinU
      val B_minV = boxStripe.getMinV
      val B_maxU = boxStripe.getMaxU
      val B_maxV = boxStripe.getMaxV
      //z
      buffer.pos(mXP, mYP, mZm, matrixStack).colored().tex(B_minU, B_minV).lightedAndEnd()
      buffer.pos(mXP, mYm, mZm, matrixStack).colored().tex(B_maxU, B_minV).lightedAndEnd()
      buffer.pos(mXm, mYm, mZm, matrixStack).colored().tex(B_maxU, B_maxV).lightedAndEnd()
      buffer.pos(mXm, mYP, mZm, matrixStack).colored().tex(B_minU, B_maxV).lightedAndEnd()

      buffer.pos(MXP, mYP, mZm, matrixStack).colored().tex(B_minU, B_minV).lightedAndEnd()
      buffer.pos(MXP, mYm, mZm, matrixStack).colored().tex(B_maxU, B_minV).lightedAndEnd()
      buffer.pos(MXm, mYm, mZm, matrixStack).colored().tex(B_maxU, B_maxV).lightedAndEnd()
      buffer.pos(MXm, mYP, mZm, matrixStack).colored().tex(B_minU, B_maxV).lightedAndEnd()

      buffer.pos(mXP, mYP, MZP, matrixStack).colored().tex(B_minU, B_minV).lightedAndEnd()
      buffer.pos(mXm, mYP, MZP, matrixStack).colored().tex(B_minU, B_maxV).lightedAndEnd()
      buffer.pos(mXm, mYm, MZP, matrixStack).colored().tex(B_maxU, B_maxV).lightedAndEnd()
      buffer.pos(mXP, mYm, MZP, matrixStack).colored().tex(B_maxU, B_minV).lightedAndEnd()

      buffer.pos(MXP, mYP, MZP, matrixStack).colored().tex(B_minU, B_minV).lightedAndEnd()
      buffer.pos(MXm, mYP, MZP, matrixStack).colored().tex(B_minU, B_maxV).lightedAndEnd()
      buffer.pos(MXm, mYm, MZP, matrixStack).colored().tex(B_maxU, B_maxV).lightedAndEnd()
      buffer.pos(MXP, mYm, MZP, matrixStack).colored().tex(B_maxU, B_minV).lightedAndEnd()

      buffer.pos(mXP, MYP, mZm, matrixStack).colored().tex(B_minU, B_minV).lightedAndEnd()
      buffer.pos(mXP, MYm, mZm, matrixStack).colored().tex(B_maxU, B_minV).lightedAndEnd()
      buffer.pos(mXm, MYm, mZm, matrixStack).colored().tex(B_maxU, B_maxV).lightedAndEnd()
      buffer.pos(mXm, MYP, mZm, matrixStack).colored().tex(B_minU, B_maxV).lightedAndEnd()

      buffer.pos(MXP, MYP, mZm, matrixStack).colored().tex(B_minU, B_minV).lightedAndEnd()
      buffer.pos(MXP, MYm, mZm, matrixStack).colored().tex(B_maxU, B_minV).lightedAndEnd()
      buffer.pos(MXm, MYm, mZm, matrixStack).colored().tex(B_maxU, B_maxV).lightedAndEnd()
      buffer.pos(MXm, MYP, mZm, matrixStack).colored().tex(B_minU, B_maxV).lightedAndEnd()

      buffer.pos(mXP, MYP, MZP, matrixStack).colored().tex(B_minU, B_minV).lightedAndEnd()
      buffer.pos(mXm, MYP, MZP, matrixStack).colored().tex(B_minU, B_maxV).lightedAndEnd()
      buffer.pos(mXm, MYm, MZP, matrixStack).colored().tex(B_maxU, B_maxV).lightedAndEnd()
      buffer.pos(mXP, MYm, MZP, matrixStack).colored().tex(B_maxU, B_minV).lightedAndEnd()

      buffer.pos(MXP, MYP, MZP, matrixStack).colored().tex(B_minU, B_minV).lightedAndEnd()
      buffer.pos(MXm, MYP, MZP, matrixStack).colored().tex(B_minU, B_maxV).lightedAndEnd()
      buffer.pos(MXm, MYm, MZP, matrixStack).colored().tex(B_maxU, B_maxV).lightedAndEnd()
      buffer.pos(MXP, MYm, MZP, matrixStack).colored().tex(B_maxU, B_minV).lightedAndEnd()

      //x
      buffer.pos(mXm, mYP, mZP, matrixStack).colored().tex(B_minU, B_minV).lightedAndEnd()
      buffer.pos(mXm, mYP, mZm, matrixStack).colored().tex(B_minU, B_maxV).lightedAndEnd()
      buffer.pos(mXm, mYm, mZm, matrixStack).colored().tex(B_maxU, B_maxV).lightedAndEnd()
      buffer.pos(mXm, mYm, mZP, matrixStack).colored().tex(B_maxU, B_minV).lightedAndEnd()

      buffer.pos(mXm, mYP, MZP, matrixStack).colored().tex(B_minU, B_minV).lightedAndEnd()
      buffer.pos(mXm, mYP, MZm, matrixStack).colored().tex(B_minU, B_maxV).lightedAndEnd()
      buffer.pos(mXm, mYm, MZm, matrixStack).colored().tex(B_maxU, B_maxV).lightedAndEnd()
      buffer.pos(mXm, mYm, MZP, matrixStack).colored().tex(B_maxU, B_minV).lightedAndEnd()

      buffer.pos(mXm, MYP, mZP, matrixStack).colored().tex(B_minU, B_minV).lightedAndEnd()
      buffer.pos(mXm, MYP, mZm, matrixStack).colored().tex(B_minU, B_maxV).lightedAndEnd()
      buffer.pos(mXm, MYm, mZm, matrixStack).colored().tex(B_maxU, B_maxV).lightedAndEnd()
      buffer.pos(mXm, MYm, mZP, matrixStack).colored().tex(B_maxU, B_minV).lightedAndEnd()

      buffer.pos(mXm, MYP, MZP, matrixStack).colored().tex(B_minU, B_minV).lightedAndEnd()
      buffer.pos(mXm, MYP, MZm, matrixStack).colored().tex(B_minU, B_maxV).lightedAndEnd()
      buffer.pos(mXm, MYm, MZm, matrixStack).colored().tex(B_maxU, B_maxV).lightedAndEnd()
      buffer.pos(mXm, MYm, MZP, matrixStack).colored().tex(B_maxU, B_minV).lightedAndEnd()

      buffer.pos(MXP, mYP, mZP, matrixStack).colored().tex(B_minU, B_minV).lightedAndEnd()
      buffer.pos(MXP, mYm, mZP, matrixStack).colored().tex(B_maxU, B_minV).lightedAndEnd()
      buffer.pos(MXP, mYm, mZm, matrixStack).colored().tex(B_maxU, B_maxV).lightedAndEnd()
      buffer.pos(MXP, mYP, mZm, matrixStack).colored().tex(B_minU, B_maxV).lightedAndEnd()

      buffer.pos(MXP, mYP, MZP, matrixStack).colored().tex(B_minU, B_minV).lightedAndEnd()
      buffer.pos(MXP, mYm, MZP, matrixStack).colored().tex(B_maxU, B_minV).lightedAndEnd()
      buffer.pos(MXP, mYm, MZm, matrixStack).colored().tex(B_maxU, B_maxV).lightedAndEnd()
      buffer.pos(MXP, mYP, MZm, matrixStack).colored().tex(B_minU, B_maxV).lightedAndEnd()

      buffer.pos(MXP, MYP, mZP, matrixStack).colored().tex(B_minU, B_minV).lightedAndEnd()
      buffer.pos(MXP, MYm, mZP, matrixStack).colored().tex(B_maxU, B_minV).lightedAndEnd()
      buffer.pos(MXP, MYm, mZm, matrixStack).colored().tex(B_maxU, B_maxV).lightedAndEnd()
      buffer.pos(MXP, MYP, mZm, matrixStack).colored().tex(B_minU, B_maxV).lightedAndEnd()

      buffer.pos(MXP, MYP, MZP, matrixStack).colored().tex(B_minU, B_minV).lightedAndEnd()
      buffer.pos(MXP, MYm, MZP, matrixStack).colored().tex(B_maxU, B_minV).lightedAndEnd()
      buffer.pos(MXP, MYm, MZm, matrixStack).colored().tex(B_maxU, B_maxV).lightedAndEnd()
      buffer.pos(MXP, MYP, MZm, matrixStack).colored().tex(B_minU, B_maxV).lightedAndEnd()

      //y
      // Lower Side
      buffer.pos(mXP, mYm, mZP, matrixStack).colored().tex(B_minU, B_minV).lightedAndEnd()
      buffer.pos(mXm, mYm, mZP, matrixStack).colored().tex(B_maxU, B_minV).lightedAndEnd()
      buffer.pos(mXm, mYm, mZm, matrixStack).colored().tex(B_maxU, B_maxV).lightedAndEnd()
      buffer.pos(mXP, mYm, mZm, matrixStack).colored().tex(B_minU, B_maxV).lightedAndEnd()

      buffer.pos(MXP, mYm, mZP, matrixStack).colored().tex(B_minU, B_minV).lightedAndEnd()
      buffer.pos(MXm, mYm, mZP, matrixStack).colored().tex(B_maxU, B_minV).lightedAndEnd()
      buffer.pos(MXm, mYm, mZm, matrixStack).colored().tex(B_maxU, B_maxV).lightedAndEnd()
      buffer.pos(MXP, mYm, mZm, matrixStack).colored().tex(B_minU, B_maxV).lightedAndEnd()

      buffer.pos(mXP, mYm, MZP, matrixStack).colored().tex(B_minU, B_minV).lightedAndEnd()
      buffer.pos(mXm, mYm, MZP, matrixStack).colored().tex(B_maxU, B_minV).lightedAndEnd()
      buffer.pos(mXm, mYm, MZm, matrixStack).colored().tex(B_maxU, B_maxV).lightedAndEnd()
      buffer.pos(mXP, mYm, MZm, matrixStack).colored().tex(B_minU, B_maxV).lightedAndEnd()

      buffer.pos(MXP, mYm, MZP, matrixStack).colored().tex(B_minU, B_minV).lightedAndEnd()
      buffer.pos(MXm, mYm, MZP, matrixStack).colored().tex(B_maxU, B_minV).lightedAndEnd()
      buffer.pos(MXm, mYm, MZm, matrixStack).colored().tex(B_maxU, B_maxV).lightedAndEnd()
      buffer.pos(MXP, mYm, MZm, matrixStack).colored().tex(B_minU, B_maxV).lightedAndEnd()

      // Upper Side
      buffer.pos(mXP, MYP, mZP, matrixStack).colored().tex(B_minU, B_minV).lightedAndEnd()
      buffer.pos(mXP, MYP, mZm, matrixStack).colored().tex(B_minU, B_maxV).lightedAndEnd()
      buffer.pos(mXm, MYP, mZm, matrixStack).colored().tex(B_maxU, B_maxV).lightedAndEnd()
      buffer.pos(mXm, MYP, mZP, matrixStack).colored().tex(B_maxU, B_minV).lightedAndEnd()

      buffer.pos(MXP, MYP, mZP, matrixStack).colored().tex(B_minU, B_minV).lightedAndEnd()
      buffer.pos(MXP, MYP, mZm, matrixStack).colored().tex(B_minU, B_maxV).lightedAndEnd()
      buffer.pos(MXm, MYP, mZm, matrixStack).colored().tex(B_maxU, B_maxV).lightedAndEnd()
      buffer.pos(MXm, MYP, mZP, matrixStack).colored().tex(B_maxU, B_minV).lightedAndEnd()

      buffer.pos(mXP, MYP, MZP, matrixStack).colored().tex(B_minU, B_minV).lightedAndEnd()
      buffer.pos(mXP, MYP, MZm, matrixStack).colored().tex(B_minU, B_maxV).lightedAndEnd()
      buffer.pos(mXm, MYP, MZm, matrixStack).colored().tex(B_maxU, B_maxV).lightedAndEnd()
      buffer.pos(mXm, MYP, MZP, matrixStack).colored().tex(B_maxU, B_minV).lightedAndEnd()

      buffer.pos(MXP, MYP, MZP, matrixStack).colored().tex(B_minU, B_minV).lightedAndEnd()
      buffer.pos(MXP, MYP, MZm, matrixStack).colored().tex(B_minU, B_maxV).lightedAndEnd()
      buffer.pos(MXm, MYP, MZm, matrixStack).colored().tex(B_maxU, B_maxV).lightedAndEnd()
      buffer.pos(MXm, MYP, MZP, matrixStack).colored().tex(B_maxU, B_minV).lightedAndEnd()

      matrixStack.func_227865_b_()
      Minecraft.getInstance.getProfiler.endSection()
    }

    if (quarry.action.mode == TileQuarry2.breakBlock) {
      Minecraft.getInstance.getProfiler.startSection("drill")
      matrixStack.func_227860_a_()
      matrixStack.func_227861_a_(-pos.getX + .5, -pos.getY + .5, -pos.getZ + .5)
      val minX = quarry.area.xMin
      val minZ = quarry.area.zMin
      val maxX = quarry.area.xMax
      val maxY = quarry.area.yMax
      val maxZ = quarry.area.zMax
      val headPosX = quarry.action.asInstanceOf[QuarryAction.BreakBlock].headX
      val headPosY = quarry.action.asInstanceOf[QuarryAction.BreakBlock].headY
      val headPosZ = quarry.action.asInstanceOf[QuarryAction.BreakBlock].headZ
      //render crossed frame
      val D_minU = drillStripe.getMinU
      val D_minV = drillStripe.getMinV
      val D_maxU = drillStripe.getInterpolatedU(8)
      val D_maxV = drillStripe.getMaxV

      val hXmd = headPosX - d4
      val hXPd = headPosX + d4
      val MYmd = maxY - d4
      val MYPd = maxY + d4
      val hZmd = headPosZ - d4
      val hZPd = headPosZ + d4

      def xLine(plus: Boolean, floor: Int, length: Double): Unit = {
        val f = if (plus) plusF else minusF

        var i1 = 0
        while (i1 < floor) {
          val i2 = i1 + 1
          val fX1 = f(headPosX, d4 + i1)
          val fX2 = f(headPosX, d4 + i2)
          if (plus) {
            buffer.pos(fX1, MYPd, hZmd, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
            buffer.pos(fX1, MYPd, hZPd, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()
            buffer.pos(fX2, MYPd, hZPd, matrixStack).colored().tex(D_maxU, D_maxV).lightedAndEnd()
            buffer.pos(fX2, MYPd, hZmd, matrixStack).colored().tex(D_minU, D_maxV).lightedAndEnd()

            buffer.pos(fX1, MYmd, hZmd, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
            buffer.pos(fX2, MYmd, hZmd, matrixStack).colored().tex(D_minU, D_maxV).lightedAndEnd()
            buffer.pos(fX2, MYmd, hZPd, matrixStack).colored().tex(D_maxU, D_maxV).lightedAndEnd()
            buffer.pos(fX1, MYmd, hZPd, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()

            buffer.pos(fX2, MYPd, hZmd, matrixStack).colored().tex(D_maxU, D_maxV).lightedAndEnd()
            buffer.pos(fX2, MYmd, hZmd, matrixStack).colored().tex(D_minU, D_maxV).lightedAndEnd()
            buffer.pos(fX1, MYmd, hZmd, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
            buffer.pos(fX1, MYPd, hZmd, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()

            buffer.pos(fX2, MYPd, hZPd, matrixStack).colored().tex(D_maxU, D_maxV).lightedAndEnd()
            buffer.pos(fX1, MYPd, hZPd, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()
            buffer.pos(fX1, MYmd, hZPd, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
            buffer.pos(fX2, MYmd, hZPd, matrixStack).colored().tex(D_minU, D_maxV).lightedAndEnd()
          } else {
            buffer.pos(fX1, MYPd, hZmd, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
            buffer.pos(fX2, MYPd, hZmd, matrixStack).colored().tex(D_minU, D_maxV).lightedAndEnd()
            buffer.pos(fX2, MYPd, hZPd, matrixStack).colored().tex(D_maxU, D_maxV).lightedAndEnd()
            buffer.pos(fX1, MYPd, hZPd, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()

            buffer.pos(fX1, MYmd, hZmd, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
            buffer.pos(fX1, MYmd, hZPd, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()
            buffer.pos(fX2, MYmd, hZPd, matrixStack).colored().tex(D_maxU, D_maxV).lightedAndEnd()
            buffer.pos(fX2, MYmd, hZmd, matrixStack).colored().tex(D_minU, D_maxV).lightedAndEnd()

            buffer.pos(fX2, MYPd, hZmd, matrixStack).colored().tex(D_maxU, D_maxV).lightedAndEnd()
            buffer.pos(fX1, MYPd, hZmd, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()
            buffer.pos(fX1, MYmd, hZmd, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
            buffer.pos(fX2, MYmd, hZmd, matrixStack).colored().tex(D_minU, D_maxV).lightedAndEnd()

            buffer.pos(fX2, MYPd, hZPd, matrixStack).colored().tex(D_maxU, D_maxV).lightedAndEnd()
            buffer.pos(fX2, MYmd, hZPd, matrixStack).colored().tex(D_minU, D_maxV).lightedAndEnd()
            buffer.pos(fX1, MYmd, hZPd, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
            buffer.pos(fX1, MYPd, hZPd, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()
          }
          i1 += 1
        }
        val fixedV = drillStripe.getInterpolatedV((length - floor) * 16)
        val xF = f(headPosX, d4 + floor)
        val xL = f(headPosX, d4 + length)
        if (plus) {
          buffer.pos(xF, MYPd, hZmd, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
          buffer.pos(xF, MYPd, hZPd, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()
          buffer.pos(xL, MYPd, hZPd, matrixStack).colored().tex(D_maxU, fixedV).lightedAndEnd()
          buffer.pos(xL, MYPd, hZmd, matrixStack).colored().tex(D_minU, fixedV).lightedAndEnd()

          buffer.pos(xF, MYmd, hZmd, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
          buffer.pos(xL, MYmd, hZmd, matrixStack).colored().tex(D_minU, fixedV).lightedAndEnd()
          buffer.pos(xL, MYmd, hZPd, matrixStack).colored().tex(D_maxU, fixedV).lightedAndEnd()
          buffer.pos(xF, MYmd, hZPd, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()

          buffer.pos(xL, MYPd, hZmd, matrixStack).colored().tex(D_maxU, fixedV).lightedAndEnd()
          buffer.pos(xL, MYmd, hZmd, matrixStack).colored().tex(D_minU, fixedV).lightedAndEnd()
          buffer.pos(xF, MYmd, hZmd, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
          buffer.pos(xF, MYPd, hZmd, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()

          buffer.pos(xL, MYPd, hZPd, matrixStack).colored().tex(D_maxU, fixedV).lightedAndEnd()
          buffer.pos(xF, MYPd, hZPd, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()
          buffer.pos(xF, MYmd, hZPd, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
          buffer.pos(xL, MYmd, hZPd, matrixStack).colored().tex(D_minU, fixedV).lightedAndEnd()
        } else {
          buffer.pos(xF, MYPd, hZmd, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
          buffer.pos(xL, MYPd, hZmd, matrixStack).colored().tex(D_minU, fixedV).lightedAndEnd()
          buffer.pos(xL, MYPd, hZPd, matrixStack).colored().tex(D_maxU, fixedV).lightedAndEnd()
          buffer.pos(xF, MYPd, hZPd, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()

          buffer.pos(xF, MYmd, hZmd, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
          buffer.pos(xF, MYmd, hZPd, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()
          buffer.pos(xL, MYmd, hZPd, matrixStack).colored().tex(D_maxU, fixedV).lightedAndEnd()
          buffer.pos(xL, MYmd, hZmd, matrixStack).colored().tex(D_minU, fixedV).lightedAndEnd()

          buffer.pos(xL, MYPd, hZmd, matrixStack).colored().tex(D_maxU, fixedV).lightedAndEnd()
          buffer.pos(xF, MYPd, hZmd, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()
          buffer.pos(xF, MYmd, hZmd, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
          buffer.pos(xL, MYmd, hZmd, matrixStack).colored().tex(D_minU, fixedV).lightedAndEnd()

          buffer.pos(xL, MYPd, hZPd, matrixStack).colored().tex(D_maxU, fixedV).lightedAndEnd()
          buffer.pos(xL, MYmd, hZPd, matrixStack).colored().tex(D_minU, fixedV).lightedAndEnd()
          buffer.pos(xF, MYmd, hZPd, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
          buffer.pos(xF, MYPd, hZPd, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()
        }
      }
      //X lines

      //positive(East)
      val xp_length = maxX - headPosX - d4 * 2
      val xp_floor = MathHelper.floor(xp_length)
      xLine(plus = true, xp_floor, xp_length)

      //negative(West)
      val xn_length = headPosX - minX - d4 * 2
      val xn_floor = MathHelper.floor(xn_length)
      xLine(plus = false, xn_floor, xn_length)


      def zLine(plus: Boolean, floor: Int, length: Double): Unit = {
        val f = if (plus) plusF else minusF
        var i1 = 0
        while (i1 < floor) {
          val i2 = i1 + 1
          val fZ1 = f(headPosZ, d4 + i1)
          val fZ2 = f(headPosZ, d4 + i2)
          if (plus) {
            buffer.pos(hXmd, MYPd, fZ1, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
            buffer.pos(hXmd, MYPd, fZ2, matrixStack).colored().tex(D_minU, D_maxV).lightedAndEnd()
            buffer.pos(hXPd, MYPd, fZ2, matrixStack).colored().tex(D_maxU, D_maxV).lightedAndEnd()
            buffer.pos(hXPd, MYPd, fZ1, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()

            buffer.pos(hXmd, MYmd, fZ1, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
            buffer.pos(hXPd, MYmd, fZ1, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()
            buffer.pos(hXPd, MYmd, fZ2, matrixStack).colored().tex(D_maxU, D_maxV).lightedAndEnd()
            buffer.pos(hXmd, MYmd, fZ2, matrixStack).colored().tex(D_minU, D_maxV).lightedAndEnd()

            buffer.pos(hXmd, MYPd, fZ2, matrixStack).colored().tex(D_maxU, D_maxV).lightedAndEnd()
            buffer.pos(hXmd, MYPd, fZ1, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()
            buffer.pos(hXmd, MYmd, fZ1, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
            buffer.pos(hXmd, MYmd, fZ2, matrixStack).colored().tex(D_minU, D_maxV).lightedAndEnd()

            buffer.pos(hXPd, MYPd, fZ2, matrixStack).colored().tex(D_maxU, D_maxV).lightedAndEnd()
            buffer.pos(hXPd, MYmd, fZ2, matrixStack).colored().tex(D_minU, D_maxV).lightedAndEnd()
            buffer.pos(hXPd, MYmd, fZ1, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
            buffer.pos(hXPd, MYPd, fZ1, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()
          } else {
            buffer.pos(hXmd, MYPd, fZ1, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
            buffer.pos(hXPd, MYPd, fZ1, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()
            buffer.pos(hXPd, MYPd, fZ2, matrixStack).colored().tex(D_maxU, D_maxV).lightedAndEnd()
            buffer.pos(hXmd, MYPd, fZ2, matrixStack).colored().tex(D_minU, D_maxV).lightedAndEnd()

            buffer.pos(hXmd, MYmd, fZ1, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
            buffer.pos(hXmd, MYmd, fZ2, matrixStack).colored().tex(D_minU, D_maxV).lightedAndEnd()
            buffer.pos(hXPd, MYmd, fZ2, matrixStack).colored().tex(D_maxU, D_maxV).lightedAndEnd()
            buffer.pos(hXPd, MYmd, fZ1, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()

            buffer.pos(hXmd, MYPd, fZ2, matrixStack).colored().tex(D_maxU, D_maxV).lightedAndEnd()
            buffer.pos(hXmd, MYmd, fZ2, matrixStack).colored().tex(D_minU, D_maxV).lightedAndEnd()
            buffer.pos(hXmd, MYmd, fZ1, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
            buffer.pos(hXmd, MYPd, fZ1, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()

            buffer.pos(hXPd, MYPd, fZ2, matrixStack).colored().tex(D_maxU, D_maxV).lightedAndEnd()
            buffer.pos(hXPd, MYPd, fZ1, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()
            buffer.pos(hXPd, MYmd, fZ1, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
            buffer.pos(hXPd, MYmd, fZ2, matrixStack).colored().tex(D_minU, D_maxV).lightedAndEnd()
          }
          i1 += 1
        }
        val fixedV = drillStripe.getInterpolatedV((length - floor) * 16)
        val zF = f(headPosZ, d4 + floor)
        val zL = f(headPosZ, d4 + length)
        if (plus) {
          buffer.pos(hXmd, MYPd, zF, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
          buffer.pos(hXmd, MYPd, zL, matrixStack).colored().tex(D_minU, fixedV).lightedAndEnd()
          buffer.pos(hXPd, MYPd, zL, matrixStack).colored().tex(D_maxU, fixedV).lightedAndEnd()
          buffer.pos(hXPd, MYPd, zF, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()

          buffer.pos(hXmd, MYmd, zF, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
          buffer.pos(hXPd, MYmd, zF, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()
          buffer.pos(hXPd, MYmd, zL, matrixStack).colored().tex(D_maxU, fixedV).lightedAndEnd()
          buffer.pos(hXmd, MYmd, zL, matrixStack).colored().tex(D_minU, fixedV).lightedAndEnd()

          buffer.pos(hXmd, MYPd, zL, matrixStack).colored().tex(D_maxU, fixedV).lightedAndEnd()
          buffer.pos(hXmd, MYPd, zF, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()
          buffer.pos(hXmd, MYmd, zF, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
          buffer.pos(hXmd, MYmd, zL, matrixStack).colored().tex(D_minU, fixedV).lightedAndEnd()

          buffer.pos(hXPd, MYPd, zL, matrixStack).colored().tex(D_maxU, fixedV).lightedAndEnd()
          buffer.pos(hXPd, MYmd, zL, matrixStack).colored().tex(D_minU, fixedV).lightedAndEnd()
          buffer.pos(hXPd, MYmd, zF, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
          buffer.pos(hXPd, MYPd, zF, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()
        } else {
          buffer.pos(hXmd, MYPd, zF, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
          buffer.pos(hXPd, MYPd, zF, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()
          buffer.pos(hXPd, MYPd, zL, matrixStack).colored().tex(D_maxU, fixedV).lightedAndEnd()
          buffer.pos(hXmd, MYPd, zL, matrixStack).colored().tex(D_minU, fixedV).lightedAndEnd()

          buffer.pos(hXmd, MYmd, zF, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
          buffer.pos(hXmd, MYmd, zL, matrixStack).colored().tex(D_minU, fixedV).lightedAndEnd()
          buffer.pos(hXPd, MYmd, zL, matrixStack).colored().tex(D_maxU, fixedV).lightedAndEnd()
          buffer.pos(hXPd, MYmd, zF, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()

          buffer.pos(hXmd, MYPd, zL, matrixStack).colored().tex(D_maxU, fixedV).lightedAndEnd()
          buffer.pos(hXmd, MYmd, zL, matrixStack).colored().tex(D_minU, fixedV).lightedAndEnd()
          buffer.pos(hXmd, MYmd, zF, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
          buffer.pos(hXmd, MYPd, zF, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()

          buffer.pos(hXPd, MYPd, zL, matrixStack).colored().tex(D_maxU, fixedV).lightedAndEnd()
          buffer.pos(hXPd, MYPd, zF, matrixStack).colored().tex(D_maxU, D_minV).lightedAndEnd()
          buffer.pos(hXPd, MYmd, zF, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
          buffer.pos(hXPd, MYmd, zL, matrixStack).colored().tex(D_minU, fixedV).lightedAndEnd()
        }
      }
      //Z lines

      //positive(South)
      val zp_length = maxZ - headPosZ - d4 * 2
      val zp_floor = MathHelper.floor(zp_length)
      zLine(plus = true, zp_floor, zp_length)
      //negative(North)
      val zn_length = headPosZ - minZ - d4 * 2
      val zn_floor = MathHelper.floor(zn_length)
      zLine(plus = false, zn_floor, zn_length)


      //render drill
      def yLine(floor: Int, length: Double): Unit = {
        val D_I8dV = drillStripe.getInterpolatedV(8d)
        val D_16dU = drillStripe.getMaxU
        //Top
        buffer.pos(hXmd, MYPd, hZmd, matrixStack).colored().tex(D_minU, D_I8dV).lightedAndEnd()
        buffer.pos(hXmd, MYPd, hZPd, matrixStack).colored().tex(D_maxU, D_I8dV).lightedAndEnd()
        buffer.pos(hXPd, MYPd, hZPd, matrixStack).colored().tex(D_maxU, drillStripe.getMaxV).lightedAndEnd()
        buffer.pos(hXPd, MYPd, hZmd, matrixStack).colored().tex(D_minU, drillStripe.getMaxV).lightedAndEnd()

        var i1 = 0
        while (i1 < floor) {
          val i2 = i1 + 1
          val MY1 = MYPd - i1
          val MY2 = MYPd - i2
          buffer.pos(hXPd, MY1, hZmd, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
          buffer.pos(hXPd, MY2, hZmd, matrixStack).colored().tex(D_16dU, D_minV).lightedAndEnd()
          buffer.pos(hXmd, MY2, hZmd, matrixStack).colored().tex(D_16dU, D_I8dV).lightedAndEnd()
          buffer.pos(hXmd, MY1, hZmd, matrixStack).colored().tex(D_minU, D_I8dV).lightedAndEnd()

          buffer.pos(hXPd, MY1, hZPd, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
          buffer.pos(hXmd, MY1, hZPd, matrixStack).colored().tex(D_minU, D_I8dV).lightedAndEnd()
          buffer.pos(hXmd, MY2, hZPd, matrixStack).colored().tex(D_16dU, D_I8dV).lightedAndEnd()
          buffer.pos(hXPd, MY2, hZPd, matrixStack).colored().tex(D_16dU, D_minV).lightedAndEnd()

          buffer.pos(hXPd, MY1, hZPd, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
          buffer.pos(hXPd, MY2, hZPd, matrixStack).colored().tex(D_16dU, D_minV).lightedAndEnd()
          buffer.pos(hXPd, MY2, hZmd, matrixStack).colored().tex(D_16dU, D_I8dV).lightedAndEnd()
          buffer.pos(hXPd, MY1, hZmd, matrixStack).colored().tex(D_minU, D_I8dV).lightedAndEnd()

          buffer.pos(hXmd, MY1, hZPd, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
          buffer.pos(hXmd, MY1, hZmd, matrixStack).colored().tex(D_minU, D_I8dV).lightedAndEnd()
          buffer.pos(hXmd, MY2, hZmd, matrixStack).colored().tex(D_16dU, D_I8dV).lightedAndEnd()
          buffer.pos(hXmd, MY2, hZPd, matrixStack).colored().tex(D_16dU, D_minV).lightedAndEnd()
          i1 += 1
        }
        val fixedU = drillStripe.getInterpolatedU((length - floor) * 16)
        val MYF = MYPd - floor
        val MYL = MYPd - length
        buffer.pos(hXPd, MYF, hZmd, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
        buffer.pos(hXPd, MYL, hZmd, matrixStack).colored().tex(fixedU, D_minV).lightedAndEnd()
        buffer.pos(hXmd, MYL, hZmd, matrixStack).colored().tex(fixedU, D_I8dV).lightedAndEnd()
        buffer.pos(hXmd, MYF, hZmd, matrixStack).colored().tex(D_minU, D_I8dV).lightedAndEnd()

        buffer.pos(hXPd, MYF, hZPd, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
        buffer.pos(hXmd, MYF, hZPd, matrixStack).colored().tex(D_minU, D_I8dV).lightedAndEnd()
        buffer.pos(hXmd, MYL, hZPd, matrixStack).colored().tex(fixedU, D_I8dV).lightedAndEnd()
        buffer.pos(hXPd, MYL, hZPd, matrixStack).colored().tex(fixedU, D_minV).lightedAndEnd()

        buffer.pos(hXPd, MYF, hZPd, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
        buffer.pos(hXPd, MYL, hZPd, matrixStack).colored().tex(fixedU, D_minV).lightedAndEnd()
        buffer.pos(hXPd, MYL, hZmd, matrixStack).colored().tex(fixedU, D_I8dV).lightedAndEnd()
        buffer.pos(hXPd, MYF, hZmd, matrixStack).colored().tex(D_minU, D_I8dV).lightedAndEnd()

        buffer.pos(hXmd, MYF, hZPd, matrixStack).colored().tex(D_minU, D_minV).lightedAndEnd()
        buffer.pos(hXmd, MYF, hZmd, matrixStack).colored().tex(D_minU, D_I8dV).lightedAndEnd()
        buffer.pos(hXmd, MYL, hZmd, matrixStack).colored().tex(fixedU, D_I8dV).lightedAndEnd()
        buffer.pos(hXmd, MYL, hZPd, matrixStack).colored().tex(fixedU, D_minV).lightedAndEnd()

        //Bottom
        buffer.pos(hXmd, MYL, hZmd, matrixStack).colored().tex(D_minU, D_I8dV).lightedAndEnd()
        buffer.pos(hXPd, MYL, hZmd, matrixStack).colored().tex(D_minU, drillStripe.getMaxV).lightedAndEnd()
        buffer.pos(hXPd, MYL, hZPd, matrixStack).colored().tex(D_maxU, drillStripe.getMaxV).lightedAndEnd()
        buffer.pos(hXmd, MYL, hZPd, matrixStack).colored().tex(D_maxU, D_I8dV).lightedAndEnd()

        //Drill
        val xm = headPosX - d4 / 2
        val xP = headPosX + d4 / 2
        val zm = headPosZ - d4 / 2
        val zP = headPosZ + d4 / 2
        val yT = headPosY + 1
        val yB = headPosY + 0
        val hmU = headSprite.getMinU
        val hMU = headSprite.getMaxU
        val hmV = headSprite.getMinV
        val hMV = headSprite.getInterpolatedV(4)
        buffer.pos(xP, yT, zm, matrixStack).colored().tex(hmU, hmV).lightedAndEnd()
        buffer.pos(xP, yB, zm, matrixStack).colored().tex(hMU, hmV).lightedAndEnd()
        buffer.pos(xm, yB, zm, matrixStack).colored().tex(hMU, hMV).lightedAndEnd()
        buffer.pos(xm, yT, zm, matrixStack).colored().tex(hmU, hMV).lightedAndEnd()

        buffer.pos(xm, yT, zP, matrixStack).colored().tex(hmU, hmV).lightedAndEnd()
        buffer.pos(xm, yB, zP, matrixStack).colored().tex(hMU, hmV).lightedAndEnd()
        buffer.pos(xP, yB, zP, matrixStack).colored().tex(hMU, hMV).lightedAndEnd()
        buffer.pos(xP, yT, zP, matrixStack).colored().tex(hmU, hMV).lightedAndEnd()

        buffer.pos(xP, yT, zP, matrixStack).colored().tex(hmU, hmV).lightedAndEnd()
        buffer.pos(xP, yB, zP, matrixStack).colored().tex(hMU, hmV).lightedAndEnd()
        buffer.pos(xP, yB, zm, matrixStack).colored().tex(hMU, hMV).lightedAndEnd()
        buffer.pos(xP, yT, zm, matrixStack).colored().tex(hmU, hMV).lightedAndEnd()

        buffer.pos(xm, yT, zm, matrixStack).colored().tex(hmU, hmV).lightedAndEnd()
        buffer.pos(xm, yB, zm, matrixStack).colored().tex(hMU, hmV).lightedAndEnd()
        buffer.pos(xm, yB, zP, matrixStack).colored().tex(hMU, hMV).lightedAndEnd()
        buffer.pos(xm, yT, zP, matrixStack).colored().tex(hmU, hMV).lightedAndEnd()
      }

      val y_length = maxY - headPosY - 0.75
      val y_floor = MathHelper.floor(y_length)
      yLine(y_floor, y_length)

      matrixStack.func_227865_b_()
      Minecraft.getInstance.getProfiler.endSection()
    }

    Minecraft.getInstance.getProfiler.endSection()
    Minecraft.getInstance.getProfiler.endSection()
  }

  override def isGlobalRenderer(te: TileQuarry2): Boolean = true
}
