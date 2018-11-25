package com.yogpc.qp

import net.minecraft.util.EnumFacing

package object render {

  implicit class FacingOffset(val facing: EnumFacing) extends AnyVal {
    @inline
    def offsetX(scale: Double) = facing.getFrontOffsetX * scale

    @inline
    def offsetY(scale: Double) = facing.getFrontOffsetY * scale

    @inline
    def offsetZ(scale: Double) = facing.getFrontOffsetZ * scale

    def offsetXAbs(scale: Double) = if (facing.getAxis == EnumFacing.Axis.X) scale else 0d

    def offsetZAbs(scale: Double) = if (facing.getAxis == EnumFacing.Axis.Z) scale else 0d
  }

}
