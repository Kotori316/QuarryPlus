package com.yogpc.qp.machines.advquarry;

import io.netty.buffer.ByteBufAllocator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkConfigTest {
    static void cycleTag(WorkConfig workConfig) {
        var tag = workConfig.toNbt();
        var fromTag = new WorkConfig(tag);
        assertEquals(workConfig, fromTag);
    }

    static void cyclePacket(WorkConfig workConfig) {
        var packet = new FriendlyByteBuf(ByteBufAllocator.DEFAULT.buffer());
        workConfig.writePacket(packet);
        var fromPacket = new WorkConfig(packet);
        assertEquals(workConfig, fromPacket);
    }

    @Test
    void cycle1() {
        WorkConfig config = new WorkConfig(true, false, true);
        cycleTag(config);
        cyclePacket(config);
    }

    @Test
    void cycle2() {
        WorkConfig config = new WorkConfig(false, true, false);
        cycleTag(config);
        cyclePacket(config);
    }

    @Test
    void nbtTag() {
        var tag = new CompoundTag();
        tag.putBoolean("startImmediately", false);
        tag.putBoolean("placeAreaFrame", false);
        tag.putBoolean("chunkByChunk", true);
        WorkConfig config = new WorkConfig(tag);
        assertEquals(new WorkConfig(false, false, true), config);
    }

    @Test
    void noAutoStart() {
        WorkConfig config = new WorkConfig(true, true, false);
        WorkConfig noStart = config.noAutoStartConfig();
        assertEquals(config.chunkByChunk(), noStart.chunkByChunk());
        assertEquals(config.placeAreaFrame(), noStart.placeAreaFrame());
        assertFalse(noStart.startImmediately());
    }

    @Test
    void startSoon() {
        WorkConfig config = new WorkConfig(true, true, false);
        WorkConfig startSoon = config.startSoonConfig();
        assertEquals(config.chunkByChunk(), startSoon.chunkByChunk());
        assertEquals(config.placeAreaFrame(), startSoon.placeAreaFrame());
        assertTrue(startSoon.startImmediately());
    }
}