package com.yogpc.qp.compat;

import net.minecraft.nbt.NBTTagCompound;

public interface INBTWritable {
    NBTTagCompound writeToNBT(NBTTagCompound nbt);
}
