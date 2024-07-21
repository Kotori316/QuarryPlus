package com.yogpc.qp.machine.quarry;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.*;
import com.yogpc.qp.packet.ClientSync;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public abstract class QuarryEntity extends PowerEntity implements ClientSync {
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

    static PowerMap.Quarry powerMap() {
        return PlatformAccess.getConfig().getPowerMap().quarry();
    }

    @SuppressWarnings("unused")
    static void serverTick(Level level, BlockPos pos, BlockState state, QuarryEntity quarryEntity) {
        if (level.getGameTime() % 40 == 0) {
            QuarryPlus.LOGGER.info(MARKER, "{}, {}, {}, {}, {}",
                quarryEntity.getBlockPos().toShortString(),
                quarryEntity.getEnergy() / ONE_FE,
                quarryEntity.currentState,
                quarryEntity.getArea(),
                quarryEntity.head
            );
        }
        if (quarryEntity.getEnergy() <= 0) {
            return;
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

    @SuppressWarnings("unused")
    static void clientTick(Level level, BlockPos pos, BlockState state, QuarryEntity quarryEntity) {
        if (level.getGameTime() % 40 == 0) {
            QuarryPlus.LOGGER.info(MARKER, "CLIENT {}, {}",
                quarryEntity.head,
                quarryEntity.targetHead
            );
        }
        quarryEntity.head = quarryEntity.targetHead;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        toClientTag(tag);
        if (targetIterator != null) {
            tag.put("targetPos", BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, targetIterator.getLastReturned()).getOrThrow());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fromClientTag(tag);
        // In server, head must be loaded from nbt
        Vec3.CODEC.parse(NbtOps.INSTANCE, tag.get("head")).ifSuccess(v -> this.head = v);
        var current = BlockPos.CODEC.parse(NbtOps.INSTANCE, tag.get("targetPos")).result().orElse(null);
        targetIterator = createTargetIterator(currentState, area, current);
        targetPos = current;
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        tag.put("head", Vec3.CODEC.encodeStart(NbtOps.INSTANCE, this.head).getOrThrow());
        tag.putString("state", currentState.name());
        if (area != null) {
            tag.put("area", Area.CODEC.codec().encodeStart(NbtOps.INSTANCE, this.area).getOrThrow());
        }
        return tag;
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        // Set head as targetHead to move drill smoothly
        Vec3.CODEC.parse(NbtOps.INSTANCE, tag.get("head")).ifSuccess(v -> this.targetHead = v);
        currentState = QuarryState.valueOf(tag.getString("state"));
        area = Area.CODEC.codec().parse(NbtOps.INSTANCE, tag.get("area")).result().orElse(null);
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
            syncToClient();
            if (level != null) {
                level.setBlock(getBlockPos(), blockState.setValue(QpBlockProperty.WORKING, QuarryState.isWorking(state)), Block.UPDATE_ALL);
            }
        }
    }

    public String renderMode() {
        return switch (this.currentState) {
            case WAITING, BREAK_INSIDE_FRAME, MAKE_FRAME -> "frame";
            case BREAK_BLOCK, MOVE_HEAD, REMOVE_FLUID -> "drill";
            default -> "none";
        };
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

        var requiredEnergy = (long) (ONE_FE * powerMap().makeFrame());
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

        var diff = new Vec3(targetPos.getX() - head.x, targetPos.getY() - head.y, targetPos.getZ() - head.z);
        var difLength = diff.length();
        if (difLength > 1e-7) {
            var defaultEnergy = (long) (ONE_FE * powerMap().moveHeadBase());
            var availableEnergy = useEnergy(defaultEnergy, true, false, "moveHead");
            var moveDistance = Math.min(difLength, (double) availableEnergy / ONE_FE);
            useEnergy((long) (moveDistance * ONE_FE), false, true, "moveHead");
            head = head.add(diff.scale(moveDistance / difLength));
            this.syncToClient();
        }

        if (targetPos.distToLowCornerSqr(head.x, head.y, head.z) <= 1e-7) {
            setState(QuarryState.BREAK_BLOCK, getBlockState());
            breakBlock();
        }
    }

    void breakBlock() {
        if (level == null || level.isClientSide() || area == null) {
            return;
        }
        if (targetIterator == null) {
            targetIterator = createTargetIterator(currentState, getArea(), null);
            assert targetIterator != null;
        }
        if (targetPos == null) {
            throw new IllegalStateException("How to break block with targetPos is null?");
        }

        var fluid = level.getFluidState(targetPos);
        if (!fluid.isEmpty()) {
            if (shouldRemoveFluid()) {
                setState(QuarryState.REMOVE_FLUID, getBlockState());
                removeFluid();
                return;
            }
            // Skip this pos
            if (targetIterator.hasNext()) {
                targetPos = targetIterator.next();
            } else {
                setNextDigTargetIterator();
            }
            setState(QuarryState.MOVE_HEAD, getBlockState());
            return;
        }

        var result = breakBlock(targetPos);
        if (result.isSuccess()) {
            if (targetIterator.hasNext()) {
                targetPos = targetIterator.next();
            } else {
                setNextDigTargetIterator();
            }
            setState(QuarryState.MOVE_HEAD, getBlockState());
        }
    }

    private void setNextDigTargetIterator() {
        var minY = digMinY();
        if (targetPos == null) {
            throw new IllegalStateException("Target pos is null");
        }
        assert area != null;
        if (minY < targetPos.getY()) {
            // Go next y
            targetIterator = area.quarryDigPosIterator(targetPos.getY() - 1);
            targetPos = targetIterator.next();
        } else {
            // Finish
            setState(QuarryState.FINISHED, getBlockState());
        }
    }

    void removeFluid() {
    }

    void filler() {
        setState(QuarryState.FINISHED, getBlockState());
    }

    @Nullable
    static PickIterator<BlockPos> createTargetIterator(QuarryState state, @Nullable Area area, @Nullable BlockPos lastReturned) {
        if (area == null) return null;
        var itr = switch (state) {
            case MAKE_FRAME -> area.quarryFramePosIterator();
            case MOVE_HEAD, BREAK_BLOCK ->
                area.quarryDigPosIterator(lastReturned != null ? lastReturned.getY() : area.minY() - 1);
            default -> null;
        };
        if (itr != null && lastReturned != null) {
            itr.setLastReturned(lastReturned);
        }
        return itr;
    }

    @NotNull
    WorkResult breakBlock(BlockPos target) {
        assert level != null;
        var state = level.getBlockState(target);
        var blockEntity = level.getBlockEntity(target);
        var player = getQuarryFakePlayer((ServerLevel) level, target);
        var hardness = state.getDestroySpeed(level, target);
        // First check event
        var eventCancelled = checkBreakEvent(level, player, state, target, blockEntity);
        if (eventCancelled) {
            return WorkResult.FAIL_EVENT;
        }
        // Second, check modules
        var moduleResult = breakBlockModuleOverride(level, state, target, hardness);
        if (moduleResult != WorkResult.SKIPPED) {
            return moduleResult;
        }

        if (hardness < 0) {
            // Unbreakable
            return WorkResult.SKIPPED;
        }
        var requiredEnergy = powerMap().getBreakEnergy(hardness, 0, 0, 0, false);
        if (useEnergy(requiredEnergy, true, false, "breakBlock") == requiredEnergy) {
            useEnergy(requiredEnergy, false, false, "breakBlock");
            QuarryPlus.LOGGER.info(MARKER, "Breaking block {}", target.toShortString());
            level.setBlock(targetPos, stateAfterBreak(level, target, state), Block.UPDATE_ALL);
            afterBreak(level, player, state, target, blockEntity);
            return WorkResult.SUCCESS;
        } else {
            return WorkResult.NOT_ENOUGH_ENERGY;
        }
    }

    /**
     * @return {@code true} if event is cancelled. {@code false} if event is successfully accepted.
     */
    protected abstract boolean checkBreakEvent(Level level, ServerPlayer fakePlayer, BlockState state, BlockPos target, @Nullable BlockEntity blockEntity);

    protected abstract void afterBreak(Level level, ServerPlayer fakePlayer, BlockState state, BlockPos target, @Nullable BlockEntity blockEntity);

    WorkResult breakBlockModuleOverride(Level level, BlockState state, BlockPos target, float hardness) {
        return WorkResult.SKIPPED;
    }

    protected abstract ServerPlayer getQuarryFakePlayer(ServerLevel level, BlockPos target);

    boolean shouldRemoveFluid() {
        return true;
    }

    int digMinY() {
        if (level == null) return 0;
        return level.getMinBuildHeight() + 1;
    }

    BlockState stateAfterBreak(Level level, BlockPos pos, BlockState before) {
        return Blocks.AIR.defaultBlockState();
    }
}
