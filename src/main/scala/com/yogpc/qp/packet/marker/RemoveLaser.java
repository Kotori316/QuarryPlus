package com.yogpc.qp.packet.marker;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileMarker;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * To client only.
 */
public class RemoveLaser implements IMessage {
    BlockPos pos;
    int dimensionId;

    public static RemoveLaser create(int x, int y, int z, int dimensionId) {
        RemoveLaser removeLaser = new RemoveLaser();
        removeLaser.pos = new BlockPos(x, y, z);
        removeLaser.dimensionId = dimensionId;
        return removeLaser;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        dimensionId = buffer.readInt();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dimensionId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        if (world.provider.getDimension() == dimensionId) {
            int i = TileMarker.LASER_INDEX.indexOf(new TileMarker.BlockIndex(world, pos.getX(), pos.getY(), pos.getZ()));
            if (i >= 0) {
                TileMarker.laserList.get(i).destructor();
            }
        }
        return null;
    }
}
