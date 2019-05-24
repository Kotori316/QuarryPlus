package com.yogpc.qp.packet.advquarry;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.yogpc.qp.machines.advquarry.TileAdvQuarry;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * To Server only.
 */
public class AdvActionMessage implements IMessage<AdvActionMessage> {
    private BlockPos pos;
    private int dim;
    private Actions action;
    private NBTTagCompound tag;

    public static AdvActionMessage create(TileAdvQuarry quarry, Actions action) {
        return create(quarry, action, new NBTTagCompound());
    }

    public static AdvActionMessage create(TileAdvQuarry quarry, Actions action, NBTTagCompound compound) {
        AdvActionMessage message = new AdvActionMessage();
        message.pos = quarry.getPos();
        message.dim = IMessage.getDimId(quarry.getWorld());
        message.action = action;
        message.tag = compound;
        return message;
    }

    @Override
    public AdvActionMessage readFromBuffer(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        dim = buffer.readInt();
        action = buffer.readEnumValue(Actions.class);
        tag = buffer.readCompoundTag();
        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim);
        buffer.writeEnumValue(action);
        buffer.writeCompoundTag(tag);
    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        IMessage.findTile(ctx, pos, dim, TileAdvQuarry.class)
            .ifPresent(quarry -> ctx.get().enqueueWork(action.runnable(quarry, tag)));
    }

    public enum Actions {
        QUICK_START(TileAdvQuarry::noFrameStart),
        CHANGE_RANGE((quarry, rangeNBT) ->
            quarry.digRange_$eq(TileAdvQuarry.DigRange$.MODULE$.readFromNBT(rangeNBT))
        );
        private final BiConsumer<TileAdvQuarry, NBTTagCompound> consumer;

        Actions(Consumer<TileAdvQuarry> consumer) {
            this.consumer = (quarry, nbtTagCompound) -> consumer.accept(quarry);
        }

        Actions(BiConsumer<TileAdvQuarry, NBTTagCompound> consumer) {
            this.consumer = consumer;
        }

        Runnable runnable(TileAdvQuarry quarry, NBTTagCompound compound) {
            return () -> consumer.accept(quarry, compound);
        }
    }
}
