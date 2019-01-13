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

import com.yogpc.qp.tile.TilePump;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiP_SlotList extends GuiSlot {
    private final GuiScreen parent;
    public int currentOre = 0;
    private final TilePump pump;
    private final EnumFacing facing;

    public GuiP_SlotList(Minecraft mcIn, int width, int height, int topIn, int bottomIn, final GuiScreen parents, TilePump pump, EnumFacing facing) {
        super(mcIn, width, height, topIn, bottomIn, 18);
        this.parent = parents;
        this.pump = pump;
        this.facing = facing;
    }

    @Override
    protected int getSize() {
        return this.pump.mapping.get(facing).size();
    }

    @Override
    protected void elementClicked(final int slotIndex, final boolean isDoubleClick, final int mouseX, final int mouseY) {
        this.currentOre = slotIndex;
    }

    @Override
    protected int getContentHeight() {
        return this.getSize() * 18;
    }

    @Override
    protected boolean isSelected(final int slotIndex) {
        return slotIndex == this.currentOre;
    }

    @Override
    protected void drawBackground() {
        this.parent.drawDefaultBackground();
    }

    @Override
    protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn, float a) {
        String name = this.pump.mapping.get(facing).get(entryID);
        if (FluidRegistry.isFluidRegistered(name))
            name = FluidRegistry.getFluid(name).getLocalizedName(FluidRegistry.getFluidStack(name, 0));
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(name,
            (this.parent.width * 3 / 5 - Minecraft.getMinecraft().fontRenderer.getStringWidth(name)) / 2,
            yPos + 2, 0xFFFFFF);
    }

}
