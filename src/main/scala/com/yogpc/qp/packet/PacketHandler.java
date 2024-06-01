package com.yogpc.qp.packet;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.advpump.AdvPumpMessage;
import com.yogpc.qp.machines.advquarry.AdvActionMessage;
import com.yogpc.qp.machines.advquarry.AdvQuarryInitialMessage;
import com.yogpc.qp.machines.controller.ControllerOpenMessage;
import com.yogpc.qp.machines.controller.SetSpawnerEntityMessage;
import com.yogpc.qp.machines.filler.FillerButtonMessage;
import com.yogpc.qp.machines.marker.FlexMarkerMessage;
import com.yogpc.qp.machines.marker.Marker16Message;
import com.yogpc.qp.machines.mini_quarry.MiniListSyncMessage;
import com.yogpc.qp.machines.mini_quarry.MiniRequestListMessage;
import com.yogpc.qp.machines.misc.CreativeGeneratorSyncMessage;
import com.yogpc.qp.machines.mover.MoverMessage;
import com.yogpc.qp.machines.placer.RemotePlacerMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiConsumer;

public class PacketHandler {
    private static final int PROTOCOL_VERSION = 1;
    public static final SimpleChannel INSTANCE = registerMessage(
        ChannelBuilder.named(new ResourceLocation(QuarryPlus.modID, "main"))
        .networkProtocolVersion(PROTOCOL_VERSION)
        .acceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
        .simpleChannel()
    );

    private static final Proxy PROXY = ProxyProvider.getInstance();

    public static void init() {
    }

    private static SimpleChannel registerMessage(SimpleChannel channel) {
        return channel
            .messageBuilder(TileMessage.class).encoder(TileMessage::write).decoder(TileMessage::new).consumerNetworkThread(setHandled(TileMessage::onReceive)).add()
            .messageBuilder(ClientSyncMessage.class).encoder(ClientSyncMessage::write).decoder(ClientSyncMessage::new).consumerNetworkThread(setHandled(ClientSyncMessage::onReceive)).add()
            .messageBuilder(Marker16Message.class).encoder(Marker16Message::write).decoder(Marker16Message::new).consumerNetworkThread(setHandled(Marker16Message::onReceive)).add()
            .messageBuilder(FlexMarkerMessage.class).encoder(FlexMarkerMessage::write).decoder(FlexMarkerMessage::new).consumerNetworkThread(setHandled(FlexMarkerMessage::onReceive)).add()
            .messageBuilder(LevelMessage.class).encoder(LevelMessage::write).decoder(LevelMessage::new).consumerNetworkThread(setHandled(LevelMessage::onReceive)).add()
            .messageBuilder(MoverMessage.class).encoder(MoverMessage::write).decoder(MoverMessage::new).consumerNetworkThread(setHandled(MoverMessage::onReceive)).add()
            .messageBuilder(AdvActionMessage.class).encoder(AdvActionMessage::write).decoder(AdvActionMessage::new).consumerNetworkThread(setHandled(AdvActionMessage::onReceive)).add()
            .messageBuilder(SetSpawnerEntityMessage.class).encoder(SetSpawnerEntityMessage::write).decoder(SetSpawnerEntityMessage::new).consumerNetworkThread(setHandled(SetSpawnerEntityMessage::onReceive)).add()
            .messageBuilder(ControllerOpenMessage.class).encoder(ControllerOpenMessage::write).decoder(ControllerOpenMessage::new).consumerNetworkThread(setHandled(ControllerOpenMessage::onReceive)).add()
            .messageBuilder(MiniListSyncMessage.class).encoder(MiniListSyncMessage::write).decoder(MiniListSyncMessage::new).consumerNetworkThread(setHandled(MiniListSyncMessage::onReceive)).add()
            .messageBuilder(MiniRequestListMessage.class).encoder(MiniRequestListMessage::write).decoder(MiniRequestListMessage::new).consumerNetworkThread(setHandled(MiniRequestListMessage::onReceive)).add()
            .messageBuilder(FillerButtonMessage.class).encoder(FillerButtonMessage::write).decoder(FillerButtonMessage::new).consumerNetworkThread(setHandled(FillerButtonMessage::onReceive)).add()
            .messageBuilder(RemotePlacerMessage.class).encoder(RemotePlacerMessage::write).decoder(RemotePlacerMessage::new).consumerNetworkThread(setHandled(RemotePlacerMessage::onReceive)).add()
            .messageBuilder(AdvQuarryInitialMessage.class).encoder(AdvQuarryInitialMessage::write).decoder(AdvQuarryInitialMessage::new).consumerNetworkThread(setHandled(AdvQuarryInitialMessage::onReceive)).add()
            .messageBuilder(AdvQuarryInitialMessage.Ask.class).encoder(AdvQuarryInitialMessage.Ask::write).decoder(AdvQuarryInitialMessage.Ask::new).consumerNetworkThread(setHandled(AdvQuarryInitialMessage.Ask::onReceive)).add()
            .messageBuilder(AdvPumpMessage.class).encoder(AdvPumpMessage::write).decoder(AdvPumpMessage::new).consumerNetworkThread(setHandled(AdvPumpMessage::onReceive)).add()
            ;
    }

    public static void sendToClient(@NotNull IMessage message, @NotNull Level world) {
        INSTANCE.send(message, PacketDistributor.DIMENSION.with(world.dimension()));
    }

    public static void sendToClientPlayer(@NotNull IMessage message, @NotNull ServerPlayer player) {
        INSTANCE.send(message, PacketDistributor.PLAYER.with(player));
    }

    public static void sendToServer(@NotNull IMessage message) {
        INSTANCE.send(message, PacketDistributor.SERVER.noArg());
    }

    @NotNull
    public static ResourceKey<Level> getDimension(@Nullable BlockEntity entity) {
        return Optional.ofNullable(entity)
            .map(BlockEntity::getLevel)
            .map(Level::dimension)
            .orElse(Level.OVERWORLD);
    }

    @NotNull
    public static Optional<Level> getWorld(@NotNull CustomPayloadEvent.Context context, @NotNull BlockPos pos, @NotNull ResourceKey<Level> expectedDim) {
        return PROXY.getPacketWorld(context)
            .filter(l -> l.dimension().equals(expectedDim))
            .filter(l -> l.isLoaded(pos));
    }

    @NotNull
    public static Optional<Player> getPlayer(@NotNull CustomPayloadEvent.Context context) {
        return PROXY.getPacketPlayer(context);
    }

    private static <T> BiConsumer<T, CustomPayloadEvent.Context> setHandled(BiConsumer<T, CustomPayloadEvent.Context> execution) {
        return (t, supplier) -> {
            execution.accept(t, supplier);
            supplier.setPacketHandled(true);
        };
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
        Optional<Level> getPacketWorld(CustomPayloadEvent.Context context) {
            return Optional.ofNullable(context.getSender()).map(ServerPlayer::serverLevel);
        }

        @Override
        Optional<Player> getPacketPlayer(CustomPayloadEvent.Context context) {
            return Optional.ofNullable(context.getSender());
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static class ProxyClient extends Proxy {

        @Override
        Optional<Level> getPacketWorld(CustomPayloadEvent.Context context) {
            var sender = context.getSender();
            if (sender == null) {
                return Optional.ofNullable(Minecraft.getInstance().level);
            } else {
                return Optional.of(sender).map(ServerPlayer::serverLevel);
            }
        }

        @Override
        Optional<Player> getPacketPlayer(CustomPayloadEvent.Context context) {
            return Optional.<Player>ofNullable(context.getSender()).or(() -> Optional.ofNullable(Minecraft.getInstance().player));
        }
    }
}
