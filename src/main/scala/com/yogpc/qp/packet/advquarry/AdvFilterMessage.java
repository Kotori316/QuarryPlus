package com.yogpc.qp.packet.advquarry;

import java.io.IOException;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileAdvQuarry;
import com.yogpc.qp.version.VersionUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import scala.collection.convert.WrapAsJava$;
import scala.collection.mutable.Set;

/**
 * To both client and server.
 */
public class AdvFilterMessage implements IMessage {

    private int dim;
    private BlockPos pos;
    private NBTTagCompound filter;

    public static AdvFilterMessage create(TileAdvQuarry quarry) {
        AdvFilterMessage message = new AdvFilterMessage();
        message.dim = quarry.getWorld().provider.getDimension();
        message.pos = quarry.getPos();
        NBTTagCompound nbt = new NBTTagCompound();
        WrapAsJava$.MODULE$.mapAsJavaMap(quarry.fluidExtractFacings()).forEach((facing, fluidStackSet) -> {
            NBTTagList list = new NBTTagList();
            WrapAsJava$.MODULE$.setAsJavaSet(fluidStackSet).stream()
                .map(s -> s.writeToNBT(new NBTTagCompound()))
                .forEach(list::appendTag);
            nbt.setTag(facing.toString(), list);
        });
        message.filter = nbt;
        return message;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) throws IOException {
        pos = buffer.readBlockPos();
        dim = buffer.readInt();
        filter = buffer.readCompoundTag();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim);
        buffer.writeCompoundTag(filter);
    }

    @Override
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        TileAdvQuarry quarry = ((TileAdvQuarry) world.getTileEntity(pos));
        if (world.provider.getDimension() == dim && quarry != null) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                for (EnumFacing facing : EnumFacing.VALUES) {
                    Set<FluidStack> set = quarry.fluidExtractFacings().apply(facing);
                    set.clear();
                    VersionUtil.nbtListStream(filter.getTagList(facing.toString(), Constants.NBT.TAG_COMPOUND))
                        .map(FluidStack::loadFluidStackFromNBT).forEach(set::add);
                }
            });
        }
        return null;
    }
}
