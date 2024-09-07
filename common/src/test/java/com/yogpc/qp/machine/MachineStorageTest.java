package com.yogpc.qp.machine;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.yogpc.qp.BeforeMC;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class MachineStorageTest extends BeforeMC {
    @Test
    void instance() {
        assertDoesNotThrow(MachineStorage::new);
    }

    @Test
    void instanceViaFactory() {
        assertDoesNotThrow(() -> MachineStorage.of());
    }

    @Test
    void instanceFromMap() {
        assertDoesNotThrow(() -> MachineStorage.of(Map.of(), Map.of()));
    }

    @Nested
    class ItemTest {
        MachineStorage storage = new MachineStorage();

        @Test
        void addItem() {
            MachineStorage.ItemKey itemKey = new MachineStorage.ItemKey(Items.APPLE, DataComponentPatch.EMPTY);
            storage.addItem(itemKey.toStack(20));
            assertEquals(20, storage.getItemCount(itemKey));
            storage.addItem(itemKey.toStack(40));
            assertEquals(60, storage.getItemCount(itemKey));
        }

        @Test
        void defaultCount() {
            MachineStorage.ItemKey itemKey = new MachineStorage.ItemKey(Items.APPLE, DataComponentPatch.EMPTY);
            assertEquals(0, storage.getItemCount(itemKey));
        }

        @Test
        void otherItemCount1() {
            MachineStorage.ItemKey itemKey = new MachineStorage.ItemKey(Items.APPLE, DataComponentPatch.EMPTY);
            storage.addItem(itemKey.toStack(20));
            var key2 = new MachineStorage.ItemKey(Items.GOLDEN_APPLE, DataComponentPatch.EMPTY);
            assertEquals(0, storage.getItemCount(key2));
            storage.addItem(key2.toStack(40));
            assertAll(
                () -> assertEquals(20, storage.getItemCount(itemKey)),
                () -> assertEquals(40, storage.getItemCount(key2))
            );
        }

        @Test
        void otherItemCount2() {
            MachineStorage.ItemKey itemKey = new MachineStorage.ItemKey(Items.APPLE, DataComponentPatch.EMPTY);
            storage.addItem(itemKey.toStack(20));
            var key2 = new MachineStorage.ItemKey(Items.APPLE, DataComponentPatch.builder().set(DataComponents.DAMAGE, 3).build());
            assertEquals(0, storage.getItemCount(key2));
            storage.addItem(key2.toStack(40));
            assertAll(
                () -> assertEquals(20, storage.getItemCount(itemKey)),
                () -> assertEquals(40, storage.getItemCount(key2))
            );
        }
    }

    @Nested
    class FluidTest {
        MachineStorage storage = new MachineStorage();

        @Test
        void addFluid() {
            MachineStorage.FluidKey key = new MachineStorage.FluidKey(Fluids.WATER, DataComponentPatch.EMPTY);
            storage.addFluid(Fluids.WATER, MachineStorage.ONE_BUCKET);
            assertEquals(MachineStorage.ONE_BUCKET, storage.getFluidCount(key));
            assertEquals(MachineStorage.ONE_BUCKET, storage.getFluidCount(Fluids.WATER));
            storage.addFluid(Fluids.WATER, MachineStorage.ONE_BUCKET * 3);
            assertEquals(MachineStorage.ONE_BUCKET * 4, storage.getFluidCount(Fluids.WATER));
        }
    }

    @ParameterizedTest
    @MethodSource("ops")
    void serialize(DynamicOps<?> ops) {
        MachineStorage storage = new MachineStorage();
        storage.addItem(new ItemStack(Items.APPLE, 40));
        storage.addFluid(Fluids.WATER, MachineStorage.ONE_BUCKET);

        var serialized = assertDoesNotThrow(() -> MachineStorage.CODEC.codec().encodeStart(ops, storage).getOrThrow());
        assertNotNull(serialized);
    }


    @ParameterizedTest
    @MethodSource("ops")
    <T> void cycle(DynamicOps<T> ops) {
        MachineStorage storage = new MachineStorage();
        storage.addItem(new ItemStack(Items.APPLE, 40));
        storage.addFluid(Fluids.WATER, MachineStorage.ONE_BUCKET);

        var serialized = assertDoesNotThrow(() -> MachineStorage.CODEC.codec().encodeStart(ops, storage).getOrThrow());
        var deserialized = assertDoesNotThrow(() -> MachineStorage.CODEC.codec().parse(ops, serialized).getOrThrow());
        assertInstanceOf(MachineStorage.class, deserialized);
        assertEquals(storage, deserialized);
    }

    @Test
    void deserializeJson1() {
        // language=json
        var json = """
            {
              "items": [
                {
                  "item": "minecraft:apple",
                  "patch": {},
                  "count": 4
                }
              ],
              "fluids": [
                {
                  "fluid": "minecraft:water",
                  "patch": {},
                  "count": 81000
                }
              ]
            }
            """;
        var deserialized = assertDoesNotThrow(() -> MachineStorage.CODEC.codec().parse(JsonOps.INSTANCE, GsonHelper.parse(json)).getOrThrow());
        assertInstanceOf(MachineStorage.class, deserialized);
        MachineStorage expected = new MachineStorage();
        expected.addItem(new ItemStack(Items.APPLE, 4));
        expected.addFluid(Fluids.WATER, MachineStorage.ONE_BUCKET);
        assertEquals(expected, deserialized);
    }

    @Test
    void deserializeJson2() {
        // language=json
        var json = """
            {
              "items": [
                {
                  "item": "minecraft:apple",
                  "count": 4
                }
              ],
              "fluids": [
                {
                  "fluid": "minecraft:water",
                  "count": 81000
                }
              ]
            }
            """;
        var deserialized = assertDoesNotThrow(() -> MachineStorage.CODEC.codec().parse(JsonOps.INSTANCE, GsonHelper.parse(json)).getOrThrow());
        assertInstanceOf(MachineStorage.class, deserialized);
        MachineStorage expected = new MachineStorage();
        expected.addItem(new ItemStack(Items.APPLE, 4));
        expected.addFluid(Fluids.WATER, MachineStorage.ONE_BUCKET);
        assertEquals(expected, deserialized);
    }

    static Stream<DynamicOps<?>> ops() {
        return Stream.of(NbtOps.INSTANCE, JsonOps.INSTANCE, JsonOps.COMPRESSED);
    }

    @Nested
    class PassTest {
        @Test
        void mapCheck() {
            var map = new Object2LongLinkedOpenHashMap<String>();
            map.put("a", 6);
            map.put("b", 4);
            map.put("c", 8);

            for (var entry : map.object2LongEntrySet()) {
                entry.setValue(entry.getLongValue() - 1);
            }
            assertEquals(Map.of(
                "a", 5L,
                "b", 3L,
                "c", 7L
            ), map);
        }
    }
}
