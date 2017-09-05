package com.yogpc.qp.packet;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.controller.AvailableEntities;
import com.yogpc.qp.packet.controller.SetEntity;
import com.yogpc.qp.packet.enchantment.DiffMessage;
import com.yogpc.qp.packet.enchantment.EnchantmentMessage;
import com.yogpc.qp.packet.enchantment.MoverMessage;
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
        wrapper.registerMessage(handler, TileMessage.class, 0, Side.CLIENT);
        wrapper.registerMessage(handler, TileMessage.class, 1, Side.SERVER);
        //pump
        wrapper.registerMessage(handler, Now.class, 2, Side.CLIENT);
        wrapper.registerMessage(handler, Mappings.All.class, 3, Side.CLIENT);
        wrapper.registerMessage(handler, Mappings.All.class, 15, Side.SERVER);
        wrapper.registerMessage(handler, Mappings.Update.class, 4, Side.SERVER);
        //enchantment
        wrapper.registerMessage(handler, EnchantmentMessage.class, 5, Side.SERVER);
        wrapper.registerMessage(handler, DiffMessage.class, 6, Side.CLIENT);
        wrapper.registerMessage(handler, MoverMessage.Move.class, 12, Side.SERVER);
        wrapper.registerMessage(handler, MoverMessage.Cursor.class, 13, Side.SERVER);
        //marker
        wrapper.registerMessage(handler, LinkRequest.class, 7, Side.SERVER);
        wrapper.registerMessage(handler, LinkReply.class, 8, Side.CLIENT);
        wrapper.registerMessage(handler, LinkUpdate.class, 9, Side.CLIENT);
        wrapper.registerMessage(handler, RemoveLaser.class, 10, Side.CLIENT);
        wrapper.registerMessage(handler, RemoveLink.class, 11, Side.CLIENT);
        //workbench
        wrapper.registerMessage(handler, WorkbenchMessage.class, 14, Side.CLIENT);
        //controller
        wrapper.registerMessage(handler, AvailableEntities.class, 16, Side.CLIENT);
        wrapper.registerMessage(handler, SetEntity.class, 17, Side.SERVER);
        //quarry
        wrapper.registerMessage(handler, ModeMessage.class, 18, Side.CLIENT);
        wrapper.registerMessage(handler, MoveHead.class, 19, Side.CLIENT);
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
