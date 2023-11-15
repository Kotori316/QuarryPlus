package com.yogpc.qp.machines;

import com.yogpc.qp.utils.ManualOrder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public record EnchantmentLevel(Enchantment enchantment, int level) {
    public EnchantmentLevel(Map.Entry<Enchantment, Integer> entry) {
        this(entry.getKey(), entry.getValue());
    }

    public EnchantmentLevel(ResourceLocation enchantmentID, int level) {
        this(ForgeRegistries.ENCHANTMENTS.getValue(enchantmentID), level);
    }

    @Nullable
    public ResourceLocation enchantmentID() {
        return ForgeRegistries.ENCHANTMENTS.getKey(enchantment());
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
    }

    public enum NoEnchantments implements HasEnchantments {
        INSTANCE;

        @Override
        public List<EnchantmentLevel> getEnchantments() {
            return Collections.emptyList();
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
            if (ForgeRegistries.ENCHANTMENTS.containsKey(name)) {
                list.add(new EnchantmentLevel(name, level));
            }
        }
        return list;
    }

    public static List<EnchantmentLevel> fromMap(Map<Enchantment, Integer> enchantmentMap) {
        return enchantmentMap.entrySet().stream()
            .map(EnchantmentLevel::new)
            .sorted(QUARRY_ENCHANTMENT_COMPARATOR)
            .toList();
    }

    public static final Comparator<EnchantmentLevel> COMPARATOR =
        Comparator.comparing(EnchantmentLevel::enchantmentID)
            .thenComparingInt(EnchantmentLevel::level);
    public static final Comparator<EnchantmentLevel> QUARRY_ENCHANTMENT_COMPARATOR =
        Comparator.comparing(EnchantmentLevel::enchantment,
            ManualOrder.builder(Comparator.comparing(ForgeRegistries.ENCHANTMENTS::getKey))
                .add(Enchantments.BLOCK_EFFICIENCY)
                .add(Enchantments.UNBREAKING)
                .add(Enchantments.BLOCK_FORTUNE)
                .add(Enchantments.SILK_TOUCH)
                .build()
        ).thenComparingInt(EnchantmentLevel::level);
}
