package com.yogpc.qp.packet;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.advpump.AdvPumpChangeMessage;
import com.yogpc.qp.packet.advpump.AdvPumpStatusMessage;
import com.yogpc.qp.packet.advquarry.AdvActionMessage;
import com.yogpc.qp.packet.advquarry.AdvContentMessage;
import com.yogpc.qp.packet.advquarry.AdvFilterMessage;
import com.yogpc.qp.packet.advquarry.AdvLevelMessage;
import com.yogpc.qp.packet.advquarry.AdvModeMessage;
import com.yogpc.qp.packet.controller.AvailableEntities;
import com.yogpc.qp.packet.controller.SetEntity;
import com.yogpc.qp.packet.marker.LinkMessage;
import com.yogpc.qp.packet.marker.UpdateBoxMessage;
import com.yogpc.qp.packet.mover.MoverMessage;
import com.yogpc.qp.packet.pump.Mappings;
import com.yogpc.qp.packet.pump.Now;
import com.yogpc.qp.packet.quarry.LevelMessage;
import com.yogpc.qp.packet.quarry.ModeMessage;
import com.yogpc.qp.packet.quarry.MoveHead;
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
        INSTANCE.registerMessage(id++, LevelMessage.class, IMessage::writeToBuffer, IMessage.decode(LevelMessage::new), IMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, ModeMessage.class, IMessage::writeToBuffer, IMessage.decode(ModeMessage::new), IMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, MoveHead.class, IMessage::writeToBuffer, IMessage.decode(MoveHead::new), IMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, Now.class, IMessage::writeToBuffer, IMessage.decode(Now::new), IMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, Mappings.All.class, IMessage::writeToBuffer, IMessage.decode(Mappings.All::new), IMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, Mappings.Copy.class, IMessage::writeToBuffer, IMessage.decode(Mappings.Copy::new), IMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, Mappings.Update.class, IMessage::writeToBuffer, IMessage.decode(Mappings.Update::new), IMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, SetEntity.class, IMessage::writeToBuffer, IMessage.decode(SetEntity::new), IMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, AvailableEntities.class, IMessage::writeToBuffer, IMessage.decode(AvailableEntities::new), IMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, AdvModeMessage.class, IMessage::writeToBuffer, IMessage.decode(AdvModeMessage::new), IMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, AdvActionMessage.class, IMessage::writeToBuffer, IMessage.decode(AdvActionMessage::new), IMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, AdvFilterMessage.class, IMessage::writeToBuffer, IMessage.decode(AdvFilterMessage::new), IMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, AdvContentMessage.class, IMessage::writeToBuffer, IMessage.decode(AdvContentMessage::new), IMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, AdvLevelMessage.class, IMessage::writeToBuffer, b -> new AdvLevelMessage().readFromBuffer(b), IMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, AdvPumpChangeMessage.class, IMessage::writeToBuffer, IMessage.decode(AdvPumpChangeMessage::new), IMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, AdvPumpStatusMessage.class, IMessage::writeToBuffer, IMessage.decode(AdvPumpStatusMessage::new), IMessage::onReceiveInternal);

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
