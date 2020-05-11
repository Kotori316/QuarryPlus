package com.yogpc.qp.machines.quarry;

import java.util.Objects;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.base.IModule;
import com.yogpc.qp.machines.base.QuarryModuleInventory;
import com.yogpc.qp.utils.Holder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerQuarryModule extends Container {
    public static final String GUI_ID = QuarryPlus.modID + ":gui_quarry_module";
    private final QuarryModuleInventory moduleInventory;
    private final int allSlots;
    final HasModuleInventory inventory;

    public ContainerQuarryModule(int id, PlayerEntity player, BlockPos pos) {
        super(Holder.quarryModuleContainerType(), id);
        inventory = (HasModuleInventory) player.getEntityWorld().getTileEntity(pos);
        Objects.requireNonNull(inventory);
        this.moduleInventory = inventory.moduleInv();
        this.allSlots = moduleInventory.getSizeInventory();
        int oneBox = 18;

        for (int i = 0; i < allSlots; i++) {
            int verticalFix = i < 5 ? i : i - 5;
            int horizontalFix = i / 5;
            addSlot(new SlotItemHandler(moduleInventory.itemHandler, i, 44 + verticalFix * oneBox, 27 + horizontalFix * oneBox));
        }

        for (int h = 0; h < 3; h++) {
            for (int v = 0; v < 9; v++) {
                this.addSlot(new Slot(player.inventory, v + h * 9 + 9, 8 + v * oneBox, 84 + h * oneBox));
            }
        }
        for (int vertical = 0; vertical < 9; vertical++) {
            this.addSlot(new Slot(player.inventory, vertical, 8 + vertical * oneBox, 142));
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return moduleInventory.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack src = ItemStack.EMPTY;
        final Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            final ItemStack remain = slot.getStack();
            src = remain.copy();
            if (index < allSlots) {
                if (!mergeItemStack(remain, allSlots, allSlots + 36, true))
                    return ItemStack.EMPTY;
            } else {
                if (!mergeItemStack(remain, 0, allSlots, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (remain.getCount() == 0)
                slot.putStack(ItemStack.EMPTY);
            else
                slot.onSlotChanged();
            if (remain.getCount() == src.getCount())
                return ItemStack.EMPTY;
            slot.onTake(playerIn, remain);
        }
        return src;
    }

    public interface HasModuleInventory {
        QuarryModuleInventory moduleInv();

        scala.collection.immutable.List<IModule> getModules();
    }

    public static class InteractionObject implements INamedContainerProvider {
        private final BlockPos pos;
        private final String name;

        public InteractionObject(BlockPos pos, String name) {
            this.pos = pos;
            this.name = name;
        }

        @Override
        public ITextComponent getDisplayName() {
            return new TranslationTextComponent(name);
        }

        @Override
        public Container createMenu(int id, PlayerInventory i, PlayerEntity p) {
            return new ContainerQuarryModule(id, p, pos);
        }

        public static <T extends TileEntity & HasModuleInventory> void openGUI(T tile, ServerPlayerEntity player, String name) {
            if (tile.hasWorld() && !Objects.requireNonNull(tile.getWorld()).isRemote) {
                NetworkHooks.openGui(player, new ContainerQuarryModule.InteractionObject(tile.getPos(), name), tile.getPos());
            }
        }
    }
}
