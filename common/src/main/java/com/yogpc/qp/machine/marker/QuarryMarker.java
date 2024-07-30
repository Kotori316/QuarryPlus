package com.yogpc.qp.machine.marker;

import com.yogpc.qp.machine.Area;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public interface QuarryMarker {
    Optional<Link> getLink();

    interface Link {
        Area area();

        void remove(Level level);

        List<ItemStack> drops();
    }

    record StaticLink(Area area) implements Link {

        @Override
        public void remove(Level level) {
        }

        @Override
        public List<ItemStack> drops() {
            return List.of();
        }
    }
}
