package com.yogpc.qp.machines.marker;

import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.utils.Holder;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReaderBase;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static jp.t2v.lab.syntax.MapStreamSyntax.streamCast;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class BlockMarker extends Block {
    private static final VoxelShape STANDING_Shape = VoxelShapes.create(.35, 0, .35, .65, .65, .65);
    private static final VoxelShape DOWN_Shape = VoxelShapes.create(.35, .35, .35, .65, 1, .65);
    private static final VoxelShape NORTH_Shape = VoxelShapes.create(.35, .35, .35, .65, .65, 1);
    private static final VoxelShape SOUTH_Shape = VoxelShapes.create(.35, .35, 0, .65, .65, .65);
    private static final VoxelShape WEST_Shape = VoxelShapes.create(.35, .35, .35, 1, .65, .65);
    private static final VoxelShape EAST_Shape = VoxelShapes.create(0.0D, .35, .35, .65, .65, .65);
    public final ItemBlock itemBlock;

    public BlockMarker() {
        super(Block.Properties.create(Material.CIRCUITS).lightValue(7));
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.marker);
        itemBlock = new ItemBlock(this, new Item.Properties().group(Holder.tab()));
        itemBlock.setRegistryName(QuarryPlus.modID, QuarryPlus.Names.marker);
        setDefaultState(getStateContainer().getBaseState().with(FACING, EnumFacing.NORTH));
    }

    @Override
    public Item asItem() {
        return itemBlock;
    }

    //---------- Setting of BlockState ----------
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public IBlockState getStateForPlacement(BlockItemUseContext context) {
        return getDefaultState().with(FACING, context.getFace());
    }

    //---------- Setting of Action ----------

    @Override
    @SuppressWarnings("deprecation")
    public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntity entity = worldIn.getTileEntity(pos);
            if (entity instanceof TileMarker) {
                TileMarker marker = (TileMarker) entity;
                if (!marker.hasLink()) {
                    marker.activated();
                } else {
                    player.sendStatusMessage(new TextComponentString(marker.link.toString()), false);
                }
            }
        }
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!worldIn.isRemote) {
            Stream.of(worldIn.getTileEntity(pos))
                .flatMap(streamCast(TileMarker.class))
                .forEach(TileMarker::redstoneUpdate);
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }

    //---------- Setting of TileEntity ----------
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(IBlockState state, IBlockReader world) {
        return Holder.markerTileType().create();
    }

    //---------- Setting of Render ----------
    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    //---------- Setting of Block ----------
    @Override
    @SuppressWarnings("deprecation")
    public BlockFaceShape getBlockFaceShape(IBlockReader worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(IBlockState state, IBlockReader worldIn, BlockPos pos) {
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
    public VoxelShape getCollisionShape(IBlockState state, IBlockReader worldIn, BlockPos pos) {
        return VoxelShapes.empty();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("deprecation")
    public boolean isSideInvisible(IBlockState state, IBlockState adjacentBlockState, EnumFacing side) {
        return true;
    }

    //---------- Setting of Placing block ----------

    /**
     * Just copied from {@link net.minecraft.block.BlockTorchWall}.
     */
    @Override
    @SuppressWarnings("deprecation")
    public boolean isValidPosition(IBlockState state, IWorldReaderBase worldIn, BlockPos pos) {
        EnumFacing enumfacing = state.get(FACING);
        BlockPos blockpos = pos.offset(enumfacing.getOpposite());
        IBlockState iblockstate = worldIn.getBlockState(blockpos);
        return iblockstate.getBlockFaceShape(worldIn, blockpos, enumfacing) == BlockFaceShape.SOLID;
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState updatePostPlacement(IBlockState stateIn, EnumFacing facing, IBlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return !stateIn.isValidPosition(worldIn, currentPos) ? Blocks.AIR.getDefaultState() : stateIn;
    }
}
