package com.yogpc.qp.packet;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.yogpc.qp.QuarryPlus;
import javax.annotation.Nullable;
import jp.t2v.lab.syntax.MapStreamSyntax;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public interface IMessage<T extends IMessage<T>> {
    static ResourceLocation getDimId(@Nullable World world) {
        return Optional.ofNullable(world)
            .map(World::func_234923_W_)
            .orElse(World.field_234918_g_)
            .func_240901_a_();
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

    static <T extends TileEntity> Optional<T> findTile(Supplier<NetworkEvent.Context> ctx, BlockPos pos, ResourceLocation dim, Class<T> aClass) {
        return QuarryPlus.proxy.getPacketWorld(ctx.get())
            .filter(world -> world.func_234923_W_().func_240901_a_().equals(dim))
            .filter(world -> world.isBlockPresent(pos))
            .map(world -> world.getTileEntity(pos))
            .flatMap(MapStreamSyntax.optCast(aClass));
    }
}
