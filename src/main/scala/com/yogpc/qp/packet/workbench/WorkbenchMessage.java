package com.yogpc.qp.packet.workbench;

import java.io.IOException;

import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileWorkbench;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * To server only.
 */
public class WorkbenchMessage implements IMessage {
    BlockPos pos;
    int dim;
    int recipeIndex;

    public static WorkbenchMessage create(TileWorkbench workbench, int recipeIndex) {
        WorkbenchMessage message = new WorkbenchMessage();
        message.pos = workbench.getPos();
        message.dim = workbench.getWorld().provider.getDimension();
        message.recipeIndex = recipeIndex;
        return message;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) throws IOException {
        pos = buffer.readBlockPos();
        dim = buffer.readInt();
        recipeIndex = buffer.readInt();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim).writeInt(recipeIndex);
    }

    @Override
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        World world = ctx.getServerHandler().player.getEntityWorld();
        if (world.provider.getDimension() == dim) {
            TileEntity entity = world.getTileEntity(pos);
            if (TileWorkbench.class.isInstance(entity)) {
                TileWorkbench workbench = (TileWorkbench) entity;
                workbench.setCurrentRecipe(recipeIndex);
            }
        }
        return null;
    }
}
