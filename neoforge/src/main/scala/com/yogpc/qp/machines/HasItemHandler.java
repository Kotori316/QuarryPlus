package com.yogpc.qp.machines;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.items.IItemHandler;

public interface HasItemHandler {
    IItemHandler getItemCapability(Direction ignore);
}
