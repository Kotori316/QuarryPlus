package com.yogpc.qp.machines.module;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleInventoryTest extends QuarryPlusTest {
    @Nested
    class SingleTest {
        @Test
        void getModules1() {
            var modules = ModuleInventory.getModules(Stream.of(new ItemStack(Holder.ITEM_PUMP_MODULE)));
            assertEquals(Set.of(QuarryModule.Constant.PUMP), Set.copyOf(modules));
        }

        @Test
        void getModules2() {
            var modules = ModuleInventory.getModules(Stream.of(new ItemStack(Holder.ITEM_BEDROCK_MODULE)));
            assertEquals(Set.of(QuarryModule.Constant.BEDROCK), Set.copyOf(modules));
        }

        @Test
        void getModules3() {
            var modules = ModuleInventory.getModules(Stream.of(new ItemStack(Holder.ITEM_FILLER_MODULE)));
            assertEquals(Set.of(QuarryModule.Constant.FILLER), Set.copyOf(modules));
        }
    }

    @Nested
    class MultiTest {
        @Test
        void getModules3() {
            var modules = ModuleInventory.getModules(Stream.of(new ItemStack(Holder.ITEM_BEDROCK_MODULE), new ItemStack(Holder.ITEM_PUMP_MODULE)));
            assertEquals(Set.of(QuarryModule.Constant.BEDROCK, QuarryModule.Constant.PUMP), Set.copyOf(modules));
        }

        @Test
        void getModules4() {
            var modules = ModuleInventory.getModules(Stream.of(new ItemStack(Holder.ITEM_PUMP_MODULE), new ItemStack(Holder.ITEM_PUMP_MODULE)));
            assertEquals(Set.of(QuarryModule.Constant.PUMP), Set.copyOf(modules));
        }
    }

    @Nested
    class InventoryTest {
        @TestFactory
        Stream<DynamicNode> empty() {
            var inv = new InventoryHolder();
            return Stream.of(
                DynamicTest.dynamicTest("Pump Module", () -> assertFalse(inv.hasPumpModule())),
                DynamicTest.dynamicTest("Bedrock Module", () -> assertFalse(inv.hasBedrockModule())),
                DynamicTest.dynamicTest("Filler Module", () -> assertFalse(inv.hasFillerModule())),
                DynamicTest.dynamicTest("Exp Module", () -> assertTrue(inv.getExpModule().isEmpty())),
                DynamicTest.dynamicTest("Replacer Module", () -> assertTrue(inv.getReplacerModule().isEmpty()))
            );
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4})
        void hasPump(int slot) {
            var inv = new InventoryHolder();
            inv.inventory.setItem(slot, new ItemStack(Holder.ITEM_PUMP_MODULE));
            assertTrue(inv.hasPumpModule());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4})
        void hasBedrock(int slot) {
            var inv = new InventoryHolder();
            inv.inventory.setItem(slot, new ItemStack(Holder.ITEM_BEDROCK_MODULE));
            assertTrue(inv.hasBedrockModule());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4})
        void hasFiller(int slot) {
            var inv = new InventoryHolder();
            inv.inventory.setItem(slot, new ItemStack(Holder.ITEM_FILLER_MODULE));
            assertTrue(inv.hasFillerModule());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4})
        void hasExp(int slot) {
            var inv = new InventoryHolder();
            inv.inventory.setItem(slot, new ItemStack(Holder.ITEM_EXP_MODULE));
            assertTrue(inv.getExpModule().isPresent());
        }

        @TestFactory
        Stream<DynamicNode> multi() {
            var inv = new InventoryHolder();
            inv.inventory.setItem(0, new ItemStack(Holder.ITEM_PUMP_MODULE));
            inv.inventory.setItem(1, new ItemStack(Holder.ITEM_BEDROCK_MODULE));
            inv.inventory.setItem(2, new ItemStack(Holder.ITEM_FILLER_MODULE));
            inv.inventory.setItem(3, new ItemStack(Holder.ITEM_EXP_MODULE));
            return Stream.of(
                DynamicTest.dynamicTest("Filler Module", () -> assertTrue(inv.hasFillerModule())),
                DynamicTest.dynamicTest("Bedrock Module", () -> assertTrue(inv.hasBedrockModule())),
                DynamicTest.dynamicTest("Pump Module", () -> assertTrue(inv.hasPumpModule())),
                DynamicTest.dynamicTest("Exp Module", () -> assertTrue(inv.getExpModule().isPresent()))
            );
        }
    }

    static class InventoryHolder implements ModuleInventory.HasModuleInventory {
        private final ModuleInventory inventory = new ModuleInventory(5, () -> {
        }, Objects::nonNull, this);

        @Override
        public ModuleInventory getModuleInventory() {
            return inventory;
        }

        @Override
        public Set<QuarryModule> getLoadedModules() {
            return Set.copyOf(inventory.getModules());
        }
    }
}
