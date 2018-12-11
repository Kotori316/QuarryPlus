package com.yogpc.qp.packet.enchantment;

import java.io.IOException;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileBasic;
import com.yogpc.qp.utils.BlockData;
import com.yogpc.qp.version.VersionUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Enchantments;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * To server only.
 */
public class EnchantmentMessage implements IMessage {

    Type type;
    BlockPos pos;
    int dim;
    Enchantment enchantment;
    BlockData data;

    public static EnchantmentMessage create(TileBasic tile, Type type, Enchantment enchantment, BlockData data) {
        EnchantmentMessage message = new EnchantmentMessage();
        message.pos = tile.getPos();
        message.dim = tile.getWorld().provider.getDimension();
        message.type = type;
        message.enchantment = enchantment;
        message.data = data;
        return message;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) throws IOException {
        pos = buffer.readBlockPos();
        type = buffer.readEnumValue(Type.class);
        enchantment = Enchantment.getEnchantmentByLocation(buffer.readString(Short.MAX_VALUE));
        data = BlockData.readFromNBT(buffer.readCompoundTag());
        dim = buffer.readInt();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeEnumValue(type).writeString(VersionUtil.getRegistryName(enchantment).toString())
            .writeCompoundTag(data.writeToNBT(new NBTTagCompound())).writeInt(dim);
    }

    @Override
    public IMessage onReceive(IMessage message, MessageContext ctx) {
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        MinecraftServer server = world.getMinecraftServer();
        if (world.provider.getDimension() == dim && server != null) {
            TileBasic tile = (TileBasic) world.getTileEntity(pos);
            if (tile != null) {
                server.addScheduledTask(() -> {
                    if (type == Type.Toggle) {
                        if (enchantment == Enchantments.FORTUNE) {
                            tile.fortuneInclude = !tile.fortuneInclude;
                        } else if (enchantment == Enchantments.SILK_TOUCH) {
                            tile.silktouchInclude = !tile.silktouchInclude;
                        }
                    } else if (type == Type.Remove) {
                        if (enchantment == Enchantments.FORTUNE)
                            tile.fortuneList.remove(data);
                        else if (enchantment == Enchantments.SILK_TOUCH)
                            tile.silktouchList.remove(data);
                    }
                });
            }
        }
        return null;
    }

    public enum Type {Toggle, Remove}
}
