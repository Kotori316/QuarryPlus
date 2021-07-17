package com.yogpc.qp.machines;

import java.util.List;
import java.util.Optional;

import net.minecraft.item.ItemStack;

public interface QuarryMarker {
    Optional<Area> getArea();

    List<ItemStack> removeAndGetItems();
}
