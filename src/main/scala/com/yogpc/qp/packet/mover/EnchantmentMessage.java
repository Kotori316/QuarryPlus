package com.yogpc.qp.packet.mover;

import java.util.Objects;
import java.util.function.Supplier;

import com.yogpc.qp.machines.quarry.TileBasic;
import com.yogpc.qp.machines.workbench.BlockData;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Enchantments;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * To server only.
 */
public class EnchantmentMessage implements IMessage<EnchantmentMessage> {

    Type type;
    BlockPos pos;
    int dim;
    Enchantment enchantment;
    BlockData data;

    public static EnchantmentMessage create(TileBasic tile, Type type, Enchantment enchantment, BlockData data) {
        EnchantmentMessage message = new EnchantmentMessage();
        message.pos = tile.getPos();
        message.dim = IMessage.getDimId(tile.getWorld());
        message.type = type;
        message.enchantment = enchantment;
        message.data = data;
        return message;
    }

    @Override
    public EnchantmentMessage readFromBuffer(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        type = buffer.readEnumValue(Type.class);
        enchantment = ForgeRegistries.ENCHANTMENTS.getValue(buffer.readResourceLocation());
        data = BlockData.read(buffer.readCompoundTag());
        dim = buffer.readInt();
        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeEnumValue(type).writeResourceLocation(Objects.requireNonNull(enchantment.getRegistryName()))
            .writeCompoundTag(data.write(new NBTTagCompound())).writeInt(dim);

    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        IMessage.findTile(ctx, pos, dim, TileBasic.class).ifPresent(tile ->
            ctx.get().enqueueWork(() -> {
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
            })
        );
    }

    public enum Type {Toggle, Remove}
}
