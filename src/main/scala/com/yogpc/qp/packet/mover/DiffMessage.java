package com.yogpc.qp.packet.mover;

/**
 * To client only.
 * For container player opening.
 */
/*
public class DiffMessage implements IMessage<DiffMessage> {

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
    public void fromBytes(PacketBuffer buffer)  {
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
    @OnlyIn(Dist.CLIENT)
    public IMessage onReceive(IMessage message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            Container container = QuarryPlus.proxy.getPacketPlayer(ctx.netHandler).openContainer;
            if (containerId == container.windowId && container instanceof ContainerEnchList) {
                ContainerEnchList enchList = (ContainerEnchList) container;
                enchList.tile.fortuneList.clear();
                enchList.tile.fortuneList.addAll(fortuneList);
                enchList.tile.silktouchList.clear();
                enchList.tile.silktouchList.addAll(silkList);
            }
        });
        return null;
    }
}
*/