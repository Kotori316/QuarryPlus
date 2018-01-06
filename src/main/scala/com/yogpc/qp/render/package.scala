package com.yogpc.qp

import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.{Vec3d, Vec3i}

package object render {

    implicit class BufferBuilderHelper(val buffer: VertexBuffer) extends AnyVal {
        def pos(vec: Vec3d): VertexBuffer = buffer.pos(vec.xCoord, vec.yCoord, vec.zCoord)

        /**
          * buffer.color(255, 255, 255, 255)
          *
          * @return the buffer
          */
        def colored(): VertexBuffer = buffer.color(255, 255, 255, 255)

        /**
          * buffer.lightmap(240, 0).endVertex()
          */
        def lightedAndEnd(): Unit = buffer.lightmap(240, 0).endVertex()
    }

    implicit class Vec3dHelper(val vec3d: Vec3d) extends AnyVal {
        def +(o: Vec3d): Vec3d = vec3d add o

        def +(o: Vec3i): Vec3d = vec3d addVector(o.getX, o.getY, o.getZ)

        def -(o: Vec3d): Vec3d = vec3d subtract o

        def -(o: Vec3i): Vec3d = vec3d subtract(o.getX, o.getY, o.getZ)
    }

    implicit class FacingOffset(val facing: EnumFacing) extends AnyVal {
        def offsetX(scale: Double) = facing.getFrontOffsetX * scale

        def offsetY(scale: Double) = facing.getFrontOffsetY * scale

        def offsetZ(scale: Double) = facing.getFrontOffsetZ * scale

        def offsetXAbs(scale: Double) = if (facing.getAxis == EnumFacing.Axis.X) scale else 0d

        def offsetZAbs(scale: Double) = if (facing.getAxis == EnumFacing.Axis.Z) scale else 0d
    }

}
