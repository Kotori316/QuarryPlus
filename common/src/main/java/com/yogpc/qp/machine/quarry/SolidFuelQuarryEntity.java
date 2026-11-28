package com.yogpc.qp.machine.quarry;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.PowerMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public abstract class SolidFuelQuarryEntity extends QuarryEntity {
    /**
     * Total energy in FE per 1 tick of burn time of a fuel. (e.g. Coal: 1600 ticks -> 6400 FE)
     * It is 10 FE, same as general thermal generators, multiplied by 0.4, the efficiency of this machine.
     * This value is independent of {@link PowerMap.SolidFuelQuarry#generationSpeed()}, so the total energy of a fuel is fixed even if the speed is changed.
     * The running time of a fuel is {@code burnTime * TOTAL_FE_PER_BURN_TICK / generationSpeed} ticks.
     */
    static final double TOTAL_FE_PER_BURN_TICK = 10d * 0.4d;
    final SimpleContainer fuelContainer;
    int fuelCount;

    protected SolidFuelQuarryEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.fuelContainer = new FuelContainer(1);
        this.fuelContainer.addListener(c -> this.setChanged());
    }

    @Override
    protected PowerMap.SolidFuelQuarry powerMap() {
        return PlatformAccess.config().powerMap().solidFuelQuarry();
    }

    public int getFuelCount() {
        return fuelCount;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("fuelContainer", fuelContainer.createTag(registries));
        tag.putInt("fuelCount", fuelCount);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fuelContainer.fromTag(tag.getList("fuelContainer", Tag.TAG_COMPOUND), registries);
        fuelCount = tag.getInt("fuelCount");
    }

    @Override
    public Stream<MutableComponent> checkerLogs() {
        return Stream.concat(
            super.checkerLogs(),
            Stream.of(detail(ChatFormatting.GREEN, "FuelCount", String.valueOf(fuelCount)))
        );
    }

    /**
     * No enchantments are supported.
     */
    @Override
    @NotNull
    public ItemEnchantments getEnchantments() {
        return ItemEnchantments.EMPTY;
    }

    @Override
    public void setEnchantments(@NotNull ItemEnchantments enchantments) {
        // Do nothing
    }

    @Override
    protected long getMaxReceive() {
        // This machine doesn't accept energy from outside.
        return 0;
    }

    @SuppressWarnings("unused")
    static void tickFuel(Level level, BlockPos pos, BlockState state, SolidFuelQuarryEntity quarry) {
        if (level.isClientSide || !quarry.enabled) return;
        var generationSpeed = quarry.powerMap().generationSpeed();
        if (generationSpeed <= 0) return;
        var tickEnergy = (long) (generationSpeed * ONE_FE);
        if (quarry.fuelCount <= 0) {
            var fuel = quarry.fuelContainer.getItem(0);
            int burnTime = PlatformAccess.getAccess().itemAccess().getBurnTime(fuel);
            if (burnTime > 0) {
                // fuelCount is the number of ticks to generate energy: (total energy of the fuel) / (energy generated in a tick)
                quarry.fuelCount += (int) (burnTime * TOTAL_FE_PER_BURN_TICK / generationSpeed);
                var remaining = PlatformAccess.getAccess().itemAccess().getCraftingRemainingItem(fuel);
                if (!remaining.isEmpty() && fuel.getCount() == 1) {
                    // e.g. Lava bucket -> Bucket
                    quarry.fuelContainer.setItem(0, remaining);
                } else {
                    fuel.shrink(1);
                    quarry.fuelContainer.setChanged();
                    if (!remaining.isEmpty()) {
                        // The slot is still used by the fuel, so the remaining item has no place to go. Store it as the last resort.
                        quarry.storage.addItem(remaining);
                    }
                }
            }
        }
        if (quarry.fuelCount > 0 && quarry.getMaxEnergy() - quarry.getEnergy() >= tickEnergy) {
            quarry.setEnergy(quarry.getEnergy() + tickEnergy, true);
            quarry.fuelCount -= 1;
            quarry.setChanged();
        }
    }

    private static class FuelContainer extends SimpleContainer {
        FuelContainer(int size) {
            super(size);
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return PlatformAccess.getAccess().itemAccess().getBurnTime(stack) > 0;
        }
    }
}
