package com.yogpc.qp.machines.placer;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(QuarryPlusTest.class)
class RemotePlacerTileTest {
    @Test
    void instance() {
        assertDoesNotThrow(() -> new RemotePlacerTile(BlockPos.ZERO, Holder.BLOCK_REMOTE_PLACER.defaultBlockState()));
    }

    @Test
    void initialTargetPos() {
        var placer = new RemotePlacerTile(new BlockPos(3, 67, 2), Holder.BLOCK_REMOTE_PLACER.defaultBlockState());
        assertEquals(new BlockPos(3, 68, 2), placer.getTargetPos());
    }

    @Test
    void saveAndLoad() {
        var placer = new RemotePlacerTile(BlockPos.ZERO, Holder.BLOCK_REMOTE_PLACER.defaultBlockState());
        placer.targetPos = new BlockPos(5, 7, 9);
        var tag = placer.saveWithoutMetadata();

        var p2 = new RemotePlacerTile(BlockPos.ZERO.above(), Holder.BLOCK_REMOTE_PLACER.defaultBlockState());
        p2.load(tag);
        assertEquals(new BlockPos(5, 7, 9), placer.targetPos);
        assertEquals(new BlockPos(5, 7, 9), p2.targetPos);
    }
}
