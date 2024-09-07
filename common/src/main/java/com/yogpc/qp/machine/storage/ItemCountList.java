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

    @Override
    public int getRowWidth() {
        return getWidth() - 10;
    }

    @Override
    protected int getScrollbarPosition() {
        return getRight() - 6;
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
            guiGraphics.renderFakeItem(stack, left, top);
            renderScrollingString(guiGraphics, minecraft.font, getNarration(), left + 8, top + 6, left + 8 + 40, top + 6, 0xFFFFFF);
            guiGraphics.drawString(minecraft.font, String.valueOf(item.count()), left + 8 + 40, top + 6, 0xFFFFFF, false);
        }
    }
}
