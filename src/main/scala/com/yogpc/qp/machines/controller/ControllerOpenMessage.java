package com.yogpc.qp.machines.controller;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.yogpc.qp.packet.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

/**
 * To Client only.
 */
public final class ControllerOpenMessage implements IMessage {
    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final List<ResourceLocation> allEntities;

    public ControllerOpenMessage(BlockPos pos, ResourceKey<Level> dim, List<ResourceLocation> allEntities) {
        this.pos = pos;
        this.dim = dim;
        this.allEntities = allEntities;
    }

    public ControllerOpenMessage(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.dim = ResourceKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation());
        int size = buf.readInt();
        this.allEntities = Stream.generate(buf::readResourceLocation).limit(size).toList();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos).writeResourceLocation(dim.location());
        buf.writeInt(allEntities.size());
        allEntities.forEach(buf::writeResourceLocation);
    }

    public static void onReceive(ControllerOpenMessage message, Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> Minecraft.getInstance().setScreen(new GuiController(message.dim, message.pos, message.allEntities)));
    }
}
