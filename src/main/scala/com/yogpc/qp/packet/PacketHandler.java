package com.yogpc.qp.packet;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.marker.LinkMessage;
import com.yogpc.qp.packet.marker.UpdateBoxMessage;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(QuarryPlus.modID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    public static void init() {
        int id = 1;

        INSTANCE.registerMessage(id++, LinkMessage.class, IMessage::writeToBuffer, IMessage.decode(LinkMessage::new), IMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, UpdateBoxMessage.class, IMessage::writeToBuffer, IMessage.decode(UpdateBoxMessage::new), IMessage::onReceiveInternal);

        assert id > 0;
    }

    public static void sendToClient(IMessage<?> message, World world) {
        INSTANCE.send(PacketDistributor.DIMENSION.with(() -> world.getDimension().getType()), message);
    }
}
