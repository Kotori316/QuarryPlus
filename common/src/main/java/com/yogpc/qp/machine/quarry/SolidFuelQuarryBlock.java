package com.yogpc.qp.machine.quarry;

import com.yogpc.qp.machine.CombinedBlockEntityTicker;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.PowerEntity;
import com.yogpc.qp.machine.QpBlock;
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

    protected SolidFuelQuarryBlock(Function<QpBlock, ? extends QuarryItem> itemGenerator) {
        super(NAME, itemGenerator);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
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
     * Enchantments are not supported in Solid Fuel Quarry, so return this item only.
     */
    @Override
    public Stream<ItemStack> creativeTabItem(CreativeModeTab.ItemDisplayParameters parameters) {
        return Stream.of(new ItemStack(this));
    }
}
