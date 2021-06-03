package com.yogpc.qp.packet;

import java.util.concurrent.atomic.AtomicInteger;

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
import com.yogpc.qp.packet.distiller.AnimationMessage;
import com.yogpc.qp.packet.enchantment.BlockListRequestMessage;
import com.yogpc.qp.packet.enchantment.DiffMessage;
import com.yogpc.qp.packet.enchantment.EnchantmentMessage;
import com.yogpc.qp.packet.enchantment.MoverMessage;
import com.yogpc.qp.packet.exppump.ExpPumpMessage;
import com.yogpc.qp.packet.filler.FillerActionMessage;
import com.yogpc.qp.packet.laser.LaserAverageMessage;
import com.yogpc.qp.packet.laser.LaserMessage;
import com.yogpc.qp.packet.listtemplate.TemplateMessage;
import com.yogpc.qp.packet.marker.LinkReply;
import com.yogpc.qp.packet.marker.LinkRequest;
import com.yogpc.qp.packet.marker.LinkUpdate;
import com.yogpc.qp.packet.marker.RemoveLaser;
import com.yogpc.qp.packet.marker.RemoveLink;
import com.yogpc.qp.packet.pump.Mappings;
import com.yogpc.qp.packet.pump.Now;
import com.yogpc.qp.packet.quarry.LevelMessage;
import com.yogpc.qp.packet.quarry.ModeMessage;
import com.yogpc.qp.packet.quarry.MoveHead;
import com.yogpc.qp.packet.quarry2.ActionMessage;
import com.yogpc.qp.packet.quarry2.Level2Message;
import com.yogpc.qp.packet.workbench.RecipeSyncMessage;
import com.yogpc.qp.packet.workbench.WorkbenchMessage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
    private static SimpleNetworkWrapper wrapper;

    public static void init() {
        wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(QuarryPlus.modID);
        IMessageHandler<IMessage, IMessage> handler = (message, ctx) -> message.onReceive(message, ctx);
        AtomicInteger id = new AtomicInteger(0);
        wrapper.registerMessage(handler, TileMessage.class, id.getAndIncrement(), Side.CLIENT);
        wrapper.registerMessage(handler, TileMessage.class, id.getAndIncrement(), Side.SERVER);
        //pump
        wrapper.registerMessage(handler, Now.class, id.getAndIncrement(), Side.CLIENT);
        wrapper.registerMessage(handler, Mappings.All.class, id.getAndIncrement(), Side.CLIENT);
        wrapper.registerMessage(handler, Mappings.Copy.class, id.getAndIncrement(), Side.SERVER);
        wrapper.registerMessage(handler, Mappings.Update.class, id.getAndIncrement(), Side.SERVER);
        //enchantment
        wrapper.registerMessage(handler, EnchantmentMessage.class, id.getAndIncrement(), Side.SERVER);
        wrapper.registerMessage(handler, DiffMessage.class, id.getAndIncrement(), Side.CLIENT);
        wrapper.registerMessage(handler, BlockListRequestMessage.class, id.getAndIncrement(), Side.SERVER);
        wrapper.registerMessage(handler, MoverMessage.Move.class, id.getAndIncrement(), Side.SERVER);
        wrapper.registerMessage(handler, MoverMessage.Cursor.class, id.getAndIncrement(), Side.SERVER);
        //marker
        wrapper.registerMessage(handler, LinkRequest.class, id.getAndIncrement(), Side.SERVER);
        wrapper.registerMessage(handler, LinkReply.class, id.getAndIncrement(), Side.CLIENT);
        wrapper.registerMessage(handler, LinkUpdate.class, id.getAndIncrement(), Side.CLIENT);
        wrapper.registerMessage(handler, RemoveLaser.class, id.getAndIncrement(), Side.CLIENT);
        wrapper.registerMessage(handler, RemoveLink.class, id.getAndIncrement(), Side.CLIENT);
        //workbench
        wrapper.registerMessage(handler, WorkbenchMessage.class, id.getAndIncrement(), Side.SERVER);
        wrapper.registerMessage(handler, RecipeSyncMessage.class, id.getAndIncrement(), Side.CLIENT);
        //controller
        wrapper.registerMessage(handler, AvailableEntities.class, id.getAndIncrement(), Side.CLIENT);
        wrapper.registerMessage(handler, SetEntity.class, id.getAndIncrement(), Side.SERVER);
        //quarry
        wrapper.registerMessage(handler, ModeMessage.class, id.getAndIncrement(), Side.CLIENT);
        wrapper.registerMessage(handler, MoveHead.class, id.getAndIncrement(), Side.CLIENT);
        wrapper.registerMessage(handler, LevelMessage.class, id.getAndIncrement(), Side.CLIENT);
        wrapper.registerMessage(handler, LevelMessage.class, id.getAndIncrement(), Side.SERVER);
        //laser
        wrapper.registerMessage(handler, LaserMessage.class, id.getAndIncrement(), Side.CLIENT);
        wrapper.registerMessage(handler, LaserAverageMessage.class, id.getAndIncrement(), Side.CLIENT);
        //chunkdestroyer
        wrapper.registerMessage(handler, AdvModeMessage.class, id.getAndIncrement(), Side.CLIENT);
        wrapper.registerMessage(handler, AdvFilterMessage.class, id.getAndIncrement(), Side.CLIENT);
        wrapper.registerMessage(handler, AdvFilterMessage.class, id.getAndIncrement(), Side.SERVER);
        wrapper.registerMessage(handler, AdvContentMessage.class, id.getAndIncrement(), Side.CLIENT);
        wrapper.registerMessage(handler, AdvLevelMessage.class, id.getAndIncrement(), Side.CLIENT);
        wrapper.registerMessage(handler, AdvLevelMessage.class, id.getAndIncrement(), Side.SERVER);
        wrapper.registerMessage(handler, AdvActionMessage.class, id.getAndIncrement(), Side.SERVER);
        //adv pump
        wrapper.registerMessage(handler, AdvPumpStatusMessage.class, id.getAndIncrement(), Side.CLIENT);
        wrapper.registerMessage(handler, AdvPumpChangeMessage.class, id.getAndIncrement(), Side.SERVER);
        //distiller
        wrapper.registerMessage(handler, AnimationMessage.class, id.getAndIncrement(), Side.CLIENT);
        //exp pump
        wrapper.registerMessage(handler, ExpPumpMessage.class, id.getAndIncrement(), Side.CLIENT);
        //template
        wrapper.registerMessage(handler, TemplateMessage.class, id.getAndIncrement(), Side.SERVER);
        //new quarry
        wrapper.registerMessage(handler, ActionMessage.class, id.getAndIncrement(), Side.CLIENT);
        wrapper.registerMessage(handler, Level2Message.class, id.getAndIncrement(), Side.CLIENT);
        wrapper.registerMessage(handler, Level2Message.class, id.getAndIncrement(), Side.SERVER);
        //filler
        wrapper.registerMessage(handler, FillerActionMessage.class, id.getAndIncrement(), Side.SERVER);
    }

    /**
     * To client
     */
    public static void sendToClient(IMessage message, EntityPlayerMP player) {
        wrapper.sendTo(message, player);
    }

    /**
     * To client
     */
    public static void sendToAround(IMessage message, World world, BlockPos pos) {
        wrapper.sendToAllAround(message, new NetworkRegistry.TargetPoint(
            world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 256
        ));
    }

    /**
     * To client
     */
    public static void sendToDimension(IMessage message, int dimensionId) {
        wrapper.sendToDimension(message, dimensionId);
    }

    /**
     * To server
     */
    public static void sendToServer(IMessage message) {
        wrapper.sendToServer(message);
    }
}
