package com.yogpc.qp.machines;

import java.util.List;

import com.yogpc.qp.machines.checker.ItemChecker;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Called in {@link ItemChecker#onItemUseFirst(ItemStack, UseOnContext)}.
 * This interface must be implemented by {@link BlockEntity}.
 */
public interface CheckerLog {
    /**
     * Get the debug log of the tile.
     * The return list is just for read, so it can be immutable list.
     *
     * @return The list of text shown in the chat log. It can be an instance of immutable list.
     */
    List<? extends Component> getDebugLogs();
}
