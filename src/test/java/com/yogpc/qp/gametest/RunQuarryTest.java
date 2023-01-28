package com.yogpc.qp.gametest;

import java.util.Map;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QuarryMarker;
import com.yogpc.qp.machines.quarry.QuarryState;
import com.yogpc.qp.machines.quarry.TileQuarry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@GameTestHolder(QuarryPlus.modID)
@PrefixGameTestTemplate(value = false)
final class RunQuarryTest {
    private static final String BATCH_NORMAL_RUN = "runQuarryTestNormal";

    @GameTest(template = "quarry_run_pump", batch = BATCH_NORMAL_RUN + "1")
    void runQuarryWithPumpModule(GameTestHelper helper) {
        var basePos = BlockPos.ZERO.above();
        var markerPos = basePos.offset(0, 5, 1);
        var quarryPos = basePos.offset(0, 5, 0);
        helper.useBlock(markerPos);
        var marker = (QuarryMarker) helper.getBlockEntity(markerPos);
        assertNotNull(marker, "Marker is not present at %s".formatted(markerPos));
        assertTrue(marker.getArea().isPresent());

        helper.setBlock(quarryPos, Holder.BLOCK_QUARRY.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.NORTH));
        var quarry = (TileQuarry) helper.getBlockEntity(quarryPos);
        assertNotNull(quarry);
        quarry.getModuleInventory().addItem(new ItemStack(Holder.ITEM_PUMP_MODULE));
        assertTrue(quarry.hasPumpModule());
        quarry.setArea(marker.getArea().get());
        quarry.digMinY = helper.absolutePos(basePos).getY();
        quarry.setEnchantments(Map.of(Enchantments.BLOCK_EFFICIENCY, 5));
        helper.startSequence()
            .thenExecuteAfter(1, () -> assertNotNull(quarry.getArea()))
            .thenExecute(() -> helper.setBlock(quarryPos.below(), Holder.BLOCK_CREATIVE_GENERATOR))
            .thenExecute(() -> quarry.setState(QuarryState.MOVE_HEAD, helper.getBlockState(quarryPos)))
            .thenIdle(50)
            .thenExecute(() -> helper.forEveryBlockInStructure(p -> helper.assertBlockNotPresent(Blocks.WATER, p)))
            .thenExecute(marker::removeAndGetItems)
            .thenSucceed();
    }

    @GameTest(template = "quarry_run_pump", batch = BATCH_NORMAL_RUN + "2")
    void runQuarryWithPumpBlock(GameTestHelper helper) {
        var basePos = BlockPos.ZERO.above();
        var markerPos = basePos.offset(0, 5, 1);
        var quarryPos = basePos.offset(0, 5, 0);
        helper.useBlock(markerPos);
        var marker = (QuarryMarker) helper.getBlockEntity(markerPos);
        assertNotNull(marker, "Marker is not present at %s".formatted(markerPos));
        assertTrue(marker.getArea().isPresent());

        helper.setBlock(quarryPos.east(), Holder.BLOCK_PUMP);
        helper.setBlock(quarryPos, Holder.BLOCK_QUARRY.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.NORTH));
        var quarry = (TileQuarry) helper.getBlockEntity(quarryPos);
        assertNotNull(quarry);
        assertTrue(quarry.hasPumpModule());
        quarry.setArea(marker.getArea().get());
        quarry.digMinY = helper.absolutePos(basePos).getY();
        quarry.setEnchantments(Map.of(Enchantments.BLOCK_EFFICIENCY, 5));
        helper.startSequence()
            .thenExecuteAfter(1, () -> assertNotNull(quarry.getArea()))
            .thenExecute(() -> helper.setBlock(quarryPos.below(), Holder.BLOCK_CREATIVE_GENERATOR))
            .thenExecute(() -> quarry.setState(QuarryState.MOVE_HEAD, helper.getBlockState(quarryPos)))
            .thenIdle(50)
            .thenExecute(() -> helper.forEveryBlockInStructure(p -> helper.assertBlockNotPresent(Blocks.WATER, p)))
            .thenExecute(marker::removeAndGetItems)
            .thenSucceed();
    }
}
