package com.yogpc.qp.packet.mover;

import java.util.Objects;
import java.util.function.Supplier;

import com.mojang.serialization.Dynamic;
import com.yogpc.qp.machines.base.EnchantmentFilter;
import com.yogpc.qp.machines.base.QuarryBlackList;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * To server only.
 */
public class EnchantmentMessage implements IMessage<EnchantmentMessage> {

    Type type;
    BlockPos pos;
    ResourceLocation dim;
    Enchantment enchantment;
    QuarryBlackList.Entry entry;

    public static EnchantmentMessage create(EnchantmentFilter.Accessor tile, Type type, Enchantment enchantment, QuarryBlackList.Entry entry) {
        EnchantmentMessage message = new EnchantmentMessage();
        message.pos = tile.getPos();
        message.dim = IMessage.getDimId(tile.getWorld());
        message.type = type;
        message.enchantment = enchantment;
        message.entry = entry;
        return message;
    }

    @Override
    public EnchantmentMessage readFromBuffer(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        type = buffer.readEnumValue(Type.class);
        enchantment = ForgeRegistries.ENCHANTMENTS.getValue(buffer.readResourceLocation());
        entry = QuarryBlackList.readEntry(new Dynamic<>(NBTDynamicOps.INSTANCE, buffer.readCompoundTag()));
        dim = buffer.readResourceLocation();
        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeEnumValue(type).writeResourceLocation(Objects.requireNonNull(enchantment.getRegistryName()))
            .writeCompoundTag(QuarryBlackList.Entry$.MODULE$.EntryToNBT().apply(entry)).writeResourceLocation(dim);

    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        IMessage.findTile(ctx, pos, dim, TileEntity.class)
            .flatMap(t -> com.yogpc.qp.package$.MODULE$.toJavaOption(EnchantmentFilter.Accessor$.MODULE$.apply(t)))
            .ifPresent(tile ->
                ctx.get().enqueueWork(() -> {
                    if (type == Type.Toggle) {
                        if (enchantment == Enchantments.FORTUNE) {
                            tile.enchantmentFilter_$eq(tile.enchantmentFilter().toggleFortune());
                        } else if (enchantment == Enchantments.SILK_TOUCH) {
                            tile.enchantmentFilter_$eq(tile.enchantmentFilter().toggleSilktouch());
                        }
                    } else if (type == Type.Remove) {
                        if (enchantment == Enchantments.FORTUNE)
                            tile.enchantmentFilter_$eq(tile.enchantmentFilter().removeFortune(entry));
                        else if (enchantment == Enchantments.SILK_TOUCH)
                            tile.enchantmentFilter_$eq(tile.enchantmentFilter().removeSilktouch(entry));
                    }
                })
            );
    }

    public enum Type {Toggle, Remove}
}
