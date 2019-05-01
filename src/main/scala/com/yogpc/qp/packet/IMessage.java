package com.yogpc.qp.packet;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.NetworkEvent;

public interface IMessage<T extends IMessage<T>> {
    static int getDimId(@Nullable World world) {
        return Optional.ofNullable(world)
            .map(World::getDimension)
            .map(Dimension::getType)
            .map(DimensionType::getId)
            .orElse(0);
    }

    static <T extends IMessage<T>> Function<PacketBuffer, T> decode(Supplier<T> supplier) {
        return buffer -> supplier.get().readFromBuffer(buffer);
    }

    T readFromBuffer(PacketBuffer buffer);

    void writeToBuffer(PacketBuffer buffer);

    void onReceive(Supplier<NetworkEvent.Context> ctx);

    default void onReceiveInternal(Supplier<NetworkEvent.Context> ctx) {
        onReceive(ctx);
        ctx.get().setPacketHandled(true);
    }
}
