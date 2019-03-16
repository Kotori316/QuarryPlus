package com.yogpc.qp.packet.quarry;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileBasic;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * To both client and server.
 * Don't make this grouped with other messages.
 * This is a sub class to use Y Setter.
 */
public class LevelMessage implements IMessage {
    protected int yLevel;
    protected BlockPos pos;
    protected int dim;

    public static LevelMessage create(TileBasic tileBasic) {
        LevelMessage message = new LevelMessage();
        message.yLevel = tileBasic.yLevel;
        message.pos = tileBasic.getPos();
        message.dim = tileBasic.getWorld().provider.getDimension();
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
            if (entity instanceof TileBasic) {
                TileBasic quarry = (TileBasic) entity;
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
