package com.yogpc.qp.machine;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Just a util method to support FakePlayer API
 */
public final class QuarryFakePlayerCommon {
    public static final GameProfile PROFILE = new GameProfile(UUID.fromString("ce6c3b8d-11ba-4b32-90d5-e5d30167fca7"), "[QuarryPlus]");

    public static void setDirection(ServerPlayer player, Direction direction) {
        player.setXRot(direction.getUnitVec3i().getY() * 90);
        player.setYRot(direction.toYRot());
    }
}
