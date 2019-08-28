package com.yogpc.qp.packet.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.yogpc.qp.machines.controller.GuiController;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * To client only.
 */
public class AvailableEntities implements IMessage<AvailableEntities> {
    BlockPos pos;
    int dim;
    List<ResourceLocation> entities;

    public static AvailableEntities create(BlockPos pos, World world, List<EntityType<?>> list) {
        AvailableEntities availableEntities = new AvailableEntities();
        availableEntities.pos = pos;
        availableEntities.entities = list.stream().map(EntityType::getRegistryName).collect(Collectors.toList());
        availableEntities.dim = IMessage.getDimId(world);
        return availableEntities;
    }

    @Override
    public AvailableEntities readFromBuffer(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        dim = buffer.readInt();
        int i = buffer.readInt();
        entities = new ArrayList<>(i);
        for (int j = 0; j < i; j++) {
            entities.add(buffer.readResourceLocation());
        }
        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeInt(dim);
        buffer.writeInt(entities.size());
        entities.forEach(buffer::writeResourceLocation);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            Minecraft.getInstance().displayGuiScreen(new GuiController(dim, pos.getX(), pos.getY(), pos.getZ(), entities))
        );
    }
}
