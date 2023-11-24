package com.yogpc.qp;

import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.quarry.QuarryBlock;
import com.yogpc.qp.machines.quarry.TileQuarry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.registries.BuiltInRegistries;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(QuarryPlusTest.class)
final class ConfigTest {

    @Test
    @DisplayName("Change maxEnergy of a machine.")
    void changeMaxEnergy() {
        var before = QuarryPlus.config.powerMap.get(QuarryBlock.NAME, "maxEnergy").orElse(-4);
        assertNotEquals(-4, before);
        try {
            QuarryPlus.config.powerMap.map.get(QuarryBlock.NAME).get("maxEnergy").set(60000d);
            var tile = new TileQuarry(BlockPos.ZERO, Holder.BLOCK_QUARRY.defaultBlockState());
            assertEquals(60000 * PowerTile.ONE_FE, tile.getMaxEnergy());
            assertEquals(60000 * PowerTile.ONE_FE, tile.getPowerConfig().maxEnergy());
        } finally {
            QuarryPlus.config.powerMap.map.get(QuarryBlock.NAME).get("maxEnergy").set(before);
        }
    }

    @Test
    @DisplayName("Change FrameEnergy of a machine")
    void changeFrameEnergy() {
        var before = QuarryPlus.config.powerMap.get(QuarryBlock.NAME, "makeFrame").orElse(-4d);
        assertNotEquals(-4d, before);
        try {
            QuarryPlus.config.powerMap.map.get(QuarryBlock.NAME).get("makeFrame").set(40d);
            var tile = new TileQuarry(BlockPos.ZERO, Holder.BLOCK_QUARRY.defaultBlockState());
            assertEquals(40d * PowerTile.ONE_FE, tile.getPowerConfig().makeFrame());
        } finally {
            QuarryPlus.config.powerMap.map.get(QuarryBlock.NAME).get("makeFrame").set(before);
        }
    }

    @Nested
    class EnableMapTest {
        @ParameterizedTest
        @NullSource
        @MethodSource({"getDefaultOn", "getDefaultOff", "notQuarryItems"})
        @DisplayName("Access Enable Map in Config")
        void accessEnableMap(ResourceLocation location) {
            assertDoesNotThrow(() -> QuarryPlus.config.enableMap.enabled(location));
        }

        @ParameterizedTest
        @MethodSource("getDefaultOn")
        void onConfig(ResourceLocation location) {
            assertTrue(QuarryPlus.config.enableMap.enabled(location));
        }

        @ParameterizedTest
        @MethodSource("getDefaultOff")
        void offConfig(ResourceLocation location) {
            // In test environment, all items are enabled.
            assertTrue(QuarryPlus.config.enableMap.enabled(location), location.toString());
        }

        @ParameterizedTest
        @MethodSource("notQuarryItems")
        @NullSource
        void invalidConfig(ResourceLocation location) {
            assertFalse(QuarryPlus.config.enableMap.enabled(location));
        }

        static Stream<ResourceLocation> getDefaultOn() {
            return Holder.conditionHolders().stream().filter(e -> e.condition().on()).map(Holder.EntryConditionHolder::location);
        }

        static Stream<ResourceLocation> getDefaultOff() {
            return Holder.conditionHolders().stream().filter(e -> !e.condition().on()).map(Holder.EntryConditionHolder::location);
        }

        static Stream<ResourceLocation> notQuarryItems() {
            return Stream.of(
                "dirt", "air", "bat", "zombie"
            ).map(ResourceLocation::new);
        }

        @Test
        void containsAll() {
            var expected = Holder.conditionHolders().stream()
                .filter(Holder.EntryConditionHolder::configurable)
                .sorted(Comparator.comparing(Holder.EntryConditionHolder::location))
                .collect(Collectors.toList());
            var loaded = GsonHelper.parse(new InputStreamReader(
                    Objects.requireNonNull(Config.PowerMap.class.getResourceAsStream("/machine_default.json"), "Content in Jar must not be absent.")
                )).entrySet().stream()
                .map(e -> Map.entry(new ResourceLocation(QuarryPlus.modID, e.getKey()), Holder.EnableOrNot.valueOf(e.getValue().getAsString())))
                .map(e -> new Holder.EntryConditionHolder(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(Holder.EntryConditionHolder::location))
                .collect(Collectors.toList());
            assertIterableEquals(expected, loaded);
        }
    }

    @Nested
    class AcceptableEnchantmentsMapTest {

        private final Config.AcceptableEnchantmentsMap config = QuarryPlus.config.acceptableEnchantmentsMap;

        @Test
        void quarryDefaultEnchantments() {
            var enchantments = config.getAllowedEnchantments(new ResourceLocation(QuarryPlus.modID, "quarry"));
            assertEquals(Set.of(Enchantments.BLOCK_EFFICIENCY, Enchantments.UNBREAKING, Enchantments.BLOCK_FORTUNE, Enchantments.SILK_TOUCH), enchantments);
        }

        @Test
        void advPumpDefaultEnchantments() {
            var enchantments = config.getAllowedEnchantments(new ResourceLocation(QuarryPlus.modID, "adv_pump"));
            assertEquals(Set.of(Enchantments.BLOCK_EFFICIENCY, Enchantments.UNBREAKING, Enchantments.BLOCK_FORTUNE), enchantments);
        }

        @Test
        void nonExist() {
            var enchantments = config.getAllowedEnchantments(new ResourceLocation(QuarryPlus.modID, "not_exist"));
            assertTrue(enchantments.isEmpty(), "Must be empty, %s".formatted(enchantments));
        }

        @Test
        void modifyQuarryEnchantments1() {
            var before = config.enchantmentsMap.get("quarry").get();
            try {
                config.enchantmentsMap.get("quarry").set(List.of("efficiency"));
                var enchantments = config.getAllowedEnchantments(new ResourceLocation(QuarryPlus.modID, "quarry"));
                assertEquals(Set.of(Enchantments.BLOCK_EFFICIENCY), enchantments);
            } finally {
                config.enchantmentsMap.get("quarry").set(before);
            }
        }

        @Test
        void modifyQuarryEnchantments2() {
            var before = config.enchantmentsMap.get("quarry").get();
            try {
                config.enchantmentsMap.get("quarry").set(List.of());
                var enchantments = config.getAllowedEnchantments(new ResourceLocation(QuarryPlus.modID, "quarry"));
                assertTrue(enchantments.isEmpty());
            } finally {
                config.enchantmentsMap.get("quarry").set(before);
            }
        }

        @TestFactory
        Stream<DynamicTest> validEnchantment() {
            return Stream.of(Config.AcceptableEnchantmentsMap.class.getDeclaredMethods())
                .filter(method -> (method.getModifiers() & Modifier.STATIC) != 0)
                .filter(method -> method.getName().endsWith("Enchantments"))
                .filter(method -> method.getParameterCount() == 0)
                .filter(method -> method.getReturnType() == List.class)
                .map(m -> DynamicTest.dynamicTest(m.getName(), () -> validEnchantment(m)));
        }

        @SuppressWarnings("unchecked")
        void validEnchantment(Method method) {
            try {
                List<String> enchantmentNames = (List<String>) method.invoke(null);
                assertAll(
                    enchantmentNames.stream()
                        .map(ResourceLocation::new)
                        .map(n -> () -> assertTrue(BuiltInRegistries.ENCHANTMENT.containsKey(n), "%s must exist.".formatted(n)))
                );
            } catch (ReflectiveOperationException e) {
                fail(e);
            }
        }
    }

    @Nested
    class GetAllTest {
        @Test
        void common() {
            var c = QuarryPlus.config.common;
            var map = assertDoesNotThrow(c::getAll);
            assertFalse(map.isEmpty());
        }

        @Test
        void enableMap() {
            var c = QuarryPlus.config.enableMap;
            var map = assertDoesNotThrow(c::getAll);
            assertFalse(map.isEmpty());
        }

        @Test
        void powerMap() {
            var c = QuarryPlus.config.powerMap;
            var map = assertDoesNotThrow(c::getAll);
            assertFalse(map.isEmpty());
        }

        @Test
        void acceptableEnchantmentsMap() {
            var c = QuarryPlus.config.acceptableEnchantmentsMap;
            var map = assertDoesNotThrow(c::getAll);
            assertFalse(map.isEmpty());
        }

        @Test
        void all() {
            var c = QuarryPlus.config;
            var map = assertDoesNotThrow(c::getAll);
            var fieldCount = Config.class.getFields().length;
            assertEquals(fieldCount, map.size());
            var fieldNames = Stream.of(Config.class.getFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
            assertEquals(fieldNames, map.keySet());
        }
    }
}
