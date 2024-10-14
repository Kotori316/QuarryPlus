package com.yogpc.qp.machine.marker;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.GeneralScreenHandler;
import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.QpEntity;
import com.yogpc.qp.machine.QpEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

sealed abstract class ExMarkerBlock extends QpEntityBlock permits FlexibleMarkerBlock, ChunkMarkerBlock {
    private static final VoxelShape STANDING_Shape = Shapes.box(.35, 0, .35, .65, .65, .65);

    public ExMarkerBlock(Properties properties, String name) {
        super(properties, name, b -> new BlockItem(b, new Item.Properties()));
    }

    @Override
    protected QpBlock createBlock(Properties properties) {
        try {
            var constructor = getClass().getConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            QuarryPlus.LOGGER.error("Can't get new instance of {}", getClass().getName(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState stateFrom, Direction direction) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return STANDING_Shape;
    }

    /**
     * Just copied from {@link WallTorchBlock}.
     */
    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        Direction direction = Direction.UP;
        BlockPos blockPos = pos.relative(direction.getOpposite());
        BlockState blockState = world.getBlockState(blockPos);
        return blockState.isFaceSturdy(world, blockPos, direction);
    }

    @Override
    public BlockState updateShape(BlockState state, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos currentPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        return state.canSurvive(levelReader, currentPos) ? super.updateShape(state, levelReader, scheduledTickAccess, currentPos, direction, blockPos2, blockState2, randomSource) : Blocks.AIR.defaultBlockState();
    }

    @Override
    public abstract void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack);

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof QpEntity entity) {
            if (!level.isClientSide()) {
                if (entity.enabled) {
                    PlatformAccess.getAccess().openGui((ServerPlayer) player, getScreenHandler(entity));
                } else {
                    player.displayClientMessage(Component.translatable("quarryplus.chat.disable_message", getName()), true);
                }
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    protected abstract GeneralScreenHandler<?> getScreenHandler(QpEntity entity);
}
