package com.yogpc.qp.packet.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.yogpc.qp.gui.GuiController;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * To client only.
 */
public class AvailableEntities implements IMessage {
    BlockPos pos;
    int dim;
    List<ResourceLocation> entities;

    public static AvailableEntities create(BlockPos pos, int dim, List<EntityEntry> list) {
        AvailableEntities availableEntities = new AvailableEntities();
        availableEntities.pos = pos;
        availableEntities.entities = list.stream().map(EntityEntry::getRegistryName).collect(Collectors.toList());
        availableEntities.dim = dim;
        return availableEntities;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) throws IOException {
        pos = buffer.readBlockPos();
        dim = buffer.readInt();
        int i = buffer.readInt();
        entities = new ArrayList<>(i);
        for (int j = 0; j < i; j++) {
            entities.add(new ResourceLocation(buffer.readString(Short.MAX_VALUE)));
        }
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeInt(dim);
        buffer.writeInt(entities.size());
        entities.forEach(resourceLocation -> buffer.writeString(resourceLocation.toString()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        //TODO unsafe?
        Minecraft.getMinecraft().displayGuiScreen(new GuiController(dim, pos.getX(), pos.getY(), pos.getZ(), entities));
        return null;
    }
}
