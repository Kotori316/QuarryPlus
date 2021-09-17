package com.yogpc.qp.machines.advpump;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.BreakResult;
import com.yogpc.qp.machines.CheckerLog;
import com.yogpc.qp.machines.EnchantmentLevel;
import com.yogpc.qp.machines.MachineStorage;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.module.EnergyModuleItem;
import com.yogpc.qp.machines.module.ModuleInventory;
import com.yogpc.qp.machines.module.QuarryModule;
import com.yogpc.qp.machines.module.ReplacerModule;
import com.yogpc.qp.packet.ClientSync;
import com.yogpc.qp.packet.ClientSyncMessage;
import com.yogpc.qp.packet.PacketHandler;
import javax.annotation.Nonnull;
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
import net.minecraftforge.fluids.FluidAttributes;

public class TileAdvPump extends PowerTile
    implements MachineStorage.HasStorage, EnchantmentLevel.HasEnchantments,
    CheckerLog, ClientSync, ModuleInventory.HasModuleInventory {

    private final MachineStorage storage = new MachineStorage();
    private int y;
    private Target target;
    private EnchantmentEfficiency enchantmentEfficiency;
    private boolean finished = false;
    public boolean deleteFluid = false;
    private final ModuleInventory moduleInventory;
    private Set<QuarryModule> modules = Set.of();

    public TileAdvPump(BlockPos pos, BlockState state) {
        super(Holder.ADV_PUMP_TYPE, pos, state);
        y = pos.getY() - 1;
        enchantmentEfficiency = new EnchantmentEfficiency(List.of());
        moduleInventory = new ModuleInventory(5, this::updateModule, TileAdvPump::isCapableModule, this);
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.put("storage", storage.toNbt());
        nbt.putInt("y", y);
        nbt.put("enchantments", enchantmentEfficiency.toNbt());
        nbt.putBoolean("finished", finished);
        nbt.putBoolean("deleteFluid", deleteFluid);
        nbt.put("moduleInventory", moduleInventory.serializeNBT());
        return super.save(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        storage.readNbt(nbt.getCompound("storage"));
        y = nbt.getInt("y");
        setEnchantment(EnchantmentEfficiency.fromNbt(nbt.getCompound("enchantments")));
        finished = nbt.getBoolean("finished");
        deleteFluid = nbt.getBoolean("deleteFluid");
        moduleInventory.deserializeNBT(nbt.getCompound("moduleInventory"));
    }

    public static void tick(Level world, BlockPos pos, BlockState state, TileAdvPump pump) {
        long fluidSum = pump.storage.getFluidMap().values().stream().mapToLong(Long::longValue).sum();
        if (pump.hasEnoughEnergy() && !pump.finished && fluidSum <= pump.enchantmentEfficiency.fluidCapacity) {
            // In server world.
            if (pump.target == null) {
                var initPos = pos.atY(pump.y);
                pump.target = Target.getTarget(world, initPos, pump.enchantmentEfficiency.rangePredicate(initPos), pump::isReplaceBlock);
                world.setBlock(pos, state.setValue(BlockAdvPump.WORKING, true), Block.UPDATE_ALL);
            }
            if (pump.target.hasNext()) {
                while (pump.target.hasNext()) {
                    var target = pump.target.next();
                    var result = pump.pumpFluid(world, target, pump::getStateForReplace, true);
                    if (!result.isSuccess())
                        break;
                    Direction.Plane.HORIZONTAL.stream().map(target::relative)
                        .filter(pump.target.getPredicate().negate())
                        .forEach(p -> pump.pumpFluid(world, p, f -> Holder.BLOCK_FRAME.getDammingState(), false));
                }
            } else {
                // Go to next y
                if (!pump.target.checkAllFluidsRemoved(world, pos.atY(pump.y), pump::isReplaceBlock)) {
                    pump.y -= 1;
                    var nextPos = pos.atY(pump.y);
                    if (shouldFinish(world, nextPos)) {
                        // Next pos doesn't have fluid block. Finish.
                        pump.finished = true;
                        pump.target = null;
                        world.setBlock(pos, state.setValue(BlockAdvPump.WORKING, false), Block.UPDATE_ALL);
                        pump.logUsage(QuarryPlus.LOGGER::info);
                        for (int i = pos.getY() - 1; i > pump.y; i--) {
                            var withY = pos.atY(i);
                            var blockState = world.getBlockState(withY);
                            if (blockState.is(Holder.BLOCK_DUMMY)) {
                                world.removeBlock(withY, false);
                                break;
                            } else if (!blockState.isAir()) {
                                break;
                            }
                        }
                    } else {
                        // Go to the next Y.
                        pump.target = Target.getTarget(world, nextPos, pump.enchantmentEfficiency.rangePredicate(nextPos), pump::isReplaceBlock);
                    }
                }
            }
        }
    }

    private static boolean shouldFinish(Level world, BlockPos nextPos) {
        var blockState = world.getBlockState(nextPos);
        var blockCondition = blockState.isAir() || blockState.is(Holder.BLOCK_DUMMY);
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
                if (!deleteFluid) this.storage.addFluid(drained);
            } else {
                if (!deleteFluid) this.storage.addFluid(fluidState.getType(), FluidAttributes.BUCKET_VOLUME);
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

    @Nonnull
    private Optional<BlockState> getReplaceModuleState() {
        return getReplacerModule().map(ReplacerModule::getState).filter(Predicate.not(BlockState::isAir));
    }

    private BlockState getStateForReplace(Fluid f) {
        return getReplaceModuleState()
            .orElse(f.is(FluidTags.WATER) ? Holder.BLOCK_DUMMY.defaultBlockState() : Blocks.AIR.defaultBlockState());
    }

    private boolean isReplaceBlock(BlockState state) {
        return state == getReplaceModuleState().orElse(Holder.BLOCK_DUMMY.defaultBlockState());
    }

    public void reset() {
        target = null;
        finished = false;
        y = getBlockPos().getY() - 1;
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

    public void sync() {
        if (level != null && !level.isClientSide)
            PacketHandler.sendToClient(new ClientSyncMessage(this), level);
    }

    void updateModule() {
        this.modules = Set.copyOf(moduleInventory.getModules());
    }

    static boolean isCapableModule(QuarryModule module) {
        return module instanceof EnergyModuleItem.EnergyModule || module instanceof ReplacerModule;
    }

    @Override
    public ModuleInventory getModuleInventory() {
        return moduleInventory;
    }

    @Override
    public Set<QuarryModule> getLoadedModules() {
        return modules;
    }
}
