package com.yogpc.qp.machines.quarry;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.EnchantmentLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.capabilities.Capabilities;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class SFQuarryEntity extends TileQuarry implements MenuProvider {
    final SimpleContainer fuelContainer;
    int fuelCount;

    public SFQuarryEntity(BlockPos pos, BlockState state) {
        super(Holder.SOLID_FUEL_QUARRY_TYPE, pos, state);
        this.fuelContainer = new FuelContainer(1);
        this.fuelContainer.addListener(c -> this.setChanged());
    }

    @Override
    public void saveNbtData(CompoundTag nbt) {
        nbt.put("fuelContainer", fuelContainer.createTag());
        nbt.putInt("fuelCount", fuelCount);
        super.saveNbtData(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        fuelContainer.fromTag(nbt.getList("fuelContainer", Tag.TAG_COMPOUND));
        fuelCount = nbt.getInt("fuelCount");
    }

    @Override
    public List<EnchantmentLevel> getEnchantments() {
        return Collections.emptyList();
    }

    @Override
    @Deprecated
    public void setEnchantments(Map<Enchantment, Integer> enchantments) {
    }

    @Override
    @Deprecated
    public void setEnchantments(List<EnchantmentLevel> enchantments) {
    }

    @Override
    public void setTileDataFromItem(@Nullable CompoundTag tileData) {
        digMinY = level == null ? 0 : level.getMinBuildHeight();
    }

    @Override
    public boolean canReceive() {
        return false;
    }

    @SuppressWarnings("unused")
    public static void tickFuel(Level world, BlockPos pos, BlockState state, SFQuarryEntity quarry) {
        if ((world == null || world.isClientSide) || !quarry.enabled) return;
        double energyInFE = QuarryPlus.config.common.sfqEnergy.get();
        var tickEnergy = (long) (energyInFE * ONE_FE);
        if (quarry.fuelCount <= 0) {
            var fuel = quarry.fuelContainer.getItem(0);
            if (FurnaceBlockEntity.isFuel(fuel)) {
                quarry.fuelCount += (int) (CommonHooks.getBurnTime(fuel, null) * 4 / energyInFE);
                if (fuel.hasCraftingRemainingItem()) {
                    quarry.fuelContainer.setItem(0, fuel.getCraftingRemainingItem());
                } else {
                    fuel.shrink(1);
                    quarry.fuelContainer.setChanged();
                }
            }
        }
        if (quarry.fuelCount > 0 && quarry.addEnergy(tickEnergy, true) == tickEnergy) {
            quarry.addEnergy(tickEnergy, false);
            quarry.fuelCount -= 1;
        }
    }

    @Override
    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == Capabilities.ITEM_HANDLER) {
            return LazyOptional.of(() -> new InvWrapper(this.fuelContainer)).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new SFQuarryMenu(id, player, this.getBlockPos());
    }

    private static class FuelContainer extends SimpleContainer {
        public FuelContainer(int slots) {
            super(slots);
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return FurnaceBlockEntity.isFuel(stack);
        }
    }
}
