package com.yogpc.qp.machines;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

import com.yogpc.qp.integration.EnergyIntegration;
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
        return super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        energy = nbt.getLong("energy");
        if (nbt.contains("maxEnergy"))
            maxEnergy = nbt.getLong("maxEnergy");
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

    public boolean useEnergy(long amount, Reason reason, int unbreaking) {
        amount = amount / (1 + Math.max(0, unbreaking));
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

    public static BlockEntityTicker<PowerTile> getGenerator() {
        if (EnergyIntegration.hasAnyEnergyModule())
            return (w, p, s, blockEntity) -> {
            };
        else
            return (w, p, s, blockEntity) -> blockEntity.addEnergy(1000 * ONE_FE, false);
    }

    public static class Constants {
        public static final long MAKE_FRAME = ONE_FE * 15;
        public static final long BREAK_BLOCK_BASE = ONE_FE * 5;
        public static final long BREAK_BLOCK_FLUID = BREAK_BLOCK_BASE * 10;
        public static final long MOVE_HEAD_BASE = ONE_FE / 10;

        public static long getBreakEnergy(float hardness) {
            if (hardness < 0) return 250 * BREAK_BLOCK_BASE;
            return (long) (hardness * BREAK_BLOCK_BASE);
        }

        public static long getMoveEnergy(double distance) {
            return (long) (distance * MOVE_HEAD_BASE);
        }
    }

    public enum Reason {
        MAKE_FRAME, BREAK_BLOCK, REMOVE_FLUID, MOVE_HEAD
    }
}
