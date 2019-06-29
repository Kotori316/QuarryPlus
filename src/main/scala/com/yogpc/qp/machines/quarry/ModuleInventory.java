package com.yogpc.qp.machines.quarry;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.yogpc.qp.machines.modules.IModuleItem;
import jp.t2v.lab.syntax.MapStreamSyntax;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;

public class ModuleInventory extends InventoryBasic {
    private final TileEntity tile;

    public ModuleInventory(ITextComponent title, int slotCount, TileEntity entity) {
        super(title, slotCount);
        tile = entity;
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
}
