package com.yogpc.qp.packet.advquarry;

import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.yogpc.qp.machines.advquarry.TileAdvQuarry;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.utils.NBTBuilder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkEvent;
import scala.collection.JavaConverters;
import scala.collection.mutable.Set;

import static jp.t2v.lab.syntax.MapStreamSyntax.keys;
import static jp.t2v.lab.syntax.MapStreamSyntax.values;

/**
 * To both client and server.
 */
public class AdvFilterMessage implements IMessage<AdvFilterMessage> {

    private int dim;
    private BlockPos pos;
    private NBTTagCompound filter;

    public static AdvFilterMessage create(TileAdvQuarry quarry) {
        AdvFilterMessage message = new AdvFilterMessage();
        message.dim = IMessage.getDimId(quarry.getWorld());
        message.pos = quarry.getPos();
        message.filter = JavaConverters.mapAsJavaMap(quarry.fluidExtractFacings()).entrySet().stream()
            .map(values(s -> JavaConverters.setAsJavaSet(s).stream()
                .map(f -> f.writeToNBT(new NBTTagCompound()))
                .collect(Collectors.toCollection(NBTTagList::new))))
            .map(keys(EnumFacing::toString))
            .collect(NBTBuilder.toNBTTag());
        return message;
    }

    @Override
    public AdvFilterMessage readFromBuffer(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        dim = buffer.readInt();
        filter = buffer.readCompoundTag();
        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim);
        buffer.writeCompoundTag(filter);
    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        IMessage.findTile(ctx, pos, dim, TileAdvQuarry.class)
            .ifPresent(quarry -> ctx.get().enqueueWork(() -> {
                for (EnumFacing facing : EnumFacing.values()) {
                    Set<FluidStack> set = quarry.fluidExtractFacings().apply(facing);
                    set.clear();
                    filter.getList(facing.toString(), Constants.NBT.TAG_COMPOUND).stream()
                        .map(b -> (NBTTagCompound) b)
                        .map(FluidStack::loadFluidStackFromNBT).forEach(set::add);
                }
            }));
    }
}
