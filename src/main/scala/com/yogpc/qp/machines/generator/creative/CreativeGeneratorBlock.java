package com.yogpc.qp.machines.generator.creative;

import java.util.List;
import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nullable;
import jp.t2v.lab.syntax.MapStreamSyntax;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

public class CreativeGeneratorBlock extends QPBlock {
    public CreativeGeneratorBlock() {
        super(Properties.create(Material.IRON)
            .hardnessAndResistance(1.5f, 10f)
            .sound(SoundType.STONE), QuarryPlus.Names.creative_generator, BlockItem::new);
    }

    @Override
    public TileEntityType<? extends TileEntity> getTileType() {
        return Holder.creativeGeneratorType();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(new StringTextComponent("Creative Only"));
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!player.isCrouching()) {
            if (!worldIn.isRemote) {
                Optional.ofNullable(worldIn.getTileEntity(pos))
                    .flatMap(MapStreamSyntax.optCast(CreativeGeneratorTile.class))
                    .ifPresent(t -> NetworkHooks.openGui(((ServerPlayerEntity) player), t, pos));
            }
            return ActionResultType.func_233537_a_(worldIn.isRemote);
        }
        return super.onBlockActivated(state, worldIn, pos, player, hand, hit);
    }
}
