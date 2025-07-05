package com.yogpc.qp.machine.storage;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.QpEntity;
import com.yogpc.qp.packet.ClientSync;
import com.yogpc.qp.packet.ClientSyncMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public final class DebugStorageEntity extends QpEntity implements ClientSync {
    @NotNull
    MachineStorage storage;

    public DebugStorageEntity(BlockPos pos, BlockState blockState) {
        super(pos, blockState);
        storage = MachineStorage.of();
        setStorage(storage);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        toClientTag(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        fromClientTag(input);
    }

    @Override
    public ValueOutput toClientTag(ValueOutput output) {
        output.store("storage", MachineStorage.CODEC.codec(), storage);
        return output;
    }

    @Override
    public void fromClientTag(ValueInput input) {
        setStorage(input.read("storage", MachineStorage.CODEC.codec()).orElseGet(MachineStorage::of));
    }

    void setStorage(MachineStorage storage) {
        this.storage = storage;
        this.storage.onUpdate(this::syncToOpeningPlayers);
        if (level != null && level.isClientSide()) {
            updateScreenList();
        }
    }

    /**
     * Client only
     */
    void updateScreenList() {
        var mc = Minecraft.getInstance();
        if (mc.screen instanceof DebugStorageScreen screen && screen.itemCountList != null) {
            screen.itemCountList.refreshEntries();
        }
    }

    private final Set<ServerPlayer> players = new HashSet<>();

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
