package com.yogpc.qp.gametest;

import com.yogpc.qp.QuarryDataComponents;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.module.ConverterModule;
import com.yogpc.qp.machine.module.FilterModuleItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class FilterModuleItemTest {
    static FilterModuleItem item() {
        return (FilterModuleItem) BuiltInRegistries.ITEM.getValue(QuarryPlus.itemKey(FilterModuleItem.NAME));
    }

    public static void fromEmpty(GameTestHelper helper) {
        var filterModuleItem = item();
        var stack = new ItemStack(filterModuleItem);
        var module = filterModuleItem.getModule(stack);
        assertEquals(new ConverterModule.FilterModule(Set.of()), module);
        helper.succeed();
    }

    public static void fromOne(GameTestHelper helper) {
        var filterModuleItem = item();
        var stack = new ItemStack(filterModuleItem);
        var apple = MachineStorage.ItemKey.of(Items.APPLE.getDefaultInstance());
        stack.set(QuarryDataComponents.ITEM_KEY_LIST_COMPONENT, List.of(apple));
        var module = filterModuleItem.getModule(stack);
        assertEquals(new ConverterModule.FilterModule(Set.of(apple)), module);
        helper.succeed();
    }

    public static void fromTwo(GameTestHelper helper) {
        var filterModuleItem = item();
        var stack = new ItemStack(filterModuleItem);
        var apple = MachineStorage.ItemKey.of(Items.APPLE.getDefaultInstance());
        var goldenApple = MachineStorage.ItemKey.of(Items.GOLDEN_APPLE.getDefaultInstance());
        stack.set(QuarryDataComponents.ITEM_KEY_LIST_COMPONENT, List.of(apple, goldenApple));
        var module = filterModuleItem.getModule(stack);
        assertEquals(new ConverterModule.FilterModule(Set.of(apple, goldenApple)), module);
        helper.succeed();
    }

    public static void rowNotSet(GameTestHelper helper) {
        var stack = new ItemStack(item());
        var row = FilterModuleItem.getRowsFromStack(stack);
        assertEquals(2, row);
        helper.succeed();
    }

    public static void rowSet(GameTestHelper helper) {
        for (var expected : new int[]{1, 2, 4, 6}) {
            var stack = new ItemStack(item());
            stack.set(QuarryDataComponents.FILTER_MODULE_ROWS_COMPONENT, expected);
            var row = FilterModuleItem.getRowsFromStack(stack);
            assertEquals(expected, row);
        }
        helper.succeed();
    }
}
