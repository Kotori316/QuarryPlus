package com.yogpc.qp.machines;

import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PowerTileTest extends QuarryPlusTest {
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
}
