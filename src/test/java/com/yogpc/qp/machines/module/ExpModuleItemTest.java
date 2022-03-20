package com.yogpc.qp.machines.module;

import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(QuarryPlusTest.class)
class ExpModuleItemTest {
    @Test
    void dummy() {
        assertTrue(expValue().findAny().isPresent());
    }

    @Test
    void instance() {
        var stack = new ItemStack(Holder.ITEM_EXP_MODULE);
        var module = Holder.ITEM_EXP_MODULE.getModule(stack);
        assertTrue(module instanceof ExpModuleItem.ExpItemModule);
    }

    @ParameterizedTest
    @MethodSource("expValue")
    void add(int value) {
        var stack = new ItemStack(Holder.ITEM_EXP_MODULE);
        var module = new ExpModuleItem.ExpItemModule(stack);
        module.addExp(value);
        assertAll(
            () -> assertEquals(value, module.getExp()),
            () -> assertTrue(stack.hasTag()),
            () -> assertEquals(value, Objects.requireNonNull(stack.getTag()).getInt(ExpModuleItem.KEY_AMOUNT))
        );
    }

    @Test
    void add0() {
        var stack = new ItemStack(Holder.ITEM_EXP_MODULE);
        var module = new ExpModuleItem.ExpItemModule(stack);
        module.addExp(0);
        assertFalse(stack.hasTag());
        assertEquals(0, module.getExp());
    }

    @ParameterizedTest
    @MethodSource("expValue")
    void addTwice(int value) {
        var stack = new ItemStack(Holder.ITEM_EXP_MODULE);
        var module = new ExpModuleItem.ExpItemModule(stack);
        module.addExp(value);
        module.addExp(20);
        assertAll(
            () -> assertEquals(value + 20, module.getExp()),
            () -> assertTrue(stack.hasTag()),
            () -> assertEquals(value + 20, Objects.requireNonNull(stack.getTag()).getInt(ExpModuleItem.KEY_AMOUNT))
        );
    }

    static IntStream expValue() {
        var random = new Random(864);
        return IntStream.generate(() -> random.nextInt(256)).map(i -> 1 + i).limit(20);
    }
}
