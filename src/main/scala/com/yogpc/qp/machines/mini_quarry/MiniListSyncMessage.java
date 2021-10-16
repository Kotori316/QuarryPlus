package com.yogpc.qp.machines.mini_quarry;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.yogpc.qp.packet.IMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public final class MiniListSyncMessage implements IMessage {
    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final List<BlockStatePredicate> allowList;
    private final List<BlockStatePredicate> denyList;

    public MiniListSyncMessage(BlockPos pos, ResourceKey<Level> dim, List<BlockStatePredicate> allowList, List<BlockStatePredicate> denyList) {
        this.pos = pos;
        this.dim = dim;
        this.allowList = allowList;
        this.denyList = denyList;
    }

    public MiniListSyncMessage(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.dim = ResourceKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation());
        int allowListSize = buf.readInt();
        this.allowList = Stream.generate(buf::readNbt)
            .limit(allowListSize)
            .filter(Objects::nonNull)
            .map(BlockStatePredicate::fromTag)
            .toList();
        int denyListSize = buf.readInt();
        this.denyList = Stream.generate(buf::readNbt)
            .limit(denyListSize)
            .filter(Objects::nonNull)
            .map(BlockStatePredicate::fromTag)
            .toList();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos).writeResourceLocation(dim.location());
        buf.writeInt(allowList.size());
        allowList.stream().map(BlockStatePredicate::toTag).forEach(buf::writeNbt);
        buf.writeInt(denyList.size());
        denyList.stream().map(BlockStatePredicate::toTag).forEach(buf::writeNbt);
    }

    public static void onReceive(MiniListSyncMessage message, Supplier<NetworkEvent.Context> supplier) {

    }
}
