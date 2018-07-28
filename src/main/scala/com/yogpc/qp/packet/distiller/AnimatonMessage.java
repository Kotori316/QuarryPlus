package com.yogpc.qp.packet.distiller;

import java.io.IOException;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileRefinery;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * To Client only.
 */
public class AnimatonMessage implements IMessage {

    public int dim;
    public BlockPos pos;
    public float speed;
    public int stage;

    public static AnimatonMessage create(TileRefinery refinery) {
        AnimatonMessage message = new AnimatonMessage();
        message.pos = refinery.getPos();
        message.dim = refinery.getWorld().provider.getDimension();
        message.speed = refinery.animationSpeed;
        message.stage = refinery.getAnimationStage();
        return message;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        dim = buffer.readInt();
        speed = buffer.readFloat();
        stage = buffer.readInt();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim).writeFloat(speed).writeInt(stage);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        if (world.provider.getDimension() == dim) {
            TileRefinery refinery = ((TileRefinery) world.getTileEntity(pos));
            if (refinery != null) {
                Minecraft.getMinecraft().addScheduledTask(refinery.receiveMessage(stage, speed));
            }
        }
        return null;
    }
}
