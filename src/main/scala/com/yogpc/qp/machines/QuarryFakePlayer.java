package com.yogpc.qp.machines;

import java.util.UUID;
import java.util.WeakHashMap;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.OutgoingPlayerChatMessage;
import net.minecraft.network.chat.SignedMessageHeader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Copied from appeng.util.FakePlayer
 */
public final class QuarryFakePlayer extends ServerPlayer {
    private static final GameProfile PROFILE = new GameProfile(UUID.fromString("ce6c3b8d-11ba-4b32-90d5-e5d30167fca7"), "[QuarryPlus]");
    private static final WeakHashMap<ServerLevel, QuarryFakePlayer> CACHE = new WeakHashMap<>();

    private QuarryFakePlayer(ServerLevel serverLevel) {
        super(serverLevel.getServer(), serverLevel, PROFILE, null);
    }

    public static QuarryFakePlayer get(ServerLevel serverLevel) {
        return CACHE.computeIfAbsent(
            serverLevel,
            QuarryFakePlayer::new
        );
    }

    public static void setDirection(QuarryFakePlayer player, Direction direction) {
        player.setXRot(direction.getNormal().getY() * 90);
        player.setYRot(direction.toYRot());
    }

    @Override
    public void tick() {
    }

    @Override
    public void doTick() {
    }

    @Override
    public void sendChatHeader(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
    }

    @Override
    public void sendChatMessage(OutgoingPlayerChatMessage outgoingPlayerChatMessage, boolean bl, ChatType.Bound bound) {
    }

    @Override
    public void sendSystemMessage(Component component, boolean b) {
    }
}
