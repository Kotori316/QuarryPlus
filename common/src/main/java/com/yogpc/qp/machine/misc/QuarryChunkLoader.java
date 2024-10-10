package com.yogpc.qp.machine.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public sealed interface QuarryChunkLoader {
    Codec<QuarryChunkLoader> CODEC = Codec.STRING.dispatch(QuarryChunkLoader::type, QuarryChunkLoader::codec);

    static boolean isChunkLoaded(ServerLevel level, BlockPos pos) {
        var chunkPos = new ChunkPos(pos);
        return level.getForcedChunks().contains(chunkPos.toLong());
    }

    static QuarryChunkLoader of(ServerLevel level, BlockPos pos) {
        if (isChunkLoaded(level, pos)) {
            return QuarryChunkLoader.None.INSTANCE;
        }
        return new Load(pos);
    }

    void makeChunkLoaded(ServerLevel level);

    void makeChunkUnLoaded(ServerLevel level);

    String type();

    static MapCodec<? extends QuarryChunkLoader> codec(String type) {
        return switch (type) {
            case "None" -> None.CODEC;
            case "Load" -> Load.CODEC;
            case null, default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    enum None implements QuarryChunkLoader {
        INSTANCE;
        private static final MapCodec<None> CODEC = MapCodec.unit(INSTANCE);

        @Override
        public void makeChunkLoaded(ServerLevel level) {
        }

        @Override
        public void makeChunkUnLoaded(ServerLevel level) {
        }

        @Override
        public String type() {
            return getDeclaringClass().getSimpleName();
        }
    }

    record Load(BlockPos pos) implements QuarryChunkLoader {
        private static final MapCodec<Load> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                RecordCodecBuilder.of(Load::pos, "pos", BlockPos.CODEC)
            ).apply(instance, Load::new)
        );

        @Override
        public void makeChunkLoaded(ServerLevel level) {
            var chunkPos = new ChunkPos(pos);
            level.setChunkForced(chunkPos.x, chunkPos.z, true);
        }

        @Override
        public void makeChunkUnLoaded(ServerLevel level) {
            var chunkPos = new ChunkPos(pos);
            level.setChunkForced(chunkPos.x, chunkPos.z, false);
        }

        @Override
        public String type() {
            return getClass().getSimpleName();
        }
    }
}
