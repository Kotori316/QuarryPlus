package com.kotori316.marker.packet;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import com.kotori316.marker.TileFlexMarker;

/**
 * To server only.
 */
public class ButtonMessage {
    private BlockPos pos;
    private RegistryKey<World> dim;
    private TileFlexMarker.Movable movable;
    private int amount;

    @SuppressWarnings("unused")
    public ButtonMessage() {
    }

    public ButtonMessage(BlockPos pos, RegistryKey<World> dim, TileFlexMarker.Movable movable, int amount) {
        this.pos = pos;
        this.dim = dim;
        this.movable = movable;
        this.amount = amount;
    }

    public static ButtonMessage fromBytes(PacketBuffer p) {
        ButtonMessage message = new ButtonMessage();
        message.pos = p.readBlockPos();
        message.dim = RegistryKey.func_240903_a_(Registry.WORLD_KEY, p.readResourceLocation());
        message.movable = p.readEnumValue(TileFlexMarker.Movable.class);
        message.amount = p.readInt();
        return message;
    }

    public void toBytes(PacketBuffer p) {
        p.writeBlockPos(pos).writeResourceLocation(dim.func_240901_a_());
        p.writeEnumValue(movable).writeInt(amount);
    }

    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> Optional.ofNullable(ctx.get().getSender())
            .map(Entity::getEntityWorld)
            .map(world -> world.getTileEntity(this.pos))
            .filter(t -> t instanceof TileFlexMarker && PacketHandler.getDimId(t.getWorld()) == dim)
            .ifPresent(entity -> {
                TileFlexMarker marker = (TileFlexMarker) entity;
                marker.move(this.movable, this.amount);
                PacketHandler.sendToClient(new AreaMessage(this.pos, this.dim, marker.min(), marker.max()), marker.getWorld());
            }));
    }
}
