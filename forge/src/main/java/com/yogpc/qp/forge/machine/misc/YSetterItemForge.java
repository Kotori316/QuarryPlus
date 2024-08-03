package com.yogpc.qp.forge.machine.misc;

import com.yogpc.qp.machine.misc.YSetterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public final class YSetterItemForge extends YSetterItem {
    @Override
    protected void openGui(ServerPlayer player, BlockPos pos, Component text) {
        player.openMenu(new YSetterItem.YSetterScreenHandler(pos, text), pos);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return interact(context.getLevel(), context.getClickedPos(), context.getPlayer());
    }
}
