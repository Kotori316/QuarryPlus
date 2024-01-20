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
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static final Proxy PROXY = ProxyProvider.getInstance();

    public static void init(RegisterPayloadHandlerEvent event) {
        var registrar = event.registrar(QuarryPlus.modID).versioned(PROTOCOL_VERSION);
        registrar.play(IMessage.createIdentifier(ClientSyncMessage.class), ClientSyncMessage::new, handler -> handler.client(ClientSyncMessage::onReceive));
        registrar.play(IMessage.createIdentifier(Marker16Message.class), Marker16Message::new, handler -> handler.server(Marker16Message::onReceive));
        registrar.play(IMessage.createIdentifier(FlexMarkerMessage.class), FlexMarkerMessage::new, handler -> handler.server(FlexMarkerMessage::onReceive));
        registrar.play(IMessage.createIdentifier(LevelMessage.class), LevelMessage::new, handler -> handler.server(LevelMessage::onReceive));
        registrar.play(IMessage.createIdentifier(MoverMessage.class), MoverMessage::new, handler -> handler.server(MoverMessage::onReceive));
        registrar.play(IMessage.createIdentifier(AdvActionMessage.class), AdvActionMessage::new, handler -> handler.server(AdvActionMessage::onReceive));
        registrar.play(IMessage.createIdentifier(SetSpawnerEntityMessage.class), SetSpawnerEntityMessage::new, handler -> handler.server(SetSpawnerEntityMessage::onReceive));
        registrar.play(IMessage.createIdentifier(ControllerOpenMessage.class), ControllerOpenMessage::new, handler -> handler.client(ControllerOpenMessage::onReceive));
        registrar.play(IMessage.createIdentifier(MiniListSyncMessage.class), MiniListSyncMessage::new, handler -> handler.client(MiniListSyncMessage::onReceive).server(MiniListSyncMessage::onReceive));
        registrar.play(IMessage.createIdentifier(MiniRequestListMessage.class), MiniRequestListMessage::new, handler -> handler.server(MiniRequestListMessage::onReceive));
        registrar.play(IMessage.createIdentifier(FillerButtonMessage.class), FillerButtonMessage::new, handler -> handler.server(FillerButtonMessage::onReceive));
        registrar.play(IMessage.createIdentifier(RemotePlacerMessage.class), RemotePlacerMessage::new, handler -> handler.server(RemotePlacerMessage::onReceive));
        registrar.play(IMessage.createIdentifier(AdvQuarryInitialMessage.class), AdvQuarryInitialMessage::new, handler -> handler.server(AdvQuarryInitialMessage::onReceive));
        registrar.play(IMessage.createIdentifier(AdvQuarryInitialMessage.Ask.class), AdvQuarryInitialMessage.Ask::new, handler -> handler.client(AdvQuarryInitialMessage.Ask::onReceive));
        registrar.play(IMessage.createIdentifier(AdvPumpMessage.class), AdvPumpMessage::new, handler -> handler.server(AdvPumpMessage::onReceive));
        registrar.play(IMessage.createIdentifier(CreativeGeneratorSyncMessage.class), CreativeGeneratorSyncMessage::new, handler -> handler.server(CreativeGeneratorSyncMessage::onReceive));

    }

    public static void sendToClient(@NotNull IMessage message, @NotNull Level world) {
        PacketDistributor.DIMENSION.with(world.dimension()).send(message);
    }

    public static void sendToClientPlayer(@NotNull IMessage message, @NotNull ServerPlayer player) {
        PacketDistributor.PLAYER.with(player).send(message);
    }

    public static void sendToServer(@NotNull IMessage message) {
        PacketDistributor.SERVER.noArg().send(message);
    }

    @NotNull
    public static ResourceKey<Level> getDimension(@Nullable BlockEntity entity) {
        return Optional.ofNullable(entity)
            .map(BlockEntity::getLevel)
            .map(Level::dimension)
            .orElse(Level.OVERWORLD);
    }

    @NotNull
    public static Optional<Level> getWorld(@NotNull PlayPayloadContext context, @NotNull BlockPos pos, @NotNull ResourceKey<Level> expectedDim) {
        return PROXY.getPacketWorld(context)
            .filter(l -> l.dimension().equals(expectedDim))
            .filter(l -> l.isLoaded(pos));
    }

    @NotNull
    public static Optional<Player> getPlayer(@NotNull PlayPayloadContext context) {
        return PROXY.getPacketPlayer(context);
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
        abstract Optional<Level> getPacketWorld(@NotNull PlayPayloadContext context);

        @NotNull
        abstract Optional<Player> getPacketPlayer(@NotNull PlayPayloadContext context);
    }

    private static class ProxyServer extends Proxy {

        @Override
        Optional<Level> getPacketWorld(PlayPayloadContext context) {
            return context.level();
        }

        @Override
        Optional<Player> getPacketPlayer(PlayPayloadContext context) {
            return context.player();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static class ProxyClient extends Proxy {

        @Override
        Optional<Level> getPacketWorld(PlayPayloadContext context) {
            return context.level();
        }

        @Override
        Optional<Player> getPacketPlayer(PlayPayloadContext context) {
            return context.player();
        }
    }
}
