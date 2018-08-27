package com.yogpc.qp.packet.exppump;

import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileExpPump;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * To Client only.
 */
public class ExpPumpMessage implements IMessage {

    public BlockPos pos;
    public int dim;
    public int xpAmount;
    public int facingOrdinal;

    public static ExpPumpMessage create(TileExpPump pump) {
        return pump.writeToPacket(new ExpPumpMessage());
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        dim = buffer.readInt();
        xpAmount = buffer.readInt();
        facingOrdinal = buffer.readInt();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim);
        buffer.writeInt(xpAmount).writeInt(facingOrdinal);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        if (world.provider.getDimension() == dim) {
            Optional.ofNullable((TileExpPump) world.getTileEntity(pos)).ifPresent(pump ->
                FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() ->
                    pump.onMessage(this)
                )
            );
        }
        return null;
    }
}
