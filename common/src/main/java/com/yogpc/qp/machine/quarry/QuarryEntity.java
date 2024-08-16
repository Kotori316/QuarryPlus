package com.yogpc.qp.machine.quarry;

import com.google.common.collect.Sets;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.*;
import com.yogpc.qp.machine.misc.DigMinY;
import com.yogpc.qp.machine.module.ModuleInventory;
import com.yogpc.qp.machine.module.QuarryModule;
import com.yogpc.qp.machine.module.QuarryModuleProvider;
import com.yogpc.qp.packet.ClientSync;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

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
    @NotNull
    private Set<BlockPos> skipped = new HashSet<>();
    @Nullable
    BlockPos targetPos;
    @NotNull
    MachineStorage storage;
    @NotNull
    public DigMinY digMinY = new DigMinY();
    @NotNull
    ItemEnchantments enchantments = ItemEnchantments.EMPTY;
    @NotNull
    final EnchantmentCache enchantmentCache = new EnchantmentCache();
    @NotNull
    Set<QuarryModule> modules = Collections.emptySet();
    @NotNull
    final ModuleInventory moduleInventory = new ModuleInventory(5, q -> true, m -> modules);

    protected QuarryEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        setMaxEnergy(10000 * ONE_FE);
        head = Vec3.atBottomCenterOf(pos);
        targetHead = head;
        currentState = QuarryState.FINISHED;
        storage = new MachineStorage();
        moduleInventory.addListener(container -> setChanged());
    }

    static PowerMap.Quarry powerMap() {
        return PlatformAccess.config().powerMap().quarry();
    }

    @Override
    public Stream<MutableComponent> checkerLogs() {
        return Stream.concat(
            super.checkerLogs(),
            Stream.of(
                detail(ChatFormatting.GREEN, "State", currentState.name()),
                detail(ChatFormatting.GREEN, "Area", String.valueOf(area)),
                detail(ChatFormatting.GREEN, "Head", String.valueOf(head)),
                detail(ChatFormatting.GREEN, "Storage", String.valueOf(storage)),
                detail(ChatFormatting.GREEN, "DigMinY", String.valueOf(digMinY.getMinY(level))),
                detail(ChatFormatting.GREEN, "Modules", String.valueOf(modules))
            )
        );
    }

    @SuppressWarnings("unused")
    static void serverTick(Level level, BlockPos pos, BlockState state, QuarryEntity quarryEntity) {
        if (!quarryEntity.hasEnoughEnergy()) {
            return;
        }
        switch (quarryEntity.currentState) {
            case FINISHED -> {
            }
            case WAITING -> quarryEntity.waiting();
            case BREAK_INSIDE_FRAME -> quarryEntity.breakInsideFrame();
            case MAKE_FRAME -> quarryEntity.makeFrame();
            case MOVE_HEAD -> quarryEntity.moveHead();
            case BREAK_BLOCK -> quarryEntity.breakBlock();
            case REMOVE_FLUID -> quarryEntity.removeFluid();
            case FILLER -> quarryEntity.filler();
        }
    }

    @SuppressWarnings("unused")
    static void clientTick(Level level, BlockPos pos, BlockState state, QuarryEntity quarryEntity) {
        quarryEntity.head = quarryEntity.targetHead;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        toClientTag(tag, registries);
        if (targetIterator != null) {
            tag.put("targetPos", BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, targetIterator.getLastReturned()).getOrThrow());
        }
        tag.put("storage", MachineStorage.CODEC.codec().encodeStart(NbtOps.INSTANCE, storage).getOrThrow());
        tag.putLongArray("skipped", skipped.stream().mapToLong(BlockPos::asLong).toArray());
        tag.put("moduleInventory", moduleInventory.createTag(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fromClientTag(tag, registries);
        // In server, head must be loaded from nbt
        Vec3.CODEC.parse(NbtOps.INSTANCE, tag.get("head")).ifSuccess(v -> this.head = v);
        var current = BlockPos.CODEC.parse(NbtOps.INSTANCE, tag.get("targetPos")).result().orElse(null);
        targetIterator = createTargetIterator(currentState, area, current);
        targetPos = current;
        storage = MachineStorage.CODEC.codec().parse(NbtOps.INSTANCE, tag.get("storage")).result().orElse(new MachineStorage());
        skipped = LongStream.of(tag.getLongArray("skipped")).mapToObj(BlockPos::of).collect(Collectors.toCollection(HashSet::new));
        moduleInventory.fromTag(tag.getList("moduleInventory", Tag.TAG_COMPOUND), registries);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("head", Vec3.CODEC.encodeStart(NbtOps.INSTANCE, this.head).getOrThrow());
        tag.putString("state", currentState.name());
        if (area != null) {
            tag.put("area", Area.CODEC.codec().encodeStart(NbtOps.INSTANCE, this.area).getOrThrow());
        }
        tag.put("digMinY", DigMinY.CODEC.codec().encodeStart(NbtOps.INSTANCE, digMinY).getOrThrow());
        return tag;
    }

    @Override
    public void fromClientTag(CompoundTag tag, HolderLookup.Provider registries) {
        // Set head as targetHead to move drill smoothly
        Vec3.CODEC.parse(NbtOps.INSTANCE, tag.get("head")).ifSuccess(v -> this.targetHead = v);
        currentState = QuarryState.valueOf(tag.getString("state"));
        area = Area.CODEC.codec().parse(NbtOps.INSTANCE, tag.get("area")).result().orElse(null);
        digMinY = DigMinY.CODEC.codec().parse(NbtOps.INSTANCE, tag.get("digMinY")).result().orElseGet(DigMinY::new);
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        enchantments = componentInput.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(DataComponents.ENCHANTMENTS, enchantments);
    }

    @Override
    public void saveToItem(ItemStack stack, HolderLookup.Provider registries) {
        // Not to save NBT, as it causes crash
        stack.applyComponents(this.collectComponents());
    }

    @Override
    public void setChanged() {
        super.setChanged();
        updateModules();
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

    void updateModules() {
        if (level == null) {
            // In test?
            this.modules = moduleInventory.getModules();
        } else {
            this.modules = Sets.union(
                moduleInventory.getModules(),
                QuarryModuleProvider.Block.getModulesInWorld(level, getBlockPos())
            );
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

        var state = level.getBlockState(targetPos);
        if (state.is(PlatformAccess.getAccess().registerObjects().frameBlock().get())) {
            // Do nothing if frame is already placed
            if (targetIterator.hasNext()) {
                targetPos = targetIterator.next();
                makeFrame();
            } else {
                targetIterator = null;
                targetPos = null;
                setState(QuarryState.MOVE_HEAD, getBlockState());
            }
            return;
        }

        if (!getBlockPos().equals(targetPos) && !state.isAir()) {
            var result = breakBlock(targetPos);
            if (!result.isSuccess()) {
                // Wait until quarry can remove the block
                return;
            }
            if (result != WorkResult.SUCCESS) {
                skipped.add(targetPos.immutable());
            }
        }

        var requiredEnergy = (long) (ONE_FE * powerMap().makeFrame());
        if (useEnergy(requiredEnergy, true, false, "makeFrame") == requiredEnergy) {
            useEnergy(requiredEnergy, false, false, "makeFrame");
            if (!targetPos.equals(getBlockPos())) {
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
            targetPos = getNextValidTarget();
            head = new Vec3(((double) area.minX() + area.maxX()) / 2, area.maxY(), ((double) area.minZ() + area.maxZ()) / 2);
            if (targetPos == null) {
                return;
            }
        }

        var diff = new Vec3(targetPos.getX() - head.x, targetPos.getY() - head.y, targetPos.getZ() - head.z);
        var difLength = diff.length();
        if (difLength > 1e-7) {
            var defaultEnergy = (long) (ONE_FE * powerMap().moveHeadBase() * moveHeadFactor());
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

    double moveHeadFactor() {
        assert level != null;
        var efficiency = enchantmentCache.getLevel(enchantments, Enchantments.EFFICIENCY, level.registryAccess().asGetterLookup());
        if (efficiency >= 4) {
            return efficiency - 3;
        } else {
            return Math.pow(4, efficiency / 4d - 1d);
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
            targetPos = getNextValidTarget();
            if (targetPos == null) {
                // Finished
                return;
            }
            setState(QuarryState.MOVE_HEAD, getBlockState());
            return;
        }

        var result = breakBlock(targetPos);
        if (result.isSuccess()) {
            if (result != WorkResult.SUCCESS) {
                skipped.add(targetPos.immutable());
            }
            targetPos = getNextValidTarget();
            if (targetPos == null) {
                // Finished
                return;
            }
            setState(QuarryState.MOVE_HEAD, getBlockState());
        }
    }

    /**
     * @return {@code true} if finished
     */
    private boolean setNextDigTargetIterator() {
        if (targetPos == null) {
            throw new IllegalStateException("Target pos is null");
        }
        assert area != null;
        assert level != null;
        if (shouldRemoveFluid()) {
            // Check fluids in this y
            var fluidPos = BlockPos.betweenClosedStream(
                    area.minX() + 1, targetPos.getY(), area.minZ() + 1,
                    area.maxX() - 1, targetPos.getY(), area.maxZ() - 1)
                .filter(p -> !skipped.contains(p))
                .filter(p -> !level.getFluidState(p).isEmpty())
                .findAny()
                .orElse(null);
            if (fluidPos != null) {
                targetIterator = new PickIterator.Single<>(fluidPos);
                setState(QuarryState.REMOVE_FLUID, getBlockState());
                return false;
            }
        }
        {
            // Check blocks in this y
            var blockPos = BlockPos.betweenClosedStream(
                    area.minX() + 1, targetPos.getY(), area.minZ() + 1,
                    area.maxX() - 1, targetPos.getY(), area.maxZ() - 1)
                .filter(p -> !skipped.contains(p))
                .filter(p -> canBreak(level, p, level.getBlockState(p)))
                .findAny()
                .orElse(null);
            if (blockPos != null) {
                targetIterator = new PickIterator.Single<>(blockPos);
                setState(QuarryState.MOVE_HEAD, getBlockState());
                return false;
            }
        }
        var minY = this.digMinY.getMinY(level);
        if (minY < targetPos.getY()) {
            skipped.removeIf(p -> p.getY() > targetPos.getY());
            // Go next y
            targetIterator = area.quarryDigPosIterator(targetPos.getY() - 1);
            return false;
        } else {
            // Finish
            setState(QuarryState.FINISHED, getBlockState());
            return true;
        }
    }

    void removeFluid() {
        if (level == null || level.isClientSide() || area == null) {
            return;
        }
        if (targetIterator == null || targetPos == null) {
            throw new IllegalStateException("Target is null");
        }
        var fluidState = level.getFluidState(targetPos);
        if (fluidState.isEmpty()) {
            // No fluid anymore
            setState(QuarryState.BREAK_BLOCK, getBlockState());
            return;
        }
        var poses = area.getChainBlocks(targetPos, p -> !level.getFluidState(p).isEmpty(), level.getMaxBuildHeight());
        useEnergy((long) (powerMap().breakBlockFluid() * poses.size() * ONE_FE), false, true, "removeFluid");
        var player = getQuarryFakePlayer((ServerLevel) level, targetPos);
        for (var fluidPos : poses) {
            removeFluidAt(level, fluidPos, player, Blocks.AIR.defaultBlockState());
            for (var edge : area.getEdgeForPos(fluidPos)) {
                if (!level.getFluidState(edge).isEmpty()) {
                    useEnergy((long) (powerMap().breakBlockFluid() * ONE_FE), false, true, "removeFluid");
                    removeFluidAt(level, edge, player, PlatformAccess.getAccess().registerObjects().frameBlock().get().getDammingState());
                }
            }
        }
        setState(QuarryState.BREAK_BLOCK, getBlockState());
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
        var serverLevel = (ServerLevel) level;
        // Gather Drops
        if (target.getX() % 3 == 0 && target.getZ() % 3 == 0) {
            serverLevel.getEntitiesOfClass(ItemEntity.class, new AABB(target).inflate(5), Predicate.not(i -> i.getItem().isEmpty()))
                .forEach(i -> {
                    storage.addItem(i.getItem());
                    i.kill();
                });
            serverLevel.getEntitiesOfClass(ExperienceOrb.class, new AABB(target).inflate(5), EntitySelector.ENTITY_STILL_ALIVE)
                .forEach(Entity::kill);
        }

        var state = serverLevel.getBlockState(target);
        if (state.isAir() || state.equals(stateAfterBreak(serverLevel, target, state))) {
            // Nothing to do
            return WorkResult.SUCCESS;
        }
        var blockEntity = serverLevel.getBlockEntity(target);
        var player = getQuarryFakePlayer(serverLevel, target);
        var pickaxe = Items.NETHERITE_PICKAXE.getDefaultInstance();
        EnchantmentHelper.setEnchantments(pickaxe, getEnchantments());
        player.setItemInHand(InteractionHand.MAIN_HAND, pickaxe);

        var hardness = state.getDestroySpeed(serverLevel, target);
        // First check event
        var eventCancelled = checkBreakEvent(serverLevel, player, state, target, blockEntity);
        if (eventCancelled) {
            return WorkResult.FAIL_EVENT;
        }
        // Second, check modules
        var moduleResult = breakBlockModuleOverride(serverLevel, state, target, hardness);
        if (moduleResult != WorkResult.SKIPPED) {
            return moduleResult;
        }

        if (hardness < 0) {
            // Unbreakable
            return WorkResult.SKIPPED;
        }
        var lookup = serverLevel.registryAccess().asGetterLookup();
        var requiredEnergy = powerMap().getBreakEnergy(hardness,
            enchantmentCache.getLevel(enchantments, Enchantments.EFFICIENCY, lookup),
            enchantmentCache.getLevel(enchantments, Enchantments.UNBREAKING, lookup),
            enchantmentCache.getLevel(enchantments, Enchantments.FORTUNE, lookup),
            enchantmentCache.getLevel(enchantments, Enchantments.SILK_TOUCH, lookup) > 0
        );
        if (useEnergy(requiredEnergy, true, getMaxEnergy() < requiredEnergy, "breakBlock") == requiredEnergy) {
            useEnergy(requiredEnergy, false, getMaxEnergy() < requiredEnergy, "breakBlock");
            var drops = Block.getDrops(state, serverLevel, target, blockEntity, player, pickaxe);
            drops.forEach(storage::addItem);
            serverLevel.setBlock(target, stateAfterBreak(serverLevel, target, state), Block.UPDATE_ALL);
            afterBreak(serverLevel, player, state, target, blockEntity);

            if (shouldRemoveFluid()) {
                assert area != null;
                for (var edge : area.getEdgeForPos(target)) {
                    if (!level.getFluidState(edge).isEmpty()) {
                        useEnergy((long) (powerMap().breakBlockFluid() * ONE_FE), false, true, "removeFluid");
                        removeFluidAt(level, edge, player, PlatformAccess.getAccess().registerObjects().frameBlock().get().getDammingState());
                    }
                }
            }
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

    protected boolean shouldRemoveFluid() {
        return modules.contains(QuarryModule.Constant.PUMP);
    }

    protected BlockState stateAfterBreak(Level level, BlockPos pos, BlockState before) {
        return Blocks.AIR.defaultBlockState();
    }

    void removeFluidAt(@NotNull Level level, BlockPos pos, ServerPlayer player, BlockState newState) {
        var state = level.getBlockState(pos);
        if (state.getBlock() instanceof LiquidBlock) {
            var f = level.getFluidState(pos);
            if (!f.isEmpty() && f.isSource()) {
                storage.addFluid(f.getType(), MachineStorage.ONE_BUCKET);
            }
            level.setBlock(pos, newState, Block.UPDATE_CLIENTS);
        } else if (state.getBlock() instanceof BucketPickup bucketPickup) {
            var picked = bucketPickup.pickupBlock(player, level, pos, state);
            storage.addBucketFluid(picked);
        } else {
            level.setBlock(pos, newState, Block.UPDATE_CLIENTS);
        }
    }

    /**
     * Return next available pos to break
     *
     * @return {@code null} if finished.
     */
    @Nullable
    BlockPos getNextValidTarget() {
        assert targetIterator != null;
        assert level != null;
        assert area != null;
        int recursiveCount = 0;
        // optimize tail recursive calls
        while (recursiveCount++ < 350) {
            while (targetIterator.hasNext()) {
                var pos = targetIterator.next();
                var state = level.getBlockState(pos);
                if (canBreak(level, pos, state)) {
                    return pos;
                }
            }
            // temporary set pos to get current Y
            targetPos = targetIterator.getLastReturned();
            if (setNextDigTargetIterator()) {
                return null;
            }
        }
        // maybe bug
        QuarryPlus.LOGGER.error("Quarry at {} can't find next target. Itr: {}, Pos: {}",
            getBlockPos().toShortString(),
            targetIterator,
            targetPos
        );
        return targetIterator.getLastReturned();
    }

    boolean canBreak(Level level, BlockPos pos, BlockState state) {
        var fluid = level.getFluidState(pos);
        if (fluid.isEmpty()) {
            return !state.isAir() && !state.equals(stateAfterBreak(level, pos, state));
        } else {
            return shouldRemoveFluid();
        }
    }

    /**
     * @return {@code null} if caller should use default implementation
     */
    @Nullable
    public AABB getRenderAabb() {
        var area = getArea();
        if (area == null) {
            return null;
        }
        int minY;
        if (getLevel() == null) {
            minY = 0;
        } else {
            minY = getLevel().getMinBuildHeight();
        }
        return switch (renderMode()) {
            case "drill" -> new AABB(area.minX(), minY, area.minZ(), area.maxX(), area.maxY(), area.maxZ());
            case null, default -> null;
        };
    }

    @VisibleForTesting
    public @NotNull ItemEnchantments getEnchantments() {
        return enchantments;
    }

    @VisibleForTesting
    public void setEnchantments(@NotNull ItemEnchantments enchantments) {
        this.enchantments = enchantments;
    }
}
