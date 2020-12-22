package com.yogpc.qp.integration.ftbchunks;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.feed_the_beast.mods.ftbchunks.api.ChunkDimPos;
import com.feed_the_beast.mods.ftbchunks.api.ClaimedChunk;
import com.feed_the_beast.mods.ftbchunks.api.FTBChunksAPI;
import com.yogpc.qp.machines.base.Area;
import com.yogpc.qp.machines.base.IRemotePowerOn;
import com.yogpc.qp.machines.quarry.QuarryFakePlayer;
import com.yogpc.qp.machines.quarry.TileMiningWell;
import com.yogpc.qp.machines.quarry.TileQuarry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.ModList;
import scala.Option;

public class QuarryChunkProtectionManager {
    @SuppressWarnings("SpellCheckingInspection")
    private static final String CHUNK_MOD_ID = "ftbchunks";
    // -1 not checked. 0 not loaded. 1 loaded.
    private static final AtomicInteger IS_LOADED = new AtomicInteger(-1);
    private static final String MESSAGE = "Quarry found protected chunks in the area. Please add [QuarryPlus] as your ally.";

    public static boolean hasProtectionMod() {
        int pre = IS_LOADED.get();
        if (pre == -1) {
            IS_LOADED.lazySet(ModList.get().isLoaded(CHUNK_MOD_ID) ? 1 : 0);
            return IS_LOADED.get() == 1;
        } else {
            // Already checked the status.
            return pre == 1;
        }
    }

    public static boolean notEditable(World world, BlockPos pos, ServerPlayerEntity player, BlockState state) {
        if (hasProtectionMod()) {
            return !Manager.canEdit(world, pos, player, state);
        } else {
            return false;
        }
    }

    public static boolean notEditable(Area area, ServerPlayerEntity player) {
        if (hasProtectionMod()) {
            return !Manager.canEdit(area, player);
        } else {
            return false;
        }
    }

    public static Consumer<TileMiningWell> minerSendProtectionNotification(LivingEntity placer) {
        if (hasProtectionMod()) {
            return t -> {
                World world = t.getWorld();
                if (world instanceof ServerWorld) {
                    ServerWorld serverWorld = (ServerWorld) world;
                    QuarryFakePlayer player = QuarryFakePlayer.get(serverWorld, t.getPos());
                    if (notEditable(serverWorld, t.getPos(), player, Blocks.AIR.getDefaultState())) {
                        placer.sendMessage(new StringTextComponent(MESSAGE), Util.DUMMY_UUID);
                    }
                }
            };
        } else {
            return t -> {
            };
        }
    }

    public static <T extends TileQuarry> Consumer<T> quarrySendProtectionNotification(LivingEntity placer) {
        if (hasProtectionMod()) {
            return t -> sendMessageWithArea(placer, t,
                Area.apply(t.xMin, t.yMax, t.zMin, t.xMax, t.yMax, t.zMax,
                    Option.apply(t.getWorld()).map(v1 -> v1.getDimensionKey().getLocation())));
        } else {
            return t -> {
            };
        }
    }

    public static <T extends TileEntity & IRemotePowerOn> Consumer<T> sendProtectionNotification(LivingEntity placer) {
        if (hasProtectionMod()) {
            return t -> sendMessageWithArea(placer, t, t.getArea());
        } else {
            return t -> {
            };
        }
    }

    private static void sendMessageWithArea(LivingEntity placer, TileEntity t, Area area) {
        World world = t.getWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;
            QuarryFakePlayer player = QuarryFakePlayer.get(serverWorld, t.getPos());
            if (notEditable(area, player)) {
                placer.sendMessage(new StringTextComponent(MESSAGE), Util.DUMMY_UUID);
            }
        }
    }
}

class Manager {
    static boolean canEdit(World world, BlockPos pos, ServerPlayerEntity player, BlockState state) {
        ClaimedChunk chunk = FTBChunksAPI.INSTANCE.getManager().getChunk(new ChunkDimPos(world, pos));
        if (chunk != null) {
            return chunk.canEdit(player, state);
        } else {
            return true;
        }
    }

    static boolean canEdit(Area area, ServerPlayerEntity player) {
        BlockState state = Blocks.AIR.getDefaultState();
        RegistryKey<World> dimensionType = area.getDimensionType() != null ? area.getDimensionType() : player.getEntityWorld().getDimensionKey();
        for (int x = area.xMin(); x <= area.xMax(); x += 16) {
            for (int z = area.zMin(); z <= area.zMax(); z += 16) {
                ClaimedChunk chunk = FTBChunksAPI.INSTANCE.getManager().getChunk(new ChunkDimPos(dimensionType, x >> 4, z >> 4));
                if (chunk != null) {
                    if (!chunk.canEdit(player, state)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
