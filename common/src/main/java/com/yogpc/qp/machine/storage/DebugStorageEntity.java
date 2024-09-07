package com.yogpc.qp.machine.storage;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.QpEntity;
import com.yogpc.qp.packet.ClientSync;
import com.yogpc.qp.packet.ClientSyncMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public final class DebugStorageEntity extends QpEntity implements ClientSync {
    @NotNull
    MachineStorage storage;

    public DebugStorageEntity(BlockPos pos, BlockState blockState) {
        super(pos, blockState);
        storage = MachineStorage.of();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        toClientTag(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fromClientTag(tag, registries);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("storage", MachineStorage.CODEC.codec().encodeStart(NbtOps.INSTANCE, storage).getOrThrow());
        return tag;
    }

    @Override
    public void fromClientTag(CompoundTag tag, HolderLookup.Provider registries) {
        storage = MachineStorage.CODEC.codec().parse(NbtOps.INSTANCE, tag.get("storage")).result().orElseGet(MachineStorage::of);
    }

    private Set<ServerPlayer> players = new HashSet<>();

    void startOpen(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            players.add(serverPlayer);
        }
    }

    void stopOpen(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            players.remove(serverPlayer);
        }
    }

    void syncToOpeningPlayers() {
        var message = new ClientSyncMessage(this);
        for (ServerPlayer player : players) {
            PlatformAccess.getAccess().packetHandler().sendToClientPlayer(message, player);
        }
    }
}
