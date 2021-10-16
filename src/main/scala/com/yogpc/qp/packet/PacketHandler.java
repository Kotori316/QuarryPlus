package com.yogpc.qp.packet;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.advquarry.AdvActionMessage;
import com.yogpc.qp.machines.controller.ControllerOpenMessage;
import com.yogpc.qp.machines.controller.SetSpawnerEntityMessage;
import com.yogpc.qp.machines.marker.FlexMarkerMessage;
import com.yogpc.qp.machines.marker.Marker16Message;
import com.yogpc.qp.machines.mini_quarry.MiniListSyncMessage;
import com.yogpc.qp.machines.mini_quarry.MiniRequestListMessage;
import com.yogpc.qp.machines.mover.MoverMessage;
import javax.annotation.Nullable;
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
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(QuarryPlus.modID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );
    private static final Proxy PROXY = ProxyProvider.getInstance();

    public static void init() {
        AtomicInteger id = new AtomicInteger(1);
        INSTANCE.registerMessage(id.getAndIncrement(), TileMessage.class, TileMessage::write, TileMessage::new, setHandled(TileMessage::onReceive));
        INSTANCE.registerMessage(id.getAndIncrement(), ClientSyncMessage.class, ClientSyncMessage::write, ClientSyncMessage::new, setHandled(ClientSyncMessage::onReceive));
        INSTANCE.registerMessage(id.getAndIncrement(), Marker16Message.class, Marker16Message::write, Marker16Message::new, setHandled(Marker16Message::onReceive));
        INSTANCE.registerMessage(id.getAndIncrement(), FlexMarkerMessage.class, FlexMarkerMessage::write, FlexMarkerMessage::new, setHandled(FlexMarkerMessage::onReceive));
        INSTANCE.registerMessage(id.getAndIncrement(), LevelMessage.class, LevelMessage::write, LevelMessage::new, setHandled(LevelMessage::onReceive));
        INSTANCE.registerMessage(id.getAndIncrement(), MoverMessage.class, MoverMessage::write, MoverMessage::new, setHandled(MoverMessage::onReceive));
        INSTANCE.registerMessage(id.getAndIncrement(), AdvActionMessage.class, AdvActionMessage::write, AdvActionMessage::new, setHandled(AdvActionMessage::onReceive));
        INSTANCE.registerMessage(id.getAndIncrement(), SetSpawnerEntityMessage.class, SetSpawnerEntityMessage::write, SetSpawnerEntityMessage::new, setHandled(SetSpawnerEntityMessage::onReceive));
        INSTANCE.registerMessage(id.getAndIncrement(), ControllerOpenMessage.class, ControllerOpenMessage::write, ControllerOpenMessage::new, setHandled(ControllerOpenMessage::onReceive));
        INSTANCE.registerMessage(id.getAndIncrement(), MiniListSyncMessage.class, MiniListSyncMessage::write, MiniListSyncMessage::new, setHandled(MiniListSyncMessage::onReceive));
        INSTANCE.registerMessage(id.getAndIncrement(), MiniRequestListMessage.class, MiniRequestListMessage::write, MiniRequestListMessage::new, setHandled(MiniRequestListMessage::onReceive));
    }

    public static void sendToClient(IMessage message, Level world) {
        INSTANCE.send(PacketDistributor.DIMENSION.with(world::dimension), message);
    }

    public static void sendToClientPlayer(IMessage message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendToServer(IMessage message) {
        INSTANCE.sendToServer(message);
    }

    public static ResourceKey<Level> getDimension(@Nullable BlockEntity entity) {
        return Optional.ofNullable(entity)
            .map(BlockEntity::getLevel)
            .map(Level::dimension)
            .orElse(Level.OVERWORLD);
    }

    public static Optional<Level> getWorld(NetworkEvent.Context context, BlockPos pos, ResourceKey<Level> expectedDim) {
        return PROXY.getPacketWorld(context)
            .filter(l -> l.dimension().equals(expectedDim))
            .filter(l -> l.isLoaded(pos));
    }

    public static Optional<Player> getPlayer(NetworkEvent.Context context) {
        return PROXY.getPacketPlayer(context);
    }

    private static <T> BiConsumer<T, Supplier<NetworkEvent.Context>> setHandled(BiConsumer<T, Supplier<NetworkEvent.Context>> execution) {
        return (t, supplier) -> {
            execution.accept(t, supplier);
            supplier.get().setPacketHandled(true);
        };
    }

    private static class ProxyProvider {
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
        abstract Optional<Level> getPacketWorld(NetworkEvent.Context context);

        abstract Optional<Player> getPacketPlayer(NetworkEvent.Context context);
    }

    private static class ProxyServer extends Proxy {

        @Override
        Optional<Level> getPacketWorld(NetworkEvent.Context context) {
            return Optional.ofNullable(context.getSender()).map(ServerPlayer::getLevel);
        }

        @Override
        Optional<Player> getPacketPlayer(NetworkEvent.Context context) {
            return Optional.ofNullable(context.getSender());
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static class ProxyClient extends Proxy {

        @Override
        Optional<Level> getPacketWorld(NetworkEvent.Context context) {
            var sender = context.getSender();
            if (sender == null) {
                return Optional.ofNullable(Minecraft.getInstance().level);
            } else {
                return Optional.of(sender).map(ServerPlayer::getLevel);
            }
        }

        @Override
        Optional<Player> getPacketPlayer(NetworkEvent.Context context) {
            return Optional.<Player>ofNullable(context.getSender()).or(() -> Optional.ofNullable(Minecraft.getInstance().player));
        }
    }
}
