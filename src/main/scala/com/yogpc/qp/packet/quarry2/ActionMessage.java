package com.yogpc.qp.packet.quarry2;

import java.io.IOException;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.QuarryAction;
import com.yogpc.qp.tile.QuarryAction$;
import com.yogpc.qp.tile.TileQuarry2;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * To client only.
 */
public class ActionMessage implements IMessage {
    int dim;
    BlockPos pos;
    NBTTagCompound actionNBT;

    public static ActionMessage create(TileQuarry2 quarry2) {
        ActionMessage message = new ActionMessage();
        message.dim = quarry2.getWorld().provider.getDimension();
        message.pos = quarry2.getPos();
        message.actionNBT = quarry2.action().clientWrite(new NBTTagCompound());
        return message;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) throws IOException {
        pos = buffer.readBlockPos();
        dim = buffer.readInt();
        actionNBT = buffer.readCompoundTag();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim);
        buffer.writeCompoundTag(actionNBT);
    }

    @Override
    public IMessage onReceive(IMessage message, MessageContext ctx) {
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        if (world.provider.getDimension() == dim) {
            TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof TileQuarry2) {
                TileQuarry2 quarry2 = (TileQuarry2) entity;
                FMLCommonHandler.instance().getWorldThread(ctx.netHandler)
                    .addScheduledTask(() -> {
                        QuarryAction action = QuarryAction$.MODULE$.loadFromNBT().apply(actionNBT).apply(quarry2);
                        quarry2.action_$eq(action);
                    });
            }
        }
        return null;
    }
}
