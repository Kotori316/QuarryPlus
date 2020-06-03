package com.yogpc.qp.packet.mini_quarry;

import java.util.function.Supplier;

import com.yogpc.qp.machines.mini_quarry.MiniQuarryTile;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * To Server Only.
 */
public class MiniRenderBoxMessage implements IMessage<MiniRenderBoxMessage> {
    private BlockPos pos;
    private int dim;
    private boolean renderBox;

    public static MiniRenderBoxMessage create(TileEntity entity, boolean renderBox) {
        MiniRenderBoxMessage message = new MiniRenderBoxMessage();
        message.pos = entity.getPos();
        message.dim = IMessage.getDimId(entity.getWorld());
        message.renderBox = renderBox;
        return message;
    }

    @Override
    public MiniRenderBoxMessage readFromBuffer(PacketBuffer buffer) {
        MiniRenderBoxMessage message = new MiniRenderBoxMessage();
        message.pos = buffer.readBlockPos();
        message.dim = buffer.readInt();
        message.renderBox = buffer.readBoolean();
        return message;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim);
        buffer.writeBoolean(renderBox);
    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        IMessage.findTile(ctx, pos, dim, MiniQuarryTile.class).ifPresent(t -> {
            t.renderBox_$eq(renderBox);
        });
    }
}
