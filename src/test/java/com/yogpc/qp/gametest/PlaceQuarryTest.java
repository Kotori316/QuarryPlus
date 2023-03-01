package com.yogpc.qp.gametest;

import java.util.List;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.miningwell.MiningWellTile;
import com.yogpc.qp.machines.quarry.FrameBlock;
import com.yogpc.qp.machines.quarry.TileQuarry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.kotori316.testutil.GameTestUtil;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@GameTestHolder(QuarryPlus.modID)
@PrefixGameTestTemplate(value = false)
public final class PlaceQuarryTest {
    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE)
    public void chainBreakEnabled(GameTestHelper helper) {
        var before = QuarryPlus.config.common.removeFrameAfterQuarryIsRemoved.get();
        try {
            QuarryPlus.config.common.removeFrameAfterQuarryIsRemoved.set(true);
            var base = GameTestUtil.getBasePos(helper).above();
            var frame1 = base.east();
            var frame2 = frame1.above();
            var frame3 = frame2.above();
            helper.setBlock(frame1, Holder.BLOCK_FRAME);
            helper.setBlock(frame2, Holder.BLOCK_FRAME);
            helper.setBlock(frame3, Holder.BLOCK_FRAME);

            helper.setBlock(base, Holder.BLOCK_QUARRY);
            List.of(frame1, frame2, frame3).forEach(p -> helper.assertBlockPresent(Holder.BLOCK_FRAME, p));

            helper.destroyBlock(base);
            List.of(base, frame1, frame2, frame3).forEach(p -> helper.assertBlockPresent(Blocks.AIR, p));
            helper.succeed();
        } finally {
            QuarryPlus.config.common.removeFrameAfterQuarryIsRemoved.set(before);
        }
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE)
    public void chainBreakEnabled2(GameTestHelper helper) {
        var before = QuarryPlus.config.common.removeFrameAfterQuarryIsRemoved.get();
        try {
            QuarryPlus.config.common.removeFrameAfterQuarryIsRemoved.set(true);
            var base = GameTestUtil.getBasePos(helper).above();
            var frame1 = base.east();
            var frame2 = frame1.above();
            var frame3 = frame2.above();
            List.of(frame1, frame2, frame3).forEach(p -> helper.setBlock(p.east(), Blocks.WATER));
            helper.setBlock(frame1, Holder.BLOCK_FRAME.getDammingState());
            helper.setBlock(frame2, Holder.BLOCK_FRAME.getDammingState());
            helper.setBlock(frame3, Holder.BLOCK_FRAME.getDammingState());

            helper.setBlock(base, Holder.BLOCK_QUARRY);
            List.of(frame1, frame2, frame3).forEach(p -> helper.assertBlockPresent(Holder.BLOCK_FRAME, p));
            List.of(frame1, frame2, frame3).forEach(p -> helper.assertBlockProperty(p, FrameBlock.DAMMING, true));

            helper.destroyBlock(base);
            List.of(frame1, frame2, frame3).forEach(p -> helper.assertBlockPresent(Holder.BLOCK_FRAME, p));
            helper.assertBlockNotPresent(Holder.BLOCK_QUARRY, base);
            helper.succeed();
        } finally {
            QuarryPlus.config.common.removeFrameAfterQuarryIsRemoved.set(before);
        }
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE)
    public void chainBreakEnabled3(GameTestHelper helper) {
        var before = QuarryPlus.config.common.removeFrameAfterQuarryIsRemoved.get();
        try {
            QuarryPlus.config.common.removeFrameAfterQuarryIsRemoved.set(true);
            var base = GameTestUtil.getBasePos(helper).above();
            var frame1 = base.east();
            var frame2 = frame1.above();
            var frame3 = frame2.above();
            helper.setBlock(frame1.east(), Blocks.WATER);
            helper.setBlock(frame1, Holder.BLOCK_FRAME.getDammingState());
            helper.setBlock(frame2, Holder.BLOCK_FRAME);
            helper.setBlock(frame3, Holder.BLOCK_FRAME);

            helper.setBlock(base, Holder.BLOCK_QUARRY);
            List.of(frame1, frame2, frame3).forEach(p -> helper.assertBlockPresent(Holder.BLOCK_FRAME, p));

            helper.destroyBlock(base);
            List.of(frame2, frame3).forEach(p -> helper.assertBlockPresent(Blocks.AIR, p));
            helper.assertBlockNotPresent(Holder.BLOCK_QUARRY, base);
            helper.assertBlockProperty(frame1, FrameBlock.DAMMING, true);
            helper.succeed();
        } finally {
            QuarryPlus.config.common.removeFrameAfterQuarryIsRemoved.set(before);
        }
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE)
    public void chainBreakDisabled(GameTestHelper helper) {
        var before = QuarryPlus.config.common.removeFrameAfterQuarryIsRemoved.get();
        try {
            QuarryPlus.config.common.removeFrameAfterQuarryIsRemoved.set(false);
            var base = GameTestUtil.getBasePos(helper).above();
            var frame1 = base.east();
            var frame2 = frame1.above();
            var frame3 = frame2.above();
            helper.setBlock(frame1, Holder.BLOCK_FRAME);
            helper.setBlock(frame2, Holder.BLOCK_FRAME);
            helper.setBlock(frame3, Holder.BLOCK_FRAME);

            helper.setBlock(base, Holder.BLOCK_QUARRY);
            List.of(frame1, frame2, frame3).forEach(p -> helper.assertBlockPresent(Holder.BLOCK_FRAME, p));

            helper.destroyBlock(base);
            List.of(frame1, frame2, frame3).forEach(p -> helper.assertBlockNotPresent(Blocks.AIR, p));
            helper.assertBlockNotPresent(Holder.BLOCK_QUARRY, base);
            helper.succeed();
        } finally {
            QuarryPlus.config.common.removeFrameAfterQuarryIsRemoved.set(before);
        }
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE)
    public void placeEnchantedQuarry1(GameTestHelper helper) {
        var stack = new ItemStack(Holder.BLOCK_QUARRY);
        stack.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
        stack.enchant(Enchantments.UNBREAKING, 3);
        stack.enchant(Enchantments.SILK_TOUCH, 1);

        BlockPos pos = GameTestUtil.getBasePos(helper).above();
        BlockPos absolutePos = helper.absolutePos(pos);
        BlockHitResult hitResult = new BlockHitResult(Vec3.upFromBottomCenterOf(absolutePos, 1), Direction.UP, absolutePos, false);
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        UseOnContext context = new UseOnContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, stack, hitResult);
        stack.useOn(context);

        helper.assertBlockPresent(Holder.BLOCK_QUARRY, pos);
        var tile = helper.getBlockEntity(pos);
        if (tile instanceof TileQuarry quarry) {
            assertAll(
                () -> assertEquals(5, quarry.efficiencyLevel()),
                () -> assertEquals(3, quarry.unbreakingLevel()),
                () -> assertEquals(1, quarry.silktouchLevel())
            );
            helper.succeed();
        } else {
            fail("Quarry is not present at %s, %s".formatted(pos, tile));
        }
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE)
    public void placeEnchantedQuarry2(GameTestHelper helper) {
        var stack = new ItemStack(Holder.BLOCK_QUARRY);
        stack.enchant(Enchantments.BLOCK_EFFICIENCY, 10);
        stack.enchant(Enchantments.UNBREAKING, 5);
        stack.enchant(Enchantments.BLOCK_FORTUNE, 8);
        stack.enchant(Enchantments.MOB_LOOTING, 4);

        BlockPos pos = GameTestUtil.getBasePos(helper).above();
        BlockPos absolutePos = helper.absolutePos(pos);
        BlockHitResult hitResult = new BlockHitResult(Vec3.upFromBottomCenterOf(absolutePos, 1), Direction.UP, absolutePos, false);
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        UseOnContext context = new UseOnContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, stack, hitResult);
        stack.useOn(context);

        helper.assertBlockPresent(Holder.BLOCK_QUARRY, pos);
        var tile = helper.getBlockEntity(pos);
        if (tile instanceof TileQuarry quarry) {
            assertAll(
                () -> assertEquals(10, quarry.efficiencyLevel()),
                () -> assertEquals(5, quarry.unbreakingLevel()),
                () -> assertEquals(8, quarry.fortuneLevel()),
                () -> assertEquals(4, quarry.getLevel(Enchantments.MOB_LOOTING))
            );
            helper.succeed();
        } else {
            fail("Quarry is not present at %s, %s".formatted(pos, tile));
        }
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE)
    public void placeMiningWell0(GameTestHelper helper) {
        var stack = new ItemStack(Holder.BLOCK_MINING_WELL);
        BlockPos pos = GameTestUtil.getBasePos(helper).above();
        BlockPos absolutePos = helper.absolutePos(pos);
        BlockHitResult hitResult = new BlockHitResult(Vec3.upFromBottomCenterOf(absolutePos, 1), Direction.UP, absolutePos, false);
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        UseOnContext context = new UseOnContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, stack, hitResult);
        stack.useOn(context);

        helper.assertBlockPresent(Holder.BLOCK_MINING_WELL, pos);
        var tile = helper.getBlockEntity(pos);
        if (tile instanceof MiningWellTile tile1) {
            assertEquals(0, tile1.efficiencyLevel());
            helper.succeed();
        } else {
            fail("Quarry is not present at %s, %s".formatted(pos, tile));
        }
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE)
    public void placeMiningWell2(GameTestHelper helper) {
        var stack = new ItemStack(Holder.BLOCK_MINING_WELL);
        stack.enchant(Enchantments.BLOCK_EFFICIENCY, 2);
        BlockPos pos = GameTestUtil.getBasePos(helper).above();
        BlockPos absolutePos = helper.absolutePos(pos);
        BlockHitResult hitResult = new BlockHitResult(Vec3.upFromBottomCenterOf(absolutePos, 1), Direction.UP, absolutePos, false);
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        UseOnContext context = new UseOnContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, stack, hitResult);
        stack.useOn(context);

        helper.assertBlockPresent(Holder.BLOCK_MINING_WELL, pos);
        var tile = helper.getBlockEntity(pos);
        if (tile instanceof MiningWellTile tile1) {
            assertEquals(2, tile1.efficiencyLevel());
            helper.succeed();
        } else {
            fail("Quarry is not present at %s, %s".formatted(pos, tile));
        }
    }
}
