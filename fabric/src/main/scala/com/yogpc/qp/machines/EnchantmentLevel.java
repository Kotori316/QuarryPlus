package com.yogpc.qp.machines;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.utils.ManualOrder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public record EnchantmentLevel(Enchantment enchantment, int level) {
    public EnchantmentLevel(Map.Entry<Enchantment, Integer> entry) {
        this(entry.getKey(), entry.getValue());
    }

    public EnchantmentLevel(ResourceLocation enchantmentID, int level) {
        this(BuiltInRegistries.ENCHANTMENT.get(enchantmentID), level);
    }

    @Nullable
    public ResourceLocation enchantmentID() {
        return BuiltInRegistries.ENCHANTMENT.getKey(enchantment());
    }

    public interface HasEnchantments {
        List<EnchantmentLevel> getEnchantments();

        default int getLevel(Enchantment enchantment) {
            return getEnchantments().stream().filter(e -> e.enchantment() == enchantment)
                .mapToInt(EnchantmentLevel::level).findFirst().orElse(0);
        }

        default int efficiencyLevel() {
            return getLevel(Enchantments.BLOCK_EFFICIENCY);
        }

        default int unbreakingLevel() {
            return getLevel(Enchantments.UNBREAKING);
        }

        default int fortuneLevel() {
            return getLevel(Enchantments.BLOCK_FORTUNE);
        }

        default int silktouchLevel() {
            return getLevel(Enchantments.SILK_TOUCH);
        }

        default ItemStack getPickaxe() {
            var stack = new ItemStack(Items.NETHERITE_PICKAXE);
            getEnchantments().forEach(e -> stack.enchant(e.enchantment(), e.level()));
            return stack;
        }

        default EnergyConfigAccessor getAccessor() {
            QuarryPlus.LOGGER.warn("Default implementation of HasEnchantments#getAccessor is called. {}", getClass());
            return EnergyConfigAccessor.ONES;
        }
    }

    public static List<EnchantmentLevel> fromItem(ItemStack stack) {
        var enchantmentList = stack.getEnchantmentTags();
        if (enchantmentList.isEmpty()) return Collections.emptyList();
        List<EnchantmentLevel> list = new ArrayList<>(enchantmentList.size());
        for (int i = 0; i < enchantmentList.size(); i++) {
            var tag = enchantmentList.getCompound(i);
            var name = EnchantmentHelper.getEnchantmentId(tag);
            var level = EnchantmentHelper.getEnchantmentLevel(tag);
            if (name != null && BuiltInRegistries.ENCHANTMENT.containsKey(name)) {
                list.add(new EnchantmentLevel(name, level));
            }
        }
        return list;
    }

    public static final Comparator<EnchantmentLevel> COMPARATOR =
        Comparator.comparingInt((EnchantmentLevel e) -> BuiltInRegistries.ENCHANTMENT.getId(e.enchantment()))
            .thenComparingInt(EnchantmentLevel::level);
    public static final Comparator<EnchantmentLevel> QUARRY_ENCHANTMENT_COMPARATOR =
        Comparator.comparing(EnchantmentLevel::enchantment,
            ManualOrder.builder(Comparator.comparingInt(BuiltInRegistries.ENCHANTMENT::getId))
                .add(Enchantments.BLOCK_EFFICIENCY)
                .add(Enchantments.UNBREAKING)
                .add(Enchantments.BLOCK_FORTUNE)
                .add(Enchantments.SILK_TOUCH)
                .build()
        ).thenComparingInt(EnchantmentLevel::level);

    @Override
    public String toString() {
        return "EnchantmentLevel[" +
            "enchantment=" + enchantment.getClass().getSimpleName().replace("Enchantment", "") +
            ", level=" + level +
            ']';
    }
}
