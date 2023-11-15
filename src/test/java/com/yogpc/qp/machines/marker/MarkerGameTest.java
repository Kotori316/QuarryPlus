package com.yogpc.qp.machines.marker;

import com.kotori316.testutil.GameTestUtil;
import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Area;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestDontPrefix;
import net.neoforged.neoforge.gametest.GameTestHolder;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.kotori316.testutil.GameTestUtil.EMPTY_STRUCTURE;
import static org.junit.jupiter.api.Assertions.*;

@GameTestHolder(QuarryPlus.modID)
@GameTestDontPrefix
public final class MarkerGameTest {
    public static final String BATCH = "MarkerTest";
    private static final AtomicInteger COUNT = new AtomicInteger(1);

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void clickCenter(GameTestHelper helper) {
        var pos1 = GameTestUtil.getBasePos(helper).above(COUNT.getAndIncrement());
        var pos2 = pos1.offset(4, 0, 0);
        var pos3 = pos1.offset(0, 0, 6);
        helper.setBlock(pos1, Holder.BLOCK_MARKER.defaultBlockState());
        helper.setBlock(pos2, Holder.BLOCK_MARKER.defaultBlockState());
        helper.setBlock(pos3, Holder.BLOCK_MARKER.defaultBlockState());

        var centerTile = (TileMarker) helper.getBlockEntity(pos1);
        assertNotNull(centerTile);
        assertTrue(centerTile.getArea().isEmpty());

        centerTile.tryConnect(true);
        var area = new Area(helper.absolutePos(pos1), helper.absolutePos(pos1).offset(4, 4, 6), Direction.UP);
        assertEquals(Optional.of(area), centerTile.getArea());
        assertEquals(Optional.of(area), ((TileMarker) Objects.requireNonNull(helper.getBlockEntity(pos2))).getArea());
        assertEquals(Optional.of(area), ((TileMarker) Objects.requireNonNull(helper.getBlockEntity(pos3))).getArea());
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void clickCenter2(GameTestHelper helper) {
        var pos1 = GameTestUtil.getBasePos(helper).above(COUNT.getAndIncrement());
        var pos2 = pos1.offset(4, 0, 0);
        var pos3 = pos1.offset(0, 0, 6);
        helper.setBlock(pos1, Holder.BLOCK_MARKER.defaultBlockState());
        helper.setBlock(pos2, Holder.BLOCK_WATERLOGGED_MARKER.defaultBlockState());
        helper.setBlock(pos3, Holder.BLOCK_WATERLOGGED_MARKER.defaultBlockState());

        var centerTile = (TileMarker) helper.getBlockEntity(pos1);
        assertNotNull(centerTile);
        assertTrue(centerTile.getArea().isEmpty());

        centerTile.tryConnect(true);
        var area = new Area(helper.absolutePos(pos1), helper.absolutePos(pos1).offset(4, 4, 6), Direction.UP);
        assertEquals(Optional.of(area), centerTile.getArea());
        assertEquals(Optional.of(area), ((TileMarker) Objects.requireNonNull(helper.getBlockEntity(pos2))).getArea());
        assertEquals(Optional.of(area), ((TileMarker) Objects.requireNonNull(helper.getBlockEntity(pos3))).getArea());
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void clickCenter3(GameTestHelper helper) {
        var pos1 = GameTestUtil.getBasePos(helper).above(COUNT.getAndIncrement());
        var pos2 = pos1.offset(7, 0, 0);
        var pos3 = pos1.offset(0, 0, 6);
        helper.setBlock(pos1, Holder.BLOCK_WATERLOGGED_MARKER.defaultBlockState());
        helper.setBlock(pos2, Holder.BLOCK_WATERLOGGED_MARKER.defaultBlockState());
        helper.setBlock(pos3, Holder.BLOCK_WATERLOGGED_MARKER.defaultBlockState());

        var centerTile = (TileMarker) helper.getBlockEntity(pos1);
        assertNotNull(centerTile);
        assertTrue(centerTile.getArea().isEmpty());

        centerTile.tryConnect(true);
        var area = new Area(helper.absolutePos(pos1), helper.absolutePos(pos1).offset(7, 4, 6), Direction.UP);
        assertEquals(Optional.of(area), centerTile.getArea());
        assertEquals(Optional.of(area), ((TileMarker) Objects.requireNonNull(helper.getBlockEntity(pos2))).getArea());
        assertEquals(Optional.of(area), ((TileMarker) Objects.requireNonNull(helper.getBlockEntity(pos3))).getArea());
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void clickOne1(GameTestHelper helper) {
        var pos1 = GameTestUtil.getBasePos(helper).above(COUNT.getAndIncrement());
        var pos2 = pos1.offset(4, 0, 0);
        var pos3 = pos1.offset(0, 0, 6);
        helper.setBlock(pos1, Holder.BLOCK_MARKER.defaultBlockState());
        helper.setBlock(pos2, Holder.BLOCK_MARKER.defaultBlockState());
        helper.setBlock(pos3, Holder.BLOCK_MARKER.defaultBlockState());

        var centerTile = (TileMarker) helper.getBlockEntity(pos1);
        assertNotNull(centerTile);
        assertTrue(centerTile.getArea().isEmpty());

        ((TileMarker) Objects.requireNonNull(helper.getBlockEntity(pos2))).tryConnect(true);
        var area = new Area(helper.absolutePos(pos1), helper.absolutePos(pos1).offset(4, 4, 6), Direction.UP);
        assertEquals(Optional.of(area), centerTile.getArea());
        assertEquals(Optional.of(area), ((TileMarker) Objects.requireNonNull(helper.getBlockEntity(pos2))).getArea());
        assertEquals(Optional.of(area), ((TileMarker) Objects.requireNonNull(helper.getBlockEntity(pos3))).getArea());
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void clickOne2(GameTestHelper helper) {
        var pos1 = GameTestUtil.getBasePos(helper).above(COUNT.getAndIncrement());
        var pos2 = pos1.offset(4, 0, 0);
        var pos3 = pos1.offset(0, 0, 6);
        helper.setBlock(pos1, Holder.BLOCK_MARKER.defaultBlockState());
        helper.setBlock(pos2, Holder.BLOCK_MARKER.defaultBlockState());
        helper.setBlock(pos3, Holder.BLOCK_MARKER.defaultBlockState());

        var centerTile = (TileMarker) helper.getBlockEntity(pos1);
        assertNotNull(centerTile);
        assertTrue(centerTile.getArea().isEmpty());

        ((TileMarker) Objects.requireNonNull(helper.getBlockEntity(pos3))).tryConnect(true);
        var area = new Area(helper.absolutePos(pos1), helper.absolutePos(pos1).offset(4, 4, 6), Direction.UP);
        assertEquals(Optional.of(area), centerTile.getArea());
        assertEquals(Optional.of(area), ((TileMarker) Objects.requireNonNull(helper.getBlockEntity(pos2))).getArea());
        assertEquals(Optional.of(area), ((TileMarker) Objects.requireNonNull(helper.getBlockEntity(pos3))).getArea());
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void noArea1(GameTestHelper helper) {
        var pos1 = GameTestUtil.getBasePos(helper).above(COUNT.getAndIncrement());
        var pos2 = pos1.offset(4, 0, 0);
        helper.setBlock(pos1, Holder.BLOCK_MARKER.defaultBlockState());
        helper.setBlock(pos2, Holder.BLOCK_MARKER.defaultBlockState());

        var centerTile = (TileMarker) helper.getBlockEntity(pos1);
        assertNotNull(centerTile);
        assertTrue(centerTile.getArea().isEmpty());

        centerTile.tryConnect(true);
        assertTrue(centerTile.getArea().isEmpty());
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void setY(GameTestHelper helper) {
        var pos1 = GameTestUtil.getBasePos(helper).above(COUNT.getAndIncrement());
        var pos2 = pos1.offset(2, 0, 0);
        var pos3 = pos1.offset(0, 0, 4);
        var pos4 = pos1.offset(0, 2, 0);
        helper.setBlock(pos1, Holder.BLOCK_MARKER.defaultBlockState());
        helper.setBlock(pos2, Holder.BLOCK_MARKER.defaultBlockState());
        helper.setBlock(pos3, Holder.BLOCK_MARKER.defaultBlockState());
        helper.setBlock(pos4, Holder.BLOCK_MARKER.defaultBlockState());

        var centerTile = (TileMarker) helper.getBlockEntity(pos1);
        assertNotNull(centerTile);
        assertTrue(centerTile.getArea().isEmpty());

        centerTile.tryConnect(true);
        var area = new Area(helper.absolutePos(pos1), helper.absolutePos(pos1).offset(2, 2, 4), Direction.UP);
        assertEquals(Optional.of(area), centerTile.getArea());
        assertEquals(Optional.of(area), ((TileMarker) Objects.requireNonNull(helper.getBlockEntity(pos2))).getArea());
        assertEquals(Optional.of(area), ((TileMarker) Objects.requireNonNull(helper.getBlockEntity(pos3))).getArea());
        assertEquals(Optional.of(area), ((TileMarker) Objects.requireNonNull(helper.getBlockEntity(pos4))).getArea());
        helper.succeed();
    }
}
