package com.yogpc.qp.packet.laser;

import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileLaser;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
    public void fromBytes(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        vec3ds = new Vec3d[buffer.readInt()];
        for (int i = 0; i < vec3ds.length; i++) {
            vec3ds[i] = new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        }
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeInt((int) Stream.of(vec3ds).filter(TileLaser.nonNull).count());
        Stream.of(vec3ds).filter(TileLaser.nonNull).forEach(vec3d -> buffer.writeDouble(vec3d.x).writeDouble(vec3d.y).writeDouble(vec3d.z));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        TileLaser laser = (TileLaser) world.getTileEntity(pos);
        if (laser != null) {
            Minecraft.getMinecraft().addScheduledTask(() -> laser.lasers = vec3ds);
        }
        return null;
    }
}
