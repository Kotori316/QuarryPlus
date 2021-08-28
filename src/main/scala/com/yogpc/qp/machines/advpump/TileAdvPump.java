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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.FluidDrainable;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.tag.FluidTags;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class TileAdvPump extends PowerTile
    implements MachineStorage.HasStorage, EnchantmentLevel.HasEnchantments,
    CheckerLog, BlockEntityClientSerializable {

    private final MachineStorage storage = new MachineStorage();
    private int y;
    private Target target;
    private EnchantmentEfficiency enchantmentEfficiency;
    private boolean finished = false;

    public TileAdvPump(BlockPos pos, BlockState state) {
        super(QuarryPlus.ModObjects.ADV_PUMP_TYPE, pos, state);
        y = pos.getY() - 1;
        enchantmentEfficiency = new EnchantmentEfficiency(List.of());
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.put("storage", storage.toNbt());
        nbt.putInt("y", y);
        nbt.put("enchantments", enchantmentEfficiency.toNbt());
        nbt.putBoolean("finished", finished);
        return super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        storage.readNbt(nbt.getCompound("storage"));
        y = nbt.getInt("y");
        setEnchantment(EnchantmentEfficiency.fromNbt(nbt.getCompound("enchantments")));
        finished = nbt.getBoolean("finished");
    }

    public static void tick(World world, BlockPos pos, BlockState state, TileAdvPump pump) {
        long fluidSum = pump.storage.getFluidMap().values().stream().mapToLong(Long::longValue).sum();
        if (pump.hasEnoughEnergy() && !pump.finished && fluidSum <= pump.enchantmentEfficiency.fluidCapacity) {
            // In server world.
            if (pump.target == null) {
                var initPos = pos.withY(pump.y);
                pump.target = Target.getTarget(world, initPos, pump.enchantmentEfficiency.rangePredicate(initPos));
                world.setBlockState(pos, state.with(BlockAdvPump.WORKING, true));
            }
            if (pump.target.hasNext()) {
                while (pump.target.hasNext()) {
                    var target = pump.target.next();
                    var result = pump.pumpFluid(world, target,
                        f -> f.isIn(FluidTags.WATER) ? QuarryPlus.ModObjects.BLOCK_DUMMY.getDefaultState() : Blocks.AIR.getDefaultState(), true);
                    if (!result.isSuccess())
                        break;
                    Direction.Type.HORIZONTAL.stream().map(target::offset)
                        .filter(pump.target.getPredicate().negate())
                        .forEach(p -> pump.pumpFluid(world, p, f -> QuarryPlus.ModObjects.BLOCK_FRAME.getDammingState(), false));
                }
            } else {
                // Go to next y
                if (!pump.target.checkAllFluidsRemoved(world, pos.withY(pump.y))) {
                    pump.y -= 1;
                    var nextPos = pos.withY(pump.y);
                    if (shouldFinish(world, nextPos)) {
                        // Next pos doesn't have fluid block. Finish.
                        pump.finished = true;
                        pump.target = null;
                        world.setBlockState(pos, state.with(BlockAdvPump.WORKING, false));
                        pump.logUsage(QuarryPlus.LOGGER::info);
                        for (int i = pos.getY() - 1; i > pump.y; i--) {
                            var withY = pos.withY(i);
                            var blockState = world.getBlockState(withY);
                            if (blockState.isOf(QuarryPlus.ModObjects.BLOCK_DUMMY)) {
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

    private static boolean shouldFinish(World world, BlockPos nextPos) {
        var blockCondition = world.isAir(nextPos) || world.getBlockState(nextPos).isOf(QuarryPlus.ModObjects.BLOCK_DUMMY);
        return world.getFluidState(nextPos).isEmpty() && !blockCondition;
    }

    private BreakResult pumpFluid(World world, BlockPos target, Function<Fluid, BlockState> replaceBlockGetter, boolean useEnergy) {
        var fluidState = world.getFluidState(target);
        if (fluidState.isEmpty()) {
            return BreakResult.SKIPPED;
        } else if (!fluidState.isStill()) {
            // Just remove with no cost.
            world.setBlockState(target, replaceBlockGetter.apply(fluidState.getFluid()), Block.NOTIFY_ALL);
            return BreakResult.SUCCESS;
        } else {
            if (useEnergy && !useEnergy(this.enchantmentEfficiency.baseEnergy, Reason.ADV_PUMP_FLUID, false)) {
                return BreakResult.NOT_ENOUGH_ENERGY;
            }
            var blockState = world.getBlockState(target);
            if (!(blockState.getBlock() instanceof FluidBlock) && blockState.getBlock() instanceof FluidDrainable drain) {
                var drained = drain.tryDrainFluid(world, target, blockState);
                this.storage.addFluid(drained);
            } else {
                this.storage.addFluid(fluidState.getFluid(), MachineStorage.ONE_BUCKET);
                world.setBlockState(target, replaceBlockGetter.apply(fluidState.getFluid()), Block.NOTIFY_ALL);
            }
            return BreakResult.SUCCESS;
        }
    }

    public void setEnchantment(EnchantmentEfficiency enchantmentEfficiency) {
        this.enchantmentEfficiency = enchantmentEfficiency;
        this.maxEnergy = enchantmentEfficiency.energyCapacity;
        if (world != null && !world.isClient)
            sync();
    }

    public void reset() {
        target = null;
        finished = false;
        y = pos.getY() - 1;
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
    public List<? extends Text> getDebugLogs() {
        var fluidSummery = this.storage.getFluidMap().entrySet().stream()
            .map(e -> "%s: %d mB".formatted(e.getKey().getId(), e.getValue()))
            .map(LiteralText::new)
            .toList();
        if (fluidSummery.isEmpty()) {
            return List.of(new LiteralText("No Fluid."));
        } else {
            return fluidSummery;
        }
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        readNbt(tag);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        return writeNbt(tag);
    }
}
