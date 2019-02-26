package com.yogpc.qp.packet.quarry;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileQuarry;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * To both client and server.
 */
public class LevelMessage implements IMessage {
    int yLevel;
    BlockPos pos;
    int dim;

    public static LevelMessage create(TileQuarry quarry) {
        LevelMessage message = new LevelMessage();
        message.yLevel = quarry.yLevel;
        message.pos = quarry.getPos();
        message.dim = quarry.getWorld().provider.getDimension();
        return message;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        dim = buffer.readInt();
        yLevel = buffer.readInt();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim);
        buffer.writeInt(yLevel);
    }

    @Override
    public IMessage onReceive(IMessage message, MessageContext ctx) {
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        if (world.provider.getDimension() == dim) {
            TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof TileQuarry) {
                TileQuarry quarry = (TileQuarry) entity;
                switch (ctx.side) {
                    case CLIENT:
                        quarry.setYLevel(yLevel);
                        break;
                    case SERVER:
                        FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() ->
                            quarry.setYLevel(yLevel));
                        break;
                }
            }
        }

        return null;
    }
}
