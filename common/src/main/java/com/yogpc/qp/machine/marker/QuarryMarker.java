package com.yogpc.qp.machine.marker;

import com.yogpc.qp.machine.Area;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public interface QuarryMarker {
    Optional<Link> getLink();

    interface Link {
        Area area();

        void remove();

        List<ItemStack> drops();
    }
}
