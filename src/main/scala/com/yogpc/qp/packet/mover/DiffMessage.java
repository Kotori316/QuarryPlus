package com.yogpc.qp.packet.mover;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.yogpc.qp.machines.item.ContainerEnchList;
import com.yogpc.qp.machines.workbench.BlockData;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * To client only.
 * For container player opening.
 */

public class DiffMessage implements IMessage<DiffMessage> {
    @Override
    public DiffMessage readFromBuffer(PacketBuffer buffer) {
        containerId = buffer.readInt();
        int fS = buffer.readInt();
        int sS = buffer.readInt();
        fortuneList = new ArrayList<>(fS);
        silkList = new ArrayList<>(sS);
        for (int i = 0; i < fS; i++) {
            fortuneList.add(BlockData.read(buffer.readCompoundTag()));
        }
        for (int i = 0; i < sS; i++) {
            silkList.add(BlockData.read(buffer.readCompoundTag()));
        }
        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeInt(containerId);
        buffer.writeInt(fortuneList.size()).writeInt(silkList.size());
        fortuneList.stream().map(BlockData.dataToNbt()::apply).forEach(buffer::writeCompoundTag);
        silkList.stream().map(BlockData.dataToNbt()::apply).forEach(buffer::writeCompoundTag);

    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Container container = Minecraft.getInstance().player.openContainer;
            if (containerId == container.windowId && container instanceof ContainerEnchList) {
                ContainerEnchList enchList = (ContainerEnchList) container;
                enchList.tile.fortuneList.clear();
                enchList.tile.fortuneList.addAll(fortuneList);
                enchList.tile.silktouchList.clear();
                enchList.tile.silktouchList.addAll(silkList);
            }
        });
    }

    int containerId;
    List<BlockData> fortuneList;
    List<BlockData> silkList;

    public static DiffMessage create(Container container, List<BlockData> fortuneList, List<BlockData> silkList) {
        DiffMessage message = new DiffMessage();
        message.containerId = container.windowId;
        message.fortuneList = fortuneList;
        message.silkList = silkList;
        return message;
    }
}
