package com.yogpc.qp.utils;

import net.minecraft.nbt.NBTTagCompound;

public interface INBTWritable {
    NBTTagCompound writeToNBT(NBTTagCompound nbt);
}
