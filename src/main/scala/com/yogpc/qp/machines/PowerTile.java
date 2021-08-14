package com.yogpc.qp.machines;

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
import org.jetbrains.annotations.Nullable;

public class PowerTile extends BlockEntity {
    public static final long ONE_FE = 1_000_000_000L;
    private final EnergyCounter energyCounter;
    private long energy;
    private long maxEnergy;
    protected boolean chunkPreLoaded;

    public PowerTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, ONE_FE * 1000);
    }

    public PowerTile(BlockEntityType<?> type, BlockPos pos, BlockState state, long maxEnergy) {
        super(type, pos, state);
        this.maxEnergy = maxEnergy;
        this.energyCounter = EnergyCounter.createInstance(QuarryPlus.config.common.debug, "%s(%s)".formatted(getClass().getSimpleName(), pos));
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

    protected boolean hasEnoughEnergy() {
        return getEnergy() >= 0;
    }

    /**
     * @param amount   the energy
     * @param simulate if simulate, the actual energy doesn't change.
     * @return the amount of <strong>accepted</strong> energy.
     */
    public long addEnergy(long amount, boolean simulate) {
        assert world != null;
        long accepted = Math.min(maxEnergy - energy, amount);
        if (!simulate) {
            energy += accepted;
            energyCounter.getEnergy(world.getTime(), accepted);
        }
        return accepted;
    }

    /**
     * Use the energy. Returns if the energy is consumed to continue working.
     *
     * @param amount the energy
     * @param reason the reason to use the energy
     * @param force  whether machine should continue if machine doesn't have enough energy.
     * @return {@code true} if the energy is consumed. When {@code false}, the machine doesn't have enough energy to work.
     */
    public boolean useEnergy(long amount, Reason reason, boolean force) {
        assert world != null;
        if (energy >= amount || force) {
            energy -= amount;
            energyCounter.useEnergy(world.getTime(), amount, reason);
            return true;
        } else {
            return false;
        }
    }

    protected void logUsage(Consumer<String> logger) {
        energyCounter.logUsageMap(logger);
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

    @Nullable
    public static BlockEntityTicker<PowerTile> logTicker() {
        if (QuarryPlus.config.common.debug)
            return (w, p, s, blockEntity) -> blockEntity.energyCounter.logOutput(w.getTime());
        else return null;
    }

    @Nullable
    public static BlockEntityTicker<PowerTile> getGenerator() {
        if (EnergyIntegration.hasAnyEnergyModule() && !QuarryPlus.config.common.noEnergy)
            return null;
        else
            return (w, p, s, blockEntity) -> blockEntity.addEnergy(blockEntity.getMaxEnergy(), false);
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
