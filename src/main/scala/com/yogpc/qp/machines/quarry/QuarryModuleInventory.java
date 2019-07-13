package com.yogpc.qp.machines.quarry;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.yogpc.qp.machines.modules.IModuleItem;
import jp.t2v.lab.syntax.MapStreamSyntax;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.INBTSerializable;

public class QuarryModuleInventory extends InventoryBasic implements INBTSerializable<NBTTagCompound> {
    private final TileEntity tile;
    private final Consumer<QuarryModuleInventory> onUpdate;

    public QuarryModuleInventory(ITextComponent title, int slotCount, TileEntity entity, Consumer<QuarryModuleInventory> onUpdate) {
        super(title, slotCount);
        this.tile = Objects.requireNonNull(entity);
        this.onUpdate = Objects.requireNonNull(onUpdate);
    }

    public List<Map.Entry<IModuleItem, ItemStack>> moduleItems() {
        return IntStream.range(0, getSizeInventory())
            .mapToObj(this::getStackInSlot)
            .filter(s -> !s.isEmpty())
            .map(MapStreamSyntax.toEntry(ItemStack::getItem, Function.identity()))
            .filter(MapStreamSyntax.byKey(IModuleItem.class::isInstance))
            .map(MapStreamSyntax.keys(IModuleItem.class::cast))
            .collect(Collectors.toList());
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (stack.getItem() instanceof IModuleItem) {
            IModuleItem item = (IModuleItem) stack.getItem();
            return IntStream.range(0, getSizeInventory())
                .mapToObj(this::getStackInSlot)
                .map(ItemStack::getItem)
                .noneMatch(Predicate.isEqual(item));
        }
        return false;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return player.getDistanceSq(tile.getPos()) <= 64;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public void markDirty() {
        onUpdate.accept(this);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        NonNullList<ItemStack> list = IntStream.range(0, getSizeInventory())
            .mapToObj(this::getStackInSlot).collect(Collectors.toCollection(NonNullList::create));
        ItemStackHelper.saveAllItems(nbtTagCompound, list);
        return nbtTagCompound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        NonNullList<ItemStack> list = NonNullList.withSize(5, ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(nbt, list);
        for (int i = 0; i < list.size(); i++) {
            setInventorySlotContents(i, list.get(i));
        }
    }
}
