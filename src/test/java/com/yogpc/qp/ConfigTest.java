package com.yogpc.qp;

import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.quarry.QuarryBlock;
import com.yogpc.qp.machines.quarry.TileQuarry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ConfigTest extends QuarryPlusTest {

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
        @MethodSource("getAllNames")
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
            assertTrue(QuarryPlus.config.enableMap.enabled(location));
        }

        @ParameterizedTest
        @MethodSource("notQuarryItems")
        @NullSource
        void invalidConfig(ResourceLocation location) {
            assertFalse(QuarryPlus.config.enableMap.enabled(location));
        }

        static Stream<ResourceLocation> getAllNames() {
            return Stream.concat(getDefaultOn(), getDefaultOff());
        }

        static Stream<ResourceLocation> getDefaultOn() {
            return Holder.conditionHolders().stream().filter(e -> e.condition().on()).map(Holder.EntryConditionHolder::location);
        }

        static Stream<ResourceLocation> getDefaultOff() {
            return Holder.conditionHolders().stream().filter(e -> !e.condition().on()).map(Holder.EntryConditionHolder::location);
        }

        static Stream<ResourceLocation> notQuarryItems() {
            return Stream.of(Items.DIRT, Items.AIR, EntityType.BAT, EntityType.ZOMBIE)
                .map(ForgeRegistryEntry::getRegistryName);
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
}
