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

import java.util.List;

import com.yogpc.qp.BlockData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class GuiSlotEnchList extends GuiSlot {
    private final GuiScreen parent;
    public int currentore = 0;
    protected List<BlockData> target;

    public GuiSlotEnchList(final Minecraft mc, final int w, final int h, final int t, final int b,
                           final GuiScreen par, final List<BlockData> ali) {
        super(mc, w, h, t, b, 18);
        this.parent = par;
        this.target = ali;
    }

    @Override
    protected int getSize() {
        return this.target.size();
    }

    @Override
    protected void elementClicked(final int var1, final boolean var2, final int var3, final int var4) {
        this.currentore = var1;
    }

    @Override
    protected int getContentHeight() {
        return this.getSize() * 18;
    }

    @Override
    protected boolean isSelected(final int var1) {
        return var1 == this.currentore;
    }

    @Override
    protected void drawBackground() {
    }

    @Override
    protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn, float p_192637_7_) {
        final String name = this.target.get(entryID).getLocalizedName();
        Minecraft.getMinecraft().fontRenderer
                .drawStringWithShadow(name,
                        (this.parent.width * 3 / 5 - Minecraft.getMinecraft().fontRenderer.getStringWidth(name)) / 2,
                        yPos + 2, 0xFFFFFF);
    }
}
