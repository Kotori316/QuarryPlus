package com.yogpc.qp.tile;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.yogpc.qp.modules.IModuleItem;
import jp.t2v.lab.syntax.MapStreamSyntax;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.INBTSerializable;

public class QuarryModuleInventory extends InventoryBasic implements INBTSerializable<NBTTagCompound> {
    private final TileEntity tile;
    private final Consumer<QuarryModuleInventory> onUpdate;
    private final Predicate<IModuleItem> moduleFilter;

    public QuarryModuleInventory(ITextComponent title, int slotCount, TileEntity entity, Consumer<QuarryModuleInventory> onUpdate, Predicate<IModuleItem> moduleFilter) {
        super(title.getUnformattedText(), true, slotCount);
        this.tile = Objects.requireNonNull(entity);
        this.onUpdate = Objects.requireNonNull(onUpdate);
        this.moduleFilter = moduleFilter;
    }

    public QuarryModuleInventory(ITextComponent title, int slotCount, TileEntity entity, scala.Function1<QuarryModuleInventory, scala.Unit> onUpdate, Predicate<IModuleItem> moduleFilter) {
        super(title.getUnformattedText(), true, slotCount);
        this.tile = Objects.requireNonNull(entity);
        this.onUpdate = onUpdate::apply;
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
            Predicate<Item> equal = Predicate.isEqual(item);
            Predicate<Item> id = item1 -> item1 instanceof IModuleItem && ((IModuleItem) item1).getSymbol().equals(item.getSymbol());
            return IntStream.range(0, getSizeInventory())
                .mapToObj(this::getStackInSlot)
                .map(ItemStack::getItem)
                .noneMatch(equal.or(id));
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
        super.markDirty();
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
        NonNullList<ItemStack> list = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(nbt, list);
        for (int i = 0; i < list.size(); i++) {
            setInventorySlotContents(i, list.get(i));
        }
    }
}
