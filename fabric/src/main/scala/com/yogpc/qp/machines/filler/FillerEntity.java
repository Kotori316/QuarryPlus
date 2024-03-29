package com.yogpc.qp.machines.filler;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.*;
import com.yogpc.qp.utils.MapMulti;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public final class FillerEntity extends PowerTile implements CheckerLog, EnchantmentLevel.HasEnchantments, ExtendedScreenHandlerFactory {
    private static final Logger LOGGER = QuarryPlus.getLogger(FillerEntity.class);
    final FillerContainer container = new FillerContainer(27);
    final FillerAction fillerAction = new FillerAction(this.container::getFirstItem, this);

    public FillerEntity(@NotNull BlockPos pos, BlockState state) {
        super(QuarryPlus.ModObjects.FILLER_TYPE, pos, state, (long) (ONE_FE * QuarryPlus.config.filler.fillerEnergyCapacity));
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        if (!fillerAction.isFinished()) {
            nbt.put("fillerAction", this.fillerAction.toNbt());
        }
        nbt.put("container", container.createTag());
        super.saveAdditional(nbt);
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
    public EnergyConfigAccessor getAccessor() {
        return FillerEnergyConfigAccessor.INSTANCE;
    }

    @Override
    public List<EnchantmentLevel> getEnchantments() {
        return List.of();
    }

    @Override
    public List<? extends Component> getDebugLogs() {
        return Stream.of(
            "Iterator: %s".formatted(this.fillerAction.iterator),
            "%sEnergy:%s %f/%d FE (%d)".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, getEnergy() / (double) PowerTile.ONE_FE, getMaxEnergy() / PowerTile.ONE_FE, getEnergy())
        ).map(Component::literal).toList();
    }

    void tick() {
        if (!this.fillerAction.isFinished() && hasEnoughEnergy()) {
            if (level == null) {
                LOGGER.error("Level is NULL in {}#tick at {}", getClass().getSimpleName(), getBlockPos());
                return;
            }
            var energy = PowerTile.Constants.getFillerEnergy(this);
            this.fillerAction.tick(energy);
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

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeBlockPos(getBlockPos());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static InventoryStorage getFillerStorage(BlockEntity entity, Direction context) {
        if (entity instanceof FillerEntity filler) {
            return InventoryStorage.of(filler.container, context);
        } else {
            return null;
        }
    }

    public enum Action {
        BOX(FillerTargetPosIterator.Box::new), WALL(FillerTargetPosIterator.Wall::new);
        final Function<Area, FillerTargetPosIterator> iteratorProvider;

        Action(Function<Area, FillerTargetPosIterator> iteratorProvider) {
            this.iteratorProvider = iteratorProvider;
        }
    }
}
