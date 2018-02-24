package com.yogpc.qp.compat;

import java.util.LinkedList;
import java.util.List;

import buildcraft.api.inventory.IItemTransactor;
import buildcraft.lib.inventory.ItemTransactorHelper;
import buildcraft.lib.inventory.NoSpaceTransactor;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ModAPIManager;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import scala.Option;

import static com.yogpc.qp.version.VersionUtil.empty;
import static com.yogpc.qp.version.VersionUtil.getCount;
import static com.yogpc.qp.version.VersionUtil.isEmpty;
import static com.yogpc.qp.version.VersionUtil.nonEmpty;

public class InvUtils {

    /**
     * @param w   world
     * @param pos Position of TileEntity from which itemstack comes.
     * @param is  ItemStack to be inserted. The stack doesn't change in this method.
     */
    public static ItemStack injectToNearTile(final World w, BlockPos pos, final ItemStack is) {
        //Buildcraft installed.
        if (ModAPIManager.INSTANCE.hasAPI(QuarryPlus.Optionals.Buildcraft_transport)) {
            List<IItemTransactor> transactors = new LinkedList<>();
            for (EnumFacing facing : EnumFacing.VALUES) {
                TileEntity t = w.getTileEntity(pos.offset(facing));
                IItemTransactor transactor = ItemTransactorHelper.getTransactor(t, facing.getOpposite());
                if (transactor != NoSpaceTransactor.INSTANCE) {
                    transactors.add(transactor);
                }
            }
            ItemStack insert = is;
            for (IItemTransactor transactor : transactors) {
                insert = transactor.insert(is, false, false);
                if (insert.isEmpty()) {
                    return com.yogpc.qp.version.VersionUtil.empty();
                }
            }
            return insert;

        } else { //Forge ItemHandeler
            List<IItemHandler> handlers = new LinkedList<>();
            for (EnumFacing facing : EnumFacing.VALUES) {
                TileEntity t = w.getTileEntity(pos.offset(facing));
                if (t != null) {
                    IItemHandler handler = t.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
                    if (getCount(is) != getCount(ItemHandlerHelper.insertItemStacked(handler, is, true))) {
                        handlers.add(handler);
                    }
                }
            }
            ItemStack insert = is;
            for (IItemHandler handler : handlers) {
                insert = ItemHandlerHelper.insertItemStacked(handler, is, false);
                if (isEmpty(insert)) {
                    return empty();
                }
            }
            return insert;
        }
    }

    public static Option<IItemHandler> findItemHander(World world, BlockPos pos, EnumFacing from) {
        TileEntity entity = world.getTileEntity(pos);
        if (entity == null) {
            return Option.empty();
        } else {
            return Option.apply(entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, from));
        }
    }

    public static boolean isDebugItem(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (nonEmpty(stack)) {
            Item item = stack.getItem();
            return item == quarrydebug || item == ic2_meter || item == ic2_wrench || item == ic2_electric_wrench;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public static IBlockState getStateFromItem(ItemBlock itemBlock, int meta) {
        Block block = itemBlock.getBlock();
        return block.getStateFromMeta(meta);
    }

    @GameRegistry.ObjectHolder(QuarryPlus.Optionals.IC2_modID + ":meter")
    public static final Item ic2_meter = new Item();
    @GameRegistry.ObjectHolder(QuarryPlus.Optionals.IC2_modID + ":wrench")
    public static final Item ic2_wrench = new Item();
    @GameRegistry.ObjectHolder(QuarryPlus.Optionals.IC2_modID + ":electric_wrench")
    public static final Item ic2_electric_wrench = new Item();
    @GameRegistry.ObjectHolder(QuarryPlus.modID + ":quarrydebug")
    public static final Item quarrydebug = new Item();
}
