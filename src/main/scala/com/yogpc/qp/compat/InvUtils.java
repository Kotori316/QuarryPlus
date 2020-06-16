package com.yogpc.qp.compat;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlus;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.EmptyHandler;
import net.minecraftforge.registries.ObjectHolder;
import scala.Option;

public class InvUtils {

    private static class InjectorHolder {

        private static final List<IInjector> INJECTORS;

        static {
            Stream.Builder<IInjector> builder = Stream.builder();
            // TODO change to net.minecraftforge.fml.common.ModAPIManager
            if (ModList.get().isLoaded(QuarryPlus.Optionals.Buildcraft_transport)) {
                try {
                    builder.add((IInjector) Class.forName("com.yogpc.qp.compat.BCInjector").getMethod("init").invoke(null));
                } catch (ReflectiveOperationException e) {
                    QuarryPlus.LOGGER.error("Error occurred in initializing build craft injector.", e);
                }
            }
            if (ModList.get().isLoaded(QuarryPlus.Optionals.Mekanism_modID)) {
                try {
                    builder.add((IInjector) Class.forName("com.yogpc.qp.compat.MekanismInjector").getMethod("init").invoke(null));
                } catch (ReflectiveOperationException e) {
                    QuarryPlus.LOGGER.error("Error occurred in initializing Mekanism injector.", e);
                }
            }
            builder.add(new ForgeInjector(EmptyHandler.INSTANCE));

            INJECTORS = Collections.unmodifiableList(builder.build().filter(Objects::nonNull).collect(Collectors.toList()));
        }
    }

    /**
     * @param w   world
     * @param pos Position of TileEntity from which itemStack comes.
     * @param is  ItemStack to be inserted. The stack doesn't change in this method.
     * @return Stack NOT inserted to inv.
     */
    public static ItemStack injectToNearTile(@Nonnull final World w, @Nonnull BlockPos pos, final ItemStack is) {
        BlockState state = w.getBlockState(pos);
        Stream<Direction> directionStream = clockwiseDirections(
            state.has(BlockStateProperties.FACING) ? state.get(BlockStateProperties.FACING) : null
        );

        List<? extends IInjector> injectors = directionStream.flatMap(enumFacing ->
            Stream.of(w.getTileEntity(pos.offset(enumFacing)))
                .filter(Objects::nonNull)
                .flatMap(tile -> InjectorHolder.INJECTORS.stream().flatMap(i -> i.getInjector(is, tile, enumFacing)))
        ).collect(Collectors.toList());

        ItemStack inserted = is.copy();
        for (IInjector injector : injectors) {
            inserted = injector.inject(inserted, w, pos);
            if (inserted.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        return inserted;

    }

    public static Stream<Direction> clockwiseDirections(@Nullable Direction blockDirection) {
        if (blockDirection == null) {
            // Block doesn't have direction attribute. returns default list.
            return Stream.of(Direction.values());
        } else {
            switch (blockDirection) {
                case UP:
                    return Stream.concat(
                        Stream.of(Direction.UP),
                        Stream.concat(
                            Stream.iterate(Direction.NORTH, Direction::rotateY).limit(4),
                            Stream.of(Direction.DOWN)
                        )
                    );
                case DOWN:
                    return Stream.concat(
                        Stream.of(Direction.DOWN),
                        Stream.concat(
                            Stream.iterate(Direction.NORTH, Direction::rotateY).limit(4),
                            Stream.of(Direction.UP)
                        )
                    );
                default:
                    return Stream.concat(
                        Stream.iterate(blockDirection, Direction::rotateY).limit(4),
                        Stream.of(Direction.UP, Direction.DOWN)
                    );
            }
        }
    }

    @Deprecated
    public static Option<IItemHandler> findItemHandler(@Nonnull World world, @Nonnull BlockPos pos, @Nullable Direction from) {
        TileEntity entity = world.getTileEntity(pos);
        if (entity == null) {
            return Option.empty();
        } else {
            LazyOptional<IItemHandler> capability = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, from);
            if (capability.isPresent()) {
                return Option.apply(capability.orElse(EmptyHandler.INSTANCE));
            } else {
                return Option.empty();
            }
        }
    }

    public static void dropAndUpdateInv(World world, BlockPos pos, IInventory inventory, Block block) {
        if (inventory != null) {
            InventoryHelper.dropInventoryItems(world, pos, inventory);
            world.updateComparatorOutputLevel(pos, block);
        }
    }

    public static ActionResultType isDebugItem(@Nonnull PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!stack.isEmpty()) {
            Item item = stack.getItem();
            if (item == quarryDebug || item == ic2_meter || item == ic2_wrench || item == ic2_electric_wrench)
                return ActionResultType.SUCCESS;
            else
                return ActionResultType.PASS;
        }
        return ActionResultType.PASS;
    }

    public static boolean hasSmelting(ItemStack stack) {
        return (cofh_smelting != DummyEnchantment.DUMMY_ENCHANTMENT && EnchantmentHelper.getEnchantmentLevel(cofh_smelting, stack) > 0) ||
            (endercore_smelting != DummyEnchantment.DUMMY_ENCHANTMENT && EnchantmentHelper.getEnchantmentLevel(endercore_smelting, stack) > 0);
    }

    @ObjectHolder(QuarryPlus.Optionals.IC2_modID + ":meter")
    public static final Item ic2_meter = new Item(new Item.Properties());
    @ObjectHolder(QuarryPlus.Optionals.IC2_modID + ":wrench")
    public static final Item ic2_wrench = new Item(new Item.Properties());
    @ObjectHolder(QuarryPlus.Optionals.IC2_modID + ":electric_wrench")
    public static final Item ic2_electric_wrench = new Item(new Item.Properties());
    @ObjectHolder(QuarryPlus.modID + ":quarrydebug")
    public static final Item quarryDebug = new Item(new Item.Properties());
    @ObjectHolder(QuarryPlus.Optionals.COFH_modID + ":smelting")
    public static final Enchantment cofh_smelting = DummyEnchantment.DUMMY_ENCHANTMENT;
    @ObjectHolder("endercore:autosmelt")
    public static final Enchantment endercore_smelting = DummyEnchantment.DUMMY_ENCHANTMENT;

    private static class ForgeInjector implements IInjector {
        private final IItemHandler handler;

        private ForgeInjector(IItemHandler handler) {
            this.handler = handler;
        }

        @Override
        public Stream<? extends IInjector> getInjector(ItemStack stack, TileEntity entity, Direction facing) {
            return entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite())
                .filter(
                    h -> stack.getCount() != ItemHandlerHelper.insertItemStacked(h, stack, true).getCount()
                )
                .map(ForgeInjector::new)
                .map(Stream::of)
                .orElse(Stream.empty());
        }

        @Override
        public ItemStack inject(ItemStack stack, World world, BlockPos fromPos) {
            return ItemHandlerHelper.insertItem(handler, stack, false);
        }
    }

    private static final class DummyEnchantment extends Enchantment {
        private static final DummyEnchantment DUMMY_ENCHANTMENT = new DummyEnchantment();

        DummyEnchantment() {
            super(Rarity.COMMON, EnchantmentType.ALL, new EquipmentSlotType[]{});
        }
    }
}
