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
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    public boolean searchDownward = false;
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
            target = AdvPumpTarget.getTarget(level, initPos, AdvPumpTarget.inRangePredicate(initPos, range), this::isReplaceBlock, AdvPumpTarget.areaSizeHint(range), searchDownward);
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
                    target = AdvPumpTarget.getTarget(level, nextPos, AdvPumpTarget.inRangePredicate(nextPos, range), this::isReplaceBlock, AdvPumpTarget.areaSizeHint(range), searchDownward);
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
        var efficiency = enchantmentCache.getLevel(getEnchantments(), Enchantments.EFFICIENCY, level.registryAccess());
        setMaxEnergy((long) (powerMap().maxEnergy() * Math.pow(2, efficiency) * ONE_FE));
    }

    long fluidCapacity(Level level) {
        var efficiency = enchantmentCache.getLevel(getEnchantments(), Enchantments.EFFICIENCY, level.registryAccess());
        return (long) (powerMap().fluidCapacity() * (efficiency + 1)) * MachineStorage.ONE_BUCKET;
    }

    @VisibleForTesting
    int range(Level level) {
        var lookup = level.registryAccess();
        var fortune = enchantmentCache.getLevel(getEnchantments(), Enchantments.FORTUNE, lookup);
        var silkTouch = enchantmentCache.getLevel(getEnchantments(), Enchantments.SILK_TOUCH, lookup);
        var rangeLevel = Math.max(fortune, silkTouch > 0 ? 3 : 0);
        return (int) (powerMap().range() * (rangeLevel + 1));
    }

    long baseEnergyPerSource(Level level) {
        var unbreaking = enchantmentCache.getLevel(getEnchantments(), Enchantments.UNBREAKING, level.registryAccess());
        return powerMap().baseEnergyFor(unbreaking);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        fromClientTag(input);
        currentY = input.getIntOr("currentY", currentY);
        finished = input.getBooleanOr("finished", false);
        storage = input.read("storage", MachineStorage.CODEC.codec()).orElseGet(MachineStorage::of);
        moduleInventory.fromItemList(input.listOrEmpty("moduleInventory", ItemStack.CODEC));
        chunkLoader = input.read("chunkLoader", QuarryChunkLoader.CODEC).orElse(QuarryChunkLoader.None.INSTANCE);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        toClientTag(output);
        output.putInt("currentY", currentY);
        output.putBoolean("finished", finished);
        output.store("storage", MachineStorage.CODEC.codec(), storage);
        moduleInventory.storeAsItemList(output.list("moduleInventory", ItemStack.CODEC));
        output.store("chunkLoader", QuarryChunkLoader.CODEC, chunkLoader);
    }

    @Override
    public void fromClientTag(ValueInput input) {
        placeFrame = input.getBooleanOr("placeFrame", placeFrame);
        deleteFluid = input.getBooleanOr("deleteFluid", deleteFluid);
        searchDownward = input.getBooleanOr("searchDownward", searchDownward);
    }

    @Override
    public ValueOutput toClientTag(ValueOutput output) {
        output.putBoolean("placeFrame", placeFrame);
        output.putBoolean("deleteFluid", deleteFluid);
        output.putBoolean("searchDownward", searchDownward);
        return output;
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
    public void preRemoveSideEffects(BlockPos pos, BlockState blockState) {
        if (level != null) {
            Containers.dropContents(level, pos, moduleInventory);
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
                detail(ChatFormatting.GREEN, "SearchDownward", String.valueOf(searchDownward)),
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
