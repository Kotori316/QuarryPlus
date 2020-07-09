package com.yogpc.qp.packet.workbench;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.yogpc.qp.machines.workbench.TileWorkbench;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * To client only.
 */
public class UpdateOutputsMessage implements IMessage<UpdateOutputsMessage> {
    List<ItemStack> outs;
    BlockPos pos;
    ResourceLocation dim;

    public static UpdateOutputsMessage create(TileWorkbench workbench) {
        UpdateOutputsMessage message = new UpdateOutputsMessage();
        message.dim = IMessage.getDimId(workbench.getWorld());
        message.pos = workbench.getPos();
        message.outs = workbench.inventory2;
        return message;
    }

    @Override
    public UpdateOutputsMessage readFromBuffer(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        dim = buffer.readResourceLocation();
        int size = buffer.readVarInt();
        outs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            outs.add(buffer.readItemStack());
        }
        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeResourceLocation(dim);
        buffer.writeVarInt(outs.size());
        outs.forEach(buffer::writeItemStack);
    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        IMessage.findTile(ctx, pos, dim, TileWorkbench.class).ifPresent(w -> {
            w.inventory2.clear();
            for (int i = 0; i < outs.size(); i++) {
                w.inventory2.set(i, outs.get(i));
            }
        });
    }
}
