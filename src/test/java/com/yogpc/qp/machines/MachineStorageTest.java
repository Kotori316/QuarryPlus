package com.yogpc.qp.machines;

import java.util.Arrays;

import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineStorageTest extends QuarryPlusTest {
    @Test
    void hasAllDirection() {
        assertTrue(MachineStorage.INSERT_ORDER.containsAll(Arrays.asList(Direction.values())));
        assertEquals(Direction.values().length, MachineStorage.INSERT_ORDER.size());
    }

    @Nested
    class InsertTest {
        @ParameterizedTest
        @ValueSource(ints = {1, 5, 10, 16, 64, 128})
        void insertN(int size) {
            var storage = new MachineStorage();
            storage.addItem(new ItemStack(Items.APPLE, size));

            assertEquals(size, storage.itemMap.get(new ItemKey(Items.APPLE, null)));
        }

        @Test
        void insertTwice1() {
            var storage = new MachineStorage();
            storage.addItem(new ItemStack(Items.APPLE, 10));
            storage.addItem(new ItemStack(Items.APPLE, 10));

            assertEquals(20, storage.itemMap.get(new ItemKey(Items.APPLE, null)));
        }

        @Test
        void insertTwice2() {
            var storage = new MachineStorage();
            storage.addItem(new ItemStack(Items.APPLE, 10));
            storage.addItem(new ItemStack(Items.GOLDEN_APPLE, 10));
            storage.addItem(new ItemStack(Items.APPLE, 10));

            assertEquals(20, storage.itemMap.get(new ItemKey(Items.APPLE, null)));
            assertEquals(10, storage.itemMap.get(new ItemKey(Items.GOLDEN_APPLE, null)));
        }

        @Test
        void insert2Items() {
            var storage = new MachineStorage();
            storage.addItem(new ItemStack(Items.APPLE, 10));
            storage.addItem(new ItemStack(Items.GOLDEN_APPLE, 10));

            assertEquals(10, storage.itemMap.get(new ItemKey(Items.APPLE, null)));
            assertEquals(10, storage.itemMap.get(new ItemKey(Items.GOLDEN_APPLE, null)));
        }

        @Test
        void airIsNotInsertable() {
            var storage = new MachineStorage();
            storage.addItem(new ItemStack(Items.AIR, 10));
            assertNull(storage.itemMap.get(new ItemKey(Items.AIR, null)));
        }

        @Test
        void insertNbtStack1() {
            var storage = new MachineStorage();
            var tag1 = new CompoundTag();
            tag1.putString("a", "b");
            var key1 = new ItemKey(Items.ENCHANTED_GOLDEN_APPLE, tag1);
            var tag2 = new CompoundTag();
            tag2.putString("c", "b");
            var key2 = new ItemKey(Items.ENCHANTED_GOLDEN_APPLE, tag2);
            storage.addItem(key1.toStack(15));
            storage.addItem(key2.toStack(20));

            assertEquals(15, storage.itemMap.get(key1));
            assertEquals(20, storage.itemMap.get(key2));
            assertNull(storage.itemMap.get(new ItemKey(Items.ENCHANTED_GOLDEN_APPLE, null)));
        }
    }

    @Nested
    class ExtractTest {
        private static MachineStorage getInstance() {
            var storage = new MachineStorage();
            storage.addItem(new ItemStack(Items.APPLE, 10));
            storage.addItem(new ItemStack(Items.GOLDEN_APPLE, 5));
            {
                var stack = new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 20);
                stack.getOrCreateTag().putString("a", "b");
                storage.addItem(stack);
            }
            storage.addItem(new ItemStack(Items.DIAMOND_PICKAXE));

            return storage;
        }

        @Test
        void size() {
            MachineStorage storage = getInstance();

            assertEquals(4, storage.itemMap.size());
        }
    }

    @Nested
    class FluidInsertTest {
        @ParameterizedTest
        @ValueSource(ints = {1, 100, 500, (int) MachineStorage.ONE_BUCKET, 2000, 10000})
        void insertWater(int amount) {
            MachineStorage storage = new MachineStorage();
            storage.addFluid(Fluids.WATER, amount);

            assertEquals(amount, storage.fluidMap.get(new FluidKey(Fluids.WATER, null)));
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 100, 500, (int) MachineStorage.ONE_BUCKET, 2000, 10000})
        void insertWaterTwice(int amount) {
            MachineStorage storage = new MachineStorage();
            storage.addFluid(Fluids.WATER, amount);
            storage.addFluid(Fluids.WATER, amount);

            assertEquals(amount * 2L, storage.fluidMap.get(new FluidKey(Fluids.WATER, null)));
            assertEquals(1, storage.fluidMap.size());
        }

        @Test
        void insert2Fluids() {
            MachineStorage storage = new MachineStorage();
            storage.addFluid(Fluids.WATER, MachineStorage.ONE_BUCKET);
            storage.addFluid(Fluids.LAVA, MachineStorage.ONE_BUCKET * 3);

            assertEquals(MachineStorage.ONE_BUCKET, storage.fluidMap.get(new FluidKey(Fluids.WATER, null)));
            assertEquals(MachineStorage.ONE_BUCKET * 3L, storage.fluidMap.get(new FluidKey(Fluids.LAVA, null)));
        }

        @Test
        void minusFluid1() {
            MachineStorage storage = new MachineStorage();
            storage.addFluid(Fluids.LAVA, MachineStorage.ONE_BUCKET * 3);
            storage.addFluid(Fluids.LAVA, -MachineStorage.ONE_BUCKET * 3);

            assertNull(storage.fluidMap.get(new FluidKey(Fluids.LAVA, null)));
        }

        @Test
        void minusFluid2() {
            MachineStorage storage = new MachineStorage();
            storage.addFluid(Fluids.LAVA, MachineStorage.ONE_BUCKET * 3);
            storage.addFluid(Fluids.LAVA, -MachineStorage.ONE_BUCKET * 4);

            assertNull(storage.fluidMap.get(new FluidKey(Fluids.LAVA, null)));
        }
    }

    @Test
    void serialize1() {
        var storage = new MachineStorage();
        storage.addItem(new ItemStack(Items.APPLE, 10));
        storage.addItem(new ItemStack(Items.GOLDEN_APPLE, 15));
        storage.addItem(new ItemStack(Items.DIAMOND, 7));
        var tag = storage.toNbt();
        var deserialized = new MachineStorage();
        deserialized.readNbt(tag);
        assertAll(
            () -> assertEquals(10, deserialized.itemMap.get(new ItemKey(Items.APPLE, null))),
            () -> assertEquals(15, deserialized.itemMap.get(new ItemKey(Items.GOLDEN_APPLE, null))),
            () -> assertEquals(7, deserialized.itemMap.get(new ItemKey(Items.DIAMOND, null)))
        );
    }
}
