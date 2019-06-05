package com.yogpc.qp.packet.listtemplate;

import java.util.function.Supplier;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.item.ItemTemplate;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import scala.Option;

/**
 * To server only.
 */
public class TemplateMessage implements IMessage<TemplateMessage> {
    int itemIndex;
    ItemTemplate.Template template;

    public static TemplateMessage create(int itemIndex, ItemTemplate.Template template) {
        TemplateMessage message = new TemplateMessage();
        message.itemIndex = itemIndex;
        message.template = template;
        return message;
    }

    @Override
    public TemplateMessage readFromBuffer(PacketBuffer buffer) {
        itemIndex = buffer.readInt();
        template = ItemTemplate.read(Option.apply(buffer.readCompoundTag()));

        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeInt(itemIndex);
        buffer.writeCompoundTag(template.writeToNBT(new NBTTagCompound()));

    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> QuarryPlus.proxy.getPacketPlayer(ctx.get()).ifPresent(entityPlayer -> {
            ItemStack stack = entityPlayer.inventory.getCurrentItem();
            ItemTemplate.setTemplate(stack, template);
        }));
    }
}
