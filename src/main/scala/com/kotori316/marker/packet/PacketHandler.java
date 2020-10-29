package com.kotori316.marker.packet;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import com.kotori316.marker.Marker;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(Marker.modID, "marker"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    public static RegistryKey<World> getDimId(@Nullable World world) {
        return Optional.ofNullable(world)
            .map(World::getDimensionKey)
            .orElse(World.OVERWORLD);
    }

    public static void init() {
        AtomicInteger i = new AtomicInteger(0);
        INSTANCE.registerMessage(i.getAndIncrement(), ButtonMessage.class, ButtonMessage::toBytes, ButtonMessage::fromBytes, ButtonMessage::onReceive);
        INSTANCE.registerMessage(i.getAndIncrement(), AreaMessage.class, AreaMessage::toBytes, AreaMessage::fromBytes, AreaMessage::onReceive);
        INSTANCE.registerMessage(i.getAndIncrement(), Button16Message.class, Button16Message::toBytes, Button16Message::fromBytes, Button16Message::onReceive);
    }

    public static void sendToClient(Object message, World world) {
        INSTANCE.send(PacketDistributor.DIMENSION.with(world::getDimensionKey), message);
    }

    public static void sendToServer(Object message) {
        INSTANCE.sendToServer(message);
    }
}
