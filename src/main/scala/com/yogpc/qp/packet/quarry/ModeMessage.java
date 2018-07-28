package com.yogpc.qp.packet.quarry;

import java.io.IOException;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileQuarry;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * To client only.
 */
public class ModeMessage implements IMessage {
    int dim;
    BlockPos pos;
    BlockPos minPos, maxPos;
    TileQuarry.Mode mode;

    public static ModeMessage create(TileQuarry quarry) {
        ModeMessage message = new ModeMessage();
        message.dim = quarry.getWorld().provider.getDimension();
        message.pos = quarry.getPos();
        message.mode = quarry.G_getNow();
        message.minPos = quarry.getMinPos();
        message.maxPos = quarry.getMaxPos();
        return message;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        minPos = buffer.readBlockPos();
        maxPos = buffer.readBlockPos();
        mode = buffer.readEnumValue(TileQuarry.Mode.class);
        dim = buffer.readInt();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeBlockPos(minPos).writeBlockPos(maxPos).writeEnumValue(mode).writeInt(dim);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        if (world.provider.getDimension() == dim) {
            TileEntity entity = world.getTileEntity(pos);
            if (TileQuarry.class.isInstance(entity)) {
                TileQuarry quarry = (TileQuarry) entity;
                FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                    quarry.setNow(mode);
                    quarry.xMin = minPos.getX();
                    quarry.yMin = minPos.getY();
                    quarry.zMin = minPos.getZ();
                    quarry.xMax = maxPos.getX();
                    quarry.yMax = maxPos.getY();
                    quarry.zMax = maxPos.getZ();
                    quarry.G_renew_powerConfigure();
//                IBlockState state = world.getBlockState(pos);
//                world.notifyBlockUpdate(pos, state, state, 3);
                });
            }
        }
        return null;
    }
}
