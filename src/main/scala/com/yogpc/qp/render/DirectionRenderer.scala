package com.yogpc.qp.render

import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing._
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@SideOnly(Side.CLIENT)
abstract sealed class DirectionRenderer(val d: EnumFacing) {
    //        -> V
    //   ┌──────────┐
    //   │ 1      4 │
    // | │          │
    // V │          │
    //   │ 2      3 │
    // U └──────────┘
    //

    protected val originIndex: Int
    protected val uVector: EnumFacing
    protected val vVector: EnumFacing
    protected val order: Array[Func]

    def render(pos: Vec3d, buffer: VertexBuffer, scaleX: Double, scaleY: Double, scaleZ: Double, l1: Int, l2: Int, sprite: TextureAtlasSprite): Unit = {
        val max = Math.max(Math.max(scaleX, scaleY), scaleZ)
        render(pos, buffer, scaleX, scaleY, scaleZ, l1, l2,
            sprite.getMinU,
            sprite.getMinV,
            sprite.getInterpolatedU(getUInterpolate(scaleX, scaleY, scaleZ, max)),
            sprite.getInterpolatedV(getVInterpolate(scaleX, scaleY, scaleZ, max)))
    }

    def render(pos: Vec3d, buffer: VertexBuffer, scales: (Double, Double, Double), l1: Int, l2: Int, minU: Double, minV: Double, maxU: Double, maxV: Double): Unit = {
        render(pos, buffer, scales._1, scales._2, scales._3, l1, l2, minU, minV, maxU, maxV)
    }

    def render(pos: Vec3d, buffer: VertexBuffer, scaleX: Double, scaleY: Double, scaleZ: Double, l1: Int, l2: Int, minU: Double, minV: Double, maxU: Double, maxV: Double): Unit = {
        val vec = offset(pos, scaleX, scaleY, scaleZ)
        buffer.pos(order(0)(vec, scaleX, scaleY, scaleZ)).colored().tex(minU, minV).lightmap(l1, l2).endVertex()
        buffer.pos(order(1)(vec, scaleX, scaleY, scaleZ)).colored().tex(maxU, minV).lightmap(l1, l2).endVertex()
        buffer.pos(order(2)(vec, scaleX, scaleY, scaleZ)).colored().tex(maxU, maxV).lightmap(l1, l2).endVertex()
        buffer.pos(order(3)(vec, scaleX, scaleY, scaleZ)).colored().tex(minU, maxV).lightmap(l1, l2).endVertex()

    }

    protected def offset(vec: Vec3d, scaleX: Double, scaleY: Double, scaleZ: Double): Vec3d

    def getUInterpolate(scaleX: Double, scaleY: Double, scaleZ: Double, max: Double): Double = {
        DirectionRenderer.axixScale(uVector.getAxis, scaleX / max, scaleY / max, scaleZ / max) * 16
    }

    def getVInterpolate(scaleX: Double, scaleY: Double, scaleZ: Double, max: Double): Double = {
        DirectionRenderer.axixScale(vVector.getAxis, scaleX / max, scaleY / max, scaleZ / max) * 16
    }

    protected trait Func {
        def apply(v: Vec3d, scaleX: Double, scaleY: Double, scaleZ: Double): Vec3d
    }

    protected val ORIGIN = new Func {
        override def apply(v: Vec3d, scaleX: Double, scaleY: Double, scaleZ: Double): Vec3d = v
    }

    protected val U = new Func {
        override def apply(v: Vec3d, scaleX: Double, scaleY: Double, scaleZ: Double): Vec3d = {
            v + new Vec3d(uVector.getDirectionVec).scale(DirectionRenderer.axixScale(uVector.getAxis, scaleX, scaleY, scaleZ))
        }
    }

    protected val V = new Func {
        override def apply(v: Vec3d, scaleX: Double, scaleY: Double, scaleZ: Double): Vec3d = {
            v + new Vec3d(vVector.getDirectionVec).scale(DirectionRenderer.axixScale(vVector.getAxis, scaleX, scaleY, scaleZ))
        }
    }

    protected val UV = new Func {
        override def apply(v: Vec3d, scaleX: Double, scaleY: Double, scaleZ: Double): Vec3d =
            v +
              new Vec3d(uVector.getDirectionVec).scale(DirectionRenderer.axixScale(uVector.getAxis, scaleX, scaleY, scaleZ)) +
              new Vec3d(vVector.getDirectionVec).scale(DirectionRenderer.axixScale(vVector.getAxis, scaleX, scaleY, scaleZ))
    }

}

object DirectionRenderer {

    def getByFacing(facing: EnumFacing): DirectionRenderer = {
        facing match {
            case NORTH => North
            case SOUTH => South
            case WEST => West
            case EAST => East
            case UP => Top
            case DOWN => Bottom
        }
    }

    def getByIndex(i: Int): DirectionRenderer = {
        i match {
            case 0 => Bottom
            case 1 => Top
            case 2 => North
            case 3 => South
            case 4 => West
            case 5 => East
        }
    }

    val directions: Seq[DirectionRenderer] = (0 until 6).map(getByIndex)

    def axixScale(axis: Axis, scaleX: Double, scaleY: Double, scaleZ: Double) = {
        axis match {
            case Axis.X => scaleX
            case Axis.Y => scaleY
            case Axis.Z => scaleZ
        }
    }

    object North extends DirectionRenderer(NORTH) {
        //       y
        //  ┌──────────┐
        //  │ 1      4 │
        // x│          │
        //  │          │
        //  │ 2      3 │
        //  └──────────┘
        //
        override protected val originIndex = 3
        override protected val uVector = UP
        override protected val vVector = EAST
        override protected val order = Array(UV, U, ORIGIN, V)

        override protected def offset(vec: Vec3d, scaleX: Double, scaleY: Double, scaleZ: Double): Vec3d = vec
    }

    object South extends DirectionRenderer(SOUTH) {
        //       y
        //  ┌──────────┐
        //  │ 1      4 │
        //  │          │ x
        //  │          │
        //  │ 2      3 │
        //  └──────────┘
        //
        override protected val originIndex: Int = 2
        override protected val uVector: EnumFacing = UP
        override protected val vVector: EnumFacing = EAST
        override protected val order = Array(U, ORIGIN, V, UV)

        override protected def offset(vec: Vec3d, scaleX: Double, scaleY: Double, scaleZ: Double): Vec3d = vec.addVector(0, 0, scaleZ)
    }

    object West extends DirectionRenderer(WEST) {
        //       y
        //  ┌──────────┐
        //  │ 1      4 │
        //  │          │ z
        //  │          │
        //  │ 2      3 │
        //  └──────────┘
        //
        override protected val originIndex: Int = 2
        override protected val uVector: EnumFacing = UP
        override protected val vVector: EnumFacing = SOUTH
        override protected val order = Array(U, ORIGIN, V, UV)

        override protected def offset(vec: Vec3d, scaleX: Double, scaleY: Double, scaleZ: Double): Vec3d = vec
    }

    object East extends DirectionRenderer(EAST) {
        //       y
        //  ┌──────────┐
        //  │ 1      4 │
        // z│          │
        //  │          │
        //  │ 2      3 │
        //  └──────────┘
        //
        override protected val originIndex: Int = 3
        override protected val uVector: EnumFacing = UP
        override protected val vVector: EnumFacing = SOUTH
        override protected val order = Array(UV, U, ORIGIN, V)

        override protected def offset(vec: Vec3d, scaleX: Double, scaleY: Double, scaleZ: Double): Vec3d = vec.addVector(scaleX, 0, 0)
    }

    object Top extends DirectionRenderer(UP) {
        //
        //  ┌──────────┐
        //  │ 2      1 │
        // z│          │
        //  │          │
        //  │ 3      4 │
        //  └──────────┘
        //       x
        override protected val originIndex: Int = 1
        override protected val uVector: EnumFacing = EAST
        override protected val vVector: EnumFacing = SOUTH
        override protected val order = Array(ORIGIN, V, UV, U)

        override protected def offset(vec: Vec3d, scaleX: Double, scaleY: Double, scaleZ: Double): Vec3d = vec.addVector(0, scaleY, 0)
    }

    object Bottom extends DirectionRenderer(DOWN) {
        //
        //  ┌──────────┐
        //  │ 2      1 │
        //  │          │ x
        //  │          │
        //  │ 3      4 │
        //  └──────────┘
        //       z
        override protected val originIndex: Int = 2
        override protected val uVector: EnumFacing = EAST
        override protected val vVector: EnumFacing = SOUTH
        override protected val order = Array(U, ORIGIN, V, UV)

        override protected def offset(vec: Vec3d, scaleX: Double, scaleY: Double, scaleZ: Double): Vec3d = vec
    }

}

