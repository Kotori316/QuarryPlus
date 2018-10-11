package com.yogpc.qp.tile;

import javax.annotation.Nullable;
import net.minecraft.util.EnumFacing;

public interface IAttachment {
    public void setConnectTo(@Nullable EnumFacing connectTo);
}
