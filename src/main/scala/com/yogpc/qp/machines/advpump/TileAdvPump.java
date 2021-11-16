package com.yogpc.qp.machines.advpump;

import java.util.List;
import java.util.function.Function;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.BreakResult;
import com.yogpc.qp.machines.CheckerLog;
import com.yogpc.qp.machines.EnchantmentLevel;
import com.yogpc.qp.machines.MachineStorage;
import com.yogpc.qp.machines.PowerTile;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

public class TileAdvPump extends PowerTile
    implements MachineStorage.HasStorage, EnchantmentLevel.HasEnchantments,
    CheckerLog, BlockEntityClientSerializable {

    private final MachineStorage storage = new MachineStorage();
    private int y;
    private Target target;
    private EnchantmentEfficiency enchantmentEfficiency;
    private boolean finished = false;

    public TileAdvPump(BlockPos pos, BlockState state) {
        super(QuarryPlus.ModObjects.ADV_PUMP_TYPE, pos, state, (long) (ONE_FE * QuarryPlus.config.power.advPumpEnergyCapacity));
        y = pos.getY() - 1;
        enchantmentEfficiency = new EnchantmentEfficiency(List.of());
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.put("storage", storage.toNbt());
        nbt.putInt("y", y);
        nbt.put("enchantments", enchantmentEfficiency.toNbt());
        nbt.putBoolean("finished", finished);
        return super.save(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        storage.readNbt(nbt.getCompound("storage"));
        y = nbt.getInt("y");
        setEnchantment(EnchantmentEfficiency.fromNbt(nbt.getCompound("enchantments")));
        finished = nbt.getBoolean("finished");
    }

    public static void tick(Level world, BlockPos pos, BlockState state, TileAdvPump pump) {
        long fluidSum = pump.storage.getFluidMap().values().stream().mapToLong(Long::longValue).sum();
        if (pump.hasEnoughEnergy() && !pump.finished && fluidSum <= pump.enchantmentEfficiency.fluidCapacity) {
            // In server world.
            if (pump.target == null) {
                BlockPos initPos = pos.atY(pump.y);
                pump.target = Target.getTarget(world, initPos, pump.enchantmentEfficiency.rangePredicate(initPos));
                world.setBlockAndUpdate(pos, state.setValue(BlockAdvPump.WORKING, true));
            }
            if (pump.target.hasNext()) {
                while (pump.target.hasNext()) {
                    BlockPos target = pump.target.next();
                    BreakResult result = pump.pumpFluid(world, target,
                        f -> f.is(FluidTags.WATER) ? QuarryPlus.ModObjects.BLOCK_DUMMY.defaultBlockState() : Blocks.AIR.defaultBlockState(), true);
                    if (!result.isSuccess())
                        break;
                    Direction.Plane.HORIZONTAL.stream().map(target::relative)
                        .filter(pump.target.getPredicate().negate())
                        .forEach(p -> pump.pumpFluid(world, p, f -> QuarryPlus.ModObjects.BLOCK_FRAME.getDammingState(), false));
                }
            } else {
                // Go to next y
                if (!pump.target.checkAllFluidsRemoved(world, pos.atY(pump.y))) {
                    pump.y -= 1;
                    BlockPos nextPos = pos.atY(pump.y);
                    if (shouldFinish(world, nextPos)) {
                        // Next pos doesn't have fluid block. Finish.
                        pump.finished = true;
                        pump.target = null;
                        world.setBlockAndUpdate(pos, state.setValue(BlockAdvPump.WORKING, false));
                        pump.logUsage();
                        for (int i = pos.getY() - 1; i > pump.y; i--) {
                            BlockPos withY = pos.atY(i);
                            BlockState blockState = world.getBlockState(withY);
                            if (blockState.is(QuarryPlus.ModObjects.BLOCK_DUMMY)) {
                                world.removeBlock(withY, false);
                                break;
                            } else if (!blockState.isAir()) {
                                break;
                            }
                        }
                    } else {
                        // Go to the next Y.
                        pump.target = Target.getTarget(world, nextPos, pump.enchantmentEfficiency.rangePredicate(nextPos));
                    }
                }
            }
        }
    }

    private static boolean shouldFinish(Level world, BlockPos nextPos) {
        boolean blockCondition = world.isEmptyBlock(nextPos) || world.getBlockState(nextPos).is(QuarryPlus.ModObjects.BLOCK_DUMMY);
        return world.getFluidState(nextPos).isEmpty() && !blockCondition;
    }

    private BreakResult pumpFluid(Level world, BlockPos target, Function<Fluid, BlockState> replaceBlockGetter, boolean useEnergy) {
        var fluidState = world.getFluidState(target);
        if (fluidState.isEmpty()) {
            return BreakResult.SKIPPED;
        } else if (!fluidState.isSource()) {
            // Just remove with no cost.
            world.setBlock(target, replaceBlockGetter.apply(fluidState.getType()), Block.UPDATE_ALL);
            return BreakResult.SUCCESS;
        } else {
            if (useEnergy && !useEnergy(this.enchantmentEfficiency.baseEnergy, Reason.ADV_PUMP_FLUID, false)) {
                return BreakResult.NOT_ENOUGH_ENERGY;
            }
            var blockState = world.getBlockState(target);
            if (!(blockState.getBlock() instanceof LiquidBlock) && blockState.getBlock() instanceof BucketPickup drain) {
                var drained = drain.pickupBlock(world, target, blockState);
                this.storage.addFluid(drained);
            } else {
                this.storage.addFluid(fluidState.getType(), MachineStorage.ONE_BUCKET);
                world.setBlock(target, replaceBlockGetter.apply(fluidState.getType()), Block.UPDATE_ALL);
            }
            return BreakResult.SUCCESS;
        }
    }

    public void setEnchantment(EnchantmentEfficiency enchantmentEfficiency) {
        this.enchantmentEfficiency = enchantmentEfficiency;
        this.maxEnergy = enchantmentEfficiency.energyCapacity;
        if (level != null && !level.isClientSide)
            sync();
    }

    public void reset() {
        target = null;
        finished = false;
        y = worldPosition.getY() - 1;
    }

    @Override
    public MachineStorage getStorage() {
        return storage;
    }

    @Override
    public List<EnchantmentLevel> getEnchantments() {
        return enchantmentEfficiency.getEnchantments();
    }

    @Override
    public List<? extends Component> getDebugLogs() {
        var fluidSummery = this.storage.getFluidMap().entrySet().stream()
            .map(e -> "%s: %d mB".formatted(e.getKey().getId(), e.getValue()))
            .map(TextComponent::new)
            .toList();
        if (fluidSummery.isEmpty()) {
            return List.of(new TextComponent("No Fluid."));
        } else {
            return fluidSummery;
        }
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        return save(tag);
    }
}
