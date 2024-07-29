package com.yogpc.qp.machine.marker;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.QpBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class NormalMarkerEntity extends BlockEntity implements QuarryMarker {
    @NotNull
    private Status status = Status.NOT_CONNECTED;

    public NormalMarkerEntity(BlockPos pos, BlockState blockState) {
        super(PlatformAccess.getAccess().registerObjects().getBlockEntityType((QpBlock) blockState.getBlock()).orElseThrow(),
            pos, blockState);
    }

    @Override
    public Optional<Link> getLink() {
        return Optional.empty();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putString("status", status.name());
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        status = Status.valueOf(tag.getString("status"));
    }

    public enum Status {
        NOT_CONNECTED,
        RS_POWERED,
        CONNECTED_MASTER,
        CONNECTED_SLAVE,
        ;

        boolean isConnected() {
            return this == CONNECTED_MASTER || this == CONNECTED_SLAVE;
        }
    }
}
