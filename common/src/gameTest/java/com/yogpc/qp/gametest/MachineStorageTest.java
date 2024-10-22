package com.yogpc.qp.gametest;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.MachineStorageHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.entity.BlockEntity;

import static org.junit.jupiter.api.Assertions.*;

public final class MachineStorageTest {
    public static void create(GameTestHelper helper) {
        var storage = assertDoesNotThrow(MachineStorage::of);
        assertNotNull(storage);
        helper.succeed();
    }

    public static void debugStorage(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        helper.setBlock(pos, PlatformAccess.getAccess().registerObjects().debugStorageBlock().get());
        BlockEntity blockEntity = helper.getBlockEntity(pos);
        var holder = MachineStorageHolder.getHolder(blockEntity);
        assertTrue(holder.isPresent());
        helper.succeed();
    }

    public static void quarry(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        helper.setBlock(pos, PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        BlockEntity blockEntity = helper.getBlockEntity(pos);
        var holder = MachineStorageHolder.getHolder(blockEntity);
        assertTrue(holder.isPresent());
        helper.succeed();
    }

    public static void advQuarry(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        helper.setBlock(pos, PlatformAccess.getAccess().registerObjects().advQuarryBlock().get());
        BlockEntity blockEntity = helper.getBlockEntity(pos);
        var holder = MachineStorageHolder.getHolder(blockEntity);
        assertTrue(holder.isPresent());
        helper.succeed();
    }
}
