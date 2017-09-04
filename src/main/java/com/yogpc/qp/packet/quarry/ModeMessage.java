package com.yogpc.qp.packet.quarry;

import java.io.IOException;

import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileQuarry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * To client only.
 */
public class ModeMessage implements IMessage {
    int dim;
    BlockPos pos;
    TileQuarry.Mode mode;

    public static ModeMessage create(TileQuarry quarry) {
        ModeMessage message = new ModeMessage();
        message.dim = quarry.getWorld().provider.getDimension();
        message.pos = quarry.getPos();
        message.mode = quarry.G_getNow();
        return message;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) throws IOException {
        pos = buffer.readBlockPos();
        mode = buffer.readEnumValue(TileQuarry.Mode.class);
        dim = buffer.readInt();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeEnumValue(mode).writeInt(dim);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        WorldClient world = Minecraft.getMinecraft().world;
        if (world.provider.getDimension() == dim) {
            TileEntity entity = world.getTileEntity(pos);
            if (TileQuarry.class.isInstance(entity)) {
                TileQuarry quarry = (TileQuarry) entity;
                quarry.setNow(mode);
                quarry.G_renew_powerConfigure();
                IBlockState state = world.getBlockState(pos);
                world.notifyBlockUpdate(pos, state, state, 3);
            }
        }
        return null;
    }
}
