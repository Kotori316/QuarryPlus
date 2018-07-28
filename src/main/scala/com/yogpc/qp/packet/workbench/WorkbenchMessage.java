package com.yogpc.qp.packet.workbench;

import java.io.IOException;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileWorkbench;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
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
    public void fromBytes(PacketBuffer buffer) {
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
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        MinecraftServer server = world.getMinecraftServer();
        if (world.provider.getDimension() == dim && server != null) {
            TileEntity entity = world.getTileEntity(pos);
            if (TileWorkbench.class.isInstance(entity)) {
                TileWorkbench workbench = (TileWorkbench) entity;
                server.addScheduledTask(() -> workbench.setCurrentRecipeIndex(recipeIndex));
            }
        }
        return null;
    }
}
