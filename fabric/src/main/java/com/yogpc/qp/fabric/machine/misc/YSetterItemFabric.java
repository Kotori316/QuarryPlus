package com.yogpc.qp.fabric.machine.misc;

import com.yogpc.qp.machine.misc.YSetterItem;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public final class YSetterItemFabric extends YSetterItem implements UseBlockCallback {
    public YSetterItemFabric() {
        UseBlockCallback.EVENT.register(this);
    }

    @Override
    public InteractionResult interact(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        if (player.isSpectator() || player.getItemInHand(hand).getItem() != this) return InteractionResult.PASS;

        return interact(world, hitResult.getBlockPos(), player);
    }
}
