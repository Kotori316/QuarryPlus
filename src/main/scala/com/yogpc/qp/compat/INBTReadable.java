package com.yogpc.qp.compat;

import net.minecraft.nbt.NBTTagCompound;

public interface INBTReadable<T> {
    T readFromNBT(NBTTagCompound tag);
}
