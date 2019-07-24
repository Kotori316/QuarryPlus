package com.yogpc.qp.packet.quarry2;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.packet.quarry.LevelMessage;
import com.yogpc.qp.tile.TileQuarry2;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class Level2Message extends LevelMessage {

    public static Level2Message create(TileQuarry2 quarry) {
        Level2Message message = new Level2Message();
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
            if (entity instanceof TileQuarry2) {
                TileQuarry2 quarry = (TileQuarry2) entity;
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
