package com.yogpc.qp.machines;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.UUID;

public class QuarryFakePlayer {
    private static final GameProfile profile = new GameProfile(UUID.fromString("ce6c3b8d-11ba-4b32-90d5-e5d30167fca7"), "[QuarryPlus]");

    private static FakePlayer get(ServerLevel serverLevel) {
        return FakePlayerFactory.get(serverLevel, profile);
    }

    public static FakePlayer getAndSetPosition(ServerLevel serverLevel, BlockPos pos, ItemStack stack) {
        FakePlayer player = get(serverLevel);
        player.setPos(Vec3.atCenterOf(pos.above(2)));
        player.setXRot(90f);
        player.setYRot(90f);
        player.setYHeadRot(90f);
        if (stack != null) {
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        }
        return player;
    }
}
