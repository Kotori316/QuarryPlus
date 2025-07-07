package com.yogpc.qp.forge.machine;

import com.yogpc.qp.machine.QuarryFakePlayerCommon;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;

/**
 * All things are copied from FakePlayer and FakePlayerFactory
 * Copyright (c) Forge Development LLC and contributors
 */
public class QuarryFakePlayer {
    static {
        LevelEvent.Unload.BUS.addListener(QuarryFakePlayer::unloadLevel);
    }

    private static final Map<ServerLevel, ServerPlayer> players = new HashMap<>();

    @SuppressWarnings("UnstableApiUsage")
    public static ServerPlayer get(ServerLevel serverLevel) {
        return players.computeIfAbsent(serverLevel, key -> QuarryFakePlayerCommon.getOwnImplementation(key, (s) -> ServerLifecycleHooks.getCurrentServer()));
    }

    @SubscribeEvent(priority = Priority.HIGHEST)
    public static void unloadLevel(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            players.entrySet().removeIf(entry -> entry.getValue().level() == level);
        }
    }
}
