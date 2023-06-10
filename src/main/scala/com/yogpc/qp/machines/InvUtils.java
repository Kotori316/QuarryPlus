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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

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

    public static List<ItemStack> getBlockDrops(BlockState state, ServerLevel level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack tool) {
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
