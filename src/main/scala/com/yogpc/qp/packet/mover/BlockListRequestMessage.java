package com.yogpc.qp.packet.mover;

import java.util.function.Supplier;

import com.yogpc.qp.machines.item.ContainerEnchList;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * To server only.
 * For container player opening.
 */

public class BlockListRequestMessage implements IMessage<BlockListRequestMessage> {

    int containerId;

    public static BlockListRequestMessage create(int containerId) {
        BlockListRequestMessage message = new BlockListRequestMessage();
        message.containerId = containerId;
        return message;
    }

    @Override
    public BlockListRequestMessage readFromBuffer(PacketBuffer buffer) {
        containerId = buffer.readInt();
        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeInt(containerId);
    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayerEntity player = ctx.get().getSender();
        assert player != null; // Executed in server thread.
        if (player.openContainer instanceof ContainerEnchList) {
            ContainerEnchList container = (ContainerEnchList) player.openContainer;
            PacketHandler.INSTANCE.reply(DiffMessage.create(container, container.tile.enchantmentFilter()), ctx.get());
        }
    }
}
