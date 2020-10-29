package com.kotori316.marker.packet;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import com.kotori316.marker.Tile16Marker;

/**
 * To server only.
 */
public class Button16Message {
    private BlockPos pos;
    private ResourceLocation dim;
    private int amount;
    private int yMax;
    private int yMin;

    @SuppressWarnings("unused")
    public Button16Message() {
    }

    public Button16Message(BlockPos pos, RegistryKey<World> dim, int amount, int yMax, int yMin) {
        this.pos = pos;
        this.dim = dim.getLocation();
        this.amount = amount;
        this.yMax = yMax;
        this.yMin = yMin;
    }

    public static Button16Message fromBytes(PacketBuffer p) {
        Button16Message message = new Button16Message();
        message.pos = p.readBlockPos();
        message.dim = p.readResourceLocation();
        message.amount = p.readInt();
        message.yMax = p.readInt();
        message.yMin = p.readInt();
        return message;
    }

    public void toBytes(PacketBuffer p) {
        p.writeBlockPos(pos).writeResourceLocation(dim);
        p.writeInt(amount);
        p.writeInt(yMax).writeInt(yMin);
    }

    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> Optional.ofNullable(ctx.get().getSender())
            .map(Entity::getEntityWorld)
            .map(world -> world.getTileEntity(this.pos))
            .filter(t -> t instanceof Tile16Marker && PacketHandler.getDimId(t.getWorld()).getLocation().equals(dim))
            .ifPresent(entity -> {
                Tile16Marker marker = (Tile16Marker) entity;
                marker.changeSize(this.amount, this.yMax, this.yMin);
                PacketHandler.sendToClient(new AreaMessage(this.pos, RegistryKey.getOrCreateKey(Registry.WORLD_KEY, dim), marker.min(), marker.max()), entity.getWorld());
            }));
    }

}
