package com.yogpc.qp.packet.advquarry;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.yogpc.qp.machines.advquarry.TileAdvQuarry;
import com.yogpc.qp.machines.base.Area;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.util.ThreeConsumer;

/**
 * To Server only.
 */
public class AdvActionMessage implements IMessage<AdvActionMessage> {
    private BlockPos pos;
    private int dim;
    private Actions action;
    private CompoundNBT tag;

    public static AdvActionMessage create(TileAdvQuarry quarry, Actions action) {
        return create(quarry, action, new CompoundNBT());
    }

    public static AdvActionMessage create(TileAdvQuarry quarry, Actions action, CompoundNBT compound) {
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
            .ifPresent(quarry -> ctx.get().enqueueWork(action.runnable(quarry, tag, ctx.get().getSender())));
    }

    public enum Actions {
        QUICK_START(TileAdvQuarry::noFrameStart),
        CHANGE_RANGE((quarry, rangeNBT) ->
            quarry.area_$eq(Area.areaLoad(rangeNBT))
        ),
        MODULE_INV((q, t, p) -> q.openModuleInv(p));
        private final ThreeConsumer<TileAdvQuarry, CompoundNBT, ServerPlayerEntity> consumer;

        Actions(Consumer<TileAdvQuarry> consumer) {
            this.consumer = (quarry, nbtTagCompound, player) -> consumer.accept(quarry);
        }

        Actions(BiConsumer<TileAdvQuarry, CompoundNBT> consumer) {
            this.consumer = (quarry, nbtTagCompound, player) -> consumer.accept(quarry, nbtTagCompound);
        }

        Actions(ThreeConsumer<TileAdvQuarry, CompoundNBT, ServerPlayerEntity> consumer) {
            this.consumer = consumer;
        }

        Runnable runnable(TileAdvQuarry quarry, CompoundNBT compound, ServerPlayerEntity player) {
            return () -> consumer.accept(quarry, compound, player);
        }
    }
}
