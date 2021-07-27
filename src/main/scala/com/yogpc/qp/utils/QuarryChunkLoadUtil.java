package com.yogpc.qp.utils;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;

public class QuarryChunkLoadUtil {
    public static boolean isChunkLoaded(World world, BlockPos pos) {
        return isChunkLoaded(world, pos.getX(), pos.getZ());
    }

    public static boolean isChunkLoaded(World world, int x, int z) {
        if (world instanceof ServerWorld serverWorld) {
            var key = new ChunkPos(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z)).toLong();
            return serverWorld.getForcedChunks().contains(key);
        } else {
            return false;
        }
    }

    /**
     * @return whether the chunk is already loaded.
     */
    public static boolean makeChunkLoaded(World world, BlockPos pos) {
        if (world instanceof ServerWorld serverWorld) {
            if (isChunkLoaded(world, pos)) {
                return true;
            } else {
                serverWorld.setChunkForced(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()), true);
                return false;
            }
        } else {
            return false;
        }
    }

    public static void makeChunkUnloaded(World world, BlockPos pos, boolean preLoaded) {
        if (!preLoaded && world instanceof ServerWorld serverWorld) {
            serverWorld.setChunkForced(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()), false);
        }
    }
}
