/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiP_SlotBlockList extends GuiSlot {
    private static final List<String> blockList_s = new ArrayList<>();
    private final List<String> blockList = new ArrayList<>(blockList_s);
    private final GuiScreen parent;
    public String current;

    static {
        blockList_s.addAll(FluidRegistry.getRegisteredFluids().keySet());
    }

    public GuiP_SlotBlockList(final Minecraft minecraft, final int width, final int height,
                              final int topIn, final int bottomIn, final GuiScreen parents, final List<String> list) {
        super(minecraft, width, height, topIn, bottomIn, 18);
        for (int i = 0; i < this.blockList.size(); i++) {
            for (String aList : list) {
                if (aList.equals(this.blockList.get(i))) {
                    this.blockList.remove(i);
                    i--;
                    if (i < 0)
                        break;
                }
            }
        }
        this.parent = parents;
    }

    @Override
    protected int getSize() {
        return this.blockList.size();
    }

    @Override
    protected void elementClicked(final int slotIndex, final boolean isDoubleClick, final int mouseX, final int mouseY) {
        this.current = this.blockList.get(slotIndex);
    }

    @Override
    protected int getContentHeight() {
        return this.getSize() * 18;
    }

    @Override
    protected boolean isSelected(final int slotIndex) {
        return this.blockList.get(slotIndex).equals(this.current);
    }

    @Override
    protected void drawBackground() {
        this.parent.drawDefaultBackground();
    }

    @Override
    protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn, float a) {
        final String name = FluidRegistry.getFluid(this.blockList.get(entryID)).getLocalizedName(
            FluidRegistry.getFluidStack(this.blockList.get(entryID), 0));
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(name,
            (this.parent.width - Minecraft.getMinecraft().fontRenderer.getStringWidth(name)) / 2.0f,
            yPos + 2, 0xFFFFFF);
    }

}
