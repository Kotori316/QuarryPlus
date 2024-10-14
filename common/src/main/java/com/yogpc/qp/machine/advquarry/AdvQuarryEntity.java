package com.yogpc.qp.machine.advquarry;

import com.google.common.collect.Sets;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.*;
import com.yogpc.qp.machine.exp.ExpModule;
import com.yogpc.qp.machine.misc.BlockBreakEventResult;
import com.yogpc.qp.machine.misc.DigMinY;
import com.yogpc.qp.machine.misc.QuarryChunkLoader;
import com.yogpc.qp.machine.module.*;
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
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.FallingBlockEntity;
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
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class AdvQuarryEntity extends PowerEntity implements ClientSync {

    @NotNull
    AdvQuarryState currentState = AdvQuarryState.FINISHED;
    @Nullable
    private Area area;
    @Nullable
    private PickIterator<BlockPos> targetIterator;
    @Nullable
    BlockPos targetPos;
    @NotNull
    MachineStorage storage = MachineStorage.of();
    @NotNull
    WorkConfig workConfig = WorkConfig.DEFAULT;
    @NotNull
    public DigMinY digMinY = new DigMinY();
    @NotNull
    final EnchantmentCache enchantmentCache = new EnchantmentCache();
    @NotNull
    Set<QuarryModule> modules = Collections.emptySet();
    @NotNull
    final ModuleInventory moduleInventory = new ModuleInventory(5, AdvQuarryEntity::moduleFilter, m -> modules, this::setChanged);
    boolean searchEnergyConsumed = false;
    @NotNull
    QuarryChunkLoader chunkLoader = QuarryChunkLoader.None.INSTANCE;
    @NotNull
    ItemConverter itemConverter = defaultItemConverter();

    protected AdvQuarryEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        setMaxEnergy((long) (powerMap().maxEnergy() * ONE_FE));
    }

    static PowerMap.AdvQuarry powerMap() {
        return PlatformAccess.config().powerMap().advQuarry();
    }

    @SuppressWarnings("unused")
    static void serverTick(Level level, BlockPos pos, BlockState state, AdvQuarryEntity quarry) {
        for (int i = 0; i < quarry.repeatCount(); i++) {
            if (!quarry.hasEnoughEnergy()) {
                return;
            }
            switch (quarry.currentState) {
                case FINISHED -> {
                    return;
                }
                case WAITING -> {
                    quarry.waiting();
                    return;
                }
                case MAKE_FRAME -> quarry.makeFrame();
                case BREAK_BLOCK -> quarry.breakBlock();
                case CLEAN_UP -> quarry.cleanUp();
                case null, default ->
                    throw new UnsupportedOperationException("Not implemented: " + quarry.currentState);
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fromClientTag(tag, registries);
        var current = BlockPos.CODEC.parse(NbtOps.INSTANCE, tag.get("targetPos")).result().orElse(null);
        workConfig = WorkConfig.CODEC.codec().parse(NbtOps.INSTANCE, tag.get("workConfig")).result().orElse(WorkConfig.DEFAULT);
        targetIterator = createTargetIterator(currentState, area, current, workConfig);
        targetPos = current;
        storage = MachineStorage.CODEC.codec().parse(NbtOps.INSTANCE, tag.get("storage")).result().orElseGet(MachineStorage::of);
        moduleInventory.fromTag(tag.getList("moduleInventory", Tag.TAG_COMPOUND), registries);
        chunkLoader = QuarryChunkLoader.CODEC.parse(NbtOps.INSTANCE, tag.get("chunkLoader")).result().orElse(QuarryChunkLoader.None.INSTANCE);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        toClientTag(tag, registries);
        tag.put("workConfig", WorkConfig.CODEC.codec().encodeStart(NbtOps.INSTANCE, workConfig).getOrThrow());
        if (targetIterator != null) {
            tag.put("targetPos", BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, targetIterator.getLastReturned()).getOrThrow());
        }
        tag.put("storage", MachineStorage.CODEC.codec().encodeStart(NbtOps.INSTANCE, storage).getOrThrow());
        tag.put("moduleInventory", moduleInventory.createTag(registries));
        tag.put("chunkLoader", QuarryChunkLoader.CODEC.encodeStart(NbtOps.INSTANCE, chunkLoader).getOrThrow());
    }

    @Override
    public void fromClientTag(CompoundTag tag, HolderLookup.Provider registries) {
        currentState = AdvQuarryState.valueOf(tag.getString("state"));
        area = Area.CODEC.codec().parse(NbtOps.INSTANCE, tag.get("area")).result().orElse(null);
        digMinY = DigMinY.CODEC.codec().parse(NbtOps.INSTANCE, tag.get("digMinY")).result().orElseGet(DigMinY::new);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putString("state", currentState.name());
        if (area != null) {
            tag.put("area", Area.CODEC.codec().encodeStart(NbtOps.INSTANCE, this.area).getOrThrow());
        }
        tag.put("digMinY", DigMinY.CODEC.codec().encodeStart(NbtOps.INSTANCE, digMinY).getOrThrow());
        return tag;
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
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

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level instanceof ServerLevel s) {
            this.chunkLoader.makeChunkUnLoaded(s);
        }
    }

    @Override
    public Stream<MutableComponent> checkerLogs() {
        return Stream.concat(
            super.checkerLogs(),
            Stream.of(
                detail(ChatFormatting.GREEN, "State", currentState.name()),
                detail(ChatFormatting.GREEN, "Area", String.valueOf(area)),
                detail(ChatFormatting.GREEN, "Target", String.valueOf(targetPos)),
                detail(ChatFormatting.GREEN, "TargetIterator", targetIterator != null ? targetIterator.getClass().getSimpleName() : "null"),
                detail(ChatFormatting.GREEN, "Storage", String.valueOf(storage)),
                detail(ChatFormatting.GREEN, "DigMinY", String.valueOf(digMinY.getMinY(level))),
                detail(ChatFormatting.GREEN, "Modules", String.valueOf(modules)),
                detail(ChatFormatting.GREEN, "Enchantment", String.valueOf(enchantmentCache))
            )
        );
    }

    public void setArea(@Nullable Area area) {
        this.area = area;
    }

    public @Nullable Area getArea() {
        return area;
    }

    void setState(AdvQuarryState state, BlockState blockState) {
        if (level != null && this.currentState != state) {
            if (!level.isClientSide) {
                if (!AdvQuarryState.isWorking(currentState) && AdvQuarryState.isWorking(state)) {
                    // Start working
                    this.chunkLoader = QuarryChunkLoader.of((ServerLevel) level, getBlockPos());
                    this.chunkLoader.makeChunkLoaded((ServerLevel) level);
                } else if (AdvQuarryState.isWorking(currentState) && !AdvQuarryState.isWorking(state)) {
                    // Finish working
                    this.chunkLoader.makeChunkUnLoaded((ServerLevel) level);
                    this.chunkLoader = QuarryChunkLoader.None.INSTANCE;
                }
            }
            this.currentState = state;
            syncToClient();
            level.setBlock(getBlockPos(), blockState.setValue(QpBlockProperty.WORKING, AdvQuarryState.isWorking(state)), Block.UPDATE_ALL);
            if (state == AdvQuarryState.FINISHED) {
                energyCounter.logUsageMap();
            }
        }
    }

    public String renderMode() {
        return switch (this.currentState) {
            case WAITING, MAKE_FRAME -> "frame";
            default -> "none";
        };
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
        this.itemConverter = defaultItemConverter().concat(ConverterModule.findConversions(this.modules));
    }

    protected int repeatCount() {
        var repeatTickModule = RepeatTickModuleItem.getModule(modules).orElse(RepeatTickModuleItem.ZERO);
        return repeatTickModule.stackSize() + 1;
    }

    protected boolean shouldRemoveBedrock() {
        return modules.contains(QuarryModule.Constant.BEDROCK);
    }

    protected @NotNull Optional<ExpModule> getExpModule() {
        return ExpModule.getModule(modules);
    }

    @Nullable
    static PickIterator<BlockPos> createTargetIterator(@NotNull AdvQuarryState currentState, @Nullable Area area, BlockPos current, WorkConfig config) {
        if (area == null) {
            return null;
        }
        if (currentState == AdvQuarryState.MAKE_FRAME) {
            var iterator = area.quarryFramePosIterator();
            iterator.setLastReturned(current);
            return iterator;
        }
        if (currentState == AdvQuarryState.BREAK_BLOCK || currentState == AdvQuarryState.CLEAN_UP) {
            PickIterator<BlockPos> iterator;
            if (config.chunkByChunk()) {
                iterator = new AdvQuarryTarget.ChunkByChunk(area);
            } else {
                iterator = new AdvQuarryTarget.North(area);
            }
            iterator.setLastReturned(current);
            return iterator;
        }
        return null;
    }

    void waiting() {
        if (!workConfig.startImmediately()) {
            return;
        }
        if (getEnergy() > getMaxEnergy() / 200 && this.area != null) {
            startQuarryWork();
        }
    }

    void startQuarryWork() {
        var next = workConfig.placeAreaFrame() ? AdvQuarryState.MAKE_FRAME : AdvQuarryState.BREAK_BLOCK;
        setState(next, getBlockState());
    }

    void makeFrame() {
        if (level == null || level.isClientSide() || area == null) {
            return;
        }
        if (targetIterator == null) {
            targetIterator = createTargetIterator(currentState, getArea(), null, workConfig);
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
                setState(AdvQuarryState.BREAK_BLOCK, getBlockState());
            }
            return;
        }

        if (!getBlockPos().equals(targetPos) && !state.isAir()) {
            var result = breakOneBlock(targetPos);
            if (!result.isSuccess()) {
                // Wait until quarry can remove the block
                return;
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
                setState(AdvQuarryState.BREAK_BLOCK, getBlockState());
            }
        }
    }

    void breakBlock() {
        if (level == null || level.isClientSide() || area == null) {
            return;
        }
        if (targetIterator == null) {
            targetIterator = createTargetIterator(currentState, getArea(), null, workConfig);
            assert targetIterator != null;
        }
        if (targetPos == null) {
            targetPos = targetIterator.next();
            assert targetPos != null;
        }

        WorkResult result = null;
        while (result == null || result == WorkResult.SKIPPED) {
            if (targetPos == null) {
                return;
            }
            if (!searchEnergyConsumed) {
                var energy = (long) powerMap().searchBase() * ONE_FE * targetPos.getY();
                var used = useEnergy(energy, false, false, "searchEnergy");
                if (energy != used) {
                    // Not enough energy
                    return;
                }
                searchEnergyConsumed = true;
            }
            result = breakBlocks(targetPos.getX(), targetPos.getZ());
            if (result.isSuccess()) {
                searchEnergyConsumed = false;
                if (targetIterator.hasNext()) {
                    targetPos = targetIterator.next();
                } else {
                    targetIterator = null;
                    targetPos = null;
                    setState(AdvQuarryState.CLEAN_UP, getBlockState());
                    return;
                }
            } else if (result == WorkResult.NOT_ENOUGH_ENERGY) {
                return;
            }
        }
    }

    void cleanUp() {
        if (level == null || level.isClientSide() || area == null) {
            return;
        }
        if (targetIterator == null) {
            targetIterator = createTargetIterator(currentState, getArea(), null, workConfig);
            assert targetIterator != null;
        }
        if (targetPos == null) {
            targetPos = targetIterator.next();
            assert targetPos != null;
        }

        int count = 0;
        while (count < 32 && currentState == AdvQuarryState.CLEAN_UP) {
            if (targetPos == null) {
                return;
            }
            var result = cleanUpFluid(targetPos.getX(), targetPos.getZ());
            if (result.isSuccess()) {
                count++;
            }
            if (targetIterator.hasNext()) {
                targetPos = targetIterator.next();
            } else {
                targetIterator = null;
                targetPos = null;
                setState(AdvQuarryState.FINISHED, getBlockState());
                return;
            }
        }
    }

    @NotNull
    WorkResult breakOneBlock(BlockPos target) {
        assert level != null;
        var serverLevel = (ServerLevel) level;
        var state = serverLevel.getBlockState(target);
        if (state.isAir() || state.equals(stateAfterBreak(serverLevel, target, state))) {
            return WorkResult.SUCCESS;
        }
        var lookup = serverLevel.registryAccess().asGetterLookup();
        var blockEntity = serverLevel.getBlockEntity(target);
        var player = getQuarryFakePlayer(serverLevel, target);
        var pickaxe = Items.NETHERITE_PICKAXE.getDefaultInstance();
        EnchantmentHelper.setEnchantments(pickaxe, enchantmentCache.getEnchantmentsForPickaxe(getEnchantments(), lookup));
        player.setItemInHand(InteractionHand.MAIN_HAND, pickaxe);
        var hardness = state.getDestroySpeed(serverLevel, target);
        // First check event
        var eventResult = checkBreakEvent(serverLevel, player, state, target, blockEntity);
        if (eventResult.canceled()) {
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
        var requiredEnergy = powerMap().getBreakEnergy(hardness,
            enchantmentCache.getLevel(getEnchantments(), Enchantments.EFFICIENCY, lookup),
            enchantmentCache.getLevel(getEnchantments(), Enchantments.UNBREAKING, lookup),
            enchantmentCache.getLevel(getEnchantments(), Enchantments.FORTUNE, lookup),
            enchantmentCache.getLevel(getEnchantments(), Enchantments.SILK_TOUCH, lookup) > 0
        );

        useEnergy(requiredEnergy, false, true, "breakBlock");
        var drops = Block.getDrops(state, serverLevel, target, blockEntity, player, pickaxe);
        var afterBreakEventResult = afterBreak(serverLevel, player, state, target, blockEntity, drops, pickaxe, stateAfterBreak(serverLevel, target, state));
        if (!afterBreakEventResult.canceled()) {
            drops.stream().flatMap(itemConverter::convert).forEach(storage::addItem);
            var amount = eventResult.exp().orElse(afterBreakEventResult.exp().orElse(0));
            if (amount != 0) {
                getExpModule().ifPresent(e -> e.addExp(amount));
            }
        }

        assert area != null;
        for (var edge : area.getEdgeForPos(target)) {
            if (!level.getFluidState(edge).isEmpty()) {
                useEnergy((long) (powerMap().breakBlockFluid() * ONE_FE), false, true, "removeFluid");
                removeFluidAt(level, edge, player, PlatformAccess.getAccess().registerObjects().frameBlock().get().getDammingState());
            }
        }
        return WorkResult.SUCCESS;
    }

    @NotNull
    WorkResult breakBlocks(int x, int z) {
        assert level != null;
        var serverLevel = (ServerLevel) level;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(x, getBlockPos().getY() - 1, z);

        var lookup = serverLevel.registryAccess().asGetterLookup();
        var player = getQuarryFakePlayer(serverLevel, mutableBlockPos);
        var pickaxe = Items.NETHERITE_PICKAXE.getDefaultInstance();
        EnchantmentHelper.setEnchantments(pickaxe, enchantmentCache.getEnchantmentsForPickaxe(getEnchantments(), lookup));
        player.setItemInHand(InteractionHand.MAIN_HAND, pickaxe);
        var aabb = new AABB(x - 5, digMinY.getMinY(serverLevel) - 5, z - 5, x + 5, getBlockPos().getY() - 1, z + 5);
        serverLevel.getEntitiesOfClass(ItemEntity.class, aabb, Predicate.not(i -> i.getItem().isEmpty()))
            .forEach(i -> {
                itemConverter.convert(i.getItem()).forEach(storage::addItem);
                i.kill();
            });
        serverLevel.getEntitiesOfClass(FallingBlockEntity.class, aabb)
            .forEach(i -> {
                itemConverter.convert(new ItemStack(i.getBlockState().getBlock())).forEach(storage::addItem);
                i.discard();
            });
        getExpModule().ifPresent(e ->
            serverLevel.getEntitiesOfClass(ExperienceOrb.class, aabb, EntitySelector.ENTITY_STILL_ALIVE)
                .forEach(orb -> {
                    e.addExp(orb.getValue());
                    orb.kill();
                }));
        removeEdgeFluid(x, z, serverLevel, player);

        // Search blocks
        long requiredEnergy = 0;
        var exp = new AtomicInteger(0);
        List<Pair<BlockPos, BlockState>> toBreak = new ArrayList<>();
        List<Pair<BlockPos, BlockState>> toDrain = new ArrayList<>();
        Set<BlockPos> handled = new HashSet<>();
        Map<BlockPos, BlockBreakEventResult> resultMap = new HashMap<>();
        for (int y = getBlockPos().getY() - 1; y >= digMinY.getMinY(serverLevel); y--) {
            mutableBlockPos.setY(y);
            var state = serverLevel.getBlockState(mutableBlockPos);
            var fluidState = serverLevel.getFluidState(mutableBlockPos);
            if (fluidState.isEmpty()) {
                if (state.isAir()) {
                    continue;
                }
                var blockEntity = serverLevel.getBlockEntity(mutableBlockPos);
                var hardness = state.getDestroySpeed(serverLevel, mutableBlockPos);

                // First check event
                var eventResult = checkBreakEvent(serverLevel, player, state, mutableBlockPos, blockEntity);
                if (eventResult.canceled()) {
                    continue;
                }
                // Second, check modules
                var moduleResult = breakBlockModuleOverride(serverLevel, state, mutableBlockPos, hardness);
                if (moduleResult != WorkResult.SKIPPED) {
                    // Handled in breakBlockModuleOverride, skip
                    handled.add(mutableBlockPos); // Just add instance, value won't be used.
                    continue;
                }
                if (hardness < 0) {
                    // Unbreakable
                    continue;
                }
                // Calc required energy
                var energy = powerMap().getBreakEnergy(hardness,
                    enchantmentCache.getLevel(getEnchantments(), Enchantments.EFFICIENCY, lookup),
                    enchantmentCache.getLevel(getEnchantments(), Enchantments.UNBREAKING, lookup),
                    enchantmentCache.getLevel(getEnchantments(), Enchantments.FORTUNE, lookup),
                    enchantmentCache.getLevel(getEnchantments(), Enchantments.SILK_TOUCH, lookup) > 0
                );
                requiredEnergy += energy;
                toBreak.add(Pair.of(mutableBlockPos.immutable(), state));
                resultMap.put(mutableBlockPos.immutable(), eventResult);
            } else {
                var energy = (long) powerMap().breakBlockFluid() * ONE_FE;
                requiredEnergy += energy;
                toDrain.add(Pair.of(mutableBlockPos.immutable(), state));
            }
        }
        if (toBreak.isEmpty() && toDrain.isEmpty()) {
            if (handled.isEmpty()) {
                return WorkResult.SKIPPED;
            } else {
                return WorkResult.SUCCESS;
            }
        }
        useEnergy(requiredEnergy, false, true, "breakBlock");
        // Drain fluids
        for (Pair<BlockPos, BlockState> pair : toDrain) {
            if (pair.getRight().getBlock() instanceof BucketPickup fluidBlock) {
                var bucketItem = fluidBlock.pickupBlock(player, serverLevel, pair.getLeft(), pair.getRight());
                storage.addBucketFluid(bucketItem);
            }
            var state = serverLevel.getBlockState(pair.getLeft());
            if (!state.isAir()) {
                breakOneBlock(pair.getLeft());
            }
            serverLevel.setBlock(pair.getLeft(), PlatformAccess.getAccess().registerObjects().softBlock().get().defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        // Get drops
        for (Pair<BlockPos, BlockState> statePair : toBreak) {
            var state = statePair.getValue();
            var target = statePair.getKey();
            var blockEntity = serverLevel.getBlockEntity(target);
            var drops = Block.getDrops(state, serverLevel, target, blockEntity, player, pickaxe);
            var afterBreakEventResult = afterBreak(serverLevel, player, state, target, blockEntity, drops, pickaxe, stateAfterBreak(serverLevel, target, state));
            if (!afterBreakEventResult.canceled()) {
                drops.stream().flatMap(itemConverter::convert).forEach(storage::addItem);
                var amount = resultMap.getOrDefault(target, BlockBreakEventResult.EMPTY).exp().orElse(afterBreakEventResult.exp().orElse(0));
                exp.addAndGet(amount);
            }
        }
        // Remove blocks
        for (Pair<BlockPos, BlockState> p : toBreak) {
            serverLevel.setBlock(p.getKey(), stateAfterBreak(serverLevel, p.getKey(), p.getRight()), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        if (exp.get() > 0) {
            getExpModule().ifPresent(e -> {
                useEnergy((long) powerMap().expCollect() * ONE_FE, false, true, "expCollect");
                e.addExp(exp.get());
            });
        }
        setChanged();
        return WorkResult.SUCCESS;
    }

    @NotNull
    WorkResult cleanUpFluid(int x, int z) {
        assert level != null;
        var serverLevel = (ServerLevel) level;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        boolean flagRemoved = false;
        for (int y = getBlockPos().getY() - 1; y >= digMinY.getMinY(serverLevel); y--) {
            mutableBlockPos.set(x, y, z);
            var state = serverLevel.getBlockState(mutableBlockPos);
            var fluid = serverLevel.getFluidState(mutableBlockPos);

            var blockCondition = state.is(PlatformAccess.getAccess().registerObjects().softBlock().get())
                || state.is(Blocks.STONE)
                || state.is(Blocks.COBBLESTONE)
                || (!fluid.isEmpty() && !fluid.isSource());
            var blockIsReplaced = stateAfterBreak(serverLevel, mutableBlockPos, state) == state;
            if (blockCondition && !blockIsReplaced) {
                serverLevel.setBlock(mutableBlockPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                flagRemoved = true;
            }
        }

        return flagRemoved ? WorkResult.SUCCESS : WorkResult.SKIPPED;
    }

    protected abstract ServerPlayer getQuarryFakePlayer(ServerLevel level, BlockPos target);

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
            level.setBlock(pos, newState, Block.UPDATE_ALL);
        } else if (state.getBlock() instanceof BucketPickup bucketPickup) {
            var picked = bucketPickup.pickupBlock(player, level, pos, state);
            storage.addBucketFluid(picked);
        } else {
            level.setBlock(pos, newState, Block.UPDATE_ALL);
        }
    }

    void removeEdgeFluid(int x, int z, ServerLevel targetWorld, ServerPlayer player) {
        assert area != null;
        boolean flagMinX = x - 1 == area.minX();
        boolean flagMaxX = x + 1 == area.maxX();
        boolean flagMinZ = z - 1 == area.minZ();
        boolean flagMaxZ = z + 1 == area.maxZ();
        if (flagMinX) {
            removeFluidAtXZ(area.minX(), z, targetWorld, player);
        }
        if (flagMaxX) {
            removeFluidAtXZ(area.maxX(), z, targetWorld, player);
        }
        if (flagMinZ) {
            removeFluidAtXZ(x, area.minZ(), targetWorld, player);
        }
        if (flagMaxZ) {
            removeFluidAtXZ(x, area.maxZ(), targetWorld, player);
        }
        if (flagMinX && flagMinZ) {
            removeFluidAtXZ(area.minX(), area.minZ(), targetWorld, player);
        }
        if (flagMinX && flagMaxZ) {
            removeFluidAtXZ(area.minX(), area.maxZ(), targetWorld, player);
        }
        if (flagMaxX && flagMinZ) {
            removeFluidAtXZ(area.maxX(), area.minZ(), targetWorld, player);
        }
        if (flagMaxX && flagMaxZ) {
            removeFluidAtXZ(area.maxX(), area.maxZ(), targetWorld, player);
        }
    }

    void removeFluidAtXZ(int x, int z, ServerLevel world, ServerPlayer player) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, 0, z);
        for (int y = getBlockPos().getY() - 1; y > digMinY.getMinY(world); y--) {
            pos.setY(y);
            var fluidState = world.getFluidState(pos);
            if (!fluidState.isEmpty()) {
                useEnergy((long) (powerMap().breakBlockFluid() * ONE_FE), false, true, "removeFluid");
                removeFluidAt(world, pos, player, PlatformAccess.getAccess().registerObjects().frameBlock().get().getDammingState());
            }
        }
    }

    protected abstract BlockBreakEventResult checkBreakEvent(Level level, ServerPlayer fakePlayer, BlockState state, BlockPos target, @Nullable BlockEntity blockEntity);

    /**
     * In this method, you must replace/remove the target block
     */
    protected abstract BlockBreakEventResult afterBreak(Level level, ServerPlayer fakePlayer, BlockState state, BlockPos target, @Nullable BlockEntity blockEntity, List<ItemStack> drops, ItemStack pickaxe, BlockState newState);

    WorkResult breakBlockModuleOverride(ServerLevel level, BlockState state, BlockPos target, float hardness) {
        if (hardness < 0 && state.is(Blocks.BEDROCK) && shouldRemoveBedrock()) {
            var worldBottom = level.getMinBuildHeight();
            var targetY = target.getY();
            if (level.dimension().equals(Level.NETHER)) {
                int top = PlatformAccess.config().removeBedrockOnNetherTop() ? level.getMaxBuildHeight() + 1 : 127;
                if ((worldBottom >= targetY || targetY >= worldBottom + 5) && (122 >= targetY || targetY >= top)) {
                    return WorkResult.SKIPPED;
                }
            } else {
                if (worldBottom >= targetY || targetY >= worldBottom + 5) {
                    return WorkResult.SKIPPED;
                }
            }

            var lookup = level.registryAccess().asGetterLookup();
            var requiredEnergy = powerMap().getBreakEnergy(hardness,
                enchantmentCache.getLevel(getEnchantments(), Enchantments.EFFICIENCY, lookup),
                0, 0, true
            );
            useEnergy(requiredEnergy, false, true, "breakBlock");
            level.setBlock(target, stateAfterBreak(level, target, state), Block.UPDATE_ALL);
            return WorkResult.SUCCESS;
        }
        if (state.is(Blocks.NETHER_PORTAL)) {
            level.removeBlock(target, false);
            return WorkResult.SUCCESS;
        }
        return WorkResult.SKIPPED;
    }

    @VisibleForTesting
    public @NotNull ItemEnchantments getEnchantments() {
        return components().getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
    }

    @VisibleForTesting
    public void setEnchantments(@NotNull ItemEnchantments enchantments) {
        setComponents(
            DataComponentMap.builder().addAll(components())
                .set(DataComponents.ENCHANTMENTS, enchantments)
                .build()
        );
    }

    static boolean moduleFilter(QuarryModule module) {
        return module != QuarryModule.Constant.PUMP;
    }

    static ItemConverter defaultItemConverter() {
        if (PlatformAccess.config().removeCommonMaterialsByChunkDestroyer()) {
            return ItemConverter.defaultInstance().concat(List.of(new ItemConverter.ChunkDestroyerConversion()));
        } else {
            return ItemConverter.defaultInstance();
        }
    }
}
