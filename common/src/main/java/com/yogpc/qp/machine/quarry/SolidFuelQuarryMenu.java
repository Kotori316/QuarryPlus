package com.yogpc.qp.machine.quarry;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.PowerEntity;
import com.yogpc.qp.machine.misc.SlotContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SolidFuelQuarryMenu extends AbstractContainerMenu {
    public static final String NAME = "gui_" + SolidFuelQuarryBlock.NAME;
    public static final String GUI_ID = QuarryPlus.modID + ":" + NAME;
    final DataSlot fuelCountData = this.addDataSlot(DataSlot.standalone());
    final DataSlot energyData = this.addDataSlot(DataSlot.standalone());
    @NotNull
    final SolidFuelQuarryEntity quarry;
    private final int allSlots;

    public SolidFuelQuarryMenu(int id, Inventory inventory, BlockPos pos) {
        super(PlatformAccess.getAccess().registerObjects().solidFuelQuarryContainer().get(), id);
        quarry = Objects.requireNonNull((SolidFuelQuarryEntity) inventory.player.level().getBlockEntity(pos),
            "Tile at %s in %s is null".formatted(pos, inventory.player.level().dimension()));
        allSlots = quarry.fuelContainer.getContainerSize();
        addSlot(new SlotContainer(quarry.fuelContainer, 0, 44, 27));

        final int oneBox = 18;
        for (int h = 0; h < 3; h++) {
            for (int v = 0; v < 9; v++) {
                this.addSlot(new Slot(inventory, v + h * 9 + 9, 8 + v * oneBox, 84 + h * oneBox));
            }
        }
        for (int vertical = 0; vertical < 9; vertical++) {
            this.addSlot(new Slot(inventory, vertical, 8 + vertical * oneBox, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return quarry.enabled && quarry.getBlockPos().closerToCenterThan(player.position(), 8);
    }

    @Override
    public void broadcastChanges() {
        fuelCountData.set(Math.min(quarry.getFuelCount(), Short.MAX_VALUE));
        energyData.set((int) Math.min(quarry.getEnergy() / PowerEntity.ONE_FE, Short.MAX_VALUE));
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.getSlot(index);
        if (slot.hasItem()) {
            ItemStack remain = slot.getItem();
            ItemStack slotContent = remain.copy();
            if (index < allSlots) {
                if (!this.moveItemStackTo(remain, allSlots, 36 + allSlots, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(remain, 0, allSlots, false)) {
                return ItemStack.EMPTY;
            }

            if (remain.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (remain.getCount() == slotContent.getCount()) {
                // Nothing is moved
                return ItemStack.EMPTY;
            }
            slot.onTake(player, remain);
            return slotContent;
        } else {
            return ItemStack.EMPTY;
        }
    }
}
