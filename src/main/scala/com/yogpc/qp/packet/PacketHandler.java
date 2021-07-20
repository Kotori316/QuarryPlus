package com.yogpc.qp.packet;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class PacketHandler {

    public static void initServer() {
        var list = List.of(
            new ServerPacketInit(LevelMessage.NAME, LevelMessage.handler)
        );
        list.forEach(i -> ServerPlayNetworking.registerGlobalReceiver(i.name(), i.handler()));
    }

    private record ServerPacketInit(Identifier name, ServerPlayNetworking.PlayChannelHandler handler) {
    }

    @Environment(EnvType.CLIENT)
    public static void sendToServer(IMessage<?> message) {
        var packet = PacketByteBufs.create();
        message.writeToBuffer(packet);
        ClientPlayNetworking.send(message.getIdentifier(), packet);
    }
}
