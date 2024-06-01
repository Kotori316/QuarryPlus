package com.yogpc.qp.gametest;

import com.kotori316.testutil.GameTestUtil;
import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;

import java.util.stream.IntStream;
import java.util.stream.Stream;

@GameTestHolder(QuarryPlus.modID)
public final class ChainBreakTest {
    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE)
    void removeOne(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        helper.startSequence()
            .thenExecute(() -> helper.setBlock(pos, Holder.BLOCK_DUMMY))
            .thenExecuteAfter(1, () -> helper.destroyBlock(pos))
            .thenExecuteAfter(1, () -> helper.assertBlockNotPresent(Holder.BLOCK_DUMMY, pos))
            .thenSucceed();
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE)
    void removeMany(GameTestHelper helper) {
        var poses = Stream.of(new BlockPos(4, 4, 4))
            .flatMap(p ->
                IntStream.rangeClosed(-2, 2).boxed().flatMap(dx ->
                    IntStream.rangeClosed(-2, 2).boxed().flatMap(dy ->
                        IntStream.rangeClosed(-2, 2).boxed().map(dz -> p.offset(dx, dy, dz))
                    )
                )
            ).toList();
        helper.startSequence()
            .thenExecute(() -> poses.forEach(pos -> helper.setBlock(pos, Holder.BLOCK_DUMMY)))
            .thenExecuteAfter(1, () -> helper.destroyBlock(poses.get(0)))
            .thenExecuteAfter(10, () -> poses.forEach(pos -> helper.assertBlockNotPresent(Holder.BLOCK_DUMMY, pos)))
            .thenSucceed();
    }
}
