package com.yogpc.qp.machines.item;

import java.util.function.Consumer;
import java.util.function.Function;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.IHandleButton;
import com.yogpc.qp.machines.workbench.BlockData;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.listtemplate.TemplateMessage;
import com.yogpc.qp.utils.Holder;
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
import scala.collection.JavaConverters;

public class GuiListTemplate extends GuiContainer implements IHandleButton {
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
        if (stack.getItem() == Holder.itemTemplate()) {
            template = ItemTemplate.getTemplate(stack);
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        itemList = new ItemList(this.mc, guiLeft + 8, guiLeft + 133, 0, guiTop + 8, guiTop + 114, 18);
        this.children.add(itemList);
        this.focusOn(itemList);

        int id = 0;
        int buttonHeight = 20;
        int buttonWidth = 40;
        addButton(new IHandleButton.Button(id++, guiLeft + 132, guiTop + 110, buttonWidth, buttonHeight, I18n.format(TranslationKeys.ADD), this));
        addButton(new IHandleButton.Button(id++, guiLeft + 132, guiTop + 42, buttonWidth, buttonHeight, I18n.format(TranslationKeys.DELETE), this));
        addButton(new IHandleButton.Button(id++, guiLeft + 132, guiTop + 42 + buttonHeight, buttonWidth, buttonHeight, "", this));
        assert id > 0;
        buttonText();
    }

    protected void buttonText() {
        if (template.include()) {
            buttons.get(2).displayString = I18n.format(TranslationKeys.TOF_INCLUDE);
        } else {
            buttons.get(2).displayString = I18n.format(TranslationKeys.TOF_EXCLUDE);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(LOCATION);
        drawTexturedModalRect(this.width - this.xSize >> 1, this.height - this.ySize >> 1, 0, 0, this.xSize, this.ySize);
        itemList.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(I18n.format(TranslationKeys.CONTAINER_INVENTORY), 8, this.ySize - 96 + 2, 0x404040);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        PacketHandler.sendToServer(TemplateMessage.create(player.inventory.currentItem, template));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0: //ADD
                ItemStack stack = this.inventorySlots.getInventory().get(0);
                if (!stack.isEmpty() && stack.getItem() instanceof ItemBlock) {
                    BlockData data = BlockData.apply(stack.getItem().getRegistryName());
                    if (!template.items().contains(data))
                        template = template.add(data);
                }
                break;
            case 1: //DELETE
                int cursor = itemList.cursor;
                if (0 <= cursor && cursor < itemList.getChildren().size()) {
                    BlockData data = template.items().apply(cursor);
                    template = template.remove(data);
                }
                break;
            case 2: // TOGGLE INCLUDE
                template = template.toggle();
                buttonText();
                break;
        }
        itemList.refresh();
    }

    private <T extends GuiListExtended.IGuiListEntry<T>> void refreshList(Consumer<T> modListViewConsumer, Function<BlockData, T> newEntry) {
        JavaConverters.asJavaCollection(template.items())
            .stream().map(newEntry).forEach(modListViewConsumer);
    }

    private class ItemList extends GuiListExtended<ItemList.Entry> {
        int cursor = 0;

        public ItemList(Minecraft mcIn, int left, int right, int height, int topIn, int bottomIn, int slotHeightIn) {
            super(mcIn, -left + right, height, topIn, bottomIn, slotHeightIn);
            assert right > left;
            this.left = left;
            this.right = right;
            refresh();
        }

        public void refresh() {
            this.clearEntries();
            GuiListTemplate.this.refreshList(this::addEntry, Entry::new);
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
        protected boolean mouseClicked(int index, int button, double mouseX, double mouseY) {
            cursor = index;
            return true;//super.mouseClicked(index, button, mouseX, mouseY);
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
                        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
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

        private class Entry extends GuiListExtended.IGuiListEntry<Entry> {
            private final BlockData data;

            public Entry(BlockData data) {
                this.data = data;
                this.list = ItemList.this;
            }

            @Override
            public void drawEntry(int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
                drawString(ItemList.this.mc.fontRenderer, data.getLocalizedName(),
                    getX(),
                    getY() + 3, 0xFFFFFF);
            }
        }

        @Override
        protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {
        }

    }
}
