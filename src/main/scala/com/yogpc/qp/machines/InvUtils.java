package com.yogpc.qp.machines;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemHandlerHelper;

public class InvUtils {
    /**
     * Move the item to neighbor inventory of the pos.
     * The search order is the same as {@link Direction} declaration.
     *
     * @param level the level where destination and source exists
     * @param pos   the <b>source</b> pos.
     * @param stack the item to move.
     * @return the item which is not inserted (remaining stack)
     */
    public static ItemStack injectToNearTile(Level level, BlockPos pos, ItemStack stack) {
        var remain = stack.copy();
        for (Direction d : Direction.values()) {
            Optional.ofNullable(level.getBlockEntity(pos.relative(d)))
                .flatMap(t -> t.getCapability(ForgeCapabilities.ITEM_HANDLER, d.getOpposite()).resolve())
                .ifPresent(handler -> {
                    var simulate = ItemHandlerHelper.insertItem(handler, remain.copy(), true);
                    if (simulate.getCount() < remain.getCount()) {
                        var notMoved = ItemHandlerHelper.insertItem(handler,
                            ItemHandlerHelper.copyStackWithSize(remain, remain.getCount() - simulate.getCount()), false);
                        // notMoved should be empty.
                        int remainCount = simulate.getCount() + notMoved.getCount();
                        remain.setCount(remainCount);
                    }
                });
            if (remain.isEmpty()) return ItemStack.EMPTY;
        }
        return remain;
    }
}
