package com.yogpc.qp.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSlotEntityList extends GuiSlot {
    private final Minecraft mc;
    private final GuiController gc;
    int selected;

    public GuiSlotEntityList(final Minecraft minecraft, int width, int height, int topIn, int bottomIn, final GuiController g) {
        super(minecraft, width, height, topIn, bottomIn, 18);
        this.mc = minecraft;
        this.gc = g;
    }

    @Override
    protected int getSize() {
        return this.gc.list.size();
    }

    @Override
    protected void elementClicked(final int slotIndex, final boolean isDoubleClick, final int mouseX, final int mouseY) {
        this.selected = slotIndex;
    }

    @Override
    protected boolean isSelected(final int slotIndex) {
        return this.selected == slotIndex;
    }

    @Override
    protected void drawBackground() {
    }

    @Override
    protected int getContentHeight() {
        return this.getSize() * 18;
    }

    @Override
    protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn, float a) {
        final String name = this.gc.names.get(entryID);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(name,
                (this.mc.currentScreen.width - Minecraft.getMinecraft().fontRenderer.getStringWidth(name)) / 2,
                yPos + 2, 0xFFFFFF);
    }

}
