package com.yogpc.qp.machines.mini_quarry;

import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BlockStatePredicateTest extends QuarryPlusTest {
    static void containType(BlockStatePredicate predicate) {
        var tag = predicate.toTag();
        assertTrue(tag.contains("type"));
    }

    static Stream<BlockState> airBlocks() {
        return Stream.of(Blocks.AIR, Blocks.CAVE_AIR, Blocks.VOID_AIR).map(Block::defaultBlockState);
    }

    static Stream<BlockState> normalBlocks() {
        return Stream.of(Blocks.STONE, Blocks.GLASS, Blocks.FURNACE).map(Block::defaultBlockState);
    }

    static Stream<BlockState> fluidBlocks() {
        return Stream.of(Blocks.WATER, Blocks.LAVA).map(Block::defaultBlockState);
    }

    @Nested
    class AirTest {
        @Test
        void containType() {
            BlockStatePredicateTest.containType(BlockStatePredicate.air());
        }

        @ParameterizedTest
        @MethodSource("com.yogpc.qp.machines.mini_quarry.BlockStatePredicateTest#airBlocks")
        void testAir(BlockState state) {
            assertTrue(BlockStatePredicate.air().test(state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
        }

        @ParameterizedTest
        @MethodSource("com.yogpc.qp.machines.mini_quarry.BlockStatePredicateTest#normalBlocks")
        void testNormal(BlockState state) {
            assertFalse(BlockStatePredicate.air().test(state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
        }

        @ParameterizedTest
        @MethodSource("com.yogpc.qp.machines.mini_quarry.BlockStatePredicateTest#fluidBlocks")
        void testFluid(BlockState state) {
            assertFalse(BlockStatePredicate.air().test(state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
        }
    }

    @Nested
    class FluidTest {
        @Test
        void containType() {
            BlockStatePredicateTest.containType(BlockStatePredicate.fluid());
        }

        @ParameterizedTest
        @MethodSource("com.yogpc.qp.machines.mini_quarry.BlockStatePredicateTest#airBlocks")
        void testAir(BlockState state) {
            assertFalse(BlockStatePredicate.fluid().test(state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
        }

        @ParameterizedTest
        @MethodSource("com.yogpc.qp.machines.mini_quarry.BlockStatePredicateTest#normalBlocks")
        void testNormal(BlockState state) {
            assertFalse(BlockStatePredicate.fluid().test(state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
        }

        @ParameterizedTest
        @MethodSource("com.yogpc.qp.machines.mini_quarry.BlockStatePredicateTest#fluidBlocks")
        void testFluid(BlockState state) {
            assertTrue(BlockStatePredicate.fluid().test(state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
        }
    }
}
