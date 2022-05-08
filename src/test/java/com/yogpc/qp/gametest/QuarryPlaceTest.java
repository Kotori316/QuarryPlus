package com.yogpc.qp.gametest;

import java.util.List;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.quarry.FrameBlock;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.kotori316.testutil.GameTestUtil;

@GameTestHolder(QuarryPlus.modID)
@PrefixGameTestTemplate(value = false)
public final class QuarryPlaceTest {
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
}
