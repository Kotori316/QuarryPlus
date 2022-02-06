package com.yogpc.qp.machines.filler;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.PowerConfig;
import com.yogpc.qp.machines.PowerManager;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.QuarryMarker;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FillerEntity extends PowerTile implements PowerConfig.Provider, MenuProvider {
    private static final Logger LOGGER = QuarryPlus.getLogger(FillerEntity.class);
    @Nullable
    SkipIterator iterator = null;
    final FillerContainer container = new FillerContainer(27);

    public FillerEntity(@NotNull BlockPos pos, BlockState state) {
        super(Holder.FILLER_TYPE, pos, state);
    }

    @Override
    protected void saveNbtData(CompoundTag nbt) {
        if (this.iterator != null) {
            nbt.put("iterator", this.iterator.toNbt());
        }
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        if (nbt.contains("iterator")) {
            var tag = nbt.getCompound("iterator");
            Area.fromNBT(tag.getCompound("area"))
                .ifPresent(a -> {
                    this.iterator = new SkipIterator(a, FillerTargetPosIterator.Box::new);
                    this.iterator.fromNbt(tag);
                });
        }
    }

    void tick() {
        if (iterator != null && hasEnoughEnergy()) {
            if (level == null) {
                LOGGER.error("Level is NULL in {}#tick at {}", getClass().getSimpleName(), getBlockPos());
                return;
            }
            var energy = PowerManager.getFillerEnergy(this);
            var maybeStack = this.container.getFirstItem();
            maybeStack.ifPresent(blockStack -> {
                var targetPos = this.iterator.peek(predicate(level, blockStack));
                if (targetPos == null) {
                    // Finished.
                    this.iterator = null;
                    logUsage();
                } else {
                    if (useEnergy(energy, Reason.FILLER, false)) {
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
    }

    /**
     * Initialize iterator to start work. This method will search markers near the block, and remove from the world.
     * The markers are dropped near this block.
     * Must be called in server.
     */
    void start(Action fillerAction) {
        if (this.iterator != null) return;
        assert level != null;
        Stream.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
            .map(getBlockPos()::relative)
            .map(level::getBlockEntity)
            .mapMulti(MapMulti.cast(QuarryMarker.class))
            .findFirst()
            .ifPresent(m -> {
                this.iterator = m.getArea().map(a -> new SkipIterator(a, fillerAction.iteratorProvider)).orElse(null);
                m.removeAndGetItems().forEach(stack -> Block.popResource(level, getBlockPos().above(), stack));
            });
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
            return (BlockState) ObfuscationReflectionHelper.findMethod(BlockItem.class, "m_5965_", BlockPlaceContext.class)
                .invoke(blockItem, context);
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Caught exception in Filler", e);
            return null;
        }
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
        return new FillerMenu(pContainerId, pPlayer, this.getBlockPos());
    }

    public enum Action {
        BOX(FillerTargetPosIterator.Box::new), WALL(FillerTargetPosIterator.Wall::new);
        final Function<Area, FillerTargetPosIterator> iteratorProvider;

        Action(Function<Area, FillerTargetPosIterator> iteratorProvider) {
            this.iteratorProvider = iteratorProvider;
        }
    }
}
