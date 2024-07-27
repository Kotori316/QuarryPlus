package com.yogpc.qp.forge.machine.packet;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.ClientSyncMessage;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import org.jetbrains.annotations.NotNull;

public final class PacketHandler implements PlatformAccess.Packet {
    private static final int PROTOCOL = 1;
    private static final SimpleChannel CHANNEL =
        ChannelBuilder.named(ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "main"))
            .networkProtocolVersion(PROTOCOL)
            .acceptedVersions(Channel.VersionTest.exact(PROTOCOL))
            .simpleChannel()
            // FluidTankContentMessageForge
            .messageBuilder(ClientSyncMessage.class)
            .codec(ClientSyncMessage.STREAM_CODEC)
            .consumerMainThread(PacketHandler::clientSyncOnReceive)
            .add();

    public static void init() {
    }

    private static void clientSyncOnReceive(ClientSyncMessage message, CustomPayloadEvent.Context context) {
        if (context.getSender() == null) return;

        var level = context.getSender().level();
        message.onReceive(level);
    }

    @Override
    public void sendToClientWorld(@NotNull CustomPacketPayload message, @NotNull Level level) {
        if (level.getServer() instanceof GameTestServer) {
            // sending message to test server will cause NPE
            return;
        }
        CHANNEL.send(message, PacketDistributor.DIMENSION.with(level.dimension()));
    }
}
