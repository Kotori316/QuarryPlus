package com.yogpc.qp.machines.filler;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.CheckerLog;
import com.yogpc.qp.machines.PowerConfig;
import com.yogpc.qp.machines.PowerManager;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.QuarryMarker;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class FillerEntity extends PowerTile implements CheckerLog, PowerConfig.Provider, MenuProvider {
    private static final Logger LOGGER = QuarryPlus.getLogger(FillerEntity.class);
    final FillerContainer container = new FillerContainer(27);
    final FillerAction fillerAction = new FillerAction();

    public FillerEntity(@NotNull BlockPos pos, BlockState state) {
        super(Holder.FILLER_TYPE, pos, state);
    }

    @Override
    protected void saveNbtData(CompoundTag nbt) {
        if (!fillerAction.isFinished()) {
            nbt.put("fillerAction", this.fillerAction.toNbt());
        }
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        if (nbt.contains("fillerAction")) {
            this.fillerAction.fromNbt(nbt.getCompound("fillerAction"));
        }
    }

    @Override
    public List<? extends Component> getDebugLogs() {
        return Stream.of(
            "Iterator: %s".formatted(this.fillerAction.iterator),
            "%sEnergy:%s %f/%d FE (%d)".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, getEnergy() / (double) PowerTile.ONE_FE, getMaxEnergyStored(), getEnergy())
        ).map(TextComponent::new).toList();
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
