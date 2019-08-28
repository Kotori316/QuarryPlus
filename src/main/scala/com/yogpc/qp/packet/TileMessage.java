package com.yogpc.qp.packet;

import java.util.Optional;
import java.util.function.Supplier;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * To both client and server.
 */
public class TileMessage implements IMessage<TileMessage> {
    private CompoundNBT compound;

    public static TileMessage create(TileEntity entity) {
        TileMessage message = new TileMessage();
        message.compound = entity.write(new CompoundNBT());
        return message;
    }

    @Override
    public TileMessage readFromBuffer(PacketBuffer buffer) {
        compound = buffer.readCompoundTag();
        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeCompoundTag(compound);
    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        BlockPos pos = new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z"));
        Optional<World> worldOptional = QuarryPlus.proxy.getPacketWorld(ctx.get()).filter(world -> world.isBlockLoaded(pos));
        Runnable runnable = () -> worldOptional.map(world -> world.getTileEntity(pos)).ifPresent(entity -> entity.read(compound));
        ctx.get().enqueueWork(runnable);
    }

}
