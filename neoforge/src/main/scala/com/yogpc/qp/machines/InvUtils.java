package com.yogpc.qp.machines;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

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
            var handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.relative(d), d.getOpposite());
            if (handler != null) {
                var simulate = ItemHandlerHelper.insertItem(handler, remain.copy(), true);
                if (simulate.getCount() < remain.getCount()) {
                    var notMoved = ItemHandlerHelper.insertItem(handler,
                        ItemHandlerHelper.copyStackWithSize(remain, remain.getCount() - simulate.getCount()), false);
                    // notMoved should be empty.
                    int remainCount = simulate.getCount() + notMoved.getCount();
                    remain.setCount(remainCount);
                }
            }
            if (remain.isEmpty()) return ItemStack.EMPTY;
        }
        return remain;
    }

    public static List<ItemStack> getBlockDrops(BlockState state, ServerLevel level, BlockPos pos, @Nullable BlockEntity blockEntity, @NotNull Entity entity, ItemStack tool) {
        var result = Block.getDrops(state, level, pos, blockEntity, entity, tool);
        if (TraceQuarryWork.enabled && result.isEmpty()) {
            // Log if block has no drops.
            // If the block is known, such as fluid, ignore.
            // If block is unbreakable or needs just one click, such as Bedrock and Grass, ignore.
            if (state.getFluidState().isEmpty() && state.getDestroySpeed(level, pos) > 0) {
                TraceQuarryWork.noDrops(state, pos, tool);
            }
        }
        return result;
    }
}
