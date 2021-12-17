package com.yogpc.qp.machines.quarry;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.EnchantmentLevel;
import net.minecraft.core.BlockPos;
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
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.Nullable;

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
    public CompoundTag toClientTag(CompoundTag tag) {
        tag.putInt("fuelCount", fuelCount);
        return super.toClientTag(tag);
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        fuelCount = tag.getInt("fuelCount");
        super.fromClientTag(tag);
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

    public static void tickFuel(Level world, BlockPos pos, BlockState state, SFQuarryEntity quarry) {
        if ((world == null || world.isClientSide) || !quarry.enabled) return;
        var tickEnergy = 40 * ONE_FE;
        if (quarry.fuelCount <= 0) {
            var fuel = quarry.fuelContainer.getItem(0);
            if (FurnaceBlockEntity.isFuel(fuel)) {
                quarry.fuelCount += ForgeHooks.getBurnTime(fuel, null) / 5;
                if (fuel.hasContainerItem()) {
                    quarry.fuelContainer.setItem(0, fuel.getContainerItem());
                } else {
                    fuel.shrink(1);
                }
            }
        }
        if (quarry.fuelCount > 0 && quarry.addEnergy(tickEnergy, true) == tickEnergy) {
            quarry.addEnergy(tickEnergy, false);
            quarry.fuelCount -= 1;
        }
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