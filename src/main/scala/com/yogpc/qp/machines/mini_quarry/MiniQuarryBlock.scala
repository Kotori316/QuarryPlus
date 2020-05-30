package com.yogpc.qp.machines.mini_quarry

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.machines.base.{APacketTile, IEnchantableTile, QPBlock}
import com.yogpc.qp.utils.Holder
import net.minecraft.block.material.Material
import net.minecraft.block.{Block, BlockState, SoundType}
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.{PlayerEntity, ServerPlayerEntity}
import net.minecraft.item.ItemStack
import net.minecraft.state.StateContainer
import net.minecraft.state.properties.BlockStateProperties.FACING
import net.minecraft.tileentity.{TileEntity, TileEntityType}
import net.minecraft.util.math.{BlockPos, BlockRayTraceResult}
import net.minecraft.util.{ActionResultType, Direction, Hand}
import net.minecraft.world.World
import net.minecraftforge.fml.network.NetworkHooks

class MiniQuarryBlock extends QPBlock(
  Block.Properties.create(Material.IRON).hardnessAndResistance(1.5f, 10f).sound(SoundType.STONE),
  QuarryPlus.Names.mini_quarry,
  new MiniQuarryItem(_, _)
) {
  setDefaultState(getStateContainer.getBaseState.`with`(FACING, Direction.NORTH).`with`(QPBlock.WORKING, Boolean.box(false)))

  override protected def fillStateContainer(builder: StateContainer.Builder[Block, BlockState]): Unit = {
    builder.add(FACING, QPBlock.WORKING)
  }

  //noinspection ScalaDeprecation
  override def onBlockActivated(state: BlockState, worldIn: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockRayTraceResult): ActionResultType = {
    if (super.onBlockActivated(state, worldIn, pos, player, hand, hit).isSuccess) return ActionResultType.SUCCESS
    if (!player.isCrouching) {
      if (!worldIn.isRemote) {
        Option(worldIn.getTileEntity(pos)).collect { case miniQuarryTile: MiniQuarryTile => miniQuarryTile }
          .foreach(t => NetworkHooks.openGui(player.asInstanceOf[ServerPlayerEntity], t, pos))
      }
      ActionResultType.SUCCESS
    } else
      ActionResultType.PASS
  }

  override def onBlockPlacedBy(worldIn: World, pos: BlockPos, state: BlockState, placer: LivingEntity, stack: ItemStack): Unit = {
    super.onBlockPlacedBy(worldIn, pos, state, placer, stack)
    if (!worldIn.isRemote) {
      val facing: Direction = placer.getHorizontalFacing.getOpposite
      worldIn.setBlockState(pos, state.`with`(FACING, facing), 2)
      Option(worldIn.getTileEntity(pos)).collect { case m: MiniQuarryTile => m }.foreach { t =>
        IEnchantableTile.Util.init(t, stack.getEnchantmentTagList)
        APacketTile.requestTicket.accept(t)
      }
    }
  }

  override def getTileType: TileEntityType[_ <: TileEntity] = Holder.miniQuarryType
}
