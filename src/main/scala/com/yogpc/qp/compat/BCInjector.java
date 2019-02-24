package com.yogpc.qp.compat;

import java.util.stream.Stream;

import buildcraft.api.inventory.IItemTransactor;
import buildcraft.lib.inventory.ItemTransactorHelper;
import buildcraft.lib.inventory.NoSpaceTransactor;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

class BCInjector implements IInjector {

    @Optional.Method(modid = QuarryPlus.Optionals.BuildCraft_core)
    public static BCInjector init() {
        return new BCInjector(NoSpaceTransactor.INSTANCE);
    }

    private final IItemTransactor transactor;

    private BCInjector(IItemTransactor transactor) {
        this.transactor = transactor;
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.BuildCraft_core)
    public Stream<? extends IInjector> getInjector(ItemStack stack, TileEntity entity, EnumFacing facing) {
        IItemTransactor transactor = ItemTransactorHelper.getTransactor(entity, facing.getOpposite());
        if (transactor != NoSpaceTransactor.INSTANCE) {
            return Stream.of(new BCInjector(transactor));
        }
        return Stream.empty();
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.BuildCraft_core)
    public ItemStack inject(ItemStack stack, World world, BlockPos fromPos) {
        return transactor.insert(stack, false, false);
    }
}
