package com.yogpc.qp.machines.module;

import com.yogpc.qp.Holder;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnergyModuleItemTest {
    @Test
    void noOverflow() {
        var stack = new ItemStack(Holder.ITEM_FUEL_MODULE_NORMAL, 5);
        // The energy of this module is in 0-100 FE/t, so I don't care overflow.
        var module = Holder.ITEM_FUEL_MODULE_NORMAL.getModule(stack);
        assertTrue(0 < module.energy() && module.energy() < Integer.MAX_VALUE);
    }

    @Test
    void overflow1() {
        var stack = new ItemStack(Holder.ITEM_FUEL_MODULE_NORMAL, Integer.MAX_VALUE);
        var module = Holder.ITEM_FUEL_MODULE_NORMAL.getModule(stack);
        assertEquals(Integer.MAX_VALUE, module.energy());
    }

    @Test
    void minusTo0() {
        var minusItem = new EnergyModuleItem(-100, "minus_module");
        var module = minusItem.getModule(new ItemStack(minusItem));
        assertEquals(0, module.energy());
    }

    @Test
    void creativeOverflow1() {
        var creativeModuleItem = new EnergyModuleItem(Integer.MAX_VALUE, "creative_fuel_module");
        var module = creativeModuleItem.getModule(new ItemStack(creativeModuleItem));
        assertEquals(Integer.MAX_VALUE, module.energy());
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 5, 10, 16, 22, 64})
    void creativeOverflow2(int stackSize) {
        var creativeModuleItem = new EnergyModuleItem(Integer.MAX_VALUE, "creative_fuel_module");
        var module = creativeModuleItem.getModule(new ItemStack(creativeModuleItem, stackSize));
        assertEquals(Integer.MAX_VALUE, module.energy());
    }
}
