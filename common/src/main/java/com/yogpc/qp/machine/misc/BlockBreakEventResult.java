package com.yogpc.qp.machine.misc;

import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.OptionalInt;

/**
 * @param canceled {@code true} if event is cancelled. {@code false} if event is successfully accepted.
 * @param exp      the experience point to collect. Empty if point is not determined.
 * @param drops    the dropped items which might be modified in events
 */
public record BlockBreakEventResult(boolean canceled, OptionalInt exp, List<ItemStack> drops) {
    public static final BlockBreakEventResult CANCELED = new BlockBreakEventResult(true, OptionalInt.empty(), List.of());
    public static final BlockBreakEventResult EMPTY = new BlockBreakEventResult(false, OptionalInt.empty(), List.of());

    public static BlockBreakEventResult empty(List<ItemStack> drops) {
        return new BlockBreakEventResult(false, OptionalInt.empty(), drops);
    }
}
