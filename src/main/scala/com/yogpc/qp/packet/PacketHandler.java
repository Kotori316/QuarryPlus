package com.yogpc.qp.packet;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.advpump.AdvPumpChangeMessage;
import com.yogpc.qp.packet.advpump.AdvPumpStatusMessage;
import com.yogpc.qp.packet.advquarry.AdvContentMessage;
import com.yogpc.qp.packet.advquarry.AdvFilterMessage;
import com.yogpc.qp.packet.advquarry.AdvModeMessage;
import com.yogpc.qp.packet.advquarry.AdvRangeMessage;
import com.yogpc.qp.packet.controller.AvailableEntities;
import com.yogpc.qp.packet.controller.SetEntity;
import com.yogpc.qp.packet.distiller.AnimatonMessage;
import com.yogpc.qp.packet.enchantment.BlockListRequestMessage;
import com.yogpc.qp.packet.enchantment.DiffMessage;
import com.yogpc.qp.packet.enchantment.EnchantmentMessage;
import com.yogpc.qp.packet.enchantment.MoverMessage;
import com.yogpc.qp.packet.laser.LaserAverageMessage;
import com.yogpc.qp.packet.laser.LaserMessage;
import com.yogpc.qp.packet.marker.LinkReply;
import com.yogpc.qp.packet.marker.LinkRequest;
import com.yogpc.qp.packet.marker.LinkUpdate;
import com.yogpc.qp.packet.marker.RemoveLaser;
import com.yogpc.qp.packet.marker.RemoveLink;
import com.yogpc.qp.packet.pump.Mappings;
import com.yogpc.qp.packet.pump.Now;
import com.yogpc.qp.packet.quarry.ModeMessage;
import com.yogpc.qp.packet.quarry.MoveHead;
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
        IMessageHandler<IMessage, IMessage> handler = (message, ctx) -> message.onRecieve(message, ctx);
        int i = 0;
        wrapper.registerMessage(handler, TileMessage.class, i++, Side.CLIENT);
        wrapper.registerMessage(handler, TileMessage.class, i++, Side.SERVER);
        //pump
        wrapper.registerMessage(handler, Now.class, i++, Side.CLIENT);
        wrapper.registerMessage(handler, Mappings.All.class, i++, Side.CLIENT);
        wrapper.registerMessage(handler, Mappings.Copy.class, i++, Side.SERVER);
        wrapper.registerMessage(handler, Mappings.Update.class, i++, Side.SERVER);
        //enchantment
        wrapper.registerMessage(handler, EnchantmentMessage.class, i++, Side.SERVER);
        wrapper.registerMessage(handler, DiffMessage.class, i++, Side.CLIENT);
        wrapper.registerMessage(handler, BlockListRequestMessage.class, i++, Side.SERVER);
        wrapper.registerMessage(handler, MoverMessage.Move.class, i++, Side.SERVER);
        wrapper.registerMessage(handler, MoverMessage.Cursor.class, i++, Side.SERVER);
        //marker
        wrapper.registerMessage(handler, LinkRequest.class, i++, Side.SERVER);
        wrapper.registerMessage(handler, LinkReply.class, i++, Side.CLIENT);
        wrapper.registerMessage(handler, LinkUpdate.class, i++, Side.CLIENT);
        wrapper.registerMessage(handler, RemoveLaser.class, i++, Side.CLIENT);
        wrapper.registerMessage(handler, RemoveLink.class, i++, Side.CLIENT);
        //workbench
        wrapper.registerMessage(handler, WorkbenchMessage.class, i++, Side.CLIENT);
        //controller
        wrapper.registerMessage(handler, AvailableEntities.class, i++, Side.CLIENT);
        wrapper.registerMessage(handler, SetEntity.class, i++, Side.SERVER);
        //quarry
        wrapper.registerMessage(handler, ModeMessage.class, i++, Side.CLIENT);
        wrapper.registerMessage(handler, MoveHead.class, i++, Side.CLIENT);
        //laser
        wrapper.registerMessage(handler, LaserMessage.class, i++, Side.CLIENT);
        wrapper.registerMessage(handler, LaserAverageMessage.class, i++, Side.CLIENT);
        //chunkdestroyer
        wrapper.registerMessage(handler, AdvModeMessage.class, i++, Side.CLIENT);
        wrapper.registerMessage(handler, AdvRangeMessage.class, i++, Side.SERVER);
        wrapper.registerMessage(handler, AdvFilterMessage.class, i++, Side.CLIENT);
        wrapper.registerMessage(handler, AdvFilterMessage.class, i++, Side.SERVER);
        wrapper.registerMessage(handler, AdvContentMessage.class, i++, Side.CLIENT);
        //standalonepump
        wrapper.registerMessage(handler, AdvPumpStatusMessage.class, i++, Side.CLIENT);
        wrapper.registerMessage(handler, AdvPumpChangeMessage.class, i++, Side.SERVER);
        //distiller
        wrapper.registerMessage(handler, AnimatonMessage.class, i++, Side.CLIENT);
        assert i > 0 : "Dummy Operation";
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
