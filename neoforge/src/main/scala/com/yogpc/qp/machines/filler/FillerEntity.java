package com.yogpc.qp.machines.filler;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.*;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public final class FillerEntity extends PowerTile implements CheckerLog, PowerConfig.Provider, MenuProvider, HasItemHandler {
    private static final Logger LOGGER = QuarryPlus.getLogger(FillerEntity.class);
    final FillerContainer container;
    IItemHandler itemHandler;
    final FillerAction fillerAction;

    public FillerEntity(@NotNull BlockPos pos, BlockState state) {
        super(Holder.FILLER_TYPE, pos, state);
        container = new FillerContainer(27);
        itemHandler = container.createHandler();
        fillerAction = new FillerAction();
    }

    @Override
    protected void saveNbtData(CompoundTag nbt) {
        if (!fillerAction.isFinished()) {
            nbt.put("fillerAction", this.fillerAction.toNbt());
        }
        nbt.put("container", container.createTag());
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        if (nbt.contains("fillerAction")) {
            this.fillerAction.fromNbt(nbt.getCompound("fillerAction"));
        }
        container.fromTag(nbt.getList("container", Tag.TAG_COMPOUND));
    }

    @Override
    public List<? extends Component> getDebugLogs() {
        return Stream.of(
            "Iterator: %s".formatted(this.fillerAction.iterator),
            energyString()
        ).map(Component::literal).toList();
    }

    void tick() {
        if (!this.fillerAction.isFinished() && hasEnoughEnergy()) {
            if (level == null) {
                LOGGER.error("Level is NULL in {}#tick at {}", getClass().getSimpleName(), getBlockPos());
                return;
            }
            var energy = PowerManager.getFillerEnergy(this);
            this.fillerAction.tick(this.container::getFirstItem, this, energy);
            if (this.fillerAction.isFinished()) {
                logUsage();
            }
        }
    }

    /**
     * Initialize iterator to start work. This method will search markers near the block, and remove from the world.
     * The markers are dropped near this block.
     * Must be called in server.
     */
    void start(Action fillerAction) {
        if (!this.fillerAction.isFinished()) return;
        assert level != null;
        Stream.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
            .map(getBlockPos()::relative)
            .map(level::getBlockEntity)
            .mapMulti(MapMulti.cast(QuarryMarker.class))
            .findFirst()
            .ifPresent(m -> {
                this.fillerAction.setIterator(m.getArea().map(a -> new SkipIterator(a, fillerAction.iteratorProvider)).orElse(null));
                m.removeAndGetItems().forEach(stack -> Block.popResource(level, getBlockPos().above(), stack));
            });
    }

    @Override
    public IItemHandler getItemCapability(Direction ignore) {
        return itemHandler;
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
        BOX(FillerTargetPosIterator.Box::new), WALL(FillerTargetPosIterator.Wall::new), PILLAR(FillerTargetPosIterator.Pillar::new);
        final Function<Area, FillerTargetPosIterator> iteratorProvider;

        Action(Function<Area, FillerTargetPosIterator> iteratorProvider) {
            this.iteratorProvider = iteratorProvider;
        }
    }
}
