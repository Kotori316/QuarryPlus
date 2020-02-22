package com.yogpc.qp.packet.filler;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileFiller;
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
public class FillerActionMessage implements IMessage {
    private BlockPos pos;
    private int dim;
    private Actions action;
    private NBTTagCompound tag = new NBTTagCompound();

    public static FillerActionMessage create(TileFiller filler, Actions action) {
        FillerActionMessage message = new FillerActionMessage();
        message.pos = filler.getPos();
        message.dim = filler.getWorld().provider.getDimension();
        message.action = action;
        return message;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        dim = buffer.readInt();
        action = buffer.readEnumValue(Actions.class);
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim);
        buffer.writeEnumValue(action);
    }

    @Override
    public IMessage onReceive(IMessage message, MessageContext ctx) {
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        if (world.provider.getDimension() == dim) {
            TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof TileFiller) {
                TileFiller filler = (TileFiller) entity;
                FMLCommonHandler.instance().getWorldThread(ctx.netHandler)
                    .addScheduledTask(action.runnable(filler, tag, QuarryPlus.proxy.getPacketPlayer(ctx.netHandler)));
            }
        }
        return null;
    }

    public enum Actions {
        MODULE_INV((f, c, p) -> {
            if (!f.getWorld().isRemote)
                p.openGui(QuarryPlus.instance(), QuarryPlusI.guiIdQuarryModule(), f.getWorld(), f.getPos().getX(), f.getPos().getY(), f.getPos().getZ());
        });
        private final TriConsumer<TileFiller, NBTTagCompound, EntityPlayer> consumer;

        Actions(Consumer<TileFiller> consumer) {
            this.consumer = (filler, nbtTagCompound, p) -> consumer.accept(filler);
        }

        Actions(BiConsumer<TileFiller, NBTTagCompound> consumer) {
            this.consumer = (q, t, p) -> consumer.accept(q, t);
        }

        Actions(TriConsumer<TileFiller, NBTTagCompound, EntityPlayer> consumer) {
            this.consumer = consumer;
        }

        Runnable runnable(TileFiller filler, NBTTagCompound compound, EntityPlayer player) {
            return () -> consumer.accept(filler, compound, player);
        }
    }
}
