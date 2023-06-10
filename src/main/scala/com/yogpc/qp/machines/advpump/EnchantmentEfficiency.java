package com.yogpc.qp.machines.advpump;

import com.yogpc.qp.machines.EnchantmentLevel;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

class EnchantmentEfficiency {
    private final List<EnchantmentLevel> enchantments;
    final long energyCapacity;
    final int fluidCapacity;
    final long baseEnergy;
    static final List<Integer> baseEnergyMap = List.of(100, 80, 50, 20);
    final int range;

    EnchantmentEfficiency(List<EnchantmentLevel> enchantments) {
        this.enchantments = enchantments;
        int efficiency = getLevel(enchantments, Enchantments.BLOCK_EFFICIENCY);
        int unbreaking = getLevel(enchantments, Enchantments.UNBREAKING);
        int rangeLevel = Math.max(getLevel(enchantments, Enchantments.BLOCK_FORTUNE), 3 * Math.min(getLevel(enchantments, Enchantments.SILK_TOUCH), 1));
        this.range = (rangeLevel + 1) * 32;
        this.energyCapacity = (int) Math.pow(2, 10 + efficiency) * PowerTile.ONE_FE;
        this.fluidCapacity = 512 * 1000 * (efficiency + 1);
        this.baseEnergy = baseEnergyMap.get(Mth.clamp(unbreaking, 0, 3)) * PowerTile.ONE_FE;
    }

    static int getLevel(List<EnchantmentLevel> enchantments, Enchantment enchantment) {
        return enchantments.stream()
                .filter(e -> enchantment.equals(e.enchantment()))
                .mapToInt(EnchantmentLevel::level).max().orElse(0);
    }

    CompoundTag toNbt() {
        var tag = new CompoundTag();
        for (EnchantmentLevel enchantment : enchantments) {
            tag.putInt(Objects.requireNonNull(enchantment.enchantmentID()).toString(), enchantment.level());
        }
        return tag;
    }

    List<EnchantmentLevel> getEnchantments() {
        return enchantments;
    }

    Predicate<BlockPos> rangePredicate(BlockPos center) {
        return p -> {
            var xDiff = center.getX() - p.getX();
            var zDiff = center.getZ() - p.getZ();
            return xDiff * xDiff + zDiff * zDiff < range * range;
        };
    }

    int areaSize() {
        return (int) (Math.PI * range * range);
    }

    static EnchantmentEfficiency fromNbt(CompoundTag tag) {
        var enchantmentLevels = tag.getAllKeys().stream()
                .mapMulti(MapMulti.getEntry(ForgeRegistries.ENCHANTMENTS, tag::getInt))
                .map(EnchantmentLevel::new)
                .sorted(EnchantmentLevel.QUARRY_ENCHANTMENT_COMPARATOR)
                .toList();
        return new EnchantmentEfficiency(enchantmentLevels);
    }

    static EnchantmentEfficiency fromMap(Map<Enchantment, Integer> enchantmentMap) {
        return new EnchantmentEfficiency(EnchantmentLevel.fromMap(enchantmentMap));
    }

}
