package com.yogpc.qp.machines;

import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(QuarryPlusTest.class)
class ItemKeyTest {
    @Test
    @DisplayName("Key from item instance and from ItemStack should be equal.")
    void fromStack() {
        var key1 = new ItemKey(Items.APPLE, null);
        var key2 = new ItemKey(new ItemStack(Items.APPLE));
        assertEquals(key1, key2);
    }

    @Test
    void equalTag() {
        var tag1 = new CompoundTag();
        tag1.putInt("test", 4);
        var key1 = new ItemKey(Items.GOLDEN_APPLE, tag1);
        var tag2 = new CompoundTag();
        tag2.putInt("test", 4);
        var stack = new ItemStack(Items.GOLDEN_APPLE);
        stack.setTag(tag2);
        var key2 = new ItemKey(stack);
        assertEquals(key1, key2);
    }

    @Test
    void notEqualItem() {
        var k1 = new ItemKey(Items.APPLE, null);
        var k2 = new ItemKey(Items.GOLDEN_APPLE, null);
        assertNotEquals(k1, k2);
    }

    @Test
    void notEqualTag() {
        var k1 = new ItemKey(Items.APPLE, null);
        var k2 = new ItemKey(Items.APPLE, new CompoundTag());
        assertNotEquals(k1, k2);
    }

    @Test
    void toStackWithTag() {
        var tag1 = new CompoundTag();
        tag1.putInt("test", 4);
        var key1 = new ItemKey(Items.GOLDEN_APPLE, tag1);
        var tag2 = new CompoundTag();
        tag2.putInt("test", 4);
        var stack = new ItemStack(Items.GOLDEN_APPLE);
        stack.setTag(tag2);

        var fromKey = key1.toStack(1);
        assertTrue(ItemStack.isSameItemSameTags(stack, fromKey), "%s should be equal to %s".formatted(fromKey, stack));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 16, 64})
    void ignoreStackSize(int amount) {
        var key = new ItemKey(Items.GOLDEN_APPLE, null);
        var stack = new ItemStack(Items.GOLDEN_APPLE, amount);
        assertEquals(key, new ItemKey(stack));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 16, 64})
    void toStackWithoutTag(int amount) {
        var key = new ItemKey(Items.GOLDEN_APPLE, null);
        var expected = new ItemStack(Items.GOLDEN_APPLE, amount);
        var fromKey = key.toStack(amount);
        assertTrue(ItemStack.isSameItemSameTags(expected, fromKey), "%s should be equal to %s".formatted(fromKey, expected));
    }

    @Test
    @DisplayName("Key from nbt and item should be same.")
    void fromTag() {
        var key = new ItemKey(Items.ENCHANTED_GOLDEN_APPLE, null);
        var itemTag = key.createNbt(1);
        assertFalse(itemTag.isEmpty());
        var fromTag = ItemKey.fromNbt(itemTag);
        assertEquals(key, fromTag);
    }

    @Test
    @DisplayName("Key from empty stacks.")
    void fromEmpty() {
        var key = new ItemKey(Items.AIR, null);
        assertEquals(key, ItemKey.EMPTY_KEY);
        assertEquals(key, new ItemKey(new ItemStack(Items.APPLE, 0)));
    }

    @Test
    @DisplayName("Key from tag with nbt and item should be same.")
    void fromTagWithNbt() {
        var tag = new CompoundTag();
        tag.putLong("long", 635);
        var key = new ItemKey(Items.APPLE, tag);
        var itemTag = key.createNbt(5);
        var fromTag = ItemKey.fromNbt(itemTag);
        assertEquals(key, fromTag);
    }

    @Test
    void getId() {
        var key = new ItemKey(Items.ENCHANTED_GOLDEN_APPLE, null);
        var location = new ResourceLocation("minecraft", "enchanted_golden_apple");
        assertEquals(location, key.getId());
    }
}
