package com.yogpc.qp.utils

import com.yogpc.qp.NBTWrapper
import net.minecraft.fluid.{Fluid, Fluids}
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.util.Constants.NBT
import net.minecraftforge.fluids.{FluidAttributes, FluidStack}
import net.minecraftforge.registries.ForgeRegistries

case class FluidElement(fluid: Fluid, tag: Option[CompoundNBT]) {
  def toStack = withAmount(FluidAttributes.BUCKET_VOLUME)

  def withAmount(amount: Int) = new FluidStack(fluid, amount, tag.orNull)

  def toCompoundTag: CompoundNBT = this.toNBT
}

object FluidElement {
  def apply(fluid: Fluid, tag: Option[CompoundNBT]): FluidElement = new FluidElement(fluid, tag)

  def apply(fluid: Fluid): FluidElement = apply(fluid, None)

  def fromStack(stack: FluidStack): FluidElement = apply(stack.getRawFluid, Option(stack.getTag))

  def fromNBT(nbt: CompoundNBT): FluidElement = {
    val fluidName = new ResourceLocation(nbt.getString("FluidName"))
    val fluid = ForgeRegistries.FLUIDS.getValue(fluidName)
    if (fluid == null) {
      apply(Fluids.EMPTY)
    } else {
      val tag = if (nbt.contains("Tag", NBT.TAG_COMPOUND)) Some(nbt.getCompound("Tag")) else None
      apply(fluid, tag)
    }
  }

  implicit val fluidElement2Nbt: FluidElement NBTWrapper CompoundNBT = e => {
    val nbt = new CompoundNBT
    nbt.putString("FluidName", e.fluid.getRegistryName.toString)
    e.tag.foreach(tag => nbt.put("Tag", tag))
    nbt
  }
}
