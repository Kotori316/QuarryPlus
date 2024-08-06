package com.yogpc.qp.neoforge.packet;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.mover.MoverMessage;
import com.yogpc.qp.packet.ClientSyncMessage;
import com.yogpc.qp.packet.YSetterMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
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
            PacketHandler::clientSyncOnReceive
        );
        registrar.playToServer(
            YSetterMessage.TYPE,
            YSetterMessage.STREAM_CODEC,
            PacketHandler::ySetterMessageOnReceive
        );
        registrar.playToServer(
            MoverMessage.TYPE,
            MoverMessage.STREAM_CODEC,
            PacketHandler::moverMessageOnReceive
        );
    }

    private static void clientSyncOnReceive(ClientSyncMessage message, IPayloadContext context) {
        context.enqueueWork(() -> PROXY.getPacketWorld(context).ifPresent(message::onReceive));
    }

    private static void ySetterMessageOnReceive(YSetterMessage message, IPayloadContext context) {
        context.enqueueWork(() -> PROXY.getPacketWorld(context).ifPresent(message::onReceive));
    }

    private static void moverMessageOnReceive(MoverMessage message, IPayloadContext context) {
        context.enqueueWork(() -> PROXY.getPacketWorld(context).ifPresent(message::onReceive));
    }

    @Override
    public void sendToClientWorld(@NotNull CustomPacketPayload message, @NotNull Level level) {
        if (level.getServer() instanceof GameTestServer) {
            // sending message to test server will cause NPE
            QuarryPlus.LOGGER.trace("PacketHandler#sendToClient is called in GameTestServer");
        } else if (level instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersInDimension(serverLevel, message);
        } else {
            QuarryPlus.LOGGER.error("PacketHandler#sendToClient is called in client level");
        }
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
        abstract Optional<Level> getPacketWorld(@NotNull IPayloadContext context);

        @NotNull
        abstract Optional<Player> getPacketPlayer(@NotNull IPayloadContext context);
    }

    private static class ProxyServer extends Proxy {

        @Override
        @NotNull
        Optional<Level> getPacketWorld(@NotNull IPayloadContext context) {
            return Optional.of(context.player()).map(Player::level);
        }

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
        Optional<Level> getPacketWorld(@NotNull IPayloadContext context) {
            return Optional.of(context.player()).map(Player::level);
        }

        @Override
        @NotNull
        Optional<Player> getPacketPlayer(@NotNull IPayloadContext context) {
            return Optional.of(context.player()).or(() -> Optional.ofNullable(Minecraft.getInstance().player));
        }
    }
}
