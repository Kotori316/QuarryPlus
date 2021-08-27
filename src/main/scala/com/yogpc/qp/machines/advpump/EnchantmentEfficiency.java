package com.yogpc.qp.machines.advpump;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import com.yogpc.qp.machines.EnchantmentLevel;
import com.yogpc.qp.machines.PowerTile;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;

class EnchantmentEfficiency {
    private final List<EnchantmentLevel> enchantments;
    final long energyCapacity;
    final int fluidCapacity;
    final long baseEnergy;
    static final List<Integer> baseEnergyMap = List.of(100, 80, 50, 20);
    final int fortuneLevel;

    EnchantmentEfficiency(List<EnchantmentLevel> enchantments) {
        this.enchantments = enchantments;
        int efficiency = enchantments.stream()
            .filter(e -> Enchantments.EFFICIENCY.equals(e.enchantment()))
            .mapToInt(EnchantmentLevel::level).max().orElse(0);
        int unbreaking = enchantments.stream()
            .filter(e -> Enchantments.UNBREAKING.equals(e.enchantment()))
            .mapToInt(EnchantmentLevel::level).max().orElse(0);
        this.fortuneLevel = enchantments.stream()
            .filter(e -> Enchantments.FORTUNE.equals(e.enchantment()))
            .mapToInt(EnchantmentLevel::level).max().orElse(0);
        this.energyCapacity = (int) Math.pow(2, 10 + efficiency) * PowerTile.ONE_FE;
        this.fluidCapacity = 512 * 1000 * (efficiency + 1);
        this.baseEnergy = baseEnergyMap.get(MathHelper.clamp(unbreaking, 0, 3)) * PowerTile.ONE_FE;
    }

    NbtCompound toNbt() {
        var tag = new NbtCompound();
        for (EnchantmentLevel enchantment : enchantments) {
            tag.putInt(Objects.requireNonNull(enchantment.enchantmentID()).toString(), enchantment.level());
        }
        return tag;
    }

    public List<EnchantmentLevel> getEnchantments() {
        return enchantments;
    }

    public Predicate<BlockPos> rangePredicate(BlockPos center) {
        var range = (fortuneLevel + 1) * 32;
        return p -> {
            var xDiff = center.getX() - p.getX();
            var zDiff = center.getZ() - p.getZ();
            return xDiff * xDiff + zDiff * zDiff < range * range;
        };
    }

    static EnchantmentEfficiency fromNbt(NbtCompound tag) {
        var enchantmentLevels = tag.getKeys().stream()
            .flatMap(k ->
                Registry.ENCHANTMENT.getOrEmpty(new Identifier(k))
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
