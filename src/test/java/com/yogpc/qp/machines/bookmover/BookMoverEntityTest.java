package com.yogpc.qp.machines.bookmover;

import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookMoverEntityTest extends QuarryPlusTest {
    @Test
    void remove1() {
        var stack = new ItemStack(Items.DIAMOND_PICKAXE);
        stack.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
        BookMoverEntity.removeEnchantment(Enchantments.BLOCK_EFFICIENCY, stack);
        assertEquals(0, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, stack));
        assertTrue(stack.getEnchantmentTags().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    void removeAnyLevel(int level) {
        var stack = new ItemStack(Items.DIAMOND_PICKAXE);
        stack.enchant(Enchantments.BLOCK_EFFICIENCY, level);
        BookMoverEntity.removeEnchantment(Enchantments.BLOCK_EFFICIENCY, stack);
        assertEquals(0, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, stack));
        assertTrue(stack.getEnchantmentTags().isEmpty());
    }

    @Test
    void remove2() {
        var stack = new ItemStack(Items.DIAMOND_PICKAXE);
        stack.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
        stack.enchant(Enchantments.UNBREAKING, 3);
        BookMoverEntity.removeEnchantment(Enchantments.BLOCK_EFFICIENCY, stack);
        assertEquals(0, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, stack));
        assertEquals(3, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, stack));
        assertFalse(stack.getEnchantmentTags().isEmpty());
    }

    @Test
    void removeBook1() {
        var stack = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(Enchantments.BLOCK_EFFICIENCY, 5));

        BookMoverEntity.removeEnchantment(Enchantments.BLOCK_EFFICIENCY, stack);
        assertTrue(EnchantmentHelper.getEnchantments(stack).isEmpty());
    }

    @Test
    void removeBook2() {
        var stack = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantedBookItem.addEnchantment(stack, new EnchantmentInstance(Enchantments.BLOCK_EFFICIENCY, 5));
        EnchantedBookItem.addEnchantment(stack, new EnchantmentInstance(Enchantments.UNBREAKING, 3));
        var pre = EnchantmentHelper.getEnchantments(stack);
        assertEquals(5, pre.get(Enchantments.BLOCK_EFFICIENCY));
        assertEquals(3, pre.get(Enchantments.UNBREAKING));
        BookMoverEntity.removeEnchantment(Enchantments.BLOCK_EFFICIENCY, stack);

        var post = EnchantmentHelper.getEnchantments(stack);
        assertFalse(post.containsKey(Enchantments.BLOCK_EFFICIENCY));
        assertEquals(3, post.get(Enchantments.UNBREAKING));
        assertFalse(post.isEmpty());
    }
}
