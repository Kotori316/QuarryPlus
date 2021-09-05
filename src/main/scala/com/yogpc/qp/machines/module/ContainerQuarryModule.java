package com.yogpc.qp.machines.module;

import java.util.Objects;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.misc.SlotContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public class ContainerQuarryModule extends AbstractContainerMenu {
    public static final String GUI_ID = QuarryPlus.modID + ":gui_quarry_module";
    private final ModuleInventory moduleInventory;
    private final int allSlots;
    final BlockEntity blockEntity;

    public ContainerQuarryModule(int id, Player player, BlockPos pos) {
        super(Holder.MODULE_MENU_TYPE, id);
        blockEntity = player.level.getBlockEntity(pos);
        Objects.requireNonNull(blockEntity);
        this.moduleInventory = ((ModuleInventory.HasModuleInventory) blockEntity).getModuleInventory();
        this.allSlots = moduleInventory.getContainerSize();
        int oneBox = 18;

        for (int i = 0; i < allSlots; i++) {
            int verticalFix = i < 5 ? i : i - 5;
            int horizontalFix = i / 5;
            addSlot(new SlotContainer(moduleInventory, i, 44 + verticalFix * oneBox, 27 + horizontalFix * oneBox));
        }

        for (int h = 0; h < 3; h++) {
            for (int v = 0; v < 9; v++) {
                this.addSlot(new Slot(player.getInventory(), v + h * 9 + 9, 8 + v * oneBox, 84 + h * oneBox));
            }
        }
        for (int vertical = 0; vertical < 9; vertical++) {
            this.addSlot(new Slot(player.getInventory(), vertical, 8 + vertical * oneBox, 142));
        }
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return moduleInventory.stillValid(playerIn);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack src = ItemStack.EMPTY;
        final Slot slot = this.getSlot(index);
        if (slot.hasItem()) {
            final ItemStack remain = slot.getItem();
            src = remain.copy();
            if (index < allSlots) {
                if (!moveItemStackTo(remain, allSlots, allSlots + 36, true))
                    return ItemStack.EMPTY;
            } else {
                if (!moveItemStackTo(remain, 0, allSlots, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (remain.getCount() == 0)
                slot.set(ItemStack.EMPTY);
            else
                slot.setChanged();
            if (remain.getCount() == src.getCount())
                return ItemStack.EMPTY;
            slot.onTake(playerIn, remain);
        }
        return src;
    }

    public record InteractionObject(BlockPos pos, Component name) implements MenuProvider {

        @Override
        public Component getDisplayName() {
            return name;
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory i, Player p) {
            return new ContainerQuarryModule(id, p, pos);
        }

        public static <T extends BlockEntity & ModuleInventory.HasModuleInventory> void openGUI(T tile, ServerPlayer player, Component name) {
            if (tile.getLevel() != null && !tile.getLevel().isClientSide) {
                NetworkHooks.openGui(player, new InteractionObject(tile.getBlockPos(), name), tile.getBlockPos());
            }
        }
    }
}
