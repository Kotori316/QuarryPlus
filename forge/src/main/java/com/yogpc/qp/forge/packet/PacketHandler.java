package com.yogpc.qp.forge.packet;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.ClientSyncMessage;
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
            .consumerMainThread(PacketHandler::clientSyncOnReceive)
            .add();

    private static final Proxy PROXY = ProxyProvider.getInstance();

    public static void init() {
    }

    private static void clientSyncOnReceive(ClientSyncMessage message, CustomPayloadEvent.Context context) {
        PROXY.getPacketWorld(context)
            .ifPresent(message::onReceive);
    }

    @Override
    public void sendToClientWorld(@NotNull CustomPacketPayload message, @NotNull Level level) {
        if (level.getServer() instanceof GameTestServer) {
            // sending message to test server will cause NPE
            return;
        }
        CHANNEL.send(message, PacketDistributor.DIMENSION.with(level.dimension()));
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
        abstract Optional<Level> getPacketWorld(@NotNull CustomPayloadEvent.Context context);

        @NotNull
        abstract Optional<Player> getPacketPlayer(@NotNull CustomPayloadEvent.Context context);
    }

    private static class ProxyServer extends Proxy {

        @Override
        @NotNull
        Optional<Level> getPacketWorld(@NotNull CustomPayloadEvent.Context context) {
            return Optional.ofNullable(context.getSender()).map(ServerPlayer::serverLevel);
        }

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
        Optional<Level> getPacketWorld(@NotNull CustomPayloadEvent.Context context) {
            var sender = context.getSender();
            if (sender == null) {
                return Optional.ofNullable(Minecraft.getInstance().level);
            } else {
                return Optional.of(sender).map(ServerPlayer::serverLevel);
            }
        }

        @Override
        @NotNull
        Optional<Player> getPacketPlayer(@NotNull CustomPayloadEvent.Context context) {
            return Optional.<Player>ofNullable(context.getSender()).or(() -> Optional.ofNullable(Minecraft.getInstance().player));
        }
    }
}
