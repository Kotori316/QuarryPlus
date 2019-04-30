package com.yogpc.qp.packet;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.marker.LinkMessage;
import com.yogpc.qp.packet.marker.UpdateBoxMessage;
import com.yogpc.qp.packet.mover.MoverMessage;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
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
        INSTANCE.registerMessage(id++, TileMessage.class, IMessage::writeToBuffer, IMessage.decode(TileMessage::new), IMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, MoverMessage.Move.class, IMessage::writeToBuffer, IMessage.decode(MoverMessage.Move::new), IMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, MoverMessage.Cursor.class, IMessage::writeToBuffer, IMessage.decode(MoverMessage.Cursor::new), IMessage::onReceiveInternal);

        assert id > 0;
    }

    public static void sendToClient(IMessage<?> message, World world) {
        INSTANCE.send(PacketDistributor.DIMENSION.with(() -> world.getDimension().getType()), message);
    }

    public static void sendToAround(IMessage<?> message, World world, BlockPos pos) {
        INSTANCE.send(PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(pos.getX(), pos.getY(), pos.getZ(),
            256, world.getDimension().getType())), message);
    }

    public static void sendToServer(IMessage<?> message) {
        INSTANCE.sendToServer(message);
    }
}
