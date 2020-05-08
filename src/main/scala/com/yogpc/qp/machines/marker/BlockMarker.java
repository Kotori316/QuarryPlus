package com.yogpc.qp.machines.marker;

import java.util.Optional;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static jp.t2v.lab.syntax.MapStreamSyntax.optCast;
import static jp.t2v.lab.syntax.MapStreamSyntax.streamCast;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class BlockMarker extends Block {
    private static final VoxelShape STANDING_Shape = VoxelShapes.create(.35, 0, .35, .65, .65, .65);
    private static final VoxelShape DOWN_Shape = VoxelShapes.create(.35, .35, .35, .65, 1, .65);
    private static final VoxelShape NORTH_Shape = VoxelShapes.create(.35, .35, .35, .65, .65, 1);
    private static final VoxelShape SOUTH_Shape = VoxelShapes.create(.35, .35, 0, .65, .65, .65);
    private static final VoxelShape WEST_Shape = VoxelShapes.create(.35, .35, .35, 1, .65, .65);
    private static final VoxelShape EAST_Shape = VoxelShapes.create(0.0D, .35, .35, .65, .65, .65);
    public final BlockItem itemBlock;

    public BlockMarker() {
        super(Block.Properties.create(Material.MISCELLANEOUS).lightValue(7));
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.marker);
        itemBlock = new BlockItem(this, new Item.Properties().group(Holder.tab()));
        itemBlock.setRegistryName(QuarryPlus.modID, QuarryPlus.Names.marker);
        setDefaultState(getStateContainer().getBaseState().with(FACING, Direction.NORTH));
    }

    @Override
    public Item asItem() {
        return itemBlock;
    }

    //---------- Setting of BlockState ----------
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getDefaultState().with(FACING, context.getFace());
    }

    //---------- Setting of Action ----------

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos,
                                             PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!worldIn.isRemote) {
            TileEntity entity = worldIn.getTileEntity(pos);
            if (entity instanceof TileMarker) {
                TileMarker marker = (TileMarker) entity;
                if (marker.enabled()) {
                    if (!marker.hasLink()) {
                        marker.activated();
                    } else {
                        player.sendStatusMessage(new StringTextComponent(marker.link.toString()), false);
                    }
                } else {
                    player.sendStatusMessage(new TranslationTextComponent(TranslationKeys.DISABLE_MESSAGE, new TranslationTextComponent(getTranslationKey())), true);
                }
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isRemote) {
            Stream.of(worldIn.getTileEntity(pos))
                .flatMap(streamCast(TileMarker.class))
                .forEach(TileMarker::redstoneUpdate);
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        Optional.ofNullable(worldIn.getTileEntity(pos)).flatMap(optCast(TileMarker.class)).ifPresent(TileMarker.requestTicket);
    }

    //---------- Setting of TileEntity ----------
    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return Holder.markerTileType().create();
    }

    //---------- Setting of Block ----------

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        switch (state.get(FACING)) {
            default:
            case UP:
                return STANDING_Shape;
            case DOWN:
                return DOWN_Shape;
            case NORTH:
                return NORTH_Shape;
            case SOUTH:
                return SOUTH_Shape;
            case WEST:
                return WEST_Shape;
            case EAST:
                return EAST_Shape;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.empty();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("deprecation")
    public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
        return true;
    }

    //---------- Setting of Placing block ----------

    /**
     * Just copied from {@link WallTorchBlock}.
     */
    @Override
    @SuppressWarnings("deprecation")
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        Direction direction = state.get(FACING);
        BlockPos blockpos = pos.offset(direction.getOpposite());
        BlockState floorState = worldIn.getBlockState(blockpos);
        return floorState.isSolidSide(worldIn, blockpos, direction);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return !stateIn.isValidPosition(worldIn, currentPos) ? Blocks.AIR.getDefaultState() : stateIn;
    }
}
