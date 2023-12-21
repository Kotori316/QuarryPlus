package com.yogpc.qp.machines.advpump;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.EnchantmentLevel;
import com.yogpc.qp.machines.PowerTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

class EnchantmentEfficiency {
    private final List<EnchantmentLevel> enchantments;
    final long energyCapacity;
    final int fluidCapacity;
    final long baseEnergy;
    static final float[] baseEnergyMap = {5f, 4f, 2.5f, 1f};
    final int range;

    EnchantmentEfficiency(List<EnchantmentLevel> enchantments) {
        this.enchantments = enchantments;
        int efficiency = getLevel(enchantments, Enchantments.BLOCK_EFFICIENCY);
        int unbreaking = getLevel(enchantments, Enchantments.UNBREAKING);
        int rangeLevel = Math.max(getLevel(enchantments, Enchantments.BLOCK_FORTUNE), 3 * Math.min(getLevel(enchantments, Enchantments.SILK_TOUCH), 1));
        this.range = (rangeLevel + 1) * 32;
        this.energyCapacity = (int) Math.pow(2, 10 + efficiency) * PowerTile.ONE_FE;
        this.fluidCapacity = 512 * 1000 * (efficiency + 1);
        this.baseEnergy = (long) (baseEnergyMap[Mth.clamp(unbreaking, 0, 3)] * PowerTile.ONE_FE * QuarryPlus.config.adv_pump.advPumpEnergyRemoveFluid);
    }

    static int getLevel(List<EnchantmentLevel> enchantments, Enchantment enchantment) {
        return enchantments.stream()
            .filter(e -> enchantment.equals(e.enchantment()))
            .mapToInt(EnchantmentLevel::level).max().orElse(0);
    }

    CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        for (EnchantmentLevel enchantment : enchantments) {
            tag.putInt(Objects.requireNonNull(enchantment.enchantmentID()).toString(), enchantment.level());
        }
        return tag;
    }

    public List<EnchantmentLevel> getEnchantments() {
        return enchantments;
    }

    public Predicate<BlockPos> rangePredicate(BlockPos center) {
        return p -> {
            int xDiff = center.getX() - p.getX();
            int zDiff = center.getZ() - p.getZ();
            return xDiff * xDiff + zDiff * zDiff < range * range;
        };
    }

    static EnchantmentEfficiency fromNbt(CompoundTag tag) {
        var enchantmentLevels = tag.getAllKeys().stream()
            .flatMap(k ->
                BuiltInRegistries.ENCHANTMENT.getOptional(new ResourceLocation(k))
                    .map(e -> Map.entry(e, tag.getInt(k)))
                    .stream())
            .map(EnchantmentLevel::new)
            .sorted(EnchantmentLevel.QUARRY_ENCHANTMENT_COMPARATOR)
            .toList();
        return new EnchantmentEfficiency(enchantmentLevels);
    }

    static EnchantmentEfficiency fromMap(Map<Enchantment, Integer> enchantmentMap) {
        return new EnchantmentEfficiency(enchantmentMap.entrySet().stream()
            .map(EnchantmentLevel::new)
            .sorted(EnchantmentLevel.QUARRY_ENCHANTMENT_COMPARATOR)
            .toList());
    }

}
