package com.yogpc.qp.fabric.machine.misc;

import com.yogpc.qp.machine.misc.CheckerItem;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public final class CheckerItemFabric extends CheckerItem implements UseBlockCallback {
    public CheckerItemFabric() {
        UseBlockCallback.EVENT.register(this);
    }

    @Override
    public InteractionResult interact(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        if (player.isSpectator() || player.getItemInHand(hand).getItem() != this) return InteractionResult.PASS;
        return outputLog(world, hitResult.getBlockPos(), player);
    }
}
