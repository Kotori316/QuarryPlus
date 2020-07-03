package com.kotori316.marker.packet;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import com.kotori316.marker.IAreaConfigurable;

/**
 * To Client Only
 */
public class AreaMessage {
    private BlockPos pos;
    private ResourceLocation dim;
    private BlockPos min, max;

    @SuppressWarnings("unused")
    public AreaMessage() {
    }

    public AreaMessage(BlockPos pos, RegistryKey<World> dim, BlockPos min, BlockPos max) {
        this.pos = pos;
        this.dim = dim.func_240901_a_();
        this.min = min;
        this.max = max;
    }

    public static AreaMessage fromBytes(PacketBuffer p) {
        AreaMessage message = new AreaMessage();
        message.pos = p.readBlockPos();
        message.dim = p.readResourceLocation();
        message.min = p.readBlockPos();
        message.max = p.readBlockPos();
        return message;
    }

    public void toBytes(PacketBuffer p) {
        p.writeBlockPos(pos).writeResourceLocation(dim);
        p.writeBlockPos(min).writeBlockPos(max);
    }

    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        Optional.ofNullable(Minecraft.getInstance().world)
            .map(world -> world.getTileEntity(this.pos))
            .filter(t -> t instanceof IAreaConfigurable && PacketHandler.getDimId(t.getWorld()) == RegistryKey.func_240903_a_(Registry.field_239699_ae_, dim))
            .ifPresent(entity -> {
                IAreaConfigurable marker = (IAreaConfigurable) entity;
                ctx.get().enqueueWork(marker.setMinMax(this.min, this.max));
            });
        ctx.get().setPacketHandled(true);
    }
}
