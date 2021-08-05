package com.yogpc.qp.machines;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.integration.EnergyIntegration;
import com.yogpc.qp.utils.QuarryChunkLoadUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class PowerTile extends BlockEntity {
    public static final long ONE_FE = 1_000_000_000L;
    private final Map<Reason, Long> usageMap = new EnumMap<>(Reason.class);
    private long energy;
    private long maxEnergy;
    protected boolean chunkPreLoaded;

    public PowerTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, ONE_FE * 1000);
    }

    public PowerTile(BlockEntityType<?> type, BlockPos pos, BlockState state, long maxEnergy) {
        super(type, pos, state);
        this.maxEnergy = maxEnergy;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putLong("energy", energy);
        nbt.putLong("maxEnergy", maxEnergy);
        nbt.putBoolean("chunkPreLoaded", chunkPreLoaded);
        return super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        energy = nbt.getLong("energy");
        if (nbt.contains("maxEnergy"))
            maxEnergy = nbt.getLong("maxEnergy");
        chunkPreLoaded = nbt.getBoolean("chunkPreLoaded");
    }

    public long getEnergy() {
        return energy;
    }

    public long getMaxEnergy() {
        return maxEnergy;
    }

    public long addEnergy(long amount, boolean simulate) {
        long accepted = Math.min(maxEnergy - energy, amount);
        if (!simulate)
            energy += accepted;
        return accepted;
    }

    public boolean useEnergy(long amount, Reason reason) {
        if (energy > amount) {
            energy -= amount;
            usageMap.merge(reason, amount, Long::sum);
            return true;
        } else {
            return false;
        }
    }

    public void logUsage(Consumer<String> logger) {
        usageMap.entrySet().stream()
            .map(e -> "%s -> %d".formatted(e.getKey(), e.getValue()))
            .forEach(logger);
    }

    public void setChunkPreLoaded(boolean chunkPreLoaded) {
        this.chunkPreLoaded = chunkPreLoaded;
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        if (world != null && !world.isClient)
            QuarryChunkLoadUtil.makeChunkUnloaded(world, pos, chunkPreLoaded);
    }

    public static BlockEntityTicker<PowerTile> getGenerator() {
        if (EnergyIntegration.hasAnyEnergyModule() && !QuarryPlus.config.common.noEnergy)
            return (w, p, s, blockEntity) -> {
            };
        else
            return (w, p, s, blockEntity) -> blockEntity.addEnergy(1000 * ONE_FE, false);
    }

    public static class Constants {
        private static final long MAKE_FRAME = ONE_FE * 15;
        private static final long BREAK_BLOCK_BASE = ONE_FE * 5;
        private static final long BREAK_BLOCK_FLUID = BREAK_BLOCK_BASE * 10;
        private static final long MOVE_HEAD_BASE = ONE_FE / 10;

        public static long getMakeFrameEnergy(EnchantmentLevel.HasEnchantments enchantments) {
            return MAKE_FRAME / (1 + Math.max(0, enchantments.unbreakingLevel()));
        }

        public static long getBreakEnergy(float hardness, EnchantmentLevel.HasEnchantments enchantments) {
            hardness = hardness / (1 + Math.max(0, enchantments.unbreakingLevel()));
            var fortune = enchantments.fortuneLevel();
            if (fortune != 0) hardness *= (fortune + 1);
            if (enchantments.silktouchLevel() != 0) hardness = (float) Math.pow(hardness, 1.4d);

            if (hardness < 0) return 250 * BREAK_BLOCK_BASE;
            return (long) (hardness * BREAK_BLOCK_BASE);
        }

        public static long getMoveEnergy(double distance, EnchantmentLevel.HasEnchantments enchantments) {
            return (long) (distance * MOVE_HEAD_BASE) / (1 + Math.max(0, enchantments.unbreakingLevel()));
        }

        public static long getBreakBlockFluidEnergy(EnchantmentLevel.HasEnchantments enchantments) {
            return BREAK_BLOCK_FLUID / (1 + Math.max(0, enchantments.unbreakingLevel()));
        }
    }

    public enum Reason {
        MAKE_FRAME, BREAK_BLOCK, REMOVE_FLUID, MOVE_HEAD
    }
}
