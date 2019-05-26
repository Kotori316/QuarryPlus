package com.yogpc.qp.machines.advpump;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.BuildcraftHelper;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.IEnchantableTile;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.fluid.IFluidState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class BlockAdvPump extends QPBlock {
    public BlockAdvPump() {
        super(Properties.create(Material.IRON)
            .hardnessAndResistance(1.5f, 10f)
            .sound(SoundType.STONE), QuarryPlus.Names.advpump, ItemBlockAdvPump::new);
        setDefaultState(getStateContainer().getBaseState().with(QPBlock.WORKING(), false));
    }

    @Override
    public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (super.onBlockActivated(state, worldIn, pos, playerIn, hand, facing, hitX, hitY, hitZ)) return true;
        ItemStack stack = playerIn.getHeldItem(hand);
        if (BuildcraftHelper.isWrench(playerIn, hand, stack, new RayTraceResult(new Vec3d(hitX, hitY, hitZ), facing, pos))) {
            if (!worldIn.isRemote) {
                Optional.ofNullable((TileAdvPump) worldIn.getTileEntity(pos)).ifPresent(TileAdvPump::G_ReInit);
            }
            return true;
        } else if (Config.common().debug() && stack.getItem() == Items.STICK) {
            Optional.ofNullable((TileAdvPump) worldIn.getTileEntity(pos)).ifPresent(TileAdvPump::toggleDelete);
            return true;
        } else if (stack.getItem() == Holder.itemStatusChecker()) {
            if (!worldIn.isRemote)
                Optional.ofNullable((IEnchantableTile) worldIn.getTileEntity(pos)).ifPresent(t ->
                    t.sendEnchantMassage(playerIn));
            return true;
        } else if (!playerIn.isSneaking()) {
            if (!worldIn.isRemote) {
                NetworkHooks.openGui(((EntityPlayerMP) playerIn), (TileAdvPump) worldIn.getTileEntity(pos), pos);
            }
            return true;
        }
        return false;
    }


    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest, IFluidState fluid) {
        return willHarvest || super.removedByPlayer(state, world, pos, player, false, fluid);
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        worldIn.removeBlock(pos);
    }

    @Override
    public void getDrops(IBlockState state, NonNullList<ItemStack> drops, World world, BlockPos pos, int fortune) {
        TileEntity entity = world.getTileEntity(pos);
        if (entity instanceof TileAdvPump) {
            TileAdvPump quarry = (TileAdvPump) entity;
            ItemStack stack = new ItemStack(itemBlock(), 1);
            IEnchantableTile.Util.enchantmentToIS(quarry, stack);
            drops.add(stack);
        }
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (!worldIn.isRemote) {
            Consumer<TileAdvPump> consumer = IEnchantableTile.Util.initConsumer(stack);
            Optional.ofNullable((TileAdvPump) worldIn.getTileEntity(pos)).ifPresent(consumer.andThen(TileAdvPump.requestTicket));
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder) {
        builder.add(QPBlock.WORKING());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(new TextComponentTranslation(TranslationKeys.TOOLTIP_ADVPUMP, ' '));
    }

    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return Holder.advPumpType().create();
    }
}
