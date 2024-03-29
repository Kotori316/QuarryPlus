package com.yogpc.qp.utils;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.Logger;

public class QuarryChunkLoadUtil {
    private static final Logger LOGGER = QuarryPlus.getLogger(QuarryChunkLoadUtil.class);

    public static boolean isChunkLoaded(Level world, BlockPos pos) {
        if (world instanceof ServerLevel serverWorld) {
            var key = new ChunkPos(pos).toLong();
            return serverWorld.getForcedChunks().contains(key);
        } else {
            return false;
        }
    }

    /**
     * @return whether the chunk is already loaded.
     */
    public static boolean makeChunkLoaded(Level world, BlockPos pos) {
        if (world instanceof ServerLevel serverWorld) {
            LOGGER.debug("Asked to force loading chunk at {}", pos);
            if (isChunkLoaded(world, pos)) {
                return true;
            } else {
                serverWorld.setChunkForced(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()), true);
                return false;
            }
        } else {
            return false;
        }
    }

    public static void makeChunkUnloaded(Level world, BlockPos pos, boolean preLoaded) {
        LOGGER.debug("Asked to unload chunk. preLoaded={}", preLoaded);
        if (!preLoaded && world instanceof ServerLevel serverWorld) {
            serverWorld.setChunkForced(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()), false);
        }
    }
}
