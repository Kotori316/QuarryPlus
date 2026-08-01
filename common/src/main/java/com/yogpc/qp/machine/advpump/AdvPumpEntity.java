package com.yogpc.qp.machine.advpump;

import com.google.common.collect.Sets;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.*;
import com.yogpc.qp.machine.misc.QuarryChunkLoader;
import com.yogpc.qp.machine.module.ModuleInventory;
import com.yogpc.qp.machine.module.QuarryModule;
import com.yogpc.qp.machine.module.QuarryModuleProvider;
import com.yogpc.qp.machine.module.RepeatTickModuleItem;
import com.yogpc.qp.packet.ClientSync;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class AdvPumpEntity extends PowerEntity implements ClientSync {
    @NotNull
    MachineStorage storage = MachineStorage.of();
    int currentY;
    @Nullable
    AdvPumpTarget target;
    boolean finished = false;
    public boolean deleteFluid = false;
    public boolean placeFrame = true;
    @NotNull
    final EnchantmentCache enchantmentCache = new EnchantmentCache();
    @NotNull
    Set<QuarryModule> modules = Collections.emptySet();
    @NotNull
    final ModuleInventory moduleInventory = new ModuleInventory(5, AdvPumpEntity::moduleFilter, m -> modules, this::setChanged);
    @NotNull
    QuarryChunkLoader chunkLoader = QuarryChunkLoader.None.INSTANCE;

    protected AdvPumpEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.currentY = pos.getY() - 1;
        setMaxEnergy((long) (powerMap().maxEnergy() * ONE_FE));
    }

    static PowerMap.AdvPump powerMap() {
        return PlatformAccess.config().powerMap().advPump();
    }

    @SuppressWarnings("unused")
    static void serverTick(Level level, BlockPos pos, BlockState state, AdvPumpEntity pump) {
        var repeatTickModule = RepeatTickModuleItem.getModule(pump.modules).orElse(RepeatTickModuleItem.ZERO);
        var repeatCount = repeatTickModule.stackSize() + 1;
        for (int i = 0; i < repeatCount; i++) {
            pump.drainOnce(level, pos, state);
        }
    }

    void drainOnce(Level level, BlockPos pos, BlockState state) {
        var fluidSum = storage.fluidKeyCounts().stream().mapToLong(MachineStorage.FluidKeyCount::count).sum();
        if (!(hasEnoughEnergy() && !finished && fluidSum <= fluidCapacity(level))) {
            return;
        }
        if (target == null) {
            var initPos = pos.atY(currentY);
            var range = range(level);
            target = AdvPumpTarget.getTarget(level, initPos, AdvPumpTarget.inRangePredicate(initPos, range), this::isReplaceBlock, AdvPumpTarget.areaSizeHint(range));
            level.setBlock(pos, state.setValue(QpBlockProperty.WORKING, true), Block.UPDATE_ALL);
        }
        if (target.hasNext()) {
            while (target.hasNext()) {
                var t = target.next();
                var result = pumpFluid(level, t, this::getStateForReplace, true);
                if (!result.isSuccess()) {
                    break;
                }
                if (placeFrame) {
                    Direction.Plane.HORIZONTAL.stream().map(t::relative)
                        .filter(target.getPredicate().negate())
                        .forEach(p -> pumpFluid(level, p, f -> PlatformAccess.getAccess().registerObjects().frameBlock().get().getDammingState(), false));
                }
            }
        } else {
            // Go to the next Y, unless fluid flowed back in.
            if (target.updateToRemainingIterator(level, pos.atY(currentY))) {
                currentY -= 1;
                var nextPos = pos.atY(currentY);
                if (shouldFinish(level, nextPos)) {
                    finished = true;
                    target = null;
                    level.setBlock(pos, state.setValue(QpBlockProperty.WORKING, false), Block.UPDATE_ALL);
                    energyCounter.logUsageMap();
                    removeLeftoverPlaceholder(level, pos, currentY);
                } else {
                    var range = range(level);
                    target = AdvPumpTarget.getTarget(level, nextPos, AdvPumpTarget.inRangePredicate(nextPos, range), this::isReplaceBlock, AdvPumpTarget.areaSizeHint(range));
                }
            }
        }
    }

    WorkResult pumpFluid(Level level, BlockPos targetPos, Function<FluidState, BlockState> replaceBlockGetter, boolean useEnergy) {
        var fluidState = level.getFluidState(targetPos);
        if (fluidState.isEmpty()) {
            return WorkResult.SKIPPED;
        } else if (!fluidState.isSource()) {
            // Just remove with no cost.
            level.setBlock(targetPos, replaceBlockGetter.apply(fluidState), Block.UPDATE_ALL);
            return WorkResult.SUCCESS;
        } else {
            if (useEnergy) {
                var cost = baseEnergyPerSource(level);
                if (useEnergy(cost, true, false, "pumpFluid") != cost) {
                    return WorkResult.NOT_ENOUGH_ENERGY;
                }
                useEnergy(cost, false, false, "pumpFluid");
            }
            FluidDrain.drainSourceInto(level, targetPos, null, replaceBlockGetter.apply(fluidState), storage, deleteFluid);
            return WorkResult.SUCCESS;
        }
    }

    BlockState getStateForReplace(FluidState fluidState) {
        return fluidState.is(FluidTags.WATER)
            ? PlatformAccess.getAccess().registerObjects().softBlock().get().defaultBlockState()
            : Blocks.AIR.defaultBlockState();
    }

    boolean isReplaceBlock(BlockState state) {
        return state.is(PlatformAccess.getAccess().registerObjects().softBlock().get());
    }

    boolean shouldFinish(Level level, BlockPos nextPos) {
        var blockState = level.getBlockState(nextPos);
        var blockCondition = blockState.isAir() || isReplaceBlock(blockState);
        return level.getFluidState(nextPos).isEmpty() && !blockCondition;
    }

    static void removeLeftoverPlaceholder(Level level, BlockPos pos, int minY) {
        var softBlock = PlatformAccess.getAccess().registerObjects().softBlock().get();
        for (int y = pos.getY() - 1; y > minY; y--) {
            var withY = pos.atY(y);
            var blockState = level.getBlockState(withY);
            if (blockState.is(softBlock)) {
                level.removeBlock(withY, false);
                break;
            } else if (!blockState.isAir()) {
                break;
            }
        }
    }

    public void reset() {
        target = null;
        finished = false;
        currentY = getBlockPos().getY() - 1;
    }

    @Override
    public final void updateMaxEnergyWithEnchantment(Level level) {
        var efficiency = enchantmentCache.getLevel(getEnchantments(), Enchantments.EFFICIENCY, level.registryAccess().asGetterLookup());
        setMaxEnergy((long) (powerMap().maxEnergy() * Math.pow(2, efficiency) * ONE_FE));
    }

    long fluidCapacity(Level level) {
        var efficiency = enchantmentCache.getLevel(getEnchantments(), Enchantments.EFFICIENCY, level.registryAccess().asGetterLookup());
        return (long) (powerMap().fluidCapacity() * (efficiency + 1)) * MachineStorage.ONE_BUCKET;
    }

    int range(Level level) {
        var lookup = level.registryAccess().asGetterLookup();
        var fortune = enchantmentCache.getLevel(getEnchantments(), Enchantments.FORTUNE, lookup);
        var silkTouch = enchantmentCache.getLevel(getEnchantments(), Enchantments.SILK_TOUCH, lookup);
        var rangeLevel = Math.max(fortune, silkTouch > 0 ? 3 : 0);
        return (int) (powerMap().range() * (rangeLevel + 1));
    }

    long baseEnergyPerSource(Level level) {
        var unbreaking = enchantmentCache.getLevel(getEnchantments(), Enchantments.UNBREAKING, level.registryAccess().asGetterLookup());
        return powerMap().baseEnergyFor(unbreaking);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fromClientTag(tag, registries);
        currentY = tag.getInt("currentY");
        finished = tag.getBoolean("finished");
        storage = MachineStorage.CODEC.codec().parse(NbtOps.INSTANCE, tag.get("storage")).result().orElseGet(MachineStorage::of);
        moduleInventory.fromTag(tag.getList("moduleInventory", Tag.TAG_COMPOUND), registries);
        chunkLoader = QuarryChunkLoader.CODEC.parse(NbtOps.INSTANCE, tag.get("chunkLoader")).result().orElse(QuarryChunkLoader.None.INSTANCE);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        toClientTag(tag, registries);
        tag.putInt("currentY", currentY);
        tag.putBoolean("finished", finished);
        tag.put("storage", MachineStorage.CODEC.codec().encodeStart(NbtOps.INSTANCE, storage).getOrThrow());
        tag.put("moduleInventory", moduleInventory.createTag(registries));
        tag.put("chunkLoader", QuarryChunkLoader.CODEC.encodeStart(NbtOps.INSTANCE, chunkLoader).getOrThrow());
    }

    @Override
    public void fromClientTag(CompoundTag tag, HolderLookup.Provider registries) {
        placeFrame = tag.getBoolean("placeFrame");
        deleteFluid = tag.getBoolean("deleteFluid");
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("placeFrame", placeFrame);
        tag.putBoolean("deleteFluid", deleteFluid);
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
                detail(ChatFormatting.GREEN, "CurrentY", String.valueOf(currentY)),
                detail(ChatFormatting.GREEN, "Finished", String.valueOf(finished)),
                detail(ChatFormatting.GREEN, "DeleteFluid", String.valueOf(deleteFluid)),
                detail(ChatFormatting.GREEN, "PlaceFrame", String.valueOf(placeFrame)),
                detail(ChatFormatting.GREEN, "Storage", String.valueOf(storage)),
                detail(ChatFormatting.GREEN, "Modules", String.valueOf(modules)),
                detail(ChatFormatting.GREEN, "Enchantment", String.valueOf(enchantmentCache))
            )
        );
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

    static boolean moduleFilter(QuarryModule module) {
        return module != QuarryModule.Constant.PUMP;
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
}
