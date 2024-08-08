package com.yogpc.qp.forge.machine.misc;

import com.yogpc.qp.machine.misc.YSetterItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public final class YSetterItemForge extends YSetterItem {
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return interact(context.getLevel(), context.getClickedPos(), context.getPlayer());
    }
}
