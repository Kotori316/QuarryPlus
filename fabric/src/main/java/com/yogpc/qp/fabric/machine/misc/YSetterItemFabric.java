package com.yogpc.qp.fabric.machine.misc;

import com.yogpc.qp.machine.misc.YSetterItem;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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

    @Override
    protected void openGui(ServerPlayer player, BlockPos pos, Component text) {
        player.openMenu(new YSetterScreenHandlerFabric(pos, text));
    }

    private static final class YSetterScreenHandlerFabric extends YSetterScreenHandler implements ExtendedScreenHandlerFactory<BlockPos> {

        private YSetterScreenHandlerFabric(BlockPos pos, Component text) {
            super(pos, text);
        }

        @Override
        public BlockPos getScreenOpeningData(ServerPlayer player) {
            return pos;
        }
    }
}
