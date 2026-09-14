package com.yogpc.qp.machine.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryLogger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public sealed interface QuarryChunkLoader {
    Codec<QuarryChunkLoader> CODEC = Codec.STRING.dispatch(QuarryChunkLoader::type, QuarryChunkLoader::codec);

    static boolean isChunkLoaded(ServerLevel level, BlockPos pos) {
        var chunkPos = new ChunkPos(pos);
        var result = level.getForcedChunks().contains(chunkPos.toLong());
        QuarryLogger.LOGGER.info(QuarryLogger.LOG4J_CHUNK_LOADER, "Check state of chunk loading x={}, z={}, loaded={}", chunkPos.x, chunkPos.z, result);
        return result;
    }

    static QuarryChunkLoader of(ServerLevel level, BlockPos pos) {
        if (!PlatformAccess.config().enableChunkLoader()) {
            return QuarryChunkLoader.None.INSTANCE;
        }
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
            QuarryLogger.LOGGER.info(QuarryLogger.LOG4J_CHUNK_LOADER, "Force chunk load at x={}, z={}", chunkPos.x, chunkPos.z);
        }

        @Override
        public void makeChunkUnLoaded(ServerLevel level) {
            var chunkPos = new ChunkPos(pos);
            level.setChunkForced(chunkPos.x, chunkPos.z, false);
            QuarryLogger.LOGGER.info(QuarryLogger.LOG4J_CHUNK_LOADER, "Remove chunk loading at x={}, z={}", chunkPos.x, chunkPos.z);
        }

        @Override
        public String type() {
            return getClass().getSimpleName();
        }
    }
}
