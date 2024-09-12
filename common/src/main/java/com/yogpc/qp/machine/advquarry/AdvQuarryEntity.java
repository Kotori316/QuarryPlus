package com.yogpc.qp.machine.advquarry;

import com.google.common.collect.Sets;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.*;
import com.yogpc.qp.machine.misc.DigMinY;
import com.yogpc.qp.machine.module.ModuleInventory;
import com.yogpc.qp.machine.module.QuarryModule;
import com.yogpc.qp.machine.module.QuarryModuleProvider;
import com.yogpc.qp.packet.ClientSync;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

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
    final ModuleInventory moduleInventory = new ModuleInventory(5, q -> true, m -> modules);

    protected AdvQuarryEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    static PowerMap.AdvQuarry powerMap() {
        return PlatformAccess.config().powerMap().advQuarry();
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

    public void setArea(@Nullable Area area) {
        this.area = area;
    }

    public @Nullable Area getArea() {
        return area;
    }

    void setState(AdvQuarryState state, BlockState blockState) {
        if (this.currentState != state) {
            this.currentState = state;
            syncToClient();
            if (level != null) {
                level.setBlock(getBlockPos(), blockState.setValue(QpBlockProperty.WORKING, AdvQuarryState.isWorking(state)), Block.UPDATE_ALL);
            }
            if (state == AdvQuarryState.FINISHED) {
                energyCounter.logUsageMap();
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

    @Nullable
    static PickIterator<BlockPos> createTargetIterator(@NotNull AdvQuarryState currentState, @Nullable Area area, BlockPos current, WorkConfig config) {
        return null;
    }
}
