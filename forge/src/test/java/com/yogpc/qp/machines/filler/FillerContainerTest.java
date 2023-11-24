package com.yogpc.qp.machines.filler;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(QuarryPlusTest.class)
class FillerContainerTest {

    static Stream<ItemStack> canAcceptBlocks() {
        return IntStream.of(1, 8, 16, 64)
            .boxed()
            .flatMap(count -> Stream.of(Items.DIRT, Items.STONE, Items.DIAMOND_BLOCK, Items.OBSIDIAN, Items.ICE,
                    Items.BEETROOT_SEEDS, Items.WHEAT_SEEDS)
                .map(item -> new ItemStack(item, count)));
    }

    static Stream<ItemStack> cantAcceptItems() {
        return IntStream.of(1, 8, 16, 64)
            .boxed()
            .flatMap(count -> Stream.of(Items.WHEAT, Items.DIAMOND, Items.BEETROOT, Items.LEATHER_CHESTPLATE)
                .map(item -> new ItemStack(item, count)));
    }

    static Stream<ItemStack> cantAcceptSpecialItems() {
        var items = List.of(Items.CHEST, Items.COMPARATOR, Items.COMMAND_BLOCK, Items.TORCH, Items.FURNACE, Items.AIR);
        return IntStream.of(1, 8, 16, 64)
            .boxed()
            .flatMap(count -> items.stream().map(item -> new ItemStack(item, count)));
    }

    @Nested
    class AcceptTest {
        @ParameterizedTest
        @MethodSource("com.yogpc.qp.machines.filler.FillerContainerTest#canAcceptBlocks")
        void canAcceptBlocks(ItemStack stack) {
            assertTrue(FillerContainer.canAccept(stack));
        }

        @ParameterizedTest
        @MethodSource(value = {
            "com.yogpc.qp.machines.filler.FillerContainerTest#cantAcceptItems",
            "com.yogpc.qp.machines.filler.FillerContainerTest#cantAcceptSpecialItems"})
        void cantAcceptItems(ItemStack stack) {
            assertFalse(FillerContainer.canAccept(stack));
        }

        @Test
        void dummy() {
            assertTrue(FillerContainerTest.canAcceptBlocks().findAny().isPresent());
            assertTrue(FillerContainerTest.cantAcceptItems().findAny().isPresent());
            assertTrue(FillerContainerTest.cantAcceptSpecialItems().findAny().isPresent());
        }
    }

    @Nested
    class InventoryTest {
        @Test
        @DisplayName("Find Empty")
        void findFirst1() {
            var inv = new FillerContainer(5);
            assertTrue(inv.getFirstItem().isEmpty());
        }

        @Test
        void noBlocks() {
            var inv = new FillerContainer(5);
            var item = Items.APPLE;
            inv.setItem(0, new ItemStack(item));
            assertEquals(item, inv.getItem(0).getItem());
            assertTrue(inv.getFirstItem().isEmpty());
        }

        @ParameterizedTest
        @MethodSource("com.yogpc.qp.machines.filler.FillerContainerTest#canAcceptBlocks")
        void findBlock1(ItemStack stack) {
            var inv = new FillerContainer(5);
            inv.setItem(0, stack);
            assertEquals(stack.getItem(), inv.getItem(0).getItem());
            assertTrue(inv.getFirstItem().isPresent());
        }

        @ParameterizedTest
        @MethodSource("com.yogpc.qp.machines.filler.FillerContainerTest#canAcceptBlocks")
        void findBlock2(ItemStack stack) {
            var inv = new FillerContainer(5);
            inv.setItem(0, new ItemStack(Items.DIRT));
            inv.setItem(1, stack);
            var found = inv.getFirstItem();
            assertEquals(Items.DIRT, found.map(ItemStack::getItem).orElseThrow());
        }

        @Test
        void shrinkTest() {
            var inv = new FillerContainer(5);
            inv.setItem(0, new ItemStack(Items.DIRT, 2));
            var found = inv.getFirstItem();
            Assumptions.assumeTrue(found.isPresent(), "getFirstItem must work.");
            var stack = found.get();
            assertEquals(2, stack.getCount());
            stack.shrink(1);
            assertEquals(1, stack.getCount());
            assertEquals(1, inv.getItem(0).getCount());
            stack.shrink(1);
            assertTrue(stack.isEmpty());
            assertTrue(inv.isEmpty());
        }
    }
}
