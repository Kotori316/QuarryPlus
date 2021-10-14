package com.yogpc.qp.machines;

import java.util.Objects;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.utils.QuarryChunkLoadUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public class PowerTile extends BlockEntity implements IEnergyStorage {
    public static final long ONE_FE = 1_000_000_000L;
    private final EnergyCounter energyCounter;
    private long energy;
    private long maxEnergy;
    protected boolean chunkPreLoaded;
    public final boolean enabled;

    public PowerTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, ONE_FE * 1000);
    }

    public PowerTile(BlockEntityType<?> type, BlockPos pos, BlockState state, long maxEnergy) {
        super(type, pos, state);
        this.maxEnergy = maxEnergy;
        this.enabled = QuarryPlus.config.enableMap.enabled(Objects.requireNonNull(type.getRegistryName()));
        this.energyCounter = EnergyCounter.createInstance(QuarryPlus.config.debug() && enabled,
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
            setMaxEnergy(nbt.getLong("maxEnergy"));
        chunkPreLoaded = nbt.getBoolean("chunkPreLoaded");
    }

    public final long getEnergy() {
        return energy;
    }

    public final long getMaxEnergy() {
        return maxEnergy;
    }

    protected final boolean hasEnoughEnergy() {
        return enabled && getEnergy() > 0;
    }

    protected long getMaxReceive() {
        return maxEnergy;
    }

    protected final void setMaxEnergy(long maxEnergy) {
        this.maxEnergy = maxEnergy;
    }

    /**
     * @param amount   the energy
     * @param simulate if simulating, the actual energy doesn't change.
     * @return the amount of <strong>accepted</strong> energy.
     */
    public final long addEnergy(long amount, boolean simulate) {
        assert level != null;
        long accepted = Math.min(Math.min(maxEnergy - energy, amount), getMaxReceive());
        if (!simulate && accepted != 0) {
            energy += accepted;
            energyCounter.getEnergy(level.getGameTime(), accepted);
            setChanged();
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
    public final boolean useEnergy(long amount, Reason reason, boolean force) {
        assert level != null;
        if (amount > maxEnergy && QuarryPlus.config.debug())
            QuarryPlus.LOGGER.warn("{} required {} FE, which is over {}.", energyCounter.name, amount / ONE_FE, getMaxEnergyStored());
        if (energy >= amount || force) {
            energy -= amount;
            energyCounter.useEnergy(level.getGameTime(), amount, reason);
            setChanged();
            return true;
        } else {
            return false;
        }
    }

    protected final void setEnergy(long energy) {
        if (level != null) {
            if (this.energy > energy) {
                // Energy is consumed
                energyCounter.useEnergy(level.getGameTime(), this.energy - energy, Reason.FORCE);
            } else {
                // Energy is sent
                energyCounter.getEnergy(level.getGameTime(), energy - this.energy);
            }
            setChanged();
        }
        this.energy = energy;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        long accepted = addEnergy(maxReceive * ONE_FE, simulate);
        return (int) (accepted / ONE_FE);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0; // In default, this machine is not for energy extraction/generation.
    }

    @Override
    public final int getEnergyStored() {
        return (int) (getEnergy() / ONE_FE);
    }

    @Override
    public final int getMaxEnergyStored() {
        return (int) (getMaxEnergy() / ONE_FE);
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    protected void logUsage() {
        // Debug or Production check is done in counter instance.
        energyCounter.logUsageMap();
    }

    public final void setChunkPreLoaded(boolean chunkPreLoaded) {
        this.chunkPreLoaded = chunkPreLoaded;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide && enabled)
            QuarryChunkLoadUtil.makeChunkUnloaded(level, getBlockPos(), chunkPreLoaded);
    }

    @Nullable
    public static BlockEntityTicker<PowerTile> logTicker() {
        if (QuarryPlus.config.debug())
            return (w, p, s, blockEntity) -> blockEntity.energyCounter.logOutput(w.getGameTime());
        else return null;
    }

    @Nullable
    public static BlockEntityTicker<PowerTile> getGenerator() {
        if (QuarryPlus.config.common.noEnergy.get()) {
            return (w, p, s, tile) -> tile.setEnergy(tile.getMaxEnergy());
        } else {
            return null;
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.orEmpty(cap, LazyOptional.of(() -> this));
        } else if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && this instanceof MachineStorage.HasStorage storage) {
            return storage.getStorage().itemHandler.cast();
        } else if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && this instanceof MachineStorage.HasStorage storage) {
            return storage.getStorage().fluidHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    public static boolean stillValid(PowerTile tile, Player player) {
        return tile.level != null && tile.getBlockPos().distSqr(player.position(), true) < 64;
    }

    public enum Reason {
        MAKE_FRAME, BREAK_BLOCK, REMOVE_FLUID, MOVE_HEAD, ADV_PUMP_FLUID, WORKBENCH, FORCE, EXP_COLLECT, BOOK_MOVER, ADV_SEARCH
    }
}
