package com.yogpc.qp.packet;

import java.util.concurrent.atomic.AtomicInteger;

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
import com.yogpc.qp.packet.mini_quarry.MiniListSyncMessage;
import com.yogpc.qp.packet.mini_quarry.MiniRenderBoxMessage;
import com.yogpc.qp.packet.mini_quarry.MiniRequestListMessage;
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
        AtomicInteger id = new AtomicInteger(1);

        INSTANCE.registerMessage(id.getAndIncrement(), LinkMessage.class, LinkMessage::writeToBuffer, IMessage.decode(LinkMessage::new), LinkMessage::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), UpdateBoxMessage.class, UpdateBoxMessage::writeToBuffer, IMessage.decode(UpdateBoxMessage::new), UpdateBoxMessage::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), TileMessage.class, TileMessage::writeToBuffer, IMessage.decode(TileMessage::new), TileMessage::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), MoverMessage.Move.class, MoverMessage.Move::writeToBuffer, IMessage.decode(MoverMessage.Move::new), MoverMessage.Move::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), MoverMessage.Cursor.class, MoverMessage.Cursor::writeToBuffer, IMessage.decode(MoverMessage.Cursor::new), MoverMessage.Cursor::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), BlockListRequestMessage.class, BlockListRequestMessage::writeToBuffer, IMessage.decode(BlockListRequestMessage::new), BlockListRequestMessage::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), DiffMessage.class, DiffMessage::writeToBuffer, IMessage.decode(DiffMessage::new), DiffMessage::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), EnchantmentMessage.class, EnchantmentMessage::writeToBuffer, IMessage.decode(EnchantmentMessage::new), EnchantmentMessage::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), LevelMessage.class, LevelMessage::writeToBuffer, IMessage.decode(LevelMessage::new), LevelMessage::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), ModeMessage.class, ModeMessage::writeToBuffer, IMessage.decode(ModeMessage::new), ModeMessage::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), MoveHead.class, MoveHead::writeToBuffer, IMessage.decode(MoveHead::new), MoveHead::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), Now.class, Now::writeToBuffer, IMessage.decode(Now::new), Now::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), Mappings.All.class, Mappings.All::writeToBuffer, IMessage.decode(Mappings.All::new), Mappings.All::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), Mappings.Copy.class, Mappings.Copy::writeToBuffer, IMessage.decode(Mappings.Copy::new), Mappings.Copy::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), Mappings.Update.class, Mappings.Update::writeToBuffer, IMessage.decode(Mappings.Update::new), Mappings.Update::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), SetEntity.class, SetEntity::writeToBuffer, IMessage.decode(SetEntity::new), SetEntity::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), AvailableEntities.class, AvailableEntities::writeToBuffer, IMessage.decode(AvailableEntities::new), AvailableEntities::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), AdvModeMessage.class, AdvModeMessage::writeToBuffer, IMessage.decode(AdvModeMessage::new), AdvModeMessage::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), AdvActionMessage.class, AdvActionMessage::writeToBuffer, IMessage.decode(AdvActionMessage::new), AdvActionMessage::onReceiveInternal);
//        INSTANCE.registerMessage(id.getAndIncrement(), AdvFilterMessage.class, AdvFilterMessage::writeToBuffer, IMessage.decode(AdvFilterMessage::new), AdvFilterMessage::onReceiveInternal);
//        INSTANCE.registerMessage(id.getAndIncrement(), AdvContentMessage.class, AdvContentMessage::writeToBuffer, IMessage.decode(AdvContentMessage::new), AdvContentMessage::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), AdvLevelMessage.class, AdvLevelMessage::writeToBuffer, b -> new AdvLevelMessage().readFromBuffer(b), AdvLevelMessage::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), AdvPumpChangeMessage.class, AdvPumpChangeMessage::writeToBuffer, IMessage.decode(AdvPumpChangeMessage::new), AdvPumpChangeMessage::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), AdvPumpStatusMessage.class, AdvPumpStatusMessage::writeToBuffer, IMessage.decode(AdvPumpStatusMessage::new), AdvPumpStatusMessage::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), TemplateMessage.class, TemplateMessage::writeToBuffer, IMessage.decode(TemplateMessage::new), TemplateMessage::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), ActionMessage.class, ActionMessage::writeToBuffer, IMessage.decode(ActionMessage::new), ActionMessage::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), Level2Message.class, Level2Message::writeToBuffer, b -> new Level2Message().readFromBuffer(b), Level2Message::onReceiveInternal);
//        INSTANCE.registerMessage(id.getAndIncrement(), RecipeSyncMessage.class, RecipeSyncMessage::writeToBuffer, IMessage.decode(RecipeSyncMessage::new), RecipeSyncMessage::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), ClientTextMessage.class, ClientTextMessage::writeToBuffer, IMessage.decode(ClientTextMessage::new), ClientTextMessage::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), MiniRenderBoxMessage.class, MiniRenderBoxMessage::writeToBuffer, IMessage.decode(MiniRenderBoxMessage::new), MiniRenderBoxMessage::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), MiniRequestListMessage.class, MiniRequestListMessage::writeToBuffer, IMessage.decode(MiniRequestListMessage::new), MiniRequestListMessage::onReceiveInternal);
        INSTANCE.registerMessage(id.getAndIncrement(), MiniListSyncMessage.class, MiniListSyncMessage::writeToBuffer, IMessage.decode(MiniListSyncMessage::new), MiniListSyncMessage::onReceiveInternal);
    }

    public static void sendToClient(IMessage<?> message, World world) {
        INSTANCE.send(PacketDistributor.DIMENSION.with(world::func_234923_W_), message);
    }

    public static void sendToAround(IMessage<?> message, World world, BlockPos pos) {
        INSTANCE.send(PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(pos.getX(), pos.getY(), pos.getZ(),
            256, world.func_234923_W_())), message);
    }

    public static void sendToServer(IMessage<?> message) {
        INSTANCE.sendToServer(message);
    }
}
