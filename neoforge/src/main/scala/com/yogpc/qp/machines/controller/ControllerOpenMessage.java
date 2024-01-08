package com.yogpc.qp.machines.controller;

import com.yogpc.qp.packet.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.List;
import java.util.stream.Stream;

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
        this.dim = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
        int size = buf.readInt();
        this.allEntities = Stream.generate(buf::readResourceLocation).limit(size).toList();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos).writeResourceLocation(dim.location());
        buf.writeInt(allEntities.size());
        allEntities.forEach(buf::writeResourceLocation);
    }

    public static void onReceive(ControllerOpenMessage message, PlayPayloadContext context) {
        context.workHandler().execute(() -> openScreen(message));
    }

    @OnlyIn(Dist.CLIENT)
    private static void openScreen(ControllerOpenMessage message) {
        Minecraft.getInstance().setScreen(new GuiController(message.dim, message.pos, message.allEntities));
    }
}
