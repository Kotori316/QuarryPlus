package com.yogpc.qp

import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.util.EnumFacing

package object render {

    implicit class BufferBuilderHelper(val buffer: VertexBuffer) extends AnyVal {
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

    implicit class FacingOffset(val facing: EnumFacing) extends AnyVal {
        def offsetX(scale: Double) = facing.getFrontOffsetX * scale

        def offsetY(scale: Double) = facing.getFrontOffsetY * scale

        def offsetZ(scale: Double) = facing.getFrontOffsetZ * scale

        def offsetXAbs(scale: Double) = if (facing.getAxis == EnumFacing.Axis.X) scale else 0d

        def offsetZAbs(scale: Double) = if (facing.getAxis == EnumFacing.Axis.Z) scale else 0d
    }

}
