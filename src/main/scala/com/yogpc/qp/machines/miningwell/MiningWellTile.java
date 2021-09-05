package com.yogpc.qp.machines.miningwell;

import java.util.List;
import java.util.stream.Stream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.CheckerLog;
import com.yogpc.qp.machines.EnchantmentLevel;
import com.yogpc.qp.machines.ItemConverter;
import com.yogpc.qp.machines.MachineStorage;
import com.yogpc.qp.machines.PowerManager;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.QuarryFakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.FluidAttributes;

public class MiningWellTile extends PowerTile implements CheckerLog, MachineStorage.HasStorage {
    private final MachineStorage storage = new MachineStorage();
    private final ItemConverter itemConverter = ItemConverter.defaultConverter();
    public int digMinY = 0;
    private int interval = 0;
    private boolean finished = false;

    public MiningWellTile(BlockPos pos, BlockState state) {
        super(Holder.MINING_WELL_TYPE, pos, state);
    }

    public void tick() {
        // Assumed in server world
        assert level != null;
        assert !level.isClientSide;
        if (!hasEnoughEnergy() || finished || --interval > 0) {
            return;
        } else {
            interval = 40; // 2 sec
            if (!getBlockState().getValue(MiningWellBlock.WORKING)) {
                level.setBlock(getBlockPos(), getBlockState().setValue(MiningWellBlock.WORKING, true), Block.UPDATE_CLIENTS);
                setChanged();
            }
        }
        int y = getBlockPos().getY() - 1;
        for (; y >= digMinY; y--) {
            var targetPos = getBlockPos().atY(y);
            var state = level.getBlockState(targetPos);
            var fluid = level.getFluidState(targetPos);

            if (state.isAir()) continue;
            var pickaxe = new ItemStack(Items.NETHERITE_PICKAXE);
            var fakePlayer = QuarryFakePlayer.get((ServerLevel) level);
            fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, pickaxe);
            var breakEvent = new BlockEvent.BreakEvent(level, targetPos, state, fakePlayer);
            MinecraftForge.EVENT_BUS.post(breakEvent);
            if (breakEvent.isCanceled()) continue;

            if (!fluid.isEmpty()) {
                if (useEnergy(PowerManager.getBreakBlockFluidEnergy(EnchantmentLevel.NoEnchantments.INSTANCE), Reason.REMOVE_FLUID, false)) {
                    if (state.getBlock() instanceof LiquidBlock) {
                        if (!fluid.isEmpty() && fluid.isSource())
                            storage.addFluid(fluid.getType(), FluidAttributes.BUCKET_VOLUME);
                        level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    } else if (state.getBlock() instanceof BucketPickup drain) {
                        var bucket = drain.pickupBlock(level, targetPos, state);
                        storage.addFluid(bucket);
                    } else {
                        // What ?
                        level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
                break;
            } else if (canBreak(level, targetPos, state)) {
                breakBlock(level, targetPos, state);
                break;
            }
        }
        if (y < digMinY) {
            level.setBlock(getBlockPos(), getBlockState().setValue(MiningWellBlock.WORKING, false), Block.UPDATE_CLIENTS);
            setChanged();
            finished = true;
        }
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.put("storage", storage.toNbt());
        nbt.putInt("digMinY", digMinY);
        nbt.putInt("waitingTick", interval);
        nbt.putBoolean("finished", finished);
        return super.save(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        storage.readNbt(nbt.getCompound("storage"));
        digMinY = nbt.getInt("digMinY");
        interval = nbt.getInt("waitingTick");
        finished = nbt.getBoolean("finished");
    }

    @Override
    public List<? extends Component> getDebugLogs() {
        return Stream.of(
            "MinY: " + digMinY,
            "Interval: " + interval,
            "Finished: " + finished
        ).map(TextComponent::new).toList();
    }

    @Override
    public MachineStorage getStorage() {
        return storage;
    }

    private boolean canBreak(Level targetWorld, BlockPos targetPos, BlockState state) {
        var hardness = state.getDestroySpeed(targetWorld, targetPos);
        return hardness >= 0;
    }

    private void breakBlock(Level level, BlockPos pos, BlockState state) {
        var hardness = state.getDestroySpeed(level, pos);
        if (useEnergy(PowerManager.getBreakEnergy(hardness, EnchantmentLevel.NoEnchantments.INSTANCE), Reason.BREAK_BLOCK, false)) {
            var drops = Block.getDrops(state, (ServerLevel) level, pos, level.getBlockEntity(pos), null, new ItemStack(Items.NETHERITE_PICKAXE));
            drops.stream().map(itemConverter::map).forEach(this.storage::addItem);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            var sound = state.getSoundType();
            level.playSound(null, pos, sound.getBreakSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 4F, sound.getPitch() * 0.8F);
        }
    }

    void reset() {
        finished = false;
        interval = 40;
    }
}
