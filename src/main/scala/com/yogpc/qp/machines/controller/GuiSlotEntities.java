package com.yogpc.qp.machines.controller;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiSlotEntities extends ExtendedList<GuiSlotEntities.Entry> {

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

    public class Entry extends ExtendedList.AbstractListEntry<Entry> {
        public final ResourceLocation location;

        public Entry(ResourceLocation location) {
            this.location = location;
        }

        @Override
        public void render(MatrixStack matrix, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_render_8_, float partialTicks) {
            String name = location.toString();
            Minecraft minecraft = Minecraft.getInstance();

            assert minecraft.currentScreen != null;
            minecraft.fontRenderer.drawString(matrix, name,
                (minecraft.currentScreen.width - minecraft.fontRenderer.getStringWidth(name)) >> 1,
                top + 2, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            GuiSlotEntities.this.setSelected(this); // setSelected
            return false;
        }
    }
}
