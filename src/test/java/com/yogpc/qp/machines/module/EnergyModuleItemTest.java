package com.yogpc.qp.machines.module;

import com.kotori316.testutil.GameTestUtil;
import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestDontPrefix;
import net.minecraftforge.gametest.GameTestHolder;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@GameTestHolder(QuarryPlus.modID)
@GameTestDontPrefix
class EnergyModuleItemTest {
    static final String BATCH = "EnergyModuleItem";

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE, batch = BATCH)
    void noOverflow() {
        var stack = new ItemStack(Holder.ITEM_FUEL_MODULE_NORMAL, 5);
        // The energy of this module is in 0-100 FE/t, so I don't care overflow.
        var module = Holder.ITEM_FUEL_MODULE_NORMAL.getModule(stack);
        assertTrue(0 < module.energy() && module.energy() < Integer.MAX_VALUE);
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE, batch = BATCH)
    void overflow1() {
        var stack = new ItemStack(Holder.ITEM_FUEL_MODULE_NORMAL, Integer.MAX_VALUE);
        var module = Holder.ITEM_FUEL_MODULE_NORMAL.getModule(stack);
        assertEquals(Integer.MAX_VALUE, module.energy());
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE, batch = BATCH)
    void minusTo0() {
        var module = EnergyModuleItem.getEnergyModule(-100, 1);
        assertEquals(0, module.energy());
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE, batch = BATCH)
    void creativeOverflow1() {
        var module = EnergyModuleItem.getEnergyModule(Integer.MAX_VALUE, 1);
        assertEquals(Integer.MAX_VALUE, module.energy());
    }

    @GameTestGenerator
    List<TestFunction> creativeOverflow2() {
        return IntStream.of(2, 5, 10, 16, 22, 64, 128, Integer.MAX_VALUE).mapToObj(
            i -> GameTestUtil.create(QuarryPlus.modID, BATCH, "creativeOverflow2_%d".formatted(i), () -> creativeOverflow2(i))
        ).toList();
    }

    void creativeOverflow2(int stackSize) {
        var module = EnergyModuleItem.getEnergyModule(Integer.MAX_VALUE, stackSize);
        assertEquals(Integer.MAX_VALUE, module.energy());
    }

    @GameTestGenerator
    List<TestFunction> minusStackSize() {
        return IntStream.of(-1, -4, -5, -10, -16, -43, -64, Integer.MIN_VALUE / 5, Integer.MIN_VALUE).mapToObj(
            i -> GameTestUtil.create(QuarryPlus.modID, BATCH, "minusStackSize_%d".formatted(i), () -> minusStackSize(i))
        ).toList();
    }

    void minusStackSize(int stackSize) {
        var stack = new ItemStack(Holder.ITEM_FUEL_MODULE_NORMAL, stackSize);
        var module = Holder.ITEM_FUEL_MODULE_NORMAL.getModule(stack);
        assertEquals(0, module.energy());
    }
}
