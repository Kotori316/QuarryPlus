package com.yogpc.qp.packet.laser;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileLaser;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * To client only.
 */
public class LaserMessage implements IMessage {

    BlockPos pos;
    Vec3d[] vec3ds;

    public static LaserMessage create(TileLaser laser) {
        LaserMessage message = new LaserMessage();
        message.pos = laser.getPos();
        message.vec3ds = laser.lasers;
        return message;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) throws IOException {
        pos = buffer.readBlockPos();
        vec3ds = new Vec3d[buffer.readInt()];
        for (int i = 0; i < vec3ds.length; i++) {
            vec3ds[i] = new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        }
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeInt((int) Stream.of(vec3ds).filter(Objects::nonNull).count());
        for (Vec3d vec3d : vec3ds) {
            if (vec3d != null)
                buffer.writeDouble(vec3d.x).writeDouble(vec3d.y).writeDouble(vec3d.z);
        }
    }

    @Override
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        TileLaser laser = (TileLaser) world.getTileEntity(pos);
        if (laser != null) {
            laser.lasers = vec3ds;
        }
        return null;
    }
}
