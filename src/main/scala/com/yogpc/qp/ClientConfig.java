package com.yogpc.qp;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ClientConfig {
    public final ChunkDestroyerSetting chunkDestroyerSetting;

    public ClientConfig(ForgeConfigSpec.Builder builder) {
        this.chunkDestroyerSetting = new ChunkDestroyerSetting(builder);
    }

    public static final class ChunkDestroyerSetting {
        public final ForgeConfigSpec.BooleanValue placeAreaFrame;
        public final ForgeConfigSpec.BooleanValue chunkByChunk;
        public final ForgeConfigSpec.BooleanValue startImmediately;

        public ChunkDestroyerSetting(ForgeConfigSpec.Builder builder) {
            builder.comment("Personal setting for Chunk Destroyer. These are just default value and you can change them in-game GUI.").push(getClass().getSimpleName());
            this.placeAreaFrame = builder.comment("Whether the machine places initial frame blocks to show working area.").define("placeAreaFrame", true);
            this.chunkByChunk = builder.comment("If true, quarry works for a chunk and go next chunk when finished.").define("chunkByChunk", false);
            this.startImmediately = builder.comment("If true, quarry starts when it has enough power. If false it starts when you click start button in GUI.")
                .define("startImmediately", true);
        }
    }
}
