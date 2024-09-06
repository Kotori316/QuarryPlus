package com.yogpc.qp.machine.storage;

import com.yogpc.qp.machine.MachineStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

final class ItemCountList extends ObjectSelectionList<ItemCountList.ItemCountRow> {

    public ItemCountList(Minecraft minecraft, int width, int height, int y, MachineStorage storage) {
        super(minecraft, width, height, y, 22);
        setRenderHeader(false, 0);

        for (MachineStorage.ItemKeyCount itemKeyCount : storage.itemKeyCounts()) {
            addEntry(new ItemCountRow(itemKeyCount));
        }
    }

    @Override
    protected void renderListBackground(GuiGraphics guiGraphics) {
    }

    @Override
    protected boolean isSelectedItem(int index) {
        return false;
    }

    class ItemCountRow extends ObjectSelectionList.Entry<ItemCountRow> {
        final MachineStorage.ItemKeyCount item;

        ItemCountRow(MachineStorage.ItemKeyCount item) {
            this.item = item;
        }

        @Override
        public Component getNarration() {
            var stack = item.key().toStack(Math.clamp(item.count(), 0, Integer.MAX_VALUE));
            return stack.getHoverName();
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            var stack = item.key().toStack(Math.clamp(item.count(), 0, Integer.MAX_VALUE));
            guiGraphics.renderFakeItem(stack, left + 8, top);
            guiGraphics.drawString(minecraft.font, String.valueOf(item.count()), left + 8 + 16, top + 6, 0x404040, false);
        }
    }
}
