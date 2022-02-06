package com.yogpc.qp.machines.filler;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.PowerTile;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public final class FillerAction {
    private static final Logger LOGGER = QuarryPlus.getLogger(FillerAction.class);
    final Supplier<Optional<ItemStack>> stackProvider;
    final PowerTile powerSource;
    @Nullable
    SkipIterator iterator = null;

    public FillerAction(Supplier<Optional<ItemStack>> stackProvider, PowerTile powerSource) {
        this.stackProvider = stackProvider;
        this.powerSource = powerSource;
    }

    void tick(long energy) {
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
            this.iterator = SkipIterator.fromNbt(tag);
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

    @Nullable
    private static BlockState getStateFromItem(BlockItem blockItem, DirectionalPlaceContext context) {
        try {
            var blockItemName = FabricLoader.getInstance().getMappingResolver().unmapClassName("intermediary", BlockItem.class.getName());
            var placeContext = FabricLoader.getInstance().getMappingResolver().unmapClassName("intermediary", BlockPlaceContext.class.getName()).replace('.', '/');
            var blockState = FabricLoader.getInstance().getMappingResolver().unmapClassName("intermediary", BlockState.class.getName()).replace('.', '/');

            var methodName = FabricLoader.getInstance().getMappingResolver().mapMethodName("intermediary", blockItemName,
                "method_7707", "(L%s;)L%s;".formatted(placeContext, blockState));
            var method = BlockItem.class.getDeclaredMethod(methodName, BlockPlaceContext.class);
            method.trySetAccessible();
            return (BlockState) method.invoke(blockItem, context);
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Caught exception in Filler", e);
            return null;
        }
    }

}
