package com.yogpc.qp.neoforge.packet;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.advquarry.AdvActionActionMessage;
import com.yogpc.qp.machine.advquarry.AdvActionSyncMessage;
import com.yogpc.qp.machine.marker.ChunkMarkerMessage;
import com.yogpc.qp.machine.marker.FlexibleMarkerMessage;
import com.yogpc.qp.machine.mover.MoverMessage;
import com.yogpc.qp.packet.ClientSyncMessage;
import com.yogpc.qp.packet.OnReceiveWithLevel;
import com.yogpc.qp.packet.YSetterMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class PacketHandler implements PlatformAccess.Packet {
    private static final int PROTOCOL = 1;

    private static final Proxy PROXY = ProxyProvider.getInstance();

    public static void init(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(QuarryPlus.modID).versioned(String.valueOf(PROTOCOL));
        registrar.playToClient(
            ClientSyncMessage.TYPE,
            ClientSyncMessage.STREAM_CODEC,
            PacketHandler::onReceive
        );
        registrar.playToServer(
            YSetterMessage.TYPE,
            YSetterMessage.STREAM_CODEC,
            PacketHandler::onReceive
        );
        registrar.playToServer(
            MoverMessage.TYPE,
            MoverMessage.STREAM_CODEC,
            PacketHandler::onReceive
        );
        registrar.playToServer(
            FlexibleMarkerMessage.TYPE,
            FlexibleMarkerMessage.STREAM_CODEC,
            PacketHandler::onReceive
        );
        registrar.playToServer(
            ChunkMarkerMessage.TYPE,
            ChunkMarkerMessage.STREAM_CODEC,
            PacketHandler::onReceive
        );
        registrar.playToServer(
            AdvActionActionMessage.TYPE,
            AdvActionActionMessage.STREAM_CODEC,
            PacketHandler::onReceive
        );
        registrar.playToServer(
            AdvActionSyncMessage.TYPE,
            AdvActionSyncMessage.STREAM_CODEC,
            PacketHandler::onReceive
        );
    }

    private static void onReceive(OnReceiveWithLevel message, IPayloadContext context) {
        context.enqueueWork(() -> PROXY.getPacketPlayer(context).ifPresent(player -> message.onReceive(player.level(), player)));
    }

    @Override
    public void sendToClientWorld(@NotNull CustomPacketPayload message, @NotNull Level level) {
        if (level.getServer() instanceof GameTestServer) {
            // sending message to test server will cause NPE
            QuarryPlus.LOGGER.debug("PacketHandler#sendToClient is called in GameTestServer for {}", message.getClass().getSimpleName());
        } else if (level instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersInDimension(serverLevel, message);
        } else {
            QuarryPlus.LOGGER.error("PacketHandler#sendToClient is called in client level");
        }
    }

    @Override
    public void sendToClientPlayer(@NotNull CustomPacketPayload message, @NotNull ServerPlayer player) {
        if (player.level().getServer() instanceof GameTestServer) {
            // sending message to test server will cause NPE
            QuarryPlus.LOGGER.debug("PacketHandler#sendToClientPlayer is called in GameTestServer for {}", message.getClass().getSimpleName());
            return;
        }
        PacketDistributor.sendToPlayer(player, message);
    }

    @Override
    public void sendToServer(@NotNull CustomPacketPayload message) {
        PacketDistributor.sendToServer(message);
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
        abstract Optional<Player> getPacketPlayer(@NotNull IPayloadContext context);
    }

    private static class ProxyServer extends Proxy {

        @Override
        @NotNull
        Optional<Player> getPacketPlayer(@NotNull IPayloadContext context) {
            return Optional.of(context.player());
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static class ProxyClient extends Proxy {

        @Override
        @NotNull
        Optional<Player> getPacketPlayer(@NotNull IPayloadContext context) {
            return Optional.of(context.player()).or(() -> Optional.ofNullable(Minecraft.getInstance().player));
        }
    }
}
