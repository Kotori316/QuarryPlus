package com.yogpc.qp.packet;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.advpump.AdvPumpChangeMessage;
import com.yogpc.qp.packet.advpump.AdvPumpStatusMessage;
import com.yogpc.qp.packet.advquarry.AdvActionMessage;
import com.yogpc.qp.packet.advquarry.AdvLevelMessage;
import com.yogpc.qp.packet.advquarry.AdvModeMessage;
import com.yogpc.qp.packet.controller.AvailableEntities;
import com.yogpc.qp.packet.controller.SetEntity;
import com.yogpc.qp.packet.listtemplate.TemplateMessage;
import com.yogpc.qp.packet.marker.LinkMessage;
import com.yogpc.qp.packet.marker.UpdateBoxMessage;
import com.yogpc.qp.packet.mover.BlockListRequestMessage;
import com.yogpc.qp.packet.mover.DiffMessage;
import com.yogpc.qp.packet.mover.EnchantmentMessage;
import com.yogpc.qp.packet.mover.MoverMessage;
import com.yogpc.qp.packet.pump.Mappings;
import com.yogpc.qp.packet.pump.Now;
import com.yogpc.qp.packet.quarry.LevelMessage;
import com.yogpc.qp.packet.quarry.ModeMessage;
import com.yogpc.qp.packet.quarry.MoveHead;
import com.yogpc.qp.packet.quarry2.ActionMessage;
import com.yogpc.qp.packet.quarry2.Level2Message;
import com.yogpc.qp.packet.workbench.RecipeSyncMessage;
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

        INSTANCE.registerMessage(id++, LinkMessage.class, LinkMessage::writeToBuffer, IMessage.decode(LinkMessage::new), LinkMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, UpdateBoxMessage.class, UpdateBoxMessage::writeToBuffer, IMessage.decode(UpdateBoxMessage::new), UpdateBoxMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, TileMessage.class, TileMessage::writeToBuffer, IMessage.decode(TileMessage::new), TileMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, MoverMessage.Move.class, MoverMessage.Move::writeToBuffer, IMessage.decode(MoverMessage.Move::new), MoverMessage.Move::onReceiveInternal);
        INSTANCE.registerMessage(id++, MoverMessage.Cursor.class, MoverMessage.Cursor::writeToBuffer, IMessage.decode(MoverMessage.Cursor::new), MoverMessage.Cursor::onReceiveInternal);
        INSTANCE.registerMessage(id++, BlockListRequestMessage.class, BlockListRequestMessage::writeToBuffer, IMessage.decode(BlockListRequestMessage::new), BlockListRequestMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, DiffMessage.class, DiffMessage::writeToBuffer, IMessage.decode(DiffMessage::new), DiffMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, EnchantmentMessage.class, EnchantmentMessage::writeToBuffer, IMessage.decode(EnchantmentMessage::new), EnchantmentMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, LevelMessage.class, LevelMessage::writeToBuffer, IMessage.decode(LevelMessage::new), LevelMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, ModeMessage.class, ModeMessage::writeToBuffer, IMessage.decode(ModeMessage::new), ModeMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, MoveHead.class, MoveHead::writeToBuffer, IMessage.decode(MoveHead::new), MoveHead::onReceiveInternal);
        INSTANCE.registerMessage(id++, Now.class, Now::writeToBuffer, IMessage.decode(Now::new), Now::onReceiveInternal);
        INSTANCE.registerMessage(id++, Mappings.All.class, Mappings.All::writeToBuffer, IMessage.decode(Mappings.All::new), Mappings.All::onReceiveInternal);
        INSTANCE.registerMessage(id++, Mappings.Copy.class, Mappings.Copy::writeToBuffer, IMessage.decode(Mappings.Copy::new), Mappings.Copy::onReceiveInternal);
        INSTANCE.registerMessage(id++, Mappings.Update.class, Mappings.Update::writeToBuffer, IMessage.decode(Mappings.Update::new), Mappings.Update::onReceiveInternal);
        INSTANCE.registerMessage(id++, SetEntity.class, SetEntity::writeToBuffer, IMessage.decode(SetEntity::new), SetEntity::onReceiveInternal);
        INSTANCE.registerMessage(id++, AvailableEntities.class, AvailableEntities::writeToBuffer, IMessage.decode(AvailableEntities::new), AvailableEntities::onReceiveInternal);
        INSTANCE.registerMessage(id++, AdvModeMessage.class, AdvModeMessage::writeToBuffer, IMessage.decode(AdvModeMessage::new), AdvModeMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, AdvActionMessage.class, AdvActionMessage::writeToBuffer, IMessage.decode(AdvActionMessage::new), AdvActionMessage::onReceiveInternal);
//        INSTANCE.registerMessage(id++, AdvFilterMessage.class, IMessage::writeToBuffer, IMessage.decode(AdvFilterMessage::new), IMessage::onReceiveInternal);
//        INSTANCE.registerMessage(id++, AdvContentMessage.class, IMessage::writeToBuffer, IMessage.decode(AdvContentMessage::new), IMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, AdvLevelMessage.class, AdvLevelMessage::writeToBuffer, b -> new AdvLevelMessage().readFromBuffer(b), AdvLevelMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, AdvPumpChangeMessage.class, AdvPumpChangeMessage::writeToBuffer, IMessage.decode(AdvPumpChangeMessage::new), AdvPumpChangeMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, AdvPumpStatusMessage.class, AdvPumpStatusMessage::writeToBuffer, IMessage.decode(AdvPumpStatusMessage::new), AdvPumpStatusMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, TemplateMessage.class, TemplateMessage::writeToBuffer, IMessage.decode(TemplateMessage::new), TemplateMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, ActionMessage.class, ActionMessage::writeToBuffer, IMessage.decode(ActionMessage::new), ActionMessage::onReceiveInternal);
        INSTANCE.registerMessage(id++, Level2Message.class, Level2Message::writeToBuffer, b -> new Level2Message().readFromBuffer(b), Level2Message::onReceiveInternal);
        INSTANCE.registerMessage(id++, RecipeSyncMessage.class, RecipeSyncMessage::writeToBuffer, IMessage.decode(RecipeSyncMessage::new), RecipeSyncMessage::onReceiveInternal);

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
