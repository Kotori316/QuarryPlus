package com.yogpc.qp.machine.storage;

import com.yogpc.qp.FluidStackLike;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.MachineStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.stream.Stream;

final class ItemCountList extends ObjectSelectionList<ItemCountList.ItemCountRow> {
    final DebugStorageEntity storageEntity;

    public ItemCountList(Minecraft minecraft, int width, int height, int y, DebugStorageEntity storageEntity) {
        super(minecraft, width, height, y, 22);
        this.storageEntity = storageEntity;
        //setRenderHeader(false, 0);

        refreshEntries();
    }

    void refreshEntries() {
        replaceEntries(
            Stream.concat(
                storageEntity.storage.itemKeyCounts().stream().map(ItemCountRow::new),
                storageEntity.storage.fluidKeyCounts().stream().map(ItemCountRow::new)
            ).toList());
    }

    @Override
    protected void renderListBackground(GuiGraphics guiGraphics) {
    }

    @Override
    protected boolean entriesCanBeSelected() {
        return false;
    }

    @Override
    public int getRowWidth() {
        return getWidth() - 10;
    }

    @Override
    protected int scrollBarX() {
        return getRight() - 6;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        if (isMouseOver(mouseX, mouseY)) {
            var hovered = getHovered();
            if (hovered != null) {
                var stack = hovered.stack;
                var component = stack.getStyledHoverName();
                guiGraphics.renderTooltip(minecraft.font, List.of(ClientTooltipComponent.create(component.getVisualOrderText())), mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, stack.get(DataComponents.TOOLTIP_STYLE));
            }
        }
    }

    class ItemCountRow extends ObjectSelectionList.Entry<ItemCountRow> {
        final ItemStack stack;
        final Component name;
        final long count;
        final String unit;

        ItemCountRow(MachineStorage.ItemKeyCount item) {
            stack = item.key().toStack(1);
            count = item.count();
            name = stack.getHoverName();
            unit = "";
        }

        ItemCountRow(MachineStorage.FluidKeyCount fluid) {
            stack = fluid.key().fluid().getBucket().getDefaultInstance();
            count = fluid.count() * 1000 / MachineStorage.ONE_BUCKET;
            name = PlatformAccess.getAccess().getFluidName(new FluidStackLike(fluid.key().fluid(), fluid.count(), fluid.key().patch()));
            unit = " mB";
        }

        @Override
        public Component getNarration() {
            return name;
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
            guiGraphics.renderFakeItem(stack, getRowLeft(), getY());
            var text = getNarration();
            final int textWidth = minecraft.font.width(text);
            final int textX = getRowLeft() + 20;
            final int textY = getY() + 4;
            guiGraphics.drawString(minecraft.font, text, textX, textY, ARGB.opaque(0xFFFFFF));
            guiGraphics.drawString(minecraft.font, count + unit, textX + textWidth + 8, textY, ARGB.opaque(0xFFFFFF), true);
        }
    }
}
