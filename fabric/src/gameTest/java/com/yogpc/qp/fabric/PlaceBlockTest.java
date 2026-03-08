package com.yogpc.qp.fabric;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

public final class PlaceBlockTest {
    @GameTest()
    public void placeStoneBlock(GameTestHelper helper) {
        helper.setBlock(0, 1, 0, Blocks.STONE);
        helper.assertBlockState(new BlockPos(0, 1, 0), Blocks.STONE.defaultBlockState());
        helper.succeed();
    }
}
