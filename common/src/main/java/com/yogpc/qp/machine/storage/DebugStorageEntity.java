package com.yogpc.qp.machine.storage;

import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.QpEntity;
import com.yogpc.qp.packet.ClientSync;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public final class DebugStorageEntity extends QpEntity implements ClientSync {
    @NotNull
    MachineStorage storage;

    public DebugStorageEntity(BlockPos pos, BlockState blockState) {
        super(pos, blockState);
        storage = new MachineStorage();
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
        storage = MachineStorage.CODEC.codec().parse(NbtOps.INSTANCE, tag.get("storage")).result().orElseGet(MachineStorage::new);
    }
}
