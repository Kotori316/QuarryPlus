package com.yogpc.qp.packet.listtemplate;

import java.io.IOException;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.item.ItemTemplate;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import scala.Option;

/**
 * To server only.
 */
public class TemplateMessage implements IMessage {
    int itemIndex;
    ItemTemplate.Template template;

    public static TemplateMessage create(int itemIndex, ItemTemplate.Template template) {
        TemplateMessage message = new TemplateMessage();
        message.itemIndex = itemIndex;
        message.template = template;
        return message;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) throws IOException {
        itemIndex = buffer.readInt();
        template = ItemTemplate.read(Option.apply(buffer.readCompoundTag()));
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(itemIndex);
        buffer.writeCompoundTag(template.writeToNBT(new NBTTagCompound()));
    }

    @Override
    public IMessage onReceive(IMessage message, MessageContext ctx) {
        FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
            ItemStack stack = QuarryPlus.proxy.getPacketPlayer(ctx.netHandler).inventory.getCurrentItem();
            ItemTemplate.setTemplate(stack, template);
        });
        return null;
    }
}
