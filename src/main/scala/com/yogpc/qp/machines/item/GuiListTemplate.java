package com.yogpc.qp.machines.item;

/*
public class GuiListTemplate extends ContainerScreen<ContainerListTemplate> implements IHandleButton {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/template.png");
    private final PlayerEntity player;
    private ItemList itemList;
    private ItemTemplate.Template template = ItemTemplate.EmPlate();

    public GuiListTemplate(ContainerListTemplate c, PlayerInventory i, ITextComponent t) {
        super(c, i, t);
        this.player = i.player;
        this.xSize = 176;
        this.ySize = 217;
        ItemStack stack = player.inventory.getCurrentItem();
        if (stack.getItem() == Holder.itemTemplate()) {
            template = ItemTemplate.getTemplate(stack);
        }
    }

    @Override
    public void init() {
        super.init();
        itemList = new ItemList(this.getMinecraft(), guiLeft + 8, guiLeft + 133, 0, guiTop + 8, guiTop + 114, 18);
        this.children.add(itemList);
        this.setFocused(itemList);

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
            buttons.get(2).setMessage(I18n.format(TranslationKeys.TOF_INCLUDE));
        } else {
            buttons.get(2).setMessage(I18n.format(TranslationKeys.TOF_EXCLUDE));
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.getMinecraft().getTextureManager().bindTexture(LOCATION);
        blit(this.width - this.xSize >> 1, this.height - this.ySize >> 1, 0, 0, this.xSize, this.ySize);
        itemList.render(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.font.drawString(I18n.format(TranslationKeys.CONTAINER_INVENTORY), 8, this.ySize - 96 + 2, 0x404040);
    }

    @Override
    public void removed() {
        PacketHandler.sendToServer(TemplateMessage.create(player.inventory.currentItem, template));
        super.removed();
    }

    @Override
    public void onClose() {
        PacketHandler.sendToServer(TemplateMessage.create(player.inventory.currentItem, template));
        super.onClose();
    }

    @Override
    public void actionPerformed(IHandleButton.Button button) {
        switch (button.id) {
            case 0: //ADD
                ItemStack stack = this.getContainer().getInventory().get(0);
                if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                    QuarryBlackList.Entry data = new QuarryBlackList.Name(stack.getItem().getRegistryName());
                    if (!template.entries().contains(data))
                        template = template.add(data);
                }
                break;
            case 1: //DELETE
                Optional.ofNullable(itemList.getSelected())
                    .map(entry -> entry.data)
                    .ifPresent(data -> template = template.remove(data));
                break;
            case 2: // TOGGLE INCLUDE
                template = template.toggle();
                buttonText();
                break;
        }
        itemList.refresh();
    }

    private <T extends ExtendedList.AbstractListEntry<T>> void refreshList(Consumer<T> modListViewConsumer, Function<QuarryBlackList.Entry, T> newEntry) {
        CollectionConverters.asJava(template.entries()).stream().map(newEntry).forEach(modListViewConsumer);
    }

    private class ItemList extends ExtendedList<ItemList.Entry> {

        public ItemList(Minecraft mcIn, int left, int right, int height, int topIn, int bottomIn, int slotHeightIn) {
            super(mcIn, -left + right, height, topIn, bottomIn, slotHeightIn);
            assert right > left;
            this.x0 = left;
            this.x1 = right;
            refresh();
        }

        public void refresh() {
            this.clearEntries();
            GuiListTemplate.this.refreshList(this::addEntry, Entry::new);
        }

        @Override
        public int getRowWidth() {
            return this.x1 - this.x0;
        }

/*

        @Override
        protected void drawSelectionBox(int insideLeft, int insideTop, int mouseXIn, int mouseYIn, float partialTicks) {
            int i = this.getSize();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();

            for (int j = 0; j < i; ++j) {
                int yPos = insideTop + j * this.slotHeight + this.headerPadding;
                int l = this.slotHeight - 4;

                if (yPos >= ItemList.this.top && yPos + l <= ItemList.this.bottom) {
                    if (this.showSelectionBox && this.isSelectedItem(j)) {
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
*/
/*
        @Override
        protected int getScrollbarPosition() {
            return this.x1 - 6;
        }

        private class Entry extends ExtendedList.AbstractListEntry<Entry> {
            private final QuarryBlackList.Entry data;

            public Entry(QuarryBlackList.Entry data) {
                this.data = data;
            }

            @Override
            public void render(int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_render_8_, float partialTicks) {
                drawString(ItemList.this.minecraft.fontRenderer, data.toString(),
                    left, top + 3, 0xFFFFFF);
            }

            @Override
            public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
                ItemList.this.setSelected(this);
                return false;
            }
        }

        @Override
        protected void renderHoleBackground(int p_renderHoleBackground_1_, int p_renderHoleBackground_2_, int p_renderHoleBackground_3_, int p_renderHoleBackground_4_) {
        }
    }
}
*/