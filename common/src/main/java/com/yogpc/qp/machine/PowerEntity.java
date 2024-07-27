package com.yogpc.qp.machine;

import com.yogpc.qp.PlatformAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Objects;
import java.util.function.LongSupplier;
import java.util.stream.Stream;

public abstract class PowerEntity extends BlockEntity {
    public static final long ONE_FE = 1_000_000_000L;
    private final EnergyCounter energyCounter;
    private LongSupplier timeProvider;
    private long energy;
    private long maxEnergy;
    public final boolean enabled;

    protected PowerEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.energyCounter = EnergyCounter.createInstance(PlatformAccess.getConfig().isDebug(), "%s(%d, %d, %d)".formatted(getClass().getSimpleName(), pos.getX(), pos.getY(), pos.getZ()));
        this.enabled = true;
        setTimeProvider(() -> Objects.requireNonNull(this.level,
            """
                Level in block entity is null. Are you in test?
                Make sure to run `setTimeProvider` to replace the default time provider."""
        ).getGameTime());
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("energy", energy);
        tag.putLong("maxEnergy", maxEnergy);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getLong("energy");
        setMaxEnergy(tag.getLong("maxEnergy"));
    }

    /*
    -------------------- ENERGY --------------------
     */
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

    @VisibleForTesting
    public void setTimeProvider(LongSupplier timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * @param amount   the energy
     * @param simulate if simulating, the actual energy doesn't change.
     * @return the amount of <strong>accepted</strong> energy.
     */
    public final long addEnergy(long amount, boolean simulate) {
        long accepted = Math.min(Math.min(maxEnergy - energy, amount), getMaxReceive());
        if (!simulate && accepted >= 0) {
            energy += accepted;
            energyCounter.getEnergy(this.timeProvider, accepted);
            setChanged();
        }
        return accepted;
    }

    /**
     * @param amount   the energy to use
     * @param simulate if simulating, the actual energy doesn't change.
     * @param force    if true, the energy must be consumed even if this machine doesn't have enough amount.
     * @param reason   the name of how this energy is used.
     * @return the amount of <strong>consumed</strong> energy.
     */
    public final long useEnergy(long amount, boolean simulate, boolean force, String reason) {
        long used = force ? amount : Math.min(amount, energy);
        if (!simulate && used >= 0) {
            energy -= used;
            energyCounter.useEnergy(this.timeProvider, used, reason);
            setChanged();
        }
        return used;
    }

    public final void setEnergy(long energy, boolean log) {
        if (this.energy > energy) {
            // Energy is consumed
            energyCounter.useEnergy(log ? this.timeProvider : () -> 1L, this.energy - energy, "FORCE_SET");
        } else {
            // Energy is sent
            energyCounter.getEnergy(log ? this.timeProvider : () -> 1L, energy - this.energy);
        }
        setChanged();
        this.energy = energy;
    }

    public Stream<MutableComponent> checkerLogs() {
        return Stream.of(
            Component.literal("-".repeat(32)),
            Component.empty()
                .append(Component.literal("BlockEntity").withStyle(ChatFormatting.AQUA))
                .append(": %s".formatted(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(getType())))
                .append(" (%s)".formatted(getClass().getSimpleName())),
            detail(ChatFormatting.AQUA, "Pos", getBlockPos().toShortString()),
            detail(ChatFormatting.AQUA, "Energy", "%d".formatted(getEnergy() / ONE_FE))
        );
    }

    public static BlockEntityTicker<PowerEntity> logTicker() {
        return (w, p, s, blockEntity) -> blockEntity.energyCounter.logOutput(blockEntity.timeProvider.getAsLong());
    }

    @Nullable
    public static BlockEntityTicker<PowerEntity> getGenerator() {
        if (false) {
            return (w, p, s, tile) -> tile.setEnergy(tile.getMaxEnergy(), true);
        } else {
            return null;
        }
    }

    protected static MutableComponent detail(ChatFormatting color, String title, String content) {
        return Component.empty()
            .append(Component.literal(title).withStyle(color))
            .append(": ")
            .append(content);
    }
}
