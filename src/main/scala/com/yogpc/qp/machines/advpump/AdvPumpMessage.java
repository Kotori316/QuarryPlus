package com.yogpc.qp.machines.advpump;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * To server only
 */
public final class AdvPumpMessage implements IMessage {
    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final boolean placeFrame;
    private final boolean deleteFluid;

    AdvPumpMessage(BlockPos pos, ResourceKey<Level> dim, boolean placeFrame, boolean deleteFluid) {
        this.pos = pos;
        this.dim = dim;
        this.placeFrame = placeFrame;
        this.deleteFluid = deleteFluid;
    }

    AdvPumpMessage(TileAdvPump advPump) {
        this(advPump.getBlockPos(), Objects.requireNonNull(advPump.getLevel()).dimension(), advPump.placeFrame, advPump.deleteFluid);
    }

    public AdvPumpMessage(FriendlyByteBuf buf) {
        this(
            buf.readBlockPos(),
            ResourceKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation()),
            buf.readBoolean(),
            buf.readBoolean()
        );
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos).writeResourceLocation(dim.location());
        buf.writeBoolean(placeFrame).writeBoolean(deleteFluid);
    }

    public static void onReceive(AdvPumpMessage message, Supplier<NetworkEvent.Context> supplier) {
        var level = PacketHandler.getWorld(supplier.get(), message.pos, message.dim);
        supplier.get().enqueueWork(() ->
            level.flatMap(l -> l.getBlockEntity(message.pos, Holder.ADV_PUMP_TYPE))
                .ifPresentOrElse(pump -> {
                        pump.deleteFluid = message.deleteFluid;
                        pump.placeFrame = message.placeFrame;
                    }, () ->
                        QuarryPlus.LOGGER.warn("{} can't find tile at {} in {}", message.getClass().getName(), message.pos, message.dim)
                )
        );
    }
}
