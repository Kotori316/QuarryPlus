package com.yogpc.qp.neoforge.machine.misc;

import com.yogpc.qp.machine.misc.CheckerItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public final class CheckerItemNeoForge extends CheckerItem {
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return outputLog(context.getLevel(), context.getClickedPos(), context.getPlayer());
    }
}
