package com.yogpc.qp.machines.module;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.kotori316.testutil.GameTestUtil;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@GameTestHolder(QuarryPlus.modID)
@PrefixGameTestTemplate(value = false)
class ExpModuleItemTest {
    static final String BATCH = "ExpModuleItem";

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE, batch = BATCH)
    void dummy() {
        assertTrue(expValue().findAny().isPresent());
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE, batch = BATCH)
    void instance() {
        var stack = new ItemStack(Holder.ITEM_EXP_MODULE);
        var module = Holder.ITEM_EXP_MODULE.getModule(stack);
        assertTrue(module instanceof ExpModuleItem.ExpItemModule);
    }

    @GameTestGenerator
    List<TestFunction> add() {
        return expValue().mapToObj(
            i -> GameTestUtil.create(QuarryPlus.modID, BATCH, "add_%d".formatted(i), () -> add(i))
        ).toList();
    }

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

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE, batch = BATCH)
    void add0() {
        var stack = new ItemStack(Holder.ITEM_EXP_MODULE);
        var module = new ExpModuleItem.ExpItemModule(stack);
        module.addExp(0);
        assertFalse(stack.hasTag());
        assertEquals(0, module.getExp());
    }

    @GameTestGenerator
    List<TestFunction> addTwice() {
        return expValue().mapToObj(
            i -> GameTestUtil.create(QuarryPlus.modID, BATCH, "add_twice_%d".formatted(i), () -> addTwice(i))
        ).toList();
    }

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
