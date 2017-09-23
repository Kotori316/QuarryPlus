package com.yogpc.qp.render

import com.yogpc.qp.render.Box.Vec3dTuple
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.{MathHelper, Vec3d}

class Box(start: Vec3d, end: Vec3d, length: Double) {
    protected val delta = end - start
    protected val a = length / 2

    def faceList: Seq[Seq[Vec3dTuple]] = {
        val length = delta.lengthVector()
        val size = MathHelper.floor(length)
        val list = Seq.newBuilder[Seq[Vec3dTuple]]
        Seq.empty
    }
}

object Box {

    def apply(startPoint: Vec3d, endPoint: Vec3d, length: Double = 1)(first: Boolean, end: Boolean): Box = {
        if (startPoint.x == endPoint.x) {
            if (startPoint.y == endPoint.y) {
                return new Box1dZ(startPoint, endPoint, length, first, end)
            } else if (startPoint.z == endPoint.z) {
                return new Box1dY(startPoint, endPoint, length, first, end)
            }
        } else if (startPoint.y == endPoint.y && startPoint.z == endPoint.z) {
            return new Box1dX(startPoint, endPoint, length, first, end)
        }
        new Box(startPoint, endPoint, length)
    }

    private class Box1dX(start: Vec3d, end: Vec3d, length: Double, firstSide: Boolean, endSide: Boolean) extends Box(start, end, length) {
        val minX = Math.min(start.x, end.x)
        val maxX = Math.max(start.x, end.x)
        val y = start.y
        val z = start.z

        override def faceList = {
            val vecLength = maxX - minX
            val size = MathHelper.floor(vecLength / length)
            val list = Seq.newBuilder[Seq[Vec3dTuple]]
            val bool = (size * length) != vecLength

            for (i <- Range(0, size)) {
                val builder = Seq.newBuilder[Vec3dTuple]
                //DOWN
                builder.+=(Vec3dTuple(new Vec3d(minX + i * length, y - a, z - a), EnumFacing.DOWN, length, length, length, length))
                //TOP
                builder.+=(Vec3dTuple(new Vec3d(minX + i * length, y - a, z - a), EnumFacing.UP, length, length, length, length))

                //North
                builder.+=(Vec3dTuple(new Vec3d(minX + i * length, y - a, z - a), EnumFacing.NORTH, length, length, length, length))
                //South
                builder.+=(Vec3dTuple(new Vec3d(minX + i * length, y - a, z - a), EnumFacing.SOUTH, length, length, length, length))
                if ((i == 0 && firstSide) || (i == size - 1 && !bool && endSide)) {
                    //West
                    builder.+=(Vec3dTuple(new Vec3d(minX + i * length, y - a, z - a), EnumFacing.WEST, length, length, length, length))
                    //East
                    builder.+=(Vec3dTuple(new Vec3d(minX + i * length, y - a, z - a), EnumFacing.EAST, length, length, length, length))
                }

                list.+=(builder.result())
            }
            if (bool) {
                val builder = Seq.newBuilder[Vec3dTuple]
                val b = vecLength / length - size
                //DOWN
                builder.+=(Vec3dTuple(new Vec3d(minX + size * length, y - a, z - a), EnumFacing.DOWN, b, length, length, length))
                //TOP
                builder.+=(Vec3dTuple(new Vec3d(minX + size * length, y - a, z - a), EnumFacing.UP, b, length, length, length))

                //North
                builder.+=(Vec3dTuple(new Vec3d(minX + size * length, y - a, z - a), EnumFacing.NORTH, b, length, length, length))
                //South
                builder.+=(Vec3dTuple(new Vec3d(minX + size * length, y - a, z - a), EnumFacing.SOUTH, b, length, length, length))

                //West
                builder.+=(Vec3dTuple(new Vec3d(minX + size * length, y - a, z - a), EnumFacing.WEST, b, length, length, length))
                //East
                builder.+=(Vec3dTuple(new Vec3d(minX + size * length, y - a, z - a), EnumFacing.EAST, b, length, length, length))
                list.+=(builder.result())
            }

            list.result()
        }
    }

    private class Box1dY(start: Vec3d, end: Vec3d, length: Double, firstSide: Boolean, endSide: Boolean) extends Box(start, end, length) {
        val minY = Math.min(start.y, end.y)
        val maxY = Math.max(start.y, end.y)
        val x = start.x
        val z = start.z

        override def faceList = {
            val vecLength = maxY - minY
            val size = MathHelper.floor(vecLength / length)
            val list = Seq.newBuilder[Seq[Vec3dTuple]]
            val bool = (size * length) != vecLength

            for (i <- 0 until size) {
                val builder = Seq.newBuilder[Vec3dTuple]
                if ((i == 0 && firstSide) || (i == size - 1 && !bool && endSide)) {
                    //DOWN
                    builder += Vec3dTuple(new Vec3d(x - a, minY + i * length, z - a), EnumFacing.DOWN, length, length, length, length)
                    //TOP
                    builder += Vec3dTuple(new Vec3d(x - a, minY + i * length, z - a), EnumFacing.UP, length, length, length, length)
                }
                //North
                builder += Vec3dTuple(new Vec3d(x - a, minY + i * length, z - a), EnumFacing.NORTH, length, length, length, length)
                //South
                builder += Vec3dTuple(new Vec3d(x - a, minY + i * length, z - a), EnumFacing.SOUTH, length, length, length, length)

                //West
                builder += Vec3dTuple(new Vec3d(x - a, minY + i * length, z - a), EnumFacing.WEST, length, length, length, length)
                //East
                builder += Vec3dTuple(new Vec3d(x - a, minY + i * length, z - a), EnumFacing.EAST, length, length, length, length)
                list += builder.result()
            }
            if (bool) {
                val builder = Seq.newBuilder[Vec3dTuple]
                val b = vecLength - size
                //DOWN
                builder += Vec3dTuple(new Vec3d(x - a, minY + size * length, z - a), EnumFacing.DOWN, length, b, length, length)
                //TOP
                builder += Vec3dTuple(new Vec3d(x - a, minY + size * length, z - a), EnumFacing.UP, length, b, length, length)

                //North
                builder += Vec3dTuple(new Vec3d(x - a, minY + size * length, z - a), EnumFacing.NORTH, length, b, length, length)
                //South
                builder += Vec3dTuple(new Vec3d(x - a, minY + size * length, z - a), EnumFacing.SOUTH, length, b, length, length)

                //West
                builder += Vec3dTuple(new Vec3d(x - a, minY + size * length, z - a), EnumFacing.WEST, length, b, length, length)
                //East
                builder += Vec3dTuple(new Vec3d(x - a, minY + size * length, z - a), EnumFacing.EAST, length, b, length, length)
                list += builder.result()
            }
            list.result()
        }
    }

    private class Box1dZ(start: Vec3d, end: Vec3d, length: Double, firstSide: Boolean, endSide: Boolean) extends Box(start, end, length) {
        val minZ = Math.min(start.z, end.z)
        val maxZ = Math.max(start.z, end.z)
        val x = start.x
        val y = start.y

        override def faceList = {
            val vecLength = maxZ - minZ
            val size = MathHelper.floor(vecLength / length)
            val list = Seq.newBuilder[Seq[Vec3dTuple]]
            val bool = (size * length) != vecLength

            for (i <- 0 until size) {
                val builder = Seq.newBuilder[Vec3dTuple]
                //DOWN
                builder += Vec3dTuple(new Vec3d(x - a, y - a, minZ + i * length), EnumFacing.DOWN, length, length, length, length)
                //TOP
                builder += Vec3dTuple(new Vec3d(x - a, y - a, minZ + i * length), EnumFacing.UP, length, length, length, length)
                if ((i == 0 && firstSide) || (i == size - 1 && !bool && endSide)) {
                    //North
                    builder += Vec3dTuple(new Vec3d(x - a, y - a, minZ + i * length), EnumFacing.NORTH, length, length, length, length)
                    //South
                    builder += Vec3dTuple(new Vec3d(x - a, y - a, minZ + i * length), EnumFacing.SOUTH, length, length, length, length)
                }

                //West
                builder += Vec3dTuple(new Vec3d(x - a, y - a, minZ + i * length), EnumFacing.WEST, length, length, length, length)
                //East
                builder += Vec3dTuple(new Vec3d(x - a, y - a, minZ + i * length), EnumFacing.EAST, length, length, length, length)
                list += builder.result()
            }
            if (bool) {
                val builder = Seq.newBuilder[Vec3dTuple]
                val b = vecLength - size
                //DOWN
                builder += Vec3dTuple(new Vec3d(x - a, y - a, minZ + size * length), EnumFacing.DOWN, length, length, b, length)
                //TOP
                builder += Vec3dTuple(new Vec3d(x - a, y - a, minZ + size * length), EnumFacing.UP, length, length, b, length)

                //North
                builder += Vec3dTuple(new Vec3d(x - a, y - a, minZ + size * length), EnumFacing.NORTH, length, length, b, length)
                //South
                builder += Vec3dTuple(new Vec3d(x - a, y - a, minZ + size * length), EnumFacing.SOUTH, length, length, b, length)

                //West
                builder += Vec3dTuple(new Vec3d(x - a, y - a, minZ + size * length), EnumFacing.WEST, length, length, b, length)
                //East
                builder += Vec3dTuple(new Vec3d(x - a, y - a, minZ + size * length), EnumFacing.EAST, length, length, b, length)
                list += builder.result()
            }
            list.result()
        }
    }

    case class Vec3dTuple(origin: Vec3d, direction: EnumFacing, scaleX: Double, scaleY: Double, scaleZ: Double, size: Double) {
        val xScales = (scaleX, size, size)
        val yScales = (size, scaleY, size)
        val zScales = (size, size, scaleZ)
    }

}
