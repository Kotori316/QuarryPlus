package com.yogpc.qp.machines.mover;

import java.util.function.Supplier;

import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * To Server only.
 */
public final class MoverMessage implements IMessage {
    private final BlockPos pos;
    private final int windowId;
    private final ResourceLocation enchantment;

    public MoverMessage(BlockPos pos, int windowId, Enchantment enchantment) {
        this.pos = pos;
        this.windowId = windowId;
        this.enchantment = enchantment.getRegistryName();
    }

    public MoverMessage(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.windowId = buf.readInt();
        this.enchantment = buf.readResourceLocation();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(windowId);
        buf.writeResourceLocation(enchantment);
    }

    public static void onReceive(MoverMessage message, Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() ->
            PacketHandler.getPlayer(supplier.get())
                .map(p -> p.containerMenu)
                .filter(m -> m.containerId == message.windowId)
                .flatMap(MapMulti.optCast(ContainerMover.class))
                .ifPresent(c -> c.moveEnchant(ForgeRegistries.ENCHANTMENTS.getValue(message.enchantment))));
    }
}
