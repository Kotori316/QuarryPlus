package com.yogpc.qp.packet.laser;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileLaser;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * To client only.
 */
public class LaserAverageMessage implements IMessage {

    BlockPos pos;
    double powerAverage;

    public static LaserAverageMessage create(TileLaser laser) {
        LaserAverageMessage message = new LaserAverageMessage();
        message.pos = laser.getPos();
        message.powerAverage = laser.pa;
        return message;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        powerAverage = buffer.readDouble();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeDouble(powerAverage);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onReceive(IMessage message, MessageContext ctx) {
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        TileLaser laser = (TileLaser) world.getTileEntity(pos);
        if (laser != null) {
            Minecraft.getMinecraft().addScheduledTask(() -> laser.pa = powerAverage);
        }
        return null;
    }
}
