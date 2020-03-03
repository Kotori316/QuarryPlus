package com.yogpc.qp.machines.pb;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Predicate;

import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.APacketTile;
import com.yogpc.qp.machines.base.HasInv;
import com.yogpc.qp.machines.base.IDebugSender;
import com.yogpc.qp.machines.quarry.QuarryFakePlayer;
import com.yogpc.qp.utils.Holder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class PlacerTile extends APacketTile implements
    ITickableTileEntity,
    HasInv,
    IDebugSender {
    public static final scala.Symbol SYMBOL = scala.Symbol.apply("Placer");
    public static final String KEY_ITEM = "items";
    public static final String KEY_LAST_PLACED = "last_placed";
    public static final Map<Direction, Vec3d> DIRECTION_VEC3D_MAP;

    static {
        EnumMap<Direction, Vec3d> map = new EnumMap<>(Direction.class);
        map.put(Direction.DOWN, new Vec3d(0.5, 0, 0.5));
        map.put(Direction.UP, new Vec3d(0.5, 1, 0.5));
        map.put(Direction.NORTH, new Vec3d(0.5, 0.5, 0));
        map.put(Direction.SOUTH, new Vec3d(0.5, 0.5, 1));
        map.put(Direction.EAST, new Vec3d(1, 0.5, 0.5));
        map.put(Direction.WEST, new Vec3d(0, 0.5, 0.5));
        DIRECTION_VEC3D_MAP = Collections.unmodifiableMap(map);
    }

    private NonNullList<ItemStack> inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
    private int lastPlacedIndex = 0;

    public PlacerTile() {
        super(Holder.placerType());
    }

    @Override
    public void tick() {
        if (world != null && !world.isRemote) {
            if (world.isAirBlock(getPos().offset(getBlockState().get(FACING)))) {
                placeBlock();
            } else {
                breakBlock();
            }
        }
    }

    public void breakBlock() {

    }

    public void placeBlock() {
        if (isEmpty()) return;
        Direction facing = getBlockState().get(FACING);
        BlockPos pos = getPos().offset(facing);
        Vec3d hitPos = DIRECTION_VEC3D_MAP.get(facing.getOpposite()).add(pos.getX(), pos.getY(), pos.getZ());
        BlockRayTraceResult rayTrace = new BlockRayTraceResult(hitPos, facing.getOpposite(), pos, false);
        PlayerEntity fake = QuarryFakePlayer.get(((ServerWorld) world), getPos());

        findEntry(inventory,
            i -> isItemPlaceable(i, fake, rayTrace),
            lastPlacedIndex).ifPresent(i -> {
            this.lastPlacedIndex = i;
            markDirty();
        });
    }

    public static <T> OptionalInt findEntry(List<T> check, Predicate<T> filter, int startIndex) {
        int listSize = check.size();
        if (startIndex >= listSize)
            return OptionalInt.empty();
        return findEntryInternal(check, filter, startIndex, startIndex, listSize);
    }

    private static <T> OptionalInt findEntryInternal(List<T> check, Predicate<T> filter, int startIndex, int index, int listSize) {
        T value = check.get(index);
        if (filter.test(value))
            return OptionalInt.of(index);
        if (index == startIndex - 1 || (startIndex == 0 && index == listSize - 1)) {
            // last search
            return OptionalInt.empty();
        } else {
            int next = index + 1 == listSize ? 0 : index + 1;
            return findEntryInternal(check, filter, startIndex, next, listSize);
        }
    }

    public static boolean isItemPlaceable(ItemStack stack, PlayerEntity fake, BlockRayTraceResult rayTrace) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        if (!(item instanceof BlockItem)) {
            return false;
        } else {
            BlockItem blockItem = (BlockItem) item;
            fake.setHeldItem(Hand.MAIN_HAND, stack);
            BlockItemUseContext context = new BlockItemUseContext(new ItemUseContext(fake, Hand.MAIN_HAND, rayTrace));
            return blockItem.tryPlace(context).isSuccess();
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.put(KEY_ITEM, ItemStackHelper.saveAllItems(new CompoundNBT(), inventory));
        compound.putInt(KEY_LAST_PLACED, lastPlacedIndex);
        return super.write(compound);
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        ItemStackHelper.loadAllItems(compound.getCompound(KEY_ITEM), inventory);
        lastPlacedIndex = compound.getInt(KEY_LAST_PLACED);
    }

    @Override
    public ITextComponent getName() {
        return new TranslationTextComponent(getDebugName());
    }

    @Override
    public String getDebugName() {
        return TranslationKeys.placer;
    }

    @Override
    public List<? extends ITextComponent> getDebugMessages() {
        return Collections.emptyList();
    }

    @Override
    public int getSizeInventory() {
        return 9;
    }

    @Override
    public boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        if (index >= 0 && index < getSizeInventory())
            return inventory.get(index);
        else
            return ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return ItemStackHelper.getAndSplit(inventory, index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(inventory, index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index >= 0 && index < getSizeInventory())
            inventory.set(index, stack);
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return player.getDistanceSq(getPos().getX(), getPos().getY(), getPos().getZ()) <= 64;
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }
}
