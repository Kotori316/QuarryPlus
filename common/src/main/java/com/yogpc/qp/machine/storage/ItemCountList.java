package com.yogpc.qp.machine.storage;

import com.yogpc.qp.machine.MachineStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

final class ItemCountList extends ObjectSelectionList<ItemCountList.ItemCountRow> {
    final DebugStorageEntity storageEntity;

    public ItemCountList(Minecraft minecraft, int width, int height, int y, DebugStorageEntity storageEntity) {
        super(minecraft, width, height, y, 22);
        this.storageEntity = storageEntity;
        setRenderHeader(false, 0);

        for (MachineStorage.ItemKeyCount itemKeyCount : storageEntity.storage.itemKeyCounts()) {
            addEntry(new ItemCountRow(itemKeyCount));
        }
    }

    void refreshEntries() {
        replaceEntries(storageEntity.storage.itemKeyCounts().stream().map(ItemCountRow::new).toList());
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

    @Override
    protected void renderDecorations(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderDecorations(guiGraphics, mouseX, mouseY);
        if (isMouseOver(mouseX, mouseY)) {
            var hovered = getHovered();
            if (hovered != null) {
                var stack = hovered.getStack();
                guiGraphics.renderTooltip(minecraft.font, stack, mouseX, mouseY);
            }
        }
    }

    class ItemCountRow extends ObjectSelectionList.Entry<ItemCountRow> {
        final MachineStorage.ItemKeyCount item;

        ItemCountRow(MachineStorage.ItemKeyCount item) {
            this.item = item;
        }

        private @NotNull ItemStack getStack() {
            return item.key().toStack(Math.clamp(item.count(), 0, Integer.MAX_VALUE));
        }

        @Override
        public Component getNarration() {
            var stack = getStack();
            return stack.getHoverName();
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            var stack = getStack();
            guiGraphics.renderFakeItem(stack, left, top);
            var text = getNarration();
            final int textWidth = minecraft.font.width(text);
            final int textX = left + 20;
            final int textY = top + 4;
            guiGraphics.drawString(minecraft.font, text, textX, textY, 0xFFFFFF);
            guiGraphics.drawString(minecraft.font, String.valueOf(item.count()), textX + textWidth + 8, textY, 0xFFFFFF, true);
        }
    }
}
