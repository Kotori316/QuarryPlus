package com.yogpc.qp.gametest;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestDontPrefix;
import net.minecraftforge.gametest.GameTestHolder;
import org.junit.jupiter.api.Assertions;

@GameTestHolder(QuarryPlus.modID)
@GameTestDontPrefix
public final class PumpTest {
    @GameTest(template = "pump_test")
    public void runAdvancedPump(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> helper.setBlock(new BlockPos(2, 5, 2), Holder.BLOCK_ADV_PUMP))
            .thenExecuteAfter(1, () -> helper.setBlock(new BlockPos(3, 5, 2), Holder.BLOCK_CREATIVE_GENERATOR))
            .thenIdle(20)
            .thenExecute(() -> Assertions.assertAll(
                BlockPos.betweenClosedStream(1, 1, 1, 4, 3, 4)
                    .map(p -> () -> Assertions.assertTrue(helper.getBlockState(p).getFluidState().isEmpty()))
            ))
            .thenSucceed();
    }
}
