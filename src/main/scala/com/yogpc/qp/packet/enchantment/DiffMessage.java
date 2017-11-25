package com.yogpc.qp.packet.enchantment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.yogpc.qp.BlockData;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.container.ContainerEnchList;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * To client only.
 * For container player opening.
 */
public class DiffMessage implements IMessage {

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

    @Override
    public void fromBytes(PacketBuffer buffer) throws IOException {
        containerId = buffer.readInt();
        int fS = buffer.readInt();
        int sS = buffer.readInt();
        fortuneList = new ArrayList<>(fS);
        silkList = new ArrayList<>(sS);
        for (int i = 0; i < fS; i++) {
            fortuneList.add(BlockData.readFromNBT(buffer.readCompoundTag()));
        }
        for (int i = 0; i < sS; i++) {
            silkList.add(BlockData.readFromNBT(buffer.readCompoundTag()));
        }
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(containerId);
        buffer.writeInt(fortuneList.size()).writeInt(silkList.size());
        fortuneList.stream().map(data -> data.writeToNBT(new NBTTagCompound())).forEach(buffer::writeCompoundTag);
        silkList.stream().map(data -> data.writeToNBT(new NBTTagCompound())).forEach(buffer::writeCompoundTag);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        Container container = QuarryPlus.proxy.getPacketPlayer(ctx.netHandler).openContainer;
        if (containerId == container.windowId && container instanceof ContainerEnchList) {
            ContainerEnchList enchList = (ContainerEnchList) container;
            enchList.getTile().fortuneList.clear();
            enchList.getTile().fortuneList.addAll(fortuneList);
            enchList.getTile().silktouchList.clear();
            enchList.getTile().silktouchList.addAll(silkList);
        }
        return null;
    }
}
