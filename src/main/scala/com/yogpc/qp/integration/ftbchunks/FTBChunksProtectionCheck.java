package com.yogpc.qp.integration.ftbchunks;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Area;
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftblibrary.math.ChunkDimPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class FTBChunksProtectionCheck {
    public static boolean isAreaProtected(Area area, ResourceKey<Level> dimension) {
        if (QuarryPlus.config.common.allowWorkInClaimedChunkByFBTChunks.get()) {
            return false;
        }
        if (ModList.get().isLoaded("FtbChunks".toLowerCase(Locale.ROOT))) {
            return Accessor.doesAreaHasProtectedChunk(area, dimension);
        } else {
            return false;
        }
    }

    static boolean doesAreaHasProtectedChunk(Area area, Predicate<ChunkPos> predicate) {
        return getChunkPosStream(area).anyMatch(predicate);
    }

    static Stream<ChunkPos> getChunkPosStream(Area area) {
        return IntStream.rangeClosed(Math.floorDiv(area.minX(), 16), Math.floorDiv(area.maxX(), 16)).boxed()
            .flatMap(x -> IntStream.rangeClosed(Math.floorDiv(area.minZ(), 16), Math.floorDiv(area.maxZ(), 16)).boxed()
                .map(z -> new ChunkPos(x, z)));
    }

    private static final class Accessor {
        private static boolean isProtected(ResourceKey<Level> dimension, ChunkPos chunkPos) {
            return FTBChunksAPI.api().getManager().getChunk(new ChunkDimPos(dimension, chunkPos)) != null;
        }

        private static boolean doesAreaHasProtectedChunk(Area area, ResourceKey<Level> dimension) {
            return FTBChunksProtectionCheck.doesAreaHasProtectedChunk(area, c -> isProtected(dimension, c));
        }
    }
}
