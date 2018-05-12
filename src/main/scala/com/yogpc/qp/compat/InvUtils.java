package com.yogpc.qp.compat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModAPIManager;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.EmptyHandler;
import scala.Option;

import static com.yogpc.qp.version.VersionUtil.empty;
import static com.yogpc.qp.version.VersionUtil.getCount;
import static com.yogpc.qp.version.VersionUtil.isEmpty;
import static com.yogpc.qp.version.VersionUtil.nonEmpty;

public class InvUtils {
    private static final List<IInjector> INJECTORS;

    static {
        List<IInjector> injectors = new ArrayList<>();
        if (ModAPIManager.INSTANCE.hasAPI(QuarryPlus.Optionals.Buildcraft_transport)) {
            try {
                injectors.add((IInjector) Class.forName("com.yogpc.qp.compat.BCInjector").getMethod("init").invoke(null));
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
        if (Loader.isModLoaded(QuarryPlus.Optionals.Mekanism_modID)) {
            try {
                injectors.add((IInjector) Class.forName("com.yogpc.qp.compat.MekanismInjector").getMethod("init").invoke(null));
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
        injectors.add(new ForgeInjector(EmptyHandler.INSTANCE));

        INJECTORS = Collections.unmodifiableList(injectors);
    }

    /**
     * @param w   world
     * @param pos Position of TileEntity from which itemstack comes.
     * @param is  ItemStack to be inserted. The stack doesn't change in this method.
     */
    public static ItemStack injectToNearTile(final World w, BlockPos pos, final ItemStack is) {
        List<? extends IInjector> injectors = Stream.of(EnumFacing.VALUES).flatMap(enumFacing -> {
            TileEntity t = w.getTileEntity(pos.offset(enumFacing));
            return INJECTORS.stream().filter(i -> t != null).flatMap(i -> i.getInjector(is, t, enumFacing));
        }).collect(Collectors.toList());

        ItemStack inserted = is.copy();
        for (IInjector injector : injectors) {
            inserted = injector.inject(inserted, w, pos);
            if (isEmpty(inserted)) {
                return empty();
            }
        }
        return inserted;

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

    private static class ForgeInjector implements IInjector {
        private final IItemHandler handler;

        private ForgeInjector(IItemHandler handler) {
            this.handler = handler;
        }

        @Override
        public Stream<? extends IInjector> getInjector(ItemStack stack, TileEntity entity, EnumFacing facing) {
            IItemHandler handler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
            if (getCount(stack) != getCount(ItemHandlerHelper.insertItemStacked(handler, stack, true))) {
                return Stream.of(new ForgeInjector(handler));
            }
            return Stream.empty();
        }

        @Override
        public ItemStack inject(ItemStack stack, World world, BlockPos fromPos) {
            return ItemHandlerHelper.insertItem(handler, stack, false);
        }
    }

}
