package com.yogpc.qp.integration.ftbchunks;

import com.yogpc.qp.Config;
import com.yogpc.qp.machines.base.Area;
import com.yogpc.qp.machines.base.IRemotePowerOn;
import com.yogpc.qp.machines.quarry.QuarryFakePlayer;
import com.yogpc.qp.machines.quarry.TileMiningWell;
import com.yogpc.qp.machines.quarry.TileQuarry;
import dev.ftb.mods.ftbchunks.data.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.data.Protection;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scala.Option;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class QuarryChunkProtectionManager {
    @SuppressWarnings("SpellCheckingInspection")
    private static final String CHUNK_MOD_ID = "ftbchunks";
    // -1 not checked. 0 not loaded. 1 loaded.
    private static final AtomicInteger IS_LOADED = new AtomicInteger(-1);
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
    private static boolean isNew() {
        if (IS_NEW.get() == -1) {
            try {
                Class.forName("dev.ftb.mods.ftbchunks.data.FTBChunksAPI");
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
                if (isNew())
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
                if (isNew())
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

class ManagerNew {
    static boolean canEdit(World world, BlockPos pos, ServerPlayerEntity player, BlockState state) {
        return !FTBChunksAPI.getManager().protect(player, Hand.MAIN_HAND, pos, Protection.EDIT_BLOCK);
    }

    static boolean canEdit(Area area, ServerPlayerEntity player) {
        for (int x = area.xMin(); x <= area.xMax(); x += 16) {
            for (int z = area.zMin(); z <= area.zMax(); z += 16) {
                if (FTBChunksAPI.getManager().protect(player, Hand.MAIN_HAND, new BlockPos(x, 0, z), Protection.EDIT_BLOCK)) {
                    return false;
                }
            }
        }
        return true;
    }
}
