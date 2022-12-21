package com.yogpc.qp.machines.placer;

import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.kotori316.testutil.GameTestUtil;

import static com.yogpc.qp.machines.placer.PlacerGameTest.BATCH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@GameTestHolder(QuarryPlus.modID)
@PrefixGameTestTemplate(value = false)
public final class RemotePlacerGameTest {
    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE, batch = BATCH)
    public void placeRemotePlacerBlock(GameTestHelper helper) {
        var pos = GameTestUtil.getBasePos(helper).above();
        helper.setBlock(pos, Holder.BLOCK_REMOTE_PLACER);
        helper.assertBlockPresent(Holder.BLOCK_REMOTE_PLACER, pos);
        var tile = helper.getBlockEntity(pos);
        assertNotNull(tile);
        assertEquals(RemotePlacerTile.class, tile.getClass());
        helper.succeed();
    }

    @GameTestGenerator
    public List<TestFunction> breakBlock() {
        return StreamSupport.stream(BlockPos.randomBetweenClosed(RandomSource.create(), 4, 1, 2, 3, 4, 5, 6).spliterator(), false)
            .map(p -> GameTestUtil.create(QuarryPlus.modID, BATCH, "RemoteBreak(%s)".formatted(p), g -> breakBlock(g, p)))
            .toList();
    }

    private void breakBlock(GameTestHelper helper, BlockPos offset) {
        var placerPos = GameTestUtil.getBasePos(helper).above();
        var targetPos = GameTestUtil.getBasePos(helper).offset(offset);
        helper.setBlock(placerPos, Holder.BLOCK_REMOTE_PLACER);
        var tile = Objects.requireNonNull((RemotePlacerTile) helper.getBlockEntity(placerPos));
        helper.startSequence()
            .thenExecuteAfter(1, () -> helper.setBlock(targetPos, Blocks.STONE))
            .thenExecuteAfter(1, () -> tile.targetPos = targetPos)
            .thenExecuteAfter(1, tile::breakBlock)
            .thenExecuteAfter(1, () -> helper.assertBlockNotPresent(Blocks.STONE, targetPos))
            .thenExecute(() -> assertTrue(tile.countItem(Items.STONE) > 0))
            .thenSucceed();
    }

    @GameTestGenerator
    public List<TestFunction> placeBlock() {
        return StreamSupport.stream(BlockPos.randomBetweenClosed(RandomSource.create(), 4, 1, 2, 3, 4, 5, 6).spliterator(), false)
            .map(p -> GameTestUtil.create(QuarryPlus.modID, BATCH, "PlaceBlock(%s)".formatted(p), g -> placeBlock(g, p)))
            .toList();
    }

    void placeBlock(GameTestHelper helper, BlockPos offset) {
        var placerPos = GameTestUtil.getBasePos(helper).above();
        var targetPos = GameTestUtil.getBasePos(helper).offset(offset);
        helper.setBlock(placerPos, Holder.BLOCK_REMOTE_PLACER);
        var tile = Objects.requireNonNull((RemotePlacerTile) helper.getBlockEntity(placerPos));
        helper.startSequence()
            .thenExecuteAfter(1, () -> tile.targetPos = targetPos)
            .thenExecuteAfter(1, () -> tile.setItem(0, new ItemStack(Blocks.STONE)))
            .thenExecute(() -> assertEquals(1, tile.countItem(Items.STONE)))
            .thenExecuteAfter(1, () -> assertTrue(tile.placeBlock()))
            .thenExecuteAfter(1, () -> helper.assertBlockPresent(Blocks.STONE, targetPos))
            .thenExecute(() -> assertEquals(0, tile.countItem(Items.STONE), "Tile contains: %s".formatted(tile.getItem(0))))
            .thenSucceed();
    }
}
