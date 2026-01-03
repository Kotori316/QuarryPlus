package com.yogpc.qp.forge.packet;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.advquarry.AdvActionActionMessage;
import com.yogpc.qp.machine.advquarry.AdvActionSyncMessage;
import com.yogpc.qp.machine.advquarry.AdvQuarryInitialAskMessage;
import com.yogpc.qp.machine.marker.ChunkMarkerMessage;
import com.yogpc.qp.machine.marker.FlexibleMarkerMessage;
import com.yogpc.qp.machine.mover.MoverMessage;
import com.yogpc.qp.machine.placer.RemotePlacerMessage;
import com.yogpc.qp.packet.ClientSyncMessage;
import com.yogpc.qp.packet.OnReceiveWithLevel;
import com.yogpc.qp.packet.YSetterMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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
        ChannelBuilder.named(Identifier.fromNamespaceAndPath(QuarryPlus.modID, "main"))
            .networkProtocolVersion(PROTOCOL)
            .acceptedVersions(Channel.VersionTest.exact(PROTOCOL))
            .simpleChannel()
            .play()
            .bidirectional()
            // ClientSyncMessage
            .addMain(ClientSyncMessage.class, ClientSyncMessage.STREAM_CODEC, PacketHandler::onReceive)
            // YSetterMessage
            .addMain(YSetterMessage.class, YSetterMessage.STREAM_CODEC, PacketHandler::onReceive)
            // MoverMessage
            .addMain(MoverMessage.class, MoverMessage.STREAM_CODEC, PacketHandler::onReceive)
            // FlexibleMarkerMessage
            .addMain(FlexibleMarkerMessage.class, FlexibleMarkerMessage.STREAM_CODEC, PacketHandler::onReceive)
            // ChunkMarkerMessage
            .addMain(ChunkMarkerMessage.class, ChunkMarkerMessage.STREAM_CODEC, PacketHandler::onReceive)
            // AdvActionActionMessage
            .addMain(AdvActionActionMessage.class, AdvActionActionMessage.STREAM_CODEC, PacketHandler::onReceive)
            // AdvActionSyncMessage
            .addMain(AdvActionSyncMessage.class, AdvActionSyncMessage.STREAM_CODEC, PacketHandler::onReceive)
            // AdvQuarryInitialAskMessage
            .addMain(AdvQuarryInitialAskMessage.class, AdvQuarryInitialAskMessage.STREAM_CODEC, PacketHandler::onReceive)
            // RemotePlacerMessage
            .addMain(RemotePlacerMessage.class, RemotePlacerMessage.STREAM_CODEC, PacketHandler::onReceive)
            // END
            .build()
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

    private static class ProxyClient extends Proxy {

        @Override
        @NotNull
        Optional<Player> getPacketPlayer(@NotNull CustomPayloadEvent.Context context) {
            return Optional.<Player>ofNullable(context.getSender()).or(() -> Optional.ofNullable(Minecraft.getInstance().player));
        }
    }
}
