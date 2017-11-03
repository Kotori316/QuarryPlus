package com.yogpc.qp.packet.laser;

import java.io.IOException;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileLaser;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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
    public void fromBytes(PacketBuffer buffer) throws IOException {
        pos = buffer.readBlockPos();
        powerAverage = buffer.readDouble();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeDouble(powerAverage);
    }

    @Override
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        TileLaser laser = (TileLaser) world.getTileEntity(pos);
        if (laser != null) {
            laser.pa = powerAverage;
        }
        return null;
    }
}
