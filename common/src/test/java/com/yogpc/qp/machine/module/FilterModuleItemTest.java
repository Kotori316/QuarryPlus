package com.yogpc.qp.machine.module;

import com.yogpc.qp.BeforeMC;
import com.yogpc.qp.QuarryDataComponents;
import com.yogpc.qp.machine.MachineStorage;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FilterModuleItemTest extends BeforeMC {
    FilterModuleItem filterModuleItem;

    @BeforeEach
    void setUp() {
        filterModuleItem = new FilterModuleItem();
    }

    @Test
    void fromEmpty() {
        var stack = new ItemStack(filterModuleItem);
        var module = filterModuleItem.getModule(stack);
        assertEquals(new ConverterModule.FilterModule(Set.of()), module);
    }

    @Test
    void fromOne() {
        var stack = new ItemStack(filterModuleItem);
        var apple = MachineStorage.ItemKey.of(Items.APPLE.getDefaultInstance());
        stack.set(QuarryDataComponents.ITEM_KEY_LIST_COMPONENT, List.of(apple));
        var module = filterModuleItem.getModule(stack);
        assertEquals(new ConverterModule.FilterModule(Set.of(apple)), module);
    }

    @Test
    void fromTwo() {
        var stack = new ItemStack(filterModuleItem);
        var apple = MachineStorage.ItemKey.of(Items.APPLE.getDefaultInstance());
        var goldenApple = MachineStorage.ItemKey.of(Items.GOLDEN_APPLE.getDefaultInstance());
        stack.set(QuarryDataComponents.ITEM_KEY_LIST_COMPONENT, List.of(apple, goldenApple));
        var module = filterModuleItem.getModule(stack);
        assertEquals(new ConverterModule.FilterModule(Set.of(apple, goldenApple)), module);
    }
}
