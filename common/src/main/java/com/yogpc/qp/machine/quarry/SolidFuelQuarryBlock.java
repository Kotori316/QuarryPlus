package com.yogpc.qp.machine.quarry;

import com.yogpc.qp.machine.CombinedBlockEntityTicker;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.PowerEntity;
import com.yogpc.qp.machine.QpBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.stream.Stream;

public abstract class SolidFuelQuarryBlock extends QuarryBlock {
    public static final String NAME = "solid_fuel_quarry";

    protected SolidFuelQuarryBlock(Function<QpBlock, ? extends BlockItem> itemGenerator) {
        super(NAME, itemGenerator);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return createTickerHelper(blockEntityType, this.<SolidFuelQuarryEntity>getBlockEntityType().orElse(null), CombinedBlockEntityTicker.of(this, level,
                QuarryEntity::clientTick
            ));
        }
        return createTickerHelper(blockEntityType, this.<SolidFuelQuarryEntity>getBlockEntityType().orElse(null), CombinedBlockEntityTicker.of(this, level,
            SolidFuelQuarryEntity::tickFuel,
            PowerEntity.logTicker(),
            QuarryEntity::serverTick,
            MachineStorage.pushItemTicker(),
            MachineStorage.pushFluidTicker()
        ));
    }

    /**
     * To drop items in fuel slots
     */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof SolidFuelQuarryEntity entity) {
                Containers.dropContents(level, pos, entity.fuelContainer);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    /**
     * Enchantments are not supported in Solid Fuel Quarry, so return this item only.
     */
    @Override
    public Stream<ItemStack> creativeTabItem(CreativeModeTab.ItemDisplayParameters parameters) {
        return Stream.of(new ItemStack(this));
    }
}
