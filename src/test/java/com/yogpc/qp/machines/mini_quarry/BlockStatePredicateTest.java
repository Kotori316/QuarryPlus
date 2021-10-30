package com.yogpc.qp.machines.mini_quarry;

import java.util.function.Function;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
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
        return Stream.of(Blocks.STONE, Blocks.GLASS, Blocks.FURNACE, Blocks.DIAMOND_ORE).map(Block::defaultBlockState);
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

    @Nested
    class NameTest {
        static BlockStatePredicate predicate1 = BlockStatePredicate.name(new ResourceLocation("minecraft", "cobblestone"));
        static BlockStatePredicate predicate2 = BlockStatePredicate.name(new ResourceLocation("minecraft", "chest"));

        static Stream<BlockStatePredicate> namePredicates() {
            return Stream.of(predicate1, predicate2);
        }

        static Stream<Object[]> blockWithPredicates() {
            return namePredicates().flatMap(p -> Stream.of(
                airBlocks(), normalBlocks(), fluidBlocks()
            ).flatMap(s -> s.map(b -> new Object[]{p, b})));
        }

        @ParameterizedTest
        @MethodSource("namePredicates")
        void containType(BlockStatePredicate p) {
            BlockStatePredicateTest.containType(p);
        }

        @ParameterizedTest
        @MethodSource("blockWithPredicates")
        void testBlocks(BlockStatePredicate p, BlockState state) {
            assertFalse(p.test(state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
        }

        @Test
        void testCobblestone() {
            assertTrue(predicate1.test(Blocks.COBBLESTONE.defaultBlockState(), EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
        }
    }

    @Nested
    class AllTest {
        @Test
        void containType() {
            BlockStatePredicateTest.containType(BlockStatePredicate.air());
        }

        static Stream<BlockState> allBlocks() {
            return Stream.of(airBlocks(), fluidBlocks(), normalBlocks()).flatMap(Function.identity());
        }

        @ParameterizedTest
        @MethodSource("allBlocks")
        void test(BlockState state) {
            assertTrue(BlockStatePredicate.all().test(state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
        }
    }
}
