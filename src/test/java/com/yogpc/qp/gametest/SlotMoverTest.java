package com.yogpc.qp.gametest;

import java.util.List;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static com.yogpc.qp.machines.mover.MoverAccessor.createMoverSlot;
import static com.yogpc.qp.machines.mover.MoverAccessor.diamondTools;
import static com.yogpc.qp.machines.mover.MoverAccessor.ironTools;
import static com.yogpc.qp.machines.mover.MoverAccessor.netheriteTools;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@GameTestHolder(QuarryPlus.modID)
@PrefixGameTestTemplate(value = false)
public final class SlotMoverTest {
    @GameTest(template = TestUtil.EMPTY_STRUCTURE)
    public void dummy(GameTestHelper helper) {
        assertTrue(diamondTools().findAny().isPresent());
        assertTrue(netheriteTools().findAny().isPresent());
        assertTrue(ironTools().findAny().isPresent());
        helper.succeed();
    }

    @GameTestGenerator
    public List<TestFunction> canPlaceDiamond() {
        return diamondTools().map(d ->
            TestUtil.create("canPlaceDiamond:" + d, g -> canPlaceDiamond(d, g))
        ).toList();
    }

    @GameTestGenerator
    public List<TestFunction> canPlaceNetherite() {
        return netheriteTools().map(d ->
            TestUtil.create("canPlaceNetherite:" + d, g -> canPlaceNetherite(d, g))
        ).toList();
    }

    @GameTestGenerator
    public List<TestFunction> canPlaceIron() {
        return ironTools().map(d ->
            TestUtil.create("canPlaceIron:" + d, g -> canPlaceIron(d, g))
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

}
