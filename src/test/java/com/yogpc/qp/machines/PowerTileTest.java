package com.yogpc.qp.machines;

import java.util.stream.Stream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlusTest;
import com.yogpc.qp.machines.quarry.SFQuarryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(QuarryPlusTest.class)
public class PowerTileTest {
    static Stream<Block> fluidBlocks() {
        return Stream.of(
            Blocks.WATER,
            Blocks.LAVA,
            Blocks.BUBBLE_COLUMN
        );
    }

    static Stream<Block> solidBlocks() {
        return Stream.of(
            Blocks.STONE, Blocks.GLASS, Blocks.DIRT, Blocks.DIAMOND_ORE
        );
    }

    static Stream<Block> waterloggedBlocks() {
        return Stream.of(
            Blocks.CHEST, Blocks.ACACIA_STAIRS, Blocks.OAK_FENCE
        );
    }

    @Nested
    class IsFluidTest {
        @Test
        void airIsNotFluid() {
            assertFalse(PowerTile.isFullFluidBlock(Blocks.AIR.defaultBlockState()));
            assertFalse(PowerTile.isFullFluidBlock(Blocks.CAVE_AIR.defaultBlockState()));
            assertFalse(PowerTile.isFullFluidBlock(Blocks.VOID_AIR.defaultBlockState()));
        }

        @ParameterizedTest
        @MethodSource("com.yogpc.qp.machines.PowerTileTest#fluidBlocks")
        void fluidBlockIsFluid(Block block) {
            assertTrue(PowerTile.isFullFluidBlock(block.defaultBlockState()));
            if (block.defaultBlockState().hasProperty(BlockStateProperties.LEVEL)) {
                for (var value : BlockStateProperties.LEVEL.getPossibleValues()) {
                    var state = block.defaultBlockState().setValue(BlockStateProperties.LEVEL, value);
                    assertTrue(PowerTile.isFullFluidBlock(state));
                }
            }
        }

        @ParameterizedTest
        @MethodSource("com.yogpc.qp.machines.PowerTileTest#waterloggedBlocks")
        void waterloggedIsNotFluid(Block block) {
            assertFalse(PowerTile.isFullFluidBlock(block.defaultBlockState()));
            var waterlogged = block.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true);
            assertFalse(PowerTile.isFullFluidBlock(waterlogged));
        }

        @ParameterizedTest
        @MethodSource("com.yogpc.qp.machines.PowerTileTest#solidBlocks")
        void solidBlockIsNotFluid(Block block) {
            assertFalse(PowerTile.isFullFluidBlock(block.defaultBlockState()));
        }

        @Test
        void manualTest() {
            assertAll(
                () -> assertFalse(PowerTile.isFullFluidBlock(Blocks.SEAGRASS.defaultBlockState())),
                () -> assertFalse(PowerTile.isFullFluidBlock(Blocks.KELP.defaultBlockState())),
                () -> assertFalse(PowerTile.isFullFluidBlock(Blocks.KELP_PLANT.defaultBlockState()))
            );
        }
    }

    @Nested
    class FluidStateTest {
        @ParameterizedTest
        @MethodSource("com.yogpc.qp.machines.PowerTileTest#fluidBlocks")
        @DisplayName("Full fluid block always has non-empty FluidState.")
        void fluidBlock(Block fluidBlock) {
            var fluidState = fluidBlock.defaultBlockState().getFluidState();
            assertFalse(fluidState.isEmpty());
            assertTrue(fluidState.isSource());
        }

        @ParameterizedTest
        @MethodSource("com.yogpc.qp.machines.PowerTileTest#solidBlocks")
        @DisplayName("Solid block always has empty FluidState")
        void solidBlock(Block block) {
            var fluidState = block.defaultBlockState().getFluidState();
            assertTrue(fluidState.isEmpty());
        }

        @ParameterizedTest
        @MethodSource("com.yogpc.qp.machines.PowerTileTest#waterloggedBlocks")
        void waterloggedBlock(Block block) {
            var state = block.defaultBlockState();
            var noWater = state.setValue(BlockStateProperties.WATERLOGGED, false);
            var noWaterFluidState = noWater.getFluidState();
            var withWater = state.setValue(BlockStateProperties.WATERLOGGED, true);
            var withWaterFluidState = withWater.getFluidState();

            assertAll(
                () -> assertTrue(noWaterFluidState.isEmpty()),
                () -> assertFalse(withWaterFluidState.isEmpty()),
                () -> assertTrue(withWaterFluidState.isSource())
            );
        }
    }

    public static void capacityTest(PowerTile tile) {
        double maxEnergy = tile.getMaxEnergy();
        var digit = Math.log10(maxEnergy / PowerTile.ONE_FE);
        assertTrue(digit < 5, "Energy: %f, Default should be less than 10^5.".formatted(maxEnergy));
    }

    @Nested
    class UseEnergyTest {
        @ParameterizedTest
        @ValueSource(ints = {0, 1, 100, 500, 999, 1000})
        void useNormal(int energy) {
            var tile = new SFQuarryEntity(BlockPos.ZERO, Holder.BLOCK_SOLID_FUEL_QUARRY.defaultBlockState());
            ((PowerTile) tile).setTimeProvider(() -> 1L);
            assertEquals(1000 * PowerTile.ONE_FE, tile.getMaxEnergy());
            Assumptions.assumeTrue(energy * PowerTile.ONE_FE <= tile.getMaxEnergy());
            tile.addEnergy(tile.getMaxEnergy(), false);
            assertEquals(tile.getMaxEnergy(), tile.getEnergy());

            var result = tile.useEnergy(energy * PowerTile.ONE_FE, PowerTile.Reason.FORCE, false);
            assertTrue(result, "Energy(%d FE) must be consumed.".formatted(energy));
            assertEquals(tile.getMaxEnergy() - energy * PowerTile.ONE_FE, tile.getEnergy());
        }

        @ParameterizedTest
        @ValueSource(ints = {1001, 1500, 10000})
        void useOverWithForce(int energy) {
            var tile = new SFQuarryEntity(BlockPos.ZERO, Holder.BLOCK_SOLID_FUEL_QUARRY.defaultBlockState());
            ((PowerTile) tile).setTimeProvider(() -> 1L);
            assertEquals(1000 * PowerTile.ONE_FE, tile.getMaxEnergy());
            Assumptions.assumeTrue(energy * PowerTile.ONE_FE > tile.getMaxEnergy());
            tile.addEnergy(tile.getMaxEnergy(), false);
            assertEquals(tile.getMaxEnergy(), tile.getEnergy());

            var result = tile.useEnergy(energy * PowerTile.ONE_FE, PowerTile.Reason.FORCE, true);
            assertTrue(result, "Energy(%d FE) must be consumed.".formatted(energy));
            assertTrue(tile.getEnergy() < 0);
        }

        @ParameterizedTest
        @ValueSource(ints = {1001, 1500, 10000})
        void useOverWithoutForce(int energy) {
            var tile = new SFQuarryEntity(BlockPos.ZERO, Holder.BLOCK_SOLID_FUEL_QUARRY.defaultBlockState());
            ((PowerTile) tile).setTimeProvider(() -> 1L);
            assertEquals(1000 * PowerTile.ONE_FE, tile.getMaxEnergy());
            Assumptions.assumeTrue(energy * PowerTile.ONE_FE > tile.getMaxEnergy());
            tile.addEnergy(tile.getMaxEnergy(), false);
            assertEquals(tile.getMaxEnergy(), tile.getEnergy());

            var result = tile.useEnergy(energy * PowerTile.ONE_FE, PowerTile.Reason.FORCE, false);
            assertFalse(result, "Energy(%d FE) must not be consumed.".formatted(energy));
            assertEquals(tile.getMaxEnergy(), tile.getEnergy());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 100, 500, 999, 1000, 1001, 1500, 10000})
        void useNormal2(int energy) {
            var tile = new SFQuarryEntity(BlockPos.ZERO, Holder.BLOCK_SOLID_FUEL_QUARRY.defaultBlockState());
            ((PowerTile) tile).setTimeProvider(() -> 1L);
            tile.addEnergy(tile.getMaxEnergy(), false);
            assertEquals(tile.getMaxEnergy(), tile.getEnergy());

            var requiredEnergy = energy * PowerTile.ONE_FE;
            var result = tile.useEnergy(requiredEnergy, PowerTile.Reason.BREAK_BLOCK, requiredEnergy > tile.getMaxEnergy());
            assertTrue(result, "Machine must consume %d FE".formatted(energy));
            assertEquals(tile.getMaxEnergy() - energy * PowerTile.ONE_FE, tile.getEnergy());
        }
    }
}
