package com.yogpc.qp.machines.base;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.yogpc.qp.machines.modules.IModuleItem;
import com.yogpc.qp.utils.Holder;
import jp.t2v.lab.syntax.MapStreamSyntax;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

public class QuarryModuleInventory extends Inventory implements INBTSerializable<CompoundNBT> {
    private final TileEntity tile;
    private final Consumer<QuarryModuleInventory> onUpdate;
    private final Predicate<IModuleItem> moduleFilter;
    public final IItemHandlerModifiable itemHandler = new ItemHandler();

    public QuarryModuleInventory(int slotCount, TileEntity entity, Consumer<QuarryModuleInventory> onUpdate, Predicate<IModuleItem> moduleFilter) {
        super(slotCount);
        this.tile = Objects.requireNonNull(entity);
        this.onUpdate = Objects.requireNonNull(onUpdate);
        this.moduleFilter = moduleFilter;
    }

    public List<Map.Entry<IModuleItem, ItemStack>> moduleItems() {
        return IntStream.range(0, getSizeInventory())
            .mapToObj(this::getStackInSlot)
            .filter(s -> !s.isEmpty())
            .map(MapStreamSyntax.toEntry(ItemStack::getItem, Function.identity()))
            .filter(MapStreamSyntax.byKey(IModuleItem.class::isInstance))
            .map(MapStreamSyntax.keys(IModuleItem.class::cast))
            .filter(MapStreamSyntax.byKey(this.moduleFilter))
            .collect(Collectors.toList());
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (stack.getItem() instanceof IModuleItem) {
            IModuleItem item = (IModuleItem) stack.getItem();
            if (!moduleFilter.test(item))
                return false;
            if (item.getSymbol().equals(Holder.itemFuelModuleNormal().getSymbol())) {
                return index == 4;
            }
            Predicate<Item> id = item1 -> item1 instanceof IModuleItem && !((IModuleItem) item1).isCompatibleWith(item);
            return IntStream.range(0, getSizeInventory())
                .mapToObj(this::getStackInSlot)
                .map(ItemStack::getItem)
                .noneMatch(id);
        }
        return false;
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        BlockPos pos = tile.getPos();
        return player.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) <= 64;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        onUpdate.accept(this);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbtTagCompound = new CompoundNBT();
        NonNullList<ItemStack> list = IntStream.range(0, getSizeInventory())
            .mapToObj(this::getStackInSlot).collect(Collectors.toCollection(NonNullList::create));
        ItemStackHelper.saveAllItems(nbtTagCompound, list);
        return nbtTagCompound;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        NonNullList<ItemStack> list = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(nbt, list);
        for (int i = 0; i < list.size(); i++) {
            setInventorySlotContents(i, list.get(i));
        }
    }

    private class ItemHandler extends InvWrapper {

        public ItemHandler() {
            super(QuarryModuleInventory.this);
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot == 4) return super.getSlotLimit(slot);
            else return 1;
        }
    }
}
