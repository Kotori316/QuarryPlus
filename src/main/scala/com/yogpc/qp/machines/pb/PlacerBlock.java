package com.yogpc.qp.machines.pb;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.InvUtils;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import static jp.t2v.lab.syntax.MapStreamSyntax.optCast;
import static net.minecraft.state.properties.BlockStateProperties.FACING;
import static net.minecraft.state.properties.BlockStateProperties.TRIGGERED;

public class PlacerBlock extends QPBlock {
    public PlacerBlock() {
        super(Properties.create(Material.IRON).hardnessAndResistance(1.2f), QuarryPlus.Names.placer, BlockItem::new);
        setDefaultState(getStateContainer().getBaseState().with(FACING, Direction.NORTH).with(TRIGGERED, Boolean.FALSE));
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
                                             Hand hand, BlockRayTraceResult hit) {
        if (super.onBlockActivated(state, worldIn, pos, player, hand, hit).isSuccess()) return ActionResultType.SUCCESS;
        if (!player.isCrouching()) {
            if (!worldIn.isRemote) {
                ItemStack stack = player.getHeldItem(hand);
                if (stack.getItem() == Items.REDSTONE_TORCH) {
                    Optional.ofNullable((PlacerTile) worldIn.getTileEntity(pos))
                        .ifPresent(t -> {
                            t.cycleRedstoneMode();
                            player.sendStatusMessage(new TranslationTextComponent(TranslationKeys.PLACER_RS, t.redstoneMode), false);
                        });
                } else {
                    Optional.ofNullable((PlacerTile) worldIn.getTileEntity(pos))
                        .ifPresent(o -> NetworkHooks.openGui(((ServerPlayerEntity) player), o, pos));
                }
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            InvUtils.dropAndUpdateInv(worldIn, pos, (PlacerTile) worldIn.getTileEntity(pos), this);
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public TileEntityType<? extends TileEntity> getTileType() {
        return Holder.placerType();
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, TRIGGERED);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslationTextComponent(TranslationKeys.TOOLTIP_PLACER_PLUS));
        }
    }

    // Placer Works

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        super.tick(state, worldIn, pos, rand);
        Direction facing = state.get(FACING);
        boolean isPowered = state.get(TRIGGERED);
        Optional<PlacerTile> tile = Optional.ofNullable(worldIn.getTileEntity(pos)).flatMap(optCast(PlacerTile.class));
        if (worldIn.isAirBlock(pos.offset(facing)) && isPowered) {
            tile.ifPresent(PlacerTile::placeBlock);
        } else if (!isPowered) {
            tile.ifPresent(PlacerTile::breakBlock);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
        // Copied from net.minecraft.block.DispenserBlock#neighborChanged
        Direction facing = state.get(FACING);
        boolean poweredNow = isPoweredToWork(worldIn, pos, facing);
        boolean poweredOld = state.get(TRIGGERED);
        if (poweredNow && !poweredOld) {
            if (Optional.ofNullable(worldIn.getTileEntity(pos)).flatMap(optCast(PlacerTile.class))
                .filter(p -> p.redstoneMode.isPulse()).isPresent()) {
                worldIn.getPendingBlockTicks().scheduleTick(pos, this, this.tickRate(worldIn));
            }
            worldIn.setBlockState(pos, state.with(TRIGGERED, Boolean.TRUE), 4);
        } else if (!poweredNow && poweredOld) {
            if (Optional.ofNullable(worldIn.getTileEntity(pos)).flatMap(optCast(PlacerTile.class))
                .filter(p -> p.redstoneMode.isPulse()).isPresent()) {
                worldIn.getPendingBlockTicks().scheduleTick(pos, this, this.tickRate(worldIn));
            }
            worldIn.setBlockState(pos, state.with(TRIGGERED, Boolean.FALSE), 4);
        }
    }

    private static final Direction[] DIRECTIONS = Direction.values().clone();

    public static boolean isPoweredToWork(World worldIn, BlockPos pos, Direction currentFacing) {
        return Arrays.stream(DIRECTIONS).filter(Predicate.isEqual(currentFacing).negate())
            .anyMatch(f -> worldIn.isSidePowered(pos.offset(f), f)) || worldIn.isBlockPowered(pos.up());
    }

    @Override
    public int tickRate(IWorldReader worldIn) {
        return 1;
    }
}
