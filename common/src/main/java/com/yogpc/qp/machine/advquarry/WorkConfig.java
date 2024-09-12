package com.yogpc.qp.machine.advquarry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;

record WorkConfig(boolean startImmediately, boolean placeAreaFrame, boolean chunkByChunk) {
    static final WorkConfig DEFAULT = new WorkConfig(true, true, false);
    static final MapCodec<WorkConfig> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        RecordCodecBuilder.of(WorkConfig::startImmediately, "startImmediately", Codec.BOOL),
        RecordCodecBuilder.of(WorkConfig::placeAreaFrame, "placeAreaFrame", Codec.BOOL),
        RecordCodecBuilder.of(WorkConfig::chunkByChunk, "chunkByChunk", Codec.BOOL)
    ).apply(i, WorkConfig::new));

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
}
