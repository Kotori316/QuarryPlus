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
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public abstract class SolidFuelQuarryEntity extends QuarryEntity {
    /**
     * Generating speed. Energy in FE generated in a tick while the machine has fuel.
     * TODO: make it configurable
     */
    static final double GENERATE_FE_PER_TICK = 2d;
    /**
     * Fuel efficiency. Total energy in FE which a fuel has per 1 tick of its burn time. (e.g. Coal: 1600 ticks -> 6400 FE)
     * The running time of a fuel is {@code burnTime * FUEL_FE_PER_BURN_TIME / GENERATE_FE_PER_TICK} ticks.
     * TODO: make it configurable
     */
    static final double FUEL_FE_PER_BURN_TIME = 4d;
    final SimpleContainer fuelContainer;
    int fuelCount;

    protected SolidFuelQuarryEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.fuelContainer = new FuelContainer(1);
        this.fuelContainer.addListener(c -> this.setChanged());
    }

    @Override
    protected PowerMap.Quarry powerMap() {
        return PlatformAccess.config().powerMap().solidFuelQuarry();
    }

    /**
     * @return the burn time in tick of the stack. 0 if the stack is not fuel.
     */
    protected int getBurnTime(ItemStack stack) {
        // TODO: Mock implementation. Use platform dependent value (e.g. IItemExtension#getBurnTime in NeoForge)
        if (stack.isEmpty()) return 0;
        return AbstractFurnaceBlockEntity.getFuel().getOrDefault(stack.getItem(), 0);
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
        var tickEnergy = (long) (GENERATE_FE_PER_TICK * ONE_FE);
        if (quarry.fuelCount <= 0) {
            var fuel = quarry.fuelContainer.getItem(0);
            int burnTime = quarry.getBurnTime(fuel);
            if (burnTime > 0) {
                quarry.fuelCount += (int) (burnTime * FUEL_FE_PER_BURN_TIME / GENERATE_FE_PER_TICK);
                var remaining = fuel.getItem().getCraftingRemainingItem();
                if (remaining != null && fuel.getCount() == 1) {
                    // e.g. Lava bucket -> Bucket
                    quarry.fuelContainer.setItem(0, new ItemStack(remaining));
                } else {
                    fuel.shrink(1);
                    quarry.fuelContainer.setChanged();
                }
            }
        }
        if (quarry.fuelCount > 0 && quarry.getMaxEnergy() - quarry.getEnergy() >= tickEnergy) {
            quarry.setEnergy(quarry.getEnergy() + tickEnergy, true);
            quarry.fuelCount -= 1;
            quarry.setChanged();
        }
    }

    private final class FuelContainer extends SimpleContainer {
        FuelContainer(int size) {
            super(size);
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return getBurnTime(stack) > 0;
        }
    }
}
