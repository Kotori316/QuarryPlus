package com.yogpc.qp.machines;

import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public interface QuarryMarker {
    Optional<Area> getArea();

    List<ItemStack> removeAndGetItems();
}
