package com.yogpc.qp.machines.mini_quarry;

import com.kotori316.testutil.GameTestUtil;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.gametest.GameTestDontPrefix;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Similar to {@link BlockStatePredicateTest}, but this is for tags and vanilla predicates,
 * which requires actual minecraft instance.
 */
@GameTestHolder(QuarryPlus.modID)
@GameTestDontPrefix
public final class BlockStatePredicateTagsTest {
    private static final String BATCH = "BlockStatePredicateTags";

    static void cycle(BlockStatePredicate predicate) {
        var tag = predicate.toTag();
        var fromTag = BlockStatePredicate.fromTag(tag);
        assertEquals(predicate, fromTag, "Expect Tag: %s, Actual Tag: %s".formatted(tag, fromTag.toTag()));
    }

    static String convertKey(String key) {
        return key
            .replace("#", "tag_")
            .replace(":", "_")
            .replace("[", "_")
            .replace("]", "_")
            .replace("=", "_")
            ;
    }

    @GameTestGenerator
    public List<TestFunction> cycleConstant() {
        return Stream.of(BlockStatePredicate.air(), BlockStatePredicate.all(), BlockStatePredicate.fluid())
            .map(p -> GameTestUtil.create(QuarryPlus.modID, BATCH, "cycle_" + p.gameTestName(), () -> cycle(p)))
            .toList();
    }

    @GameTestGenerator
    public List<TestFunction> cycleName() {
        var names = Stream.of(Blocks.STONE, Blocks.DIAMOND_ORE).map(ForgeRegistries.BLOCKS::getKey);
        var abnormalNames = Stream.of("as", "a:t", "", "fe:").map(ResourceLocation::new);
        return Stream.concat(names, abnormalNames)
            .map(BlockStatePredicate::name)
            .map(p -> GameTestUtil.create(QuarryPlus.modID, BATCH, "cycle_" + p.gameTestName(), () -> cycle(p)))
            .toList();
    }

    @GameTestGenerator
    public List<TestFunction> cycleTag() {
        var names = Stream.of(Tags.Blocks.STONE, Tags.Blocks.COBBLESTONE, BlockTags.ACACIA_LOGS, BlockTags.BEACON_BASE_BLOCKS)
            .map(TagKey::location);
        return names.map(BlockStatePredicate::tag)
            .map(p -> GameTestUtil.create(QuarryPlus.modID, BATCH,"cycle_" + p.gameTestName(), () -> cycle(p)))
            .toList();
    }

    static void tagTest(TagKey<Block> tag, BlockState state, GameTestHelper helper) {
        var pos = GameTestUtil.getBasePos(helper).above();
        var p = BlockStatePredicate.tag(tag.location());
        helper.setBlock(pos, state);
        assertTrue(p.test(state, helper.getLevel(), helper.absolutePos(pos)), "Tag: %s, State: %s".formatted(tag, state));
        helper.succeed();
    }

    @GameTestGenerator
    public List<TestFunction> tags() {
        var tests = Stream.of(
            Pair.of(Tags.Blocks.STONE, Blocks.STONE.defaultBlockState()),
            Pair.of(Tags.Blocks.STONE, Blocks.ANDESITE.defaultBlockState()),
            Pair.of(Tags.Blocks.GLASS, Blocks.GLASS.defaultBlockState()),
            Pair.of(BlockTags.DIRT, Blocks.GRASS_BLOCK.defaultBlockState())
        );
        return tests
            .map(e -> GameTestUtil.create(QuarryPlus.modID, BATCH, "tag_test_" + convertKey(e.getKey().location().toString()), g -> tagTest(e.getKey(), e.getValue(), g)))
            .toList();
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE)
    public void stoneTagFailTest(GameTestHelper helper) {
        var tag = Tags.Blocks.STONE;
        var p = BlockStatePredicate.tag(tag.location());
        assertFalse(p.test(Blocks.GLASS.defaultBlockState(), helper.getLevel(), BlockPos.ZERO));
        assertFalse(p.test(Blocks.AIR.defaultBlockState(), helper.getLevel(), BlockPos.ZERO));
        assertFalse(p.test(Blocks.WATER.defaultBlockState(), helper.getLevel(), BlockPos.ZERO));
        helper.succeed();
    }

    @GameTestGenerator
    public List<TestFunction> cycleVanillaPredicate() {
        var names = Stream.of(
            "#forge:stone",
            "minecraft:stone",
            "#forge:stone",
            "#forge:glass",
            "minecraft:glass",
            "minecraft:grass_block[snowy=true]",
            "#minecraft:dirt"
        );
        return names.map(BlockStatePredicate::predicateString)
            .map(p -> GameTestUtil.create(QuarryPlus.modID, BATCH, "cycle_vanilla_pre_" + p.gameTestName(), () -> cycle(p)))
            .toList();
    }

    static void vanillaPredicateTest(String predicate, BlockState state, GameTestHelper helper, boolean isTrue) {
        var pos = GameTestUtil.getBasePos(helper).above();
        var p = BlockStatePredicate.predicateString(predicate);

        // Prepare
        helper.setBlock(pos, state);
        var result = p.test(state, helper.getLevel(), helper.absolutePos(pos));
        var message = "Predicate: %s, State %s".formatted(predicate, state);
        if (isTrue) {
            assertTrue(result, message);
        } else {
            assertFalse(result, message);
        }
        helper.succeed();
    }

    @GameTestGenerator
    public List<TestFunction> vanillaPredicateTrue() {
        var tests = Stream.of(
            Pair.of("#forge:stone", Blocks.STONE.defaultBlockState()),
            Pair.of("minecraft:stone", Blocks.STONE.defaultBlockState()),
            Pair.of("#forge:stone", Blocks.ANDESITE.defaultBlockState()),
            Pair.of("#forge:glass", Blocks.GLASS.defaultBlockState()),
            Pair.of("minecraft:glass", Blocks.GLASS.defaultBlockState()),
            Pair.of("minecraft:grass_block[snowy=true]", Blocks.GRASS_BLOCK.defaultBlockState().setValue(SnowyDirtBlock.SNOWY, true)),
            Pair.of("#minecraft:dirt", Blocks.GRASS_BLOCK.defaultBlockState())
        );
        return tests
            .map(e -> GameTestUtil.create(QuarryPlus.modID, BATCH, "vanilla_true_" + convertKey(e.getKey()), g -> vanillaPredicateTest(e.getKey(), e.getValue(), g, true)))
            .toList();
    }

    @GameTestGenerator
    public List<TestFunction> vanillaPredicateFalse() {
        var tests = Stream.of(
            Pair.of("#forge:stone", Blocks.GLASS.defaultBlockState()),
            Pair.of("minecraft:stone", Blocks.ANDESITE.defaultBlockState()),
            Pair.of("#forge:stone", Blocks.GRASS_BLOCK.defaultBlockState()),
            Pair.of("minecraft:glass", Blocks.AIR.defaultBlockState()),
            Pair.of("minecraft:grass_block[snowy=true]", Blocks.GRASS_BLOCK.defaultBlockState()),
            Pair.of("#minecraft:dirt", Blocks.GLASS.defaultBlockState())
        );
        return tests
            .map(e -> GameTestUtil.create(QuarryPlus.modID, BATCH, "vanilla_false_" + convertKey(e.getKey()), g -> vanillaPredicateTest(e.getKey(), e.getValue(), g, false)))
            .toList();
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE)
    public void invalidPredicateCreateInstance(GameTestHelper helper) {
        var predicate = "minecraft:not_exist";
        assertDoesNotThrow(() -> BlockStatePredicate.predicateString(predicate));
        helper.succeed();
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE)
    public void invalidPredicateReturnFalse(GameTestHelper helper) {
        var predicate = "minecraft:not_exist";
        var p = assertDoesNotThrow(() -> BlockStatePredicate.predicateString(predicate));
        assertAll(
            Stream.of(Blocks.GLASS.defaultBlockState(),
                    Blocks.ANDESITE.defaultBlockState(),
                    Blocks.GRASS_BLOCK.defaultBlockState(),
                    Blocks.AIR.defaultBlockState())
                .map(s -> () -> assertFalse(p.test(s, helper.getLevel(), BlockPos.ZERO)))
        );
        helper.succeed();
    }
}
