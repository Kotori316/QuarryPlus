package com.yogpc.qp.machine.quarry;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.Area;
import com.yogpc.qp.machine.PickIterator;
import com.yogpc.qp.machine.PowerEntity;
import com.yogpc.qp.machine.QpBlockProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public abstract class QuarryEntity extends PowerEntity {
    public static final Marker MARKER = MarkerFactory.getMarker("quarry");
    @NotNull
    public Vec3 head;
    @NotNull
    public Vec3 targetHead;
    @NotNull
    QuarryState currentState;
    @Nullable
    private Area area;
    @Nullable
    private PickIterator<BlockPos> targetIterator;
    @Nullable
    BlockPos targetPos;

    protected QuarryEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        setMaxEnergy(10000 * ONE_FE);
        head = Vec3.atBottomCenterOf(pos);
        targetHead = head;
        currentState = QuarryState.FINISHED;
    }

    static void serverTick(Level level, BlockPos pos, BlockState state, QuarryEntity quarryEntity) {
        if (level.getGameTime() % 40 == 0) {
            QuarryPlus.LOGGER.info(MARKER, "{}, {}, {}, {}",
                quarryEntity.getBlockPos().toShortString(),
                quarryEntity.getEnergy() / ONE_FE,
                quarryEntity.currentState,
                quarryEntity.getArea()
            );
        }
        @NotNull
        Runnable a = switch (quarryEntity.currentState) {
            case FINISHED -> () -> {
            };
            case WAITING -> quarryEntity::waiting;
            case BREAK_INSIDE_FRAME -> quarryEntity::breakInsideFrame;
            case MAKE_FRAME -> quarryEntity::makeFrame;
            case MOVE_HEAD -> quarryEntity::moveHead;
            case BREAK_BLOCK -> quarryEntity::breakBlock;
            case REMOVE_FLUID -> quarryEntity::removeFluid;
            case FILLER -> quarryEntity::filler;
        };
        a.run();
    }

    static void clientTick(Level level, BlockPos pos, BlockState state, QuarryEntity quarryEntity) {
        quarryEntity.head = quarryEntity.targetHead;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("head", Vec3.CODEC.encodeStart(NbtOps.INSTANCE, this.head).getOrThrow());
        tag.putString("state", currentState.name());
        if (area != null) {
            tag.put("area", Area.CODEC.codec().encodeStart(NbtOps.INSTANCE, this.area).getOrThrow());
        }
        if (targetIterator != null) {
            tag.put("targetPos", BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, targetIterator.getLastReturned()).getOrThrow());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        Vec3.CODEC.parse(NbtOps.INSTANCE, tag.get("head")).ifSuccess(v -> this.targetHead = v);
        currentState = QuarryState.valueOf(tag.getString("state"));
        area = Area.CODEC.codec().parse(NbtOps.INSTANCE, tag.get("area")).result().orElse(null);
        var current = BlockPos.CODEC.parse(NbtOps.INSTANCE, tag.get("targetPos")).result().orElse(null);
        targetIterator = createTargetIterator(currentState, area, current);
        targetPos = current;
    }

    public void setArea(@Nullable Area area) {
        this.area = area;
        if (area != null) {
            this.head = new Vec3(area.maxX(), area.minY(), area.maxZ());
        }
    }

    public @Nullable Area getArea() {
        return area;
    }

    void setState(QuarryState state, BlockState blockState) {
        if (this.currentState != state) {
            this.currentState = state;
            if (level != null) {
                level.setBlock(getBlockPos(), blockState.setValue(QpBlockProperty.WORKING, QuarryState.isWorking(state)), Block.UPDATE_ALL);
            }
        }
    }

    void waiting() {
        if (getEnergy() > getMaxEnergy() / 200 && this.area != null) {
            setState(QuarryState.BREAK_INSIDE_FRAME, getBlockState());
        }
    }

    void breakInsideFrame() {
        setState(QuarryState.MAKE_FRAME, getBlockState());
    }

    void makeFrame() {
        if (level == null || level.isClientSide() || area == null) {
            return;
        }
        if (targetIterator == null) {
            targetIterator = createTargetIterator(currentState, getArea(), null);
            assert targetIterator != null;
        }
        if (targetPos == null) {
            targetPos = targetIterator.next();
        }

        var requiredEnergy = ONE_FE * 10;
        if (useEnergy(requiredEnergy, false, false, "makeFrame") == requiredEnergy) {
            if (level.getBlockState(targetPos).isAir()) {
                level.setBlock(targetPos, PlatformAccess.getAccess().registerObjects().frameBlock().get().defaultBlockState(), Block.UPDATE_ALL);
            }
            if (targetIterator.hasNext()) {
                targetPos = targetIterator.next();
            } else {
                targetIterator = null;
                targetPos = null;
                setState(QuarryState.MOVE_HEAD, getBlockState());
            }
        }
    }

    void moveHead() {
        if (level == null || level.isClientSide() || area == null) {
            return;
        }
        if (targetIterator == null) {
            targetIterator = createTargetIterator(currentState, getArea(), null);
            assert targetIterator != null;
        }
        if (targetPos == null) {
            targetPos = targetIterator.next();
            head = new Vec3(((double) area.minX() + area.maxX()) / 2, area.maxY(), ((double) area.minZ() + area.maxZ()) / 2);
            assert targetPos != null;
        }
        var digMinY = level.getMinBuildHeight() + 1;

        var availableEnergy = useEnergy(ONE_FE, true, false, "moveHead");
        var diff = new Vec3(targetPos.getX() - head.x, targetPos.getY() - head.y, targetPos.getZ() - head.z);
        var moveDistance = Math.min(diff.length(), (double) availableEnergy / ONE_FE);
    }

    void breakBlock() {
    }

    void removeFluid() {
    }

    void filler() {
        setState(QuarryState.FINISHED, getBlockState());
    }

    @Nullable
    static PickIterator<BlockPos> createTargetIterator(QuarryState state, @Nullable Area area, @Nullable BlockPos current) {
        if (area == null) return null;
        var itr = switch (state) {
            case MAKE_FRAME -> area.quarryFramePosIterator();
            case MOVE_HEAD, BREAK_BLOCK ->
                area.quarryDigPosIterator(current != null ? current.getY() : area.minY() - 1);
            default -> null;
        };
        if (itr != null && current != null) {
            itr.setLastReturned(current);
        }
        return itr;
    }
}
