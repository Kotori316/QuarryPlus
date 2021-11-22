package com.yogpc.qp.machines;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.integration.EnergyIntegration;
import com.yogpc.qp.utils.QuarryChunkLoadUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

public class PowerTile extends BlockEntity {
    public static final long ONE_FE = 1_000_000_000L;
    private final EnergyCounter energyCounter;
    protected long energy;
    protected long maxEnergy;
    protected boolean chunkPreLoaded;

    public PowerTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, ONE_FE * 1000);
    }

    public PowerTile(BlockEntityType<?> type, BlockPos pos, BlockState state, long maxEnergy) {
        super(type, pos, state);
        this.maxEnergy = maxEnergy;
        this.energyCounter = EnergyCounter.createInstance(QuarryPlus.config.common.debug,
            "%s(%d, %d, %d)".formatted(getClass().getSimpleName(), pos.getX(), pos.getY(), pos.getZ()));
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.putLong("energy", energy);
        nbt.putLong("maxEnergy", maxEnergy);
        nbt.putBoolean("chunkPreLoaded", chunkPreLoaded);
        return super.save(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
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

    public void setMaxEnergy(long maxEnergy) {
        this.maxEnergy = maxEnergy;
    }

    protected boolean hasEnoughEnergy() {
        return getEnergy() > 0;
    }

    /**
     * @param amount   the energy
     * @param simulate if simulating, the actual energy doesn't change.
     * @return the amount of <strong>accepted</strong> energy.
     */
    public long addEnergy(long amount, boolean simulate) {
        assert level != null;
        long accepted = Math.min(maxEnergy - energy, amount);
        if (!simulate) {
            energy += accepted;
            energyCounter.getEnergy(level.getGameTime(), accepted);
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
        assert level != null;
        if (energy >= amount || force) {
            energy -= amount;
            energyCounter.useEnergy(level.getGameTime(), amount, reason);
            return true;
        } else {
            return false;
        }
    }

    protected void logUsage() {
        energyCounter.logUsageMap();
    }

    public void setChunkPreLoaded(boolean chunkPreLoaded) {
        this.chunkPreLoaded = chunkPreLoaded;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide)
            QuarryChunkLoadUtil.makeChunkUnloaded(level, worldPosition, chunkPreLoaded);
    }

    @Nullable
    public static BlockEntityTicker<PowerTile> logTicker() {
        if (QuarryPlus.config.common.debug)
            return (w, p, s, blockEntity) -> blockEntity.energyCounter.logOutput(w.getGameTime());
        else return null;
    }

    @Nullable
    public static BlockEntityTicker<PowerTile> getGenerator() {
        if (!isInfiniteEnergyEnabled(EnergyIntegration.hasAnyEnergyModule(), QuarryPlus.config.common.debug, QuarryPlus.config.common.noEnergy))
            return null;
        else
            return (w, p, s, tile) -> tile.addEnergy(tile.getMaxEnergy() - tile.getEnergy(), false);
    }

    @VisibleForTesting
    static boolean isInfiniteEnergyEnabled(boolean hasEnergyMod, boolean isDebug, boolean noEnergyConfig) {
        return (!hasEnergyMod && !isDebug) || noEnergyConfig;
    }

    public static class Constants {

        public static long getMakeFrameEnergy(EnchantmentLevel.HasEnchantments enchantments) {
            return (long) (enchantments.getAccessor().makeFrame() * ONE_FE / (1 + Math.max(0, enchantments.unbreakingLevel())));
        }

        public static long getAdvSearchEnergy(int blocks, EnchantmentLevel.HasEnchantments enchantments) {
            final double FIFTH_ROOT_OF_10 = 1.5848931924611136;
            double heightEnergy = blocks * enchantments.getAccessor().moveHead() * ONE_FE;
            double efficiencyBalanced = Math.pow(FIFTH_ROOT_OF_10, enchantments.efficiencyLevel()) * heightEnergy;
            return (long) (efficiencyBalanced / (1 + Math.max(0, enchantments.unbreakingLevel())));
        }

        public static long getBreakEnergy(float hardness, EnchantmentLevel.HasEnchantments enchantments) {
            if (hardness < 0 || Float.isInfinite(hardness))
                return (long) (200 * enchantments.getAccessor().breakBlock() * ONE_FE);
            double modified = ((double) hardness) / (1 + Math.max(0, enchantments.unbreakingLevel()));
            var fortune = enchantments.fortuneLevel();
            if (fortune != 0) modified *= (fortune + 1);
            if (enchantments.silktouchLevel() != 0) modified = (float) Math.pow(modified, 1.4d);

            return (long) (modified * enchantments.getAccessor().breakBlock() * ONE_FE);
        }

        public static long getMoveEnergy(double distance, EnchantmentLevel.HasEnchantments enchantments) {
            return (long) (distance * enchantments.getAccessor().moveHead() * ONE_FE) / (1 + Math.max(0, enchantments.unbreakingLevel()));
        }

        public static long getBreakBlockFluidEnergy(EnchantmentLevel.HasEnchantments enchantments) {
            return (long) (enchantments.getAccessor().removeFluid() * ONE_FE / (1 + Math.max(0, enchantments.unbreakingLevel())));
        }
    }

    public enum Reason {
        MAKE_FRAME, BREAK_BLOCK, REMOVE_FLUID, MOVE_HEAD, ADV_PUMP_FLUID, ADV_SEARCH
    }
}
