package com.yogpc.qp.machines.filler;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.PowerTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public final class FillerAction {
    private static final Logger LOGGER = QuarryPlus.getLogger(FillerAction.class);
    @Nullable
    SkipIterator iterator = null;

    public FillerAction() {
    }

    /**
     * Place block of the given stack and shrink the stack.
     * The block is placed in the world where powerSource exists.
     *
     * @param powerSource - The block entity which provides energy. In the same level as this entity, block is placed.
     */
    public void tick(Supplier<Optional<ItemStack>> stackProvider, PowerTile powerSource, long energy) {
        if (this.iterator == null || powerSource.getLevel() == null) return;
        var maybeStack = stackProvider.get();
        var level = powerSource.getLevel();
        maybeStack.ifPresent(blockStack -> {
            var targetPos = this.iterator.peek(predicate(level, blockStack));
            if (targetPos == null) {
                // Finished.
                this.iterator = null;
            } else {
                if (powerSource.useEnergy(energy, PowerTile.Reason.FILLER, false)) {
                    var context = new DirectionalPlaceContext(level, targetPos, Direction.DOWN, blockStack, Direction.UP);
                    var state = getStateFromItem((BlockItem) blockStack.getItem(), context);
                    if (state != null) {
                        level.setBlock(targetPos, state, Block.UPDATE_ALL);
                        blockStack.shrink(1);
                    }
                    this.iterator.commit(targetPos, false);
                }
            }
        });
    }

    /**
     * Place block selected the pos and dimension.
     * The block is placed in the given world.
     *
     * @param level - where the block is placed. It doesn't have to be equal to world of powerSource.
     */
    public void tick(Level level, PowerTile powerSource, long energy) {
        if (this.iterator == null || powerSource.getLevel() == null) return;
        var targetPos = this.iterator.peek(predicate(level));
        if (targetPos == null) {
            // Finished.
            this.iterator = null;
        } else {
            var toPlace = getToReplace(level.dimension(), targetPos);
            if (powerSource.useEnergy(energy, PowerTile.Reason.FILLER, false)) {
                level.setBlock(targetPos, toPlace, Block.UPDATE_ALL);
                this.iterator.commit(targetPos, false);
            }
        }
    }

    public boolean isFinished() {
        return this.iterator == null;
    }

    public CompoundTag toNbt() {
        var tag = new CompoundTag();
        if (this.iterator != null) {
            tag.put("iterator", this.iterator.toNbt());
        }
        return tag;
    }

    public void fromNbt(CompoundTag tag) {
        if (tag.contains("iterator")) {
            this.iterator = SkipIterator.fromNbt(tag.getCompound("iterator"));
        }
    }

    public void setIterator(@Nullable SkipIterator iterator) {
        this.iterator = iterator;
    }

    private static Predicate<BlockPos> predicate(Level level, ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem) {
            return pos -> {
                var context = new DirectionalPlaceContext(level, pos, Direction.DOWN, stack, Direction.UP);
                var state = getStateFromItem(blockItem, context);
                if (!context.canPlace() || state == null) {
                    return false;
                } else {
                    return level.isUnobstructed(state, pos, CollisionContext.empty());
                }
            };
        } else {
            return pos -> false;
        }
    }

    private static Predicate<BlockPos> predicate(Level level) {
        return pos -> {
            var state = getToReplace(level.dimension(), pos);
            var stack = new ItemStack(state.getBlock());
            var context = new DirectionalPlaceContext(level, pos, Direction.DOWN, stack, Direction.UP);
            if (!context.canPlace()) {
                return false;
            } else {
                return level.isUnobstructed(state, pos, CollisionContext.empty());
            }
        };
    }

    @Nullable
    private static BlockState getStateFromItem(BlockItem blockItem, DirectionalPlaceContext context) {
        try {
            return (BlockState) ObfuscationReflectionHelper.findMethod(BlockItem.class, "m_5965_", BlockPlaceContext.class)
                .invoke(blockItem, context);
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Caught exception in Filler", e);
            return null;
        }
    }

    public static BlockState getToReplace(ResourceKey<Level> dimension, BlockPos pos) {
        if (dimension.equals(Level.NETHER)) {
            return Blocks.NETHERRACK.defaultBlockState();
        } else if (dimension.equals(Level.END)) {
            return Blocks.END_STONE.defaultBlockState();
        } else if (dimension.equals(Level.OVERWORLD)) {
            if (pos.getY() >= 0)
                return Blocks.STONE.defaultBlockState();
            else
                return Blocks.DEEPSLATE.defaultBlockState();
        } else {
            return Blocks.STONE.defaultBlockState();
        }
    }
}
