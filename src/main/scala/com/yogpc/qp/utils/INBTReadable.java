package com.yogpc.qp.utils;

import net.minecraft.nbt.NBTTagCompound;

public interface INBTReadable<T> {
    T readFromNBT(NBTTagCompound tag);
}
