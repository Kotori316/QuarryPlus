package com.yogpc.qp.machines.checker;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.CheckerLog;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class ItemChecker extends Item implements UseBlockCallback {
    public static final String NAME = "status_checker";

    public ItemChecker() {
        super(new Properties().tab(QuarryPlus.CREATIVE_TAB));
        UseBlockCallback.EVENT.register(this);
    }

    @Override
    public InteractionResult interact(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        if (player.isSpectator() || player.getItemInHand(hand).getItem() != this) return InteractionResult.PASS;
        if (world.getBlockEntity(hitResult.getBlockPos()) instanceof CheckerLog debug) {
            player.displayClientMessage(new TextComponent(ChatFormatting.YELLOW + (world.isClientSide ? "Client" : "Server") + ChatFormatting.RESET), false);
            debug.getDebugLogs().forEach(t -> player.displayClientMessage(t, false));
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
        }
    }
}
