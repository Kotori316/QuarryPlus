package com.yogpc.qp.fabric.machine.quarry;

import com.yogpc.qp.QuarryDataComponents;
import com.yogpc.qp.fabric.PlatformAccessFabric;
import com.yogpc.qp.machine.QuarryFakePlayerCommon;
import com.yogpc.qp.machine.misc.BlockBreakEventResult;
import com.yogpc.qp.machine.quarry.QuarryEntity;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.OptionalInt;

public final class QuarryEntityFabric extends QuarryEntity {
    boolean shouldRemoveFluid = true;
    boolean shouldRemoveBedrock = false;

    public QuarryEntityFabric(BlockPos pos, BlockState blockState) {
        super(PlatformAccessFabric.RegisterObjectsFabric.QUARRY_ENTITY_TYPE, pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("shouldRemoveFluid", shouldRemoveFluid);
        tag.putBoolean("shouldRemoveBedrock", shouldRemoveBedrock);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        shouldRemoveFluid = tag.getBoolean("shouldRemoveFluid");
        shouldRemoveBedrock = tag.getBoolean("shouldRemoveBedrock");
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        shouldRemoveBedrock = componentInput.getOrDefault(QuarryDataComponents.QUARRY_REMOVE_BEDROCK_COMPONENT, Boolean.FALSE);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (shouldRemoveBedrock) {
            components.set(QuarryDataComponents.QUARRY_REMOVE_BEDROCK_COMPONENT, true);
        }
    }

    @Override
    protected BlockBreakEventResult checkBreakEvent(Level level, ServerPlayer fakePlayer, BlockState state, BlockPos target, @Nullable BlockEntity blockEntity) {
        boolean result = PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level, fakePlayer, target, state, blockEntity);
        if (!result) {
            // cancelled
            PlayerBlockBreakEvents.CANCELED.invoker().onBlockBreakCanceled(level, fakePlayer, target, state, blockEntity);
            return BlockBreakEventResult.CANCELED;
        }
        return new BlockBreakEventResult(false, OptionalInt.of(0));
    }

    @Override
    protected BlockBreakEventResult afterBreak(Level level, ServerPlayer fakePlayer, BlockState state, BlockPos target, @Nullable BlockEntity blockEntity, List<ItemStack> drops, ItemStack pickaxe) {
        PlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak(level, fakePlayer, target, state, blockEntity);
        return BlockBreakEventResult.EMPTY;
    }

    @Override
    protected ServerPlayer getQuarryFakePlayer(ServerLevel level, BlockPos target) {
        var fakePlayer = FakePlayer.get(level, QuarryFakePlayerCommon.PROFILE);
        QuarryFakePlayerCommon.setDirection(fakePlayer, Direction.DOWN);
        return fakePlayer;
    }

    @Override
    protected boolean shouldRemoveFluid() {
        return shouldRemoveFluid;
    }

    @Override
    protected boolean shouldRemoveBedrock() {
        return shouldRemoveBedrock;
    }

    @Override
    protected boolean shouldCollectExp() {
        return true;
    }

    @Override
    protected BlockState stateAfterBreak(Level level, BlockPos pos, BlockState before) {
        return Blocks.AIR.defaultBlockState();
    }
}
