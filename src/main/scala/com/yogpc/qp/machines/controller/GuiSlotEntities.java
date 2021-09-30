package com.yogpc.qp.machines.controller;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
final class GuiSlotEntities extends ObjectSelectionList<GuiSlotEntities.Entry> {

    private final GuiController parent;

    public GuiSlotEntities(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn, GuiController parent) {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.parent = parent;
        this.refreshList();
    }

    public void refreshList() {
        this.clearEntries(); // clear
        parent.buildModList(this::addEntry, Entry::new); // add
    }

    final class Entry extends ObjectSelectionList.Entry<Entry> {
        public final ResourceLocation location;

        public Entry(ResourceLocation location) {
            this.location = location;
        }

        @Override
        public void render(PoseStack matrix, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float partialTicks) {
            String name = location.toString();
            Minecraft minecraft = Minecraft.getInstance();

            assert minecraft.screen != null;
            minecraft.font.draw(matrix, name,
                (minecraft.screen.width - minecraft.font.width(name)) >> 1,
                top + 2, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            GuiSlotEntities.this.setSelected(this); // setSelected
            return false;
        }

        @Override
        public Component getNarration() {
            return new TranslatableComponent("narrator.select", location);
        }
    }
}
