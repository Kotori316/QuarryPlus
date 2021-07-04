package com.yogpc.qp.integration.ftbchunks;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.yogpc.qp.Config;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scala.Option;

public class QuarryChunkProtectionManager {
    @SuppressWarnings("SpellCheckingInspection")
    private static final String CHUNK_MOD_ID = "ftbchunks";
    // -1 not checked. 0 not loaded. 1 loaded.
    private static final AtomicInteger IS_LOADED = new AtomicInteger(-1);
    private static final AtomicInteger IS_OLD = new AtomicInteger(-1);
    private static final AtomicInteger IS_NEW = new AtomicInteger(-1);
    private static final String MESSAGE = "Quarry found protected chunks in the area. Please add [QuarryPlus] as your ally or allow fake players to interact this area.";
    private static final Logger LOGGER = LogManager.getLogger();

    private static boolean hasProtectionMod() {
        int pre = IS_LOADED.get();
        if (pre == -1) {
            IS_LOADED.set(isModLoaded() ? 1 : 0);
            return IS_LOADED.get() == 1;
        } else {
            // Already checked the status.
            return pre == 1;
        }
    }

    private static boolean isModLoaded() {
        if (ModList.get().isLoaded(CHUNK_MOD_ID)) {
            try {
                return Config.common().sendNotificationOfChunkProtection().get();
            } catch (Exception ignore) {
                return true;
            }
        } else {
            return false;
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    private static boolean isOld() {
        if (IS_OLD.get() == -1) {
            try {
                Class.forName("com.feed_the_beast.mods.ftbchunks.api.FTBChunksAPI");
                IS_OLD.set(1);
            } catch (Exception ignore) {
                IS_OLD.set(0);
            }
        }
        return IS_OLD.get() == 1;
    }

    @SuppressWarnings("SpellCheckingInspection")
    private static boolean isNew() {
        if (IS_NEW.get() == -1) {
            try {
                Class.forName("com.feed_the_beast.mods.ftbchunks.api.FTBChunksAPI");
                IS_NEW.set(1);
            } catch (Exception ignore) {
                IS_NEW.set(0);
            }
        }
        return IS_NEW.get() == 1;
    }

    private static boolean notEditable(World world, BlockPos pos, ServerPlayerEntity player, BlockState state) {
        if (hasProtectionMod()) {
            try {
                if (isOld())
                    return !ManagerOld.canEdit(world, pos, player, state);
                else if (isNew())
                    return !ManagerNew.canEdit(world, pos, player, state);
                else
                    return false;
            } catch (Exception e) {
                IS_LOADED.set(0);
                LOGGER.error("Error happened in FTB Chunk check. MiningWell", e);
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean notEditable(Area area, ServerPlayerEntity player) {
        if (hasProtectionMod()) {
            try {
                if (isOld())
                    return !ManagerOld.canEdit(area, player);
                else if (isNew())
                    return !ManagerNew.canEdit(area, player);
                else
                    return false;
            } catch (Exception e) {
                IS_LOADED.set(0);
                LOGGER.error("Error happened in FTB Chunk check. Quarry", e);
                return false;
            }
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

class ManagerOld {
    static boolean canEdit(World world, BlockPos pos, ServerPlayerEntity player, BlockState state) {
        com.feed_the_beast.mods.ftbchunks.api.ClaimedChunk chunk =
            com.feed_the_beast.mods.ftbchunks.api.FTBChunksAPI.INSTANCE.getManager().getChunk(new com.feed_the_beast.mods.ftbchunks.api.ChunkDimPos(world, pos));
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
                com.feed_the_beast.mods.ftbchunks.api.ClaimedChunk chunk =
                    com.feed_the_beast.mods.ftbchunks.api.FTBChunksAPI.INSTANCE.getManager().getChunk(new com.feed_the_beast.mods.ftbchunks.api.ChunkDimPos(dimensionType, x >> 4, z >> 4));
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

class ManagerNew {
    static boolean canEdit(World world, BlockPos pos, ServerPlayerEntity player, BlockState state) {
        dev.ftb.mods.ftbchunks.data.ClaimedChunk chunk = dev.ftb.mods.ftbchunks.data.FTBChunksAPI.getManager().getChunk(new dev.ftb.mods.ftblibrary.math.ChunkDimPos(world, pos));
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
                dev.ftb.mods.ftbchunks.data.ClaimedChunk chunk =
                    dev.ftb.mods.ftbchunks.data.FTBChunksAPI.getManager().getChunk(new dev.ftb.mods.ftblibrary.math.ChunkDimPos(dimensionType, x >> 4, z >> 4));
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
