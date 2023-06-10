package com.yogpc.qp.machines.advquarry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

record WorkConfig(boolean startImmediately, boolean placeAreaFrame, boolean chunkByChunk) {
    CompoundTag toNbt() {
        var nbt = new CompoundTag();
        nbt.putBoolean("startImmediately", startImmediately);
        nbt.putBoolean("placeAreaFrame", placeAreaFrame);
        nbt.putBoolean("chunkByChunk", chunkByChunk);
        return nbt;
    }

    WorkConfig(CompoundTag tag) {
        this(
                getBool(tag, "startImmediately", true),
                getBool(tag, "placeAreaFrame", true),
                getBool(tag, "chunkByChunk", false)
        );
    }

    void writePacket(FriendlyByteBuf buf) {
        buf.writeBoolean(startImmediately).writeBoolean(placeAreaFrame).writeBoolean(chunkByChunk);
    }

    WorkConfig(FriendlyByteBuf buf) {
        this(
                buf.readBoolean(), // startImmediately
                buf.readBoolean(), // placeAreaFrame
                buf.readBoolean() // chunkByChunk
        );
    }

    WorkConfig startSoonConfig() {
        return new WorkConfig(true, this.placeAreaFrame, this.chunkByChunk);
    }

    WorkConfig noAutoStartConfig() {
        return new WorkConfig(false, this.placeAreaFrame, this.chunkByChunk);
    }

    static boolean getBool(CompoundTag tag, String key, boolean defaultValue) {
        if (tag.contains(key)) return tag.getBoolean(key);
        return defaultValue;
    }
}
