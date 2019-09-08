package com.yogpc.qp.packet.advquarry;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileAdvQuarry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.logging.log4j.util.TriConsumer;

/**
 * To Server only.
 */
public class AdvActionMessage implements IMessage {
    private BlockPos pos;
    private int dim;
    private Actions action;
    private NBTTagCompound tag;

    public static AdvActionMessage create(TileAdvQuarry quarry, Actions action) {
        return create(quarry, action, new NBTTagCompound());
    }

    public static AdvActionMessage create(TileAdvQuarry quarry, Actions action, NBTTagCompound compound) {
        AdvActionMessage message = new AdvActionMessage();
        message.pos = quarry.getPos();
        message.dim = quarry.getWorld().provider.getDimension();
        message.action = action;
        message.tag = compound;
        return message;
    }


    @Override
    public void fromBytes(PacketBuffer buffer) throws IOException {
        pos = buffer.readBlockPos();
        dim = buffer.readInt();
        action = buffer.readEnumValue(Actions.class);
        tag = buffer.readCompoundTag();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim);
        buffer.writeEnumValue(action);
        buffer.writeCompoundTag(tag);
    }

    @Override
    public IMessage onReceive(IMessage message, MessageContext ctx) {
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        if (world.provider.getDimension() == dim) {
            TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof TileAdvQuarry) {
                TileAdvQuarry quarry = (TileAdvQuarry) entity;
                FMLCommonHandler.instance().getWorldThread(ctx.netHandler)
                    .addScheduledTask(action.runnable(quarry, tag, QuarryPlus.proxy.getPacketPlayer(ctx.netHandler)));
            }
        }
        return null;
    }

    public enum Actions {
        QUICK_START(TileAdvQuarry::noFrameStart),
        CHANGE_RANGE((quarry, rangeNBT) ->
            quarry.digRange_$eq(TileAdvQuarry.DigRange$.MODULE$.readFromNBT(rangeNBT))
        ),
        MODULE_INV((q, t, p) -> q.openModuleInv(p));;
        private final TriConsumer<TileAdvQuarry, NBTTagCompound, EntityPlayer> consumer;

        Actions(Consumer<TileAdvQuarry> consumer) {
            this.consumer = (quarry, nbtTagCompound, p) -> consumer.accept(quarry);
        }

        Actions(BiConsumer<TileAdvQuarry, NBTTagCompound> consumer) {
            this.consumer = (q, t, p) -> consumer.accept(q, t);
        }

        Actions(TriConsumer<TileAdvQuarry, NBTTagCompound, EntityPlayer> consumer) {
            this.consumer = consumer;
        }

        Runnable runnable(TileAdvQuarry quarry, NBTTagCompound compound, EntityPlayer player) {
            return () -> consumer.accept(quarry, compound, player);
        }
    }
}
