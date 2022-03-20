package com.yogpc.qp.machines.mover;

import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("SlotMoverTest: TierSortingRegistry cause initial crash.")
@ExtendWith(QuarryPlusTest.class)
class SlotMoverTest {
    @Test
    void dummy() {
        assertTrue(diamondTools().findAny().isPresent());
        assertTrue(netheriteTools().findAny().isPresent());
        assertTrue(ironTools().findAny().isPresent());
    }

    @ParameterizedTest
    @MethodSource("diamondTools")
    void canPlaceDiamond(Item diamondTool) {
        var slot = new SlotMover(new SimpleContainer(1), 0, 0, 0);
        var stack = new ItemStack(diamondTool);
        stack.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
        assertTrue(slot.mayPlace(stack));
    }

    @ParameterizedTest
    @MethodSource("netheriteTools")
    void canPlaceNetherite(Item netheriteTool) {
        var slot = new SlotMover(new SimpleContainer(1), 0, 0, 0);
        var stack = new ItemStack(netheriteTool);
        stack.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
        assertTrue(slot.mayPlace(stack));
    }

    @ParameterizedTest
    @MethodSource("ironTools")
    void canPlaceIron(Item ironTool) {
        var slot = new SlotMover(new SimpleContainer(1), 0, 0, 0);
        var stack = new ItemStack(ironTool);
        stack.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
        assertFalse(slot.mayPlace(stack));
    }

    static Stream<Item> diamondTools() {
        return Stream.of(Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_SWORD);
    }

    static Stream<Item> netheriteTools() {
        return Stream.of(Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_SHOVEL, Items.NETHERITE_HOE, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_SWORD);
    }

    static Stream<Item> ironTools() {
        return Stream.of(Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_SHOVEL, Items.IRON_HOE, Items.IRON_CHESTPLATE, Items.IRON_SWORD);
    }
}
