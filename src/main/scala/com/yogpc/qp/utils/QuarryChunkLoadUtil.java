package com.yogpc.qp.utils;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Area;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.function.Consumer;

public class QuarryChunkLoadUtil {
    private static final Logger LOGGER = QuarryPlus.getLogger(QuarryChunkLoadUtil.class);

    public static boolean isChunkLoaded(Level world, BlockPos pos) {
        if (QuarryPlus.config != null && !QuarryPlus.config.common.enableChunkLoader.get()) return false;
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
    public static boolean makeChunkLoaded(Level world, BlockPos pos, boolean machineEnabled) {
        if (QuarryPlus.config != null && !QuarryPlus.config.common.enableChunkLoader.get()) return false;
        if (!machineEnabled) return false;
        if (world instanceof ServerLevel serverWorld) {
            var chunkLoaded = isChunkLoaded(world, pos);
            LOGGER.debug("Asked to force loading chunk at {}, loaded={}", pos, chunkLoaded);
            if (chunkLoaded) {
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
        if (QuarryPlus.config != null && !QuarryPlus.config.common.enableChunkLoader.get()) return;
        LOGGER.debug("Asked to unload chunk at {}. preLoaded={}", pos, preLoaded);
        if (!preLoaded && world instanceof ServerLevel serverWorld) {
            serverWorld.setChunkForced(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()), false);
        }
    }

    static final TicketType<ChunkPos> QUARRY_PLUS_MINING = TicketType.create("%s:%s".formatted(QuarryPlus.modID, "mining_ticket"), Comparator.comparingLong(ChunkPos::toLong));
    /**
     * Border level
     *
     * @see net.minecraft.server.level.ChunkLevel
     * @see <a href="https://minecraft.wiki/w/Chunk#Java_Edition_chunk_loading">WIKI</a>
     */
    static final int TICKET_LEVEL = 33;

    public static void makeChunkLoadedForMining(ServerLevel level, Area area) {
        LOGGER.debug("Make custom chunk load ticket for {}", area);
        var distanceManager = level.getChunkSource().chunkMap.getDistanceManager();
        operateForChunks(area, pos -> distanceManager.addTicket(QUARRY_PLUS_MINING, pos, TICKET_LEVEL, pos));
    }

    public static void removeChunkLoadTicket(ServerLevel level, Area area) {
        LOGGER.debug("Remove custom chunk load ticket for {}", area);
        var distanceManager = level.getChunkSource().chunkMap.getDistanceManager();
        operateForChunks(area, pos -> distanceManager.removeTicket(QUARRY_PLUS_MINING, pos, TICKET_LEVEL, pos));
    }

    static void operateForChunks(Area area, Consumer<ChunkPos> consumer) {
        var minX = SectionPos.blockToSectionCoord(area.minX());
        var minZ = SectionPos.blockToSectionCoord(area.minZ());
        var maxX = SectionPos.blockToSectionCoord(area.maxX());
        var maxZ = SectionPos.blockToSectionCoord(area.maxZ());
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                consumer.accept(new ChunkPos(x, z));
            }
        }
    }
}
