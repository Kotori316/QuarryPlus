package com.yogpc.qp.integration.ftbchunks;

import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Area;
import dev.ftb.mods.ftbchunks.data.FTBChunksAPI;
import dev.ftb.mods.ftblibrary.math.ChunkDimPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

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
        return IntStream.rangeClosed(area.minX() / 16, area.maxX() / 16).boxed()
            .flatMap(x -> IntStream.rangeClosed(area.minZ() / 16, area.maxZ() / 16).boxed()
                .map(z -> new ChunkPos(x, z)));
    }

    private static final class Accessor {
        private static boolean isProtected(ResourceKey<Level> dimension, ChunkPos chunkPos) {
            return FTBChunksAPI.getManager().getChunk(new ChunkDimPos(dimension, chunkPos)) != null;
        }

        private static boolean doesAreaHasProtectedChunk(Area area, ResourceKey<Level> dimension) {
            return FTBChunksProtectionCheck.doesAreaHasProtectedChunk(area, c -> isProtected(dimension, c));
        }
    }
}
