package com.yogpc.qp.forge.packet;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.marker.ChunkMarkerMessage;
import com.yogpc.qp.machine.marker.FlexibleMarkerMessage;
import com.yogpc.qp.machine.mover.MoverMessage;
import com.yogpc.qp.packet.ClientSyncMessage;
import com.yogpc.qp.packet.OnReceiveWithLevel;
import com.yogpc.qp.packet.YSetterMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class PacketHandler implements PlatformAccess.Packet {
    private static final int PROTOCOL = 1;
    private static final SimpleChannel CHANNEL =
        ChannelBuilder.named(ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "main"))
            .networkProtocolVersion(PROTOCOL)
            .acceptedVersions(Channel.VersionTest.exact(PROTOCOL))
            .simpleChannel()
            // ClientSyncMessage
            .messageBuilder(ClientSyncMessage.class)
            .codec(ClientSyncMessage.STREAM_CODEC)
            .consumerMainThread(PacketHandler::onReceive)
            .add()
            // YSetterMessage
            .messageBuilder(YSetterMessage.class)
            .codec(YSetterMessage.STREAM_CODEC)
            .consumerMainThread(PacketHandler::onReceive)
            .add()
            // MoverMessage
            .messageBuilder(MoverMessage.class)
            .codec(MoverMessage.STREAM_CODEC)
            .consumerMainThread(PacketHandler::onReceive)
            .add()
            // FlexibleMarkerMessage
            .messageBuilder(FlexibleMarkerMessage.class)
            .codec(FlexibleMarkerMessage.STREAM_CODEC)
            .consumerMainThread(PacketHandler::onReceive)
            .add()
            // ChunkMarkerMessage
            .messageBuilder(ChunkMarkerMessage.class)
            .codec(ChunkMarkerMessage.STREAM_CODEC)
            .consumerMainThread(PacketHandler::onReceive)
            .add()
        // END
        ;

    private static final Proxy PROXY = ProxyProvider.getInstance();

    public static void init() {
    }

    private static void onReceive(OnReceiveWithLevel message, CustomPayloadEvent.Context context) {
        PROXY.getPacketPlayer(context)
            .ifPresent(player -> message.onReceive(player.level(), player));
    }

    @Override
    public void sendToClientWorld(@NotNull CustomPacketPayload message, @NotNull Level level) {
        if (level.getServer() instanceof GameTestServer) {
            // sending message to test server will cause NPE
            QuarryPlus.LOGGER.debug("PacketHandler#sendToClientWorld is called in GameTestServer for {}", message.getClass().getSimpleName());
            return;
        }
        CHANNEL.send(message, PacketDistributor.DIMENSION.with(level.dimension()));
    }

    @Override
    public void sendToClientPlayer(@NotNull CustomPacketPayload message, @NotNull ServerPlayer player) {
        if (player.level().getServer() instanceof GameTestServer) {
            // sending message to test server will cause NPE
            QuarryPlus.LOGGER.debug("PacketHandler#sendToClientPlayer is called in GameTestServer for {}", message.getClass().getSimpleName());
            return;
        }
        CHANNEL.send(message, PacketDistributor.PLAYER.with(player));
    }

    @Override
    public void sendToServer(@NotNull CustomPacketPayload message) {
        CHANNEL.send(message, PacketDistributor.SERVER.noArg());
    }

    private static class ProxyProvider {
        @NotNull
        private static Proxy getInstance() {
            return switch (FMLLoader.getDist()) {
                case CLIENT -> new ClientSupplier().get();
                case DEDICATED_SERVER -> new ServerSupplier().get();
            };
        }

        private static class ClientSupplier {
            @OnlyIn(Dist.CLIENT)
            Proxy get() {
                return new ProxyClient();
            }
        }

        private static class ServerSupplier {
            Proxy get() {
                return new ProxyServer();
            }
        }
    }

    private static abstract class Proxy {

        @NotNull
        abstract Optional<Player> getPacketPlayer(@NotNull CustomPayloadEvent.Context context);
    }

    private static class ProxyServer extends Proxy {

        @Override
        @NotNull
        Optional<Player> getPacketPlayer(@NotNull CustomPayloadEvent.Context context) {
            return Optional.ofNullable(context.getSender());
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static class ProxyClient extends Proxy {

        @Override
        @NotNull
        Optional<Player> getPacketPlayer(@NotNull CustomPayloadEvent.Context context) {
            return Optional.<Player>ofNullable(context.getSender()).or(() -> Optional.ofNullable(Minecraft.getInstance().player));
        }
    }
}
