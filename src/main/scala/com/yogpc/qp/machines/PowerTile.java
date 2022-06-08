package com.yogpc.qp.machines;

import java.util.Objects;
import java.util.function.LongSupplier;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.utils.QuarryChunkLoadUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

public abstract class PowerTile extends BlockEntity implements IEnergyStorage {
    public static final long ONE_FE = 1_000_000_000L;
    private final EnergyCounter energyCounter;
    private long energy;
    private long maxEnergy;
    @Nullable
    private Boolean chunkPreLoaded = null;
    public final boolean enabled;
    private LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> this);
    protected final PowerConfig powerConfig;
    private LongSupplier timeProvider;

    public PowerTile(BlockEntityType<?> type, @NotNull BlockPos pos, BlockState state) {
        super(type, pos, state);
        ResourceLocation typeName = Objects.requireNonNull(ForgeRegistries.BLOCK_ENTITIES.getKey(type));
        this.enabled = QuarryPlus.config.enableMap.enabled(typeName);
        this.energyCounter = EnergyCounter.createInstance(QuarryPlus.config.debug() && enabled,
            "%s(%d, %d, %d)".formatted(getClass().getSimpleName(), pos.getX(), pos.getY(), pos.getZ()));
        this.powerConfig = PowerConfig.getMachineConfig(typeName.getPath());
        this.maxEnergy = this.powerConfig.maxEnergy();
        setTimeProvider(() -> Objects.requireNonNull(this.level, "Level in block entity is null. Are you in test?").getGameTime());
    }

    @Override
    protected final void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putLong("energy", energy);
        nbt.putLong("maxEnergy", maxEnergy);
        if (chunkPreLoaded != null)
            nbt.putBoolean("chunkPreLoaded", chunkPreLoaded);
        saveNbtData(nbt);
    }

    protected abstract void saveNbtData(CompoundTag nbt);

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        energy = nbt.getLong("energy");
        if (nbt.contains("maxEnergy"))
            setMaxEnergy(nbt.getLong("maxEnergy"));
        if (nbt.contains("chunkPreLoaded", CompoundTag.TAG_BYTE))
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
        long accepted = Math.min(Math.min(maxEnergy - energy, amount), getMaxReceive());
        if (!simulate && accepted != 0) {
            energy += accepted;
            energyCounter.getEnergy(this.timeProvider, accepted);
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
        if (amount > maxEnergy && QuarryPlus.config.debug())
            QuarryPlus.LOGGER.warn("{} required {} FE, which is over {}.", energyCounter.name, amount / ONE_FE, getMaxEnergyStored());
        if (energy >= amount || force) {
            energy -= amount;
            energyCounter.useEnergy(this.timeProvider, amount, reason);
            setChanged();
            return true;
        } else {
            return false;
        }
    }

    protected final void setEnergy(long energy, boolean log) {
        if (this.energy > energy) {
            // Energy is consumed
            energyCounter.useEnergy(log ? this.timeProvider : () -> 1L, this.energy - energy, Reason.FORCE);
        } else {
            // Energy is sent
            energyCounter.getEnergy(log ? this.timeProvider : () -> 1L, energy - this.energy);
        }
        setChanged();
        this.energy = energy;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        long accepted = addEnergy(maxReceive * ONE_FE, simulate);
        int acceptedInFE = (int) (accepted / ONE_FE);
        if (acceptedInFE > maxReceive || acceptedInFE < 0) {
            QuarryPlus.LOGGER.warn("{} got unexpected energy({} FE, {} micro MJ), MaxReceive={}",
                energyCounter.name, acceptedInFE, acceptedInFE, maxReceive);
            return maxReceive;
        }
        return acceptedInFE;
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

    protected final String energyString() {
        return "%sEnergy:%s %f/%d FE (%d)".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, getEnergy() / (double) PowerTile.ONE_FE, getMaxEnergyStored(), getEnergy());
    }

    /**
     * Implementation of {@link PowerConfig.Provider}
     */
    public PowerConfig getPowerConfig() {
        return powerConfig;
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
        if (level != null && !level.isClientSide && enabled && chunkPreLoaded != null)
            QuarryChunkLoadUtil.makeChunkUnloaded(level, getBlockPos(), chunkPreLoaded);
    }

    @Nullable
    public static BlockEntityTicker<PowerTile> logTicker() {
        if (QuarryPlus.config.debug())
            return (w, p, s, blockEntity) -> blockEntity.energyCounter.logOutput(blockEntity.timeProvider.getAsLong());
        else return null;
    }

    @Nullable
    public static BlockEntityTicker<PowerTile> getGenerator() {
        if (QuarryPlus.config.common.noEnergy.get()) {
            return (w, p, s, tile) -> tile.setEnergy(tile.getMaxEnergy(), true);
        } else {
            return null;
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyHandler.invalidate();
        if (this instanceof MachineStorage.HasStorage storage) {
            storage.getStorage().itemHandler.invalidate();
            storage.getStorage().fluidHandler.invalidate();
        }
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        energyHandler = LazyOptional.of(() -> this);
        if (this instanceof MachineStorage.HasStorage storage) {
            storage.getStorage().setHandler();
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityEnergy.ENERGY) {
            if (this.canReceive() || this.canExtract())
                return CapabilityEnergy.ENERGY.orEmpty(cap, energyHandler);
        } else if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && this instanceof MachineStorage.HasStorage storage) {
            return storage.getStorage().itemHandler.cast();
        } else if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && this instanceof MachineStorage.HasStorage storage) {
            return storage.getStorage().fluidHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    public static boolean stillValid(PowerTile tile, Player player) {
        return tile.level != null && tile.getBlockPos().distToCenterSqr(player.position()) < 64;
    }

    public static boolean isFullFluidBlock(BlockState state) {
        return state.getMaterial().isLiquid();
    }

    @VisibleForTesting
    void setTimeProvider(LongSupplier timeProvider) {
        this.timeProvider = timeProvider;
    }

    public enum Reason {
        MAKE_FRAME, BREAK_BLOCK, REMOVE_FLUID, MOVE_HEAD, ADV_PUMP_FLUID, WORKBENCH, FORCE, EXP_COLLECT, BOOK_MOVER, ADV_SEARCH, MINI_QUARRY, FILLER
    }
}
