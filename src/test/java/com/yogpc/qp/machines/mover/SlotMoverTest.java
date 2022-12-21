package com.yogpc.qp.machines.mover;

import java.util.List;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.kotori316.testutil.GameTestUtil;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@GameTestHolder(QuarryPlus.modID)
@PrefixGameTestTemplate(value = false)
public final class SlotMoverTest {
    private static final String BATCH = "SlotMover";

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE)
    public void dummy(GameTestHelper helper) {
        assertTrue(diamondTools().findAny().isPresent());
        assertTrue(netheriteTools().findAny().isPresent());
        assertTrue(ironTools().findAny().isPresent());
        helper.succeed();
    }

    @GameTestGenerator
    public List<TestFunction> canPlaceDiamond() {
        return diamondTools().map(d ->
            GameTestUtil.create(QuarryPlus.modID, BATCH, "canPlaceDiamond_" + d, g -> canPlaceDiamond(d, g))
        ).toList();
    }

    @GameTestGenerator
    public List<TestFunction> canPlaceNetherite() {
        return netheriteTools().map(d ->
            GameTestUtil.create(QuarryPlus.modID, BATCH, "canPlaceNetherite_" + d, g -> canPlaceNetherite(d, g))
        ).toList();
    }

    @GameTestGenerator
    public List<TestFunction> canPlaceIron() {
        return ironTools().map(d ->
            GameTestUtil.create(QuarryPlus.modID, BATCH, "canPlaceIron_" + d, g -> canPlaceIron(d, g))
        ).toList();
    }

    void canPlaceDiamond(Item diamondTool, GameTestHelper helper) {
        var slot = createMoverSlot(new SimpleContainer(1), 0, 0, 0);
        var stack = new ItemStack(diamondTool);
        stack.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
        assertTrue(slot.mayPlace(stack), "Stack %s should be placed.".formatted(stack));
        helper.succeed();
    }

    void canPlaceNetherite(Item netheriteTool, GameTestHelper helper) {
        var slot = createMoverSlot(new SimpleContainer(1), 0, 0, 0);
        var stack = new ItemStack(netheriteTool);
        stack.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
        assertTrue(slot.mayPlace(stack), "Stack %s should be placed.".formatted(stack));
        helper.succeed();
    }

    void canPlaceIron(Item ironTool, GameTestHelper helper) {
        var slot = createMoverSlot(new SimpleContainer(1), 0, 0, 0);
        var stack = new ItemStack(ironTool);
        stack.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
        assertFalse(slot.mayPlace(stack), "Stack %s shouldn't be placed.".formatted(stack));
        helper.succeed();
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE)
    public void canPlaceBowWithEnchantment(GameTestHelper helper) {
        var slot = createMoverSlot(new SimpleContainer(1), 0, 0, 0);
        var stack = new ItemStack(Items.BOW);
        stack.enchant(Enchantments.UNBREAKING, 3);
        assertTrue(slot.mayPlace(stack), "Bow(%s) with enchantments should be placed in slot.".formatted(stack));
        helper.succeed();
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE)
    public void cantPlaceBowWithoutEnchantment(GameTestHelper helper) {
        var slot = createMoverSlot(new SimpleContainer(1), 0, 0, 0);
        var stack = new ItemStack(Items.BOW);
        assertFalse(slot.mayPlace(stack), "Bow(%s) without enchantments should not be placed in slot.".formatted(stack));
        helper.succeed();
    }

    public static Slot createMoverSlot(Container container, int index, int x, int y) {
        return new SlotMover(container, index, x, y);
    }

    public static Stream<Item> diamondTools() {
        return Stream.of(Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_SWORD);
    }

    public static Stream<Item> netheriteTools() {
        return Stream.of(Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_SHOVEL, Items.NETHERITE_HOE, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_SWORD);
    }

    public static Stream<Item> ironTools() {
        return Stream.of(Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_SHOVEL, Items.IRON_HOE, Items.IRON_CHESTPLATE, Items.IRON_SWORD);
    }
}
