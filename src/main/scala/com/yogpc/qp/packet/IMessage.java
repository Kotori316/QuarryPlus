package com.yogpc.qp.packet;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.yogpc.qp.QuarryPlus;
import javax.annotation.Nullable;
import jp.t2v.lab.syntax.MapStreamSyntax;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
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

    static <T extends TileEntity> Optional<T> findTile(Supplier<NetworkEvent.Context> ctx, BlockPos pos, int dim, Class<T> aClass){
        return QuarryPlus.proxy.getPacketWorld(ctx.get())
            .filter(world -> world.getDimension().getType().getId() == dim)
            .filter(world -> world.isBlockLoaded(pos))
            .map(world -> world.getTileEntity(pos))
            .flatMap(MapStreamSyntax.optCast(aClass));
    }
}
