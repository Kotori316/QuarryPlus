package com.yogpc.qp.fabric.machine.quarry;

import com.yogpc.qp.QuarryDataComponents;
import com.yogpc.qp.fabric.PlatformAccessFabric;
import com.yogpc.qp.machine.exp.ExpModule;
import com.yogpc.qp.machine.quarry.QuarryEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Stream;

public final class QuarryEntityFabric extends QuarryEntity implements ExpModule {
    boolean shouldRemoveFluid = true;
    boolean shouldRemoveBedrock = false;
    int collectedExp = 0;

    public QuarryEntityFabric(BlockPos pos, BlockState blockState) {
        super(PlatformAccessFabric.RegisterObjectsFabric.QUARRY_ENTITY_TYPE, pos, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("collectedExp", collectedExp);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        collectedExp = input.getIntOr("collectedExp", collectedExp);
    }

    @Override
    public ValueOutput toClientTag(ValueOutput output) {
        output.putBoolean("shouldRemoveFluid", shouldRemoveFluid);
        output.putBoolean("shouldRemoveBedrock", shouldRemoveBedrock);
        return super.toClientTag(output);
    }

    @Override
    public void fromClientTag(ValueInput input) {
        super.fromClientTag(input);
        shouldRemoveFluid = input.getBooleanOr("shouldRemoveFluid", shouldRemoveFluid);
        shouldRemoveBedrock = input.getBooleanOr("shouldRemoveBedrock", shouldRemoveBedrock);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter dataComponentGetter) {
        super.applyImplicitComponents(dataComponentGetter);
        shouldRemoveBedrock = dataComponentGetter.getOrDefault(QuarryDataComponents.QUARRY_REMOVE_BEDROCK_COMPONENT, Boolean.FALSE);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (shouldRemoveBedrock) {
            components.set(QuarryDataComponents.QUARRY_REMOVE_BEDROCK_COMPONENT, true);
        }
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
    protected @NotNull Optional<ExpModule> getExpModule() {
        return Optional.of(this);
    }

    @Override
    protected BlockState stateAfterBreak(Level level, BlockPos pos, BlockState before) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public void addExp(int amount) {
        collectedExp += amount;
    }

    @Override
    public int getExp() {
        return collectedExp;
    }

    @Override
    public Stream<MutableComponent> checkerLogs() {
        return Stream.concat(
            super.checkerLogs(),
            Stream.of(
                detail(ChatFormatting.GREEN, "ShouldRemoveFluid", String.valueOf(shouldRemoveFluid())),
                detail(ChatFormatting.GREEN, "ShouldRemoveBedrock", String.valueOf(shouldRemoveBedrock()))
            )
        );
    }
}
