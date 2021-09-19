package com.yogpc.qp.machines.bookmover;

import java.util.List;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.CheckerLog;
import com.yogpc.qp.machines.EnchantableItem;
import com.yogpc.qp.machines.PowerTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

public class BookMoverEntity extends PowerTile implements Container, MenuProvider, CheckerLog {
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);

    public BookMoverEntity(BlockPos pos, BlockState state) {
        super(Holder.BOOK_MOVER_TYPE, pos, state);
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        ContainerHelper.saveAllItems(nbt, inventory);
        return super.save(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        ContainerHelper.loadAllItems(nbt, inventory);
    }

    @Override
    public int getContainerSize() {
        return 3;
    }

    @Override
    public boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int index) {
        return inventory.get(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return ContainerHelper.removeItem(inventory, index, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(inventory, index);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        inventory.set(index, stack);
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null &&
            player.distanceToSqr(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ()) <= 64;
    }

    @Override
    public void clearContent() {
        inventory.clear();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return switch (index) {
            case 0 -> stack.getItem() instanceof EnchantableItem;
            case 1 -> stack.getItem() == Items.ENCHANTED_BOOK;
            default -> false;
        };
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public List<? extends Component> getDebugLogs() {
        return null;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new BookMoverMenu(id, player, getBlockPos());
    }

    boolean isWorking() {
        return false;
    }
}
