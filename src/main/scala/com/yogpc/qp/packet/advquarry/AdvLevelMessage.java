package com.yogpc.qp.packet.advquarry;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.packet.quarry.LevelMessage;
import com.yogpc.qp.tile.TileAdvQuarry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * To both client and server.
 * Don't make this grouped with other AdvMessages.
 * This is a sub class to use Y Setter.
 */
public class AdvLevelMessage extends LevelMessage {

    public static AdvLevelMessage create(TileAdvQuarry quarry) {
        AdvLevelMessage message = new AdvLevelMessage();
        message.yLevel = quarry.yLevel();
        message.pos = quarry.getPos();
        message.dim = quarry.getWorld().provider.getDimension();
        return message;
    }

    @Override
    public IMessage onReceive(IMessage message, MessageContext ctx) {
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        if (world.provider.getDimension() == dim) {
            TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof TileAdvQuarry) {
                TileAdvQuarry quarry = (TileAdvQuarry) entity;
                switch (ctx.side) {
                    case CLIENT:
                        quarry.yLevel_$eq(yLevel);
                        break;
                    case SERVER:
                        FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() ->
                            quarry.yLevel_$eq(yLevel));
                        break;
                }
            }
        }

        return null;
    }
}
