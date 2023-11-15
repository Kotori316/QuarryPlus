package com.yogpc.qp.machines.placer;

import com.kotori316.testutil.GameTestUtil;
import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.gametest.GameTestDontPrefix;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@GameTestHolder(QuarryPlus.modID)
@GameTestDontPrefix
public final class PlacerGameTest {
    static final String BATCH = "PlacerGameTest";

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE, batch = BATCH)
    public void placePlacerBlock(GameTestHelper helper) {
        var pos = GameTestUtil.getBasePos(helper).above();
        helper.setBlock(pos, Holder.BLOCK_PLACER);
        helper.assertBlockPresent(Holder.BLOCK_PLACER, pos);
        helper.succeed();
    }

    @GameTestGenerator
    public List<TestFunction> removeBlock() {
        return Arrays.stream(Direction.values()).map(f ->
                GameTestUtil.create(QuarryPlus.modID, BATCH, "PlacerGameTest_removeBlock_%s".formatted(f.getName()), g -> removeBlock(g, f)))
            .toList();
    }

    void removeBlock(GameTestHelper helper, Direction direction) {
        var placerPos = GameTestUtil.getBasePos(helper).offset(2, 2, 2);
        var stonePos = placerPos.relative(direction);
        helper.startSequence()
            .thenExecuteAfter(1, () -> helper.setBlock(placerPos, Holder.BLOCK_PLACER.defaultBlockState().setValue(BlockStateProperties.FACING, direction)))
            .thenExecuteAfter(1, () -> helper.setBlock(stonePos, Blocks.STONE))
            .thenExecuteAfter(1, () ->
                Optional.ofNullable((PlacerTile) helper.getBlockEntity(placerPos))
                    .ifPresent(PlacerTile::breakBlock))
            .thenExecuteAfter(1, () -> helper.assertBlockNotPresent(Blocks.STONE, stonePos))
            .thenSucceed();
    }

    @GameTestGenerator
    public List<TestFunction> placeBlockTest1() {
        return Arrays.stream(Direction.values()).map(f ->
                GameTestUtil.create(QuarryPlus.modID, BATCH, "PlacerGameTest_placeBlock_%s".formatted(f.getName()), g -> placeBlockTest1(g, f)))
            .toList();
    }

    void placeBlockTest1(GameTestHelper helper, Direction pDirection) {
        var placerPos = GameTestUtil.getBasePos(helper).offset(2, 2, 2);
        var stonePos = placerPos.relative(pDirection);
        helper.startSequence()
            .thenExecuteAfter(1, () -> helper.setBlock(placerPos, Holder.BLOCK_PLACER.defaultBlockState().setValue(BlockStateProperties.FACING, pDirection)))
            .thenExecuteAfter(1, () ->
                Optional.ofNullable((PlacerTile) helper.getBlockEntity(placerPos)).ifPresent(t -> t.setItem(0, new ItemStack(Blocks.STONE))))
            .thenExecuteAfter(1, () -> Optional.ofNullable((PlacerTile) helper.getBlockEntity(placerPos))
                .ifPresent(PlacerTile::placeBlock))
            .thenExecuteAfter(1, () -> helper.assertBlockPresent(Blocks.STONE, stonePos))
            .thenSucceed();
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE, batch = BATCH)
    public void notPlaceMode(GameTestHelper helper) {
        var placerPos = GameTestUtil.getBasePos(helper).offset(2, 2, 2);
        helper.setBlock(placerPos, Holder.BLOCK_PLACER.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.NORTH));
        var tile = Objects.requireNonNull((PlacerTile) helper.getBlockEntity(placerPos));
        tile.redstoneMode = PlacerTile.RedstoneMode.PULSE_BREAK_ONLY;
        helper.startSequence()
            .thenExecuteAfter(1, () -> tile.setItem(0, new ItemStack(Blocks.STONE)))
            .thenExecuteAfter(1, tile::placeBlock)
            .thenExecuteAfter(1, () -> helper.assertBlockNotPresent(Blocks.STONE, placerPos.relative(Direction.NORTH)))
            .thenSucceed();
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE, batch = BATCH)
    public void notBreakMode(GameTestHelper helper) {
        var placerPos = GameTestUtil.getBasePos(helper).offset(2, 2, 2);
        helper.setBlock(placerPos, Holder.BLOCK_PLACER.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.NORTH));
        helper.setBlock(placerPos.relative(Direction.NORTH), Blocks.STONE);
        var tile = Objects.requireNonNull((PlacerTile) helper.getBlockEntity(placerPos));
        tile.redstoneMode = PlacerTile.RedstoneMode.PULSE_PLACE_ONLY;
        helper.startSequence()
            .thenExecuteAfter(1, tile::breakBlock)
            .thenExecuteAfter(1, () -> helper.assertBlockPresent(Blocks.STONE, placerPos.relative(Direction.NORTH)))
            .thenSucceed();
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE, batch = BATCH)
    public void sendRSSignal(GameTestHelper helper) {
        var placerPos = GameTestUtil.getBasePos(helper).offset(2, 2, 2);
        var stonePos = placerPos.relative(Direction.NORTH);
        helper.setBlock(placerPos, Holder.BLOCK_PLACER.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.NORTH));
        var tile = Objects.requireNonNull((PlacerTile) helper.getBlockEntity(placerPos));
        helper.startSequence()
            .thenExecuteAfter(1, () -> tile.setItem(0, new ItemStack(Blocks.STONE)))
            .thenExecuteAfter(1, () -> helper.setBlock(stonePos, Blocks.STONE))
            .thenExecuteAfter(1, () -> helper.setBlock(placerPos.relative(Direction.EAST), Blocks.REDSTONE_BLOCK))
            .thenExecuteAfter(1, () -> helper.assertBlockPresent(Blocks.STONE, stonePos))
            .thenSucceed();
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE, batch = BATCH)
    public void placeInWater(GameTestHelper helper) {
        var placerPos = GameTestUtil.getBasePos(helper).offset(2, 2, 2);
        var waterPos = placerPos.above();
        helper.setBlock(placerPos, Holder.BLOCK_PLACER.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP));
        helper.setBlock(waterPos, Blocks.WATER);
        var tile = Objects.requireNonNull((PlacerTile) helper.getBlockEntity(placerPos));
        helper.startSequence()
            .thenExecuteAfter(1, () -> tile.setItem(0, new ItemStack(Blocks.STONE)))
            .thenExecuteAfter(1, tile::placeBlock)
            .thenExecuteAfter(1, () -> helper.assertBlockPresent(Blocks.STONE, placerPos.relative(Direction.UP)))
            .thenExecute(() -> Assertions.assertTrue(tile.getItem(0).isEmpty()))
            .thenSucceed();
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE, batch = BATCH)
    public void cantPlaceInSolidBlock(GameTestHelper helper) {
        var placerPos = GameTestUtil.getBasePos(helper).offset(2, 2, 2);
        var blockPos = placerPos.above();
        helper.setBlock(placerPos, Holder.BLOCK_PLACER.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP));
        helper.setBlock(blockPos, Blocks.DIAMOND_BLOCK);
        var tile = Objects.requireNonNull((PlacerTile) helper.getBlockEntity(placerPos));
        helper.startSequence()
            .thenExecuteAfter(1, () -> tile.setItem(0, new ItemStack(Blocks.STONE)))
            .thenExecuteAfter(1, tile::placeBlock)
            .thenExecuteAfter(1, () -> helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, placerPos.relative(Direction.UP)))
            .thenExecute(() -> Assertions.assertEquals(Blocks.STONE.asItem(), tile.getItem(0).getItem()))
            .thenSucceed();
    }
}
