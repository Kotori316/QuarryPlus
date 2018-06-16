package com.yogpc.qp.packet.advquarry;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileAdvQuarry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import scala.collection.convert.WrapAsJava$;

/**
 * To Client only
 */
public class AdvContentMessage implements IMessage {

    private BlockPos pos;
    private int dim;
    private Map<FluidStack, FluidTank> map;

    public static AdvContentMessage create(TileAdvQuarry quarry) {
        AdvContentMessage message = new AdvContentMessage();
        message.pos = quarry.getPos();
        message.dim = quarry.getWorld().provider.getDimension();
        message.map = WrapAsJava$.MODULE$.mutableMapAsJavaMap(quarry.fluidStacks());
        return message;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) throws IOException {
        pos = buffer.readBlockPos();
        dim = buffer.readInt();
        map = new HashMap<>();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            FluidStack stack = FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag());
            FluidTank tank = new FluidTank(null, 0);
            Optional.ofNullable(buffer.readCompoundTag()).ifPresent(tank::readFromNBT);
            if (tank.getFluidAmount() != 0) {
                map.put(stack, tank);
            }
        }
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim);
        buffer.writeInt(map.size());
        map.forEach((fluidStack, fluidTank) -> {
            buffer.writeCompoundTag(fluidStack.writeToNBT(new NBTTagCompound()));
            buffer.writeCompoundTag(fluidTank.writeToNBT(new NBTTagCompound()));
        });
    }

    @Override
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        if (world.provider.getDimension() == dim) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() ->
                Optional.ofNullable((TileAdvQuarry) world.getTileEntity(pos)).ifPresent(quarry -> {
                    quarry.fluidStacks().clear();
                    map.forEach((fluidStack, fluidTank) -> quarry.fluidStacks().put(fluidStack, fluidTank));
                }));
        }
        return null;
    }
}
