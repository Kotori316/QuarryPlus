package com.yogpc.qp.machine;

import com.yogpc.qp.FluidStackLike;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.storage.DebugStorageBlock;
import com.yogpc.qp.machine.storage.DebugStorageEntity;
import com.yogpc.qp.machine.storage.DebugStorageHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

import static org.junit.jupiter.api.Assertions.*;

public final class DebugStorageTest {
    static final BlockPos base = BlockPos.ZERO.above();

    public static void place(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> helper.setBlock(base, PlatformAccess.getAccess().registerObjects().debugStorageBlock().get()))
            .thenExecuteAfter(1, () ->
                assertInstanceOf(DebugStorageBlock.class, helper.getBlockState(base).getBlock())
            )
            .thenExecuteAfter(1, () ->
                assertInstanceOf(DebugStorageEntity.class, helper.getBlockEntity(base))
            )
            .thenSucceed();
    }

    public static void firstEmpty(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> helper.setBlock(base, PlatformAccess.getAccess().registerObjects().debugStorageBlock().get()))
            .thenExecuteAfter(1, () -> {
                DebugStorageEntity entity = helper.getBlockEntity(base);
                assertAll(
                    () -> assertTrue(entity.enabled),
                    () -> assertTrue(getStorage(entity).items.isEmpty()),
                    () -> assertTrue(getStorage(entity).fluids.isEmpty())
                );
            }).thenSucceed();
    }

    public static void addItem(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> helper.setBlock(base, PlatformAccess.getAccess().registerObjects().debugStorageBlock().get()))
            .thenExecuteAfter(1, () -> PlatformAccess.getAccess().transfer().transferItem(helper.getLevel(), helper.absolutePos(base), new ItemStack(Items.APPLE, 3), Direction.UP, false))
            .thenExecuteAfter(1, () -> {
                DebugStorageEntity entity = helper.getBlockEntity(base);
                var storage = getStorage(entity);
                assertAll(
                    () -> assertEquals(3, storage.items.getLong(MachineStorage.ItemKey.of(new ItemStack(Items.APPLE)))),
                    () -> assertEquals(0, storage.items.getLong(MachineStorage.ItemKey.of(new ItemStack(Items.GOLD_BLOCK)))),
                    () -> assertFalse(storage.items.isEmpty()),
                    () -> assertTrue(storage.fluids.isEmpty())
                );
            })
            .thenSucceed();
    }

    public static void addFluid(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> helper.setBlock(base, PlatformAccess.getAccess().registerObjects().debugStorageBlock().get()))
            .thenExecuteAfter(1, () -> PlatformAccess.getAccess().transfer().transferFluid(helper.getLevel(), helper.absolutePos(base), new FluidStackLike(Fluids.WATER, MachineStorage.ONE_BUCKET * 4, DataComponentPatch.EMPTY), Direction.UP, false))
            .thenExecuteAfter(1, () -> {
                DebugStorageEntity entity = helper.getBlockEntity(base);
                var storage = getStorage(entity);
                assertAll(
                    () -> assertEquals(0, storage.items.getLong(MachineStorage.ItemKey.of(new ItemStack(Items.APPLE)))),
                    () -> assertEquals(MachineStorage.ONE_BUCKET * 4, storage.fluids.getLong(new MachineStorage.FluidKey(Fluids.WATER, DataComponentPatch.EMPTY))),
                    () -> assertEquals(0, storage.fluids.getLong(new MachineStorage.FluidKey(Fluids.LAVA, DataComponentPatch.EMPTY))),
                    () -> assertFalse(storage.fluids.isEmpty()),
                    () -> assertTrue(storage.items.isEmpty())
                );
            })
            .thenSucceed();
    }

    private static MachineStorage getStorage(DebugStorageEntity entity) {
        var holder = new DebugStorageHolder();
        return holder.getMachineStorage(entity);
    }
}
