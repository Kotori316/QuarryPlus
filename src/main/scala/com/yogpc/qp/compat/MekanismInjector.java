package com.yogpc.qp.compat;

import java.util.stream.Stream;

import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.transporter.TransitRequest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

class MekanismInjector implements IInjector {
    private final ILogisticalTransporter transporter;

    //    @Optional.Method(modid = QuarryPlus.Optionals.Mekanism_modID)
    public static MekanismInjector init() {
        return new MekanismInjector(Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY.getDefaultInstance());
    }

    private MekanismInjector(ILogisticalTransporter transporter) {
        this.transporter = transporter;
    }

    @Override
    //@Optional.Method(modid = QuarryPlus.Optionals.Mekanism_modID)
    public Stream<? extends IInjector> getInjector(ItemStack stack, TileEntity entity, Direction facing) {
        return entity.getCapability(Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, facing.getOpposite())
            .map(MekanismInjector::new)
            .map(Stream::of)
            .orElse(Stream.empty());
    }

    @Override
    //@Optional.Method(modid = QuarryPlus.Optionals.Mekanism_modID)
    public ItemStack inject(ItemStack stack, World world, BlockPos fromPos) {
        TransitRequest.TransitResponse insert = transporter.insert(world.getTileEntity(fromPos),
            TransitRequest.getFromStack(stack.copy()), transporter.getColor(), true, 0);
        return insert.getRejected(stack);
    }
}
