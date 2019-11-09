package com.yogpc.qp.machines.base;

import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.Cancelable;

import static jp.t2v.lab.syntax.MapStreamSyntax.optCast;

/**
 * Implemented by a sub class of {@link TileEntity} which has ability to keep chunk loaded.
 */
public interface IChunkLoadTile {

    /**
     * Called when tile is placed. <strong>No need to call this when world is loaded.</strong>
     */
    default void requestTicket() {
        Optional.ofNullable(((TileEntity) this).getWorld())
            .flatMap(optCast(ServerWorld.class))
            .ifPresent(worldServer -> {
                BlockPos blockPos = ((TileEntity) this).getPos();
                ChunkPos pos = new ChunkPos(blockPos);
                MinecraftForge.EVENT_BUS.register(this);
                worldServer.forceChunk(pos.x, pos.z, true);
                QuarryPlus.LOGGER.debug(String.format("QuarryPlus ChunkLoader added [%d, %d] for %s", pos.x, pos.z, this));
            });
    }

    /**
     * Called when block is removed, in {@link Block#onReplaced(BlockState, World, BlockPos, BlockState, boolean)}
     * or {@link TileEntity#remove()}.<br>
     * There is no means to call this in {@link TileEntity#onChunkUnloaded()}.
     */
    @SuppressWarnings("deprecation") //Just here to suppress JavaDoc waring.
    default void releaseTicket() {
        Optional.ofNullable(((TileEntity) this).getWorld())
            .flatMap(optCast(ServerWorld.class))
            .ifPresent(worldServer -> {
                BlockPos blockPos = ((TileEntity) this).getPos();
                ChunkPos pos = new ChunkPos(blockPos);
                MinecraftForge.EVENT_BUS.unregister(this);
                if (!MinecraftForge.EVENT_BUS.post(new ReleaseChunkLoadEvent(worldServer, pos))) {
                    worldServer.forceChunk(pos.x, pos.z, false);
                    QuarryPlus.LOGGER.debug(String.format("QuarryPlus ChunkLoader removed [%d, %d] for %s", pos.x, pos.z, this));
                }
            });
    }

    @Cancelable
    class ReleaseChunkLoadEvent extends WorldEvent {

        public final ChunkPos pos;

        public ReleaseChunkLoadEvent(IWorld world, ChunkPos pos) {
            super(world);
            this.pos = pos;
        }
    }
}
