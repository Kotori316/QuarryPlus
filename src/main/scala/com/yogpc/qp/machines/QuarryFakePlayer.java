package com.yogpc.qp.machines;

import java.util.UUID;
import java.util.WeakHashMap;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Copied from appeng.util.FakePlayer
 */
public final class QuarryFakePlayer extends ServerPlayer {
    private static final GameProfile PROFILE = new GameProfile(UUID.fromString("ce6c3b8d-11ba-4b32-90d5-e5d30167fca7"), "[QuarryPlus]");
    private static final WeakHashMap<ServerLevel, QuarryFakePlayer> CACHE = new WeakHashMap<>();

    private QuarryFakePlayer(ServerLevel serverLevel) {
        super(serverLevel.getServer(), serverLevel, PROFILE);
    }

    public static QuarryFakePlayer get(ServerLevel serverLevel) {
        return CACHE.computeIfAbsent(
            serverLevel,
            QuarryFakePlayer::new
        );
    }

    @Override
    public void tick() {
    }

    @Override
    public void doTick() {
    }

    @Override
    public void sendMessage(Component component, ChatType chatType, UUID uUID) {
    }
}
