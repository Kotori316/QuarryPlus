package com.yogpc.qp.machines.checker;

import java.util.List;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.CheckerLog;
import com.yogpc.qp.machines.QPItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class ItemChecker extends QPItem {
    public static final String NAME = "status_checker";

    public ItemChecker() {
        super(new ResourceLocation(QuarryPlus.modID, NAME), new Item.Properties());
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        var player = context.getPlayer();
        var level = context.getLevel();
        if (player != null && level.getBlockEntity(context.getClickedPos()) instanceof CheckerLog debug) {
            List<? extends Component> logs = debug.getDebugLogs();
            if (logs != null) {
                player.displayClientMessage(Component.literal(ChatFormatting.YELLOW + (level.isClientSide ? "Client" : "Server") + ChatFormatting.RESET), false);
                logs.forEach(t -> player.displayClientMessage(t, false));
            } else {
                QuarryPlus.LOGGER.warn("CheckerLog implementation was insufficient. " + debug.getClass());
            }
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
        }
    }

}
