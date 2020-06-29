package com.yogpc.qp.packet.mover;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.mojang.serialization.Dynamic;
import com.yogpc.qp.machines.base.EnchantmentFilter;
import com.yogpc.qp.machines.base.QuarryBlackList;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import scala.jdk.javaapi.CollectionConverters;

//import com.yogpc.qp.machines.item.GuiEnchList;

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
        fortuneList = new HashSet<>(fS);
        silkList = new HashSet<>(sS);
        for (int i = 0; i < fS; i++) {
            fortuneList.add(QuarryBlackList.readEntry(new Dynamic<>(NBTDynamicOps.INSTANCE, buffer.readCompoundTag())));
        }
        for (int i = 0; i < sS; i++) {
            silkList.add(QuarryBlackList.readEntry(new Dynamic<>(NBTDynamicOps.INSTANCE, buffer.readCompoundTag())));
        }
        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeInt(containerId);
        buffer.writeInt(fortuneList.size()).writeInt(silkList.size());
        fortuneList.stream().map(QuarryBlackList.Entry$.MODULE$.EntryToNBT()::apply).forEach(buffer::writeCompoundTag);
        silkList.stream().map(QuarryBlackList.Entry$.MODULE$.EntryToNBT()::apply).forEach(buffer::writeCompoundTag);

    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Screen screen = Minecraft.getInstance().currentScreen;
            // TODO GuiEnchList
            /*if (screen instanceof GuiEnchList) {
                ContainerEnchList enchList = ((GuiEnchList) screen).getContainer();
                EnchantmentFilter filter = enchList.tile.enchantmentFilter();
                enchList.tile.enchantmentFilter_$eq(filter.copy(
                    filter.fortuneInclude(), filter.silktouchInclude(),
                    CollectionConverters.asScala(fortuneList).toSet(),
                    CollectionConverters.asScala(silkList).toSet()
                ));
                ((GuiEnchList) screen).refreshList();
            }*/
        });
    }

    int containerId;
    Set<QuarryBlackList.Entry> fortuneList;
    Set<QuarryBlackList.Entry> silkList;

    public static DiffMessage create(Container container, EnchantmentFilter filter) {
        DiffMessage message = new DiffMessage();
        message.containerId = container.windowId;
        message.fortuneList = CollectionConverters.asJava(filter.fortuneList());
        message.silkList = CollectionConverters.asJava(filter.silktouchList());
        return message;
    }
}
