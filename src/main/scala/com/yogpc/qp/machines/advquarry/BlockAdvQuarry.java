package com.yogpc.qp.machines.advquarry;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.MachineStorage;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.machines.module.EnergyModuleItem;
import com.yogpc.qp.utils.CombinedBlockEntityTicker;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class BlockAdvQuarry extends QPBlock implements EntityBlock {
    public static final String NAME = "adv_quarry";

    public BlockAdvQuarry() {
        super(Properties.of(Material.METAL)
            .strength(1.5f, 10f)
            .sound(SoundType.STONE), NAME, ItemAdvQuarry::new);
        registerDefaultState(getStateDefinition().any()
            .setValue(WORKING, false)
            .setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return Holder.ADV_QUARRY_TYPE.create(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WORKING, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction facing = ctx.getPlayer() == null ? Direction.NORTH : ctx.getPlayer().getDirection().getOpposite();
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        super.neighborChanged(state, world, pos, block, fromPos, notify);
        if (!world.isClientSide) {
            world.getBlockEntity(pos, Holder.ADV_QUARRY_TYPE).ifPresent(TileAdvQuarry::updateModule);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : checkType(type, Holder.ADV_QUARRY_TYPE,
            new CombinedBlockEntityTicker<>(
                PowerTile.getGenerator(),
                EnergyModuleItem.energyModuleTicker(),
                TileAdvQuarry::tick,
                PowerTile.logTicker(),
                MachineStorage.passItems(),
                MachineStorage.passFluid())
        );
    }
}
