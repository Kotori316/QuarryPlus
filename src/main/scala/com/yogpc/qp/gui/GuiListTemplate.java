package com.yogpc.qp.gui;

import java.io.IOException;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.container.ContainerListTemplate;
import com.yogpc.qp.item.ItemTemplate;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.listtemplate.TemplateMessage;
import com.yogpc.qp.utils.BlockData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GuiListTemplate extends GuiContainer {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/template.png");
    private final EntityPlayer player;
    private ItemList itemList;
    private ItemTemplate.Template template = ItemTemplate.EmPlate();

    public GuiListTemplate(EntityPlayer player) {
        super(new ContainerListTemplate(player));
        this.player = player;
        this.xSize = 176;
        this.ySize = 217;
        ItemStack stack = player.inventory.getCurrentItem();
        if (stack.getItem() == QuarryPlusI.itemTemplate()) {
            template = ItemTemplate.getTemplate(stack);
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        itemList = new ItemList(this.mc, guiLeft + 8, guiLeft + 133, 0, guiTop + 8, guiTop + 114, 18);

        int id = 0;
        int buttonHeight = 16;
        addButton(new GuiButton(id++, guiLeft + 135, guiTop + 117, 32, buttonHeight, I18n.format(TranslationKeys.ADD)));
        addButton(new GuiButton(id++, guiLeft + 135, guiTop + 62, 32, buttonHeight, I18n.format(TranslationKeys.DELETE)));
        addButton(new GuiButton(id++, guiLeft + 135, guiTop + 62 + buttonHeight, 32, buttonHeight, ""));
        assert id > 0;
        buttonText();
    }

    protected void buttonText() {
        if (template.include()) {
            buttonList.get(2).displayString = I18n.format(TranslationKeys.TOF_INCLUDE);
        } else {
            buttonList.get(2).displayString = I18n.format(TranslationKeys.TOF_EXCLUDE);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(LOCATION);
        drawTexturedModalRect(this.width - this.xSize >> 1, this.height - this.ySize >> 1, 0, 0, this.xSize, this.ySize);
        itemList.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(I18n.format(TranslationKeys.CONTAINER_INVENTORY), 8, this.ySize - 96 + 2, 0x404040);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        itemList.handleMouseInput();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        PacketHandler.sendToServer(TemplateMessage.create(player.inventory.currentItem, template));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0: //ADD
                ItemStack stack = this.inventorySlots.getInventory().get(0);
                if (!stack.isEmpty() && stack.getItem() instanceof ItemBlock) {
                    BlockData data = BlockData.apply(stack.getItem().getRegistryName(), stack.getItemDamage());
                    if (!template.items().contains(data))
                        template = template.add(data);
                }
                break;
            case 1: //DELETE
                int cursor = itemList.cursor;
                if (0 <= cursor && cursor < itemList.getSize()) {
                    BlockData data = template.items().apply(cursor);
                    template = template.remove(data);
                }
                break;
            case 2: // TOGGLE INCLUDE
                template = template.toggle();
                buttonText();
                break;
        }
    }

    private class ItemList extends GuiListExtended {
        int cursor = 0;

        public ItemList(Minecraft mcIn, int left, int right, int height, int topIn, int bottomIn, int slotHeightIn) {
            super(mcIn, -left + right, height, topIn, bottomIn, slotHeightIn);
            assert right > left;
            this.left = left;
            this.right = right;
        }

        @Override
        protected int getSize() {
            return template.items().size();
        }

        @Override
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int x, int y) {
            cursor = slotIndex;
        }

        @Override
        public int getListWidth() {
            return this.right - this.left;
        }

        @Override
        protected boolean isSelected(int slotIndex) {
            return cursor == slotIndex;
        }

        @Override
        public IGuiListEntry getListEntry(int index) {
            BlockData data = template.items().apply(index);
            return new Entry(data);
        }

        @Override
        protected void drawSelectionBox(int insideLeft, int insideTop, int mouseXIn, int mouseYIn, float partialTicks) {
            int i = this.getSize();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();

            for (int j = 0; j < i; ++j) {
                int yPos = insideTop + j * this.slotHeight + this.headerPadding;
                int l = this.slotHeight - 4;

                if (yPos >= ItemList.this.top && yPos + l <= ItemList.this.bottom) {
                    if (this.showSelectionBox && this.isSelected(j)) {
                        int i1 = this.left + (this.width / 2 - this.getListWidth() / 2);
                        int j1 = this.left + this.width / 2 + this.getListWidth() / 2;
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        GlStateManager.disableTexture2D();
                        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                        bufferbuilder.pos((double) i1, (double) (yPos + l + 2), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                        bufferbuilder.pos((double) j1, (double) (yPos + l + 2), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                        bufferbuilder.pos((double) j1, (double) (yPos - 2), 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                        bufferbuilder.pos((double) i1, (double) (yPos - 2), 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                        bufferbuilder.pos((double) (i1 + 1), (double) (yPos + l + 1), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                        bufferbuilder.pos((double) (j1 - 1), (double) (yPos + l + 1), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                        bufferbuilder.pos((double) (j1 - 1), (double) (yPos - 1), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                        bufferbuilder.pos((double) (i1 + 1), (double) (yPos - 1), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                        tessellator.draw();
                        GlStateManager.enableTexture2D();
                    }

                    this.drawSlot(j, insideLeft, yPos, l, mouseXIn, mouseYIn, partialTicks);
                }

            }
        }

        @Override
        protected int getScrollBarX() {
            return this.right - 6;
        }

        private class Entry implements IGuiListEntry {
            private final BlockData data;

            public Entry(BlockData data) {
                this.data = data;
            }

            @Override
            public void updatePosition(int slotIndex, int x, int y, float partialTicks) {
            }

            @Override
            public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
                if (x >= ItemList.this.top && y + slotHeight <= ItemList.this.bottom)
                    drawCenteredString(ItemList.this.mc.fontRenderer, data.getLocalizedName(),
                        ItemList.this.left + ItemList.this.width / 2,
                        y + 2, 0xFFFFFF);
            }

            @Override
            public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
                return false;
            }

            @Override
            public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
            }
        }

        @Override
        public void handleMouseInput() {
            this.width = right + left;
            super.handleMouseInput();
            this.width = getListWidth();
        }

        @Override
        protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {
        }

    }
}
