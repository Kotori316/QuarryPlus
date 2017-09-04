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

import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.pump.Mappings;
import com.yogpc.qp.tile.TilePump;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiP_SelectBlock extends GuiScreenA {
    private GuiP_SlotBlockList blocks;
    private final TilePump tile;
    private final EnumFacing targetid;

    public GuiP_SelectBlock(final GuiScreen pscr, final TilePump tb, EnumFacing id) {
        super(pscr);
        this.tile = tb;
        this.targetid = id;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.blocks = new GuiP_SlotBlockList(this.mc, this.width, this.height, 24, this.height - 32, this,
                this.tile.mapping.get(targetid));
        this.buttonList.add(new GuiButton(-1, this.width / 2 - 150, this.height - 26, 140, 20,
                I18n.format("gui.done")));
        this.buttonList.add(new GuiButton(-2, this.width / 2 + 10, this.height - 26, 140, 20,
                I18n.format("gui.cancel")));
    }

    @Override
    public void actionPerformed(final GuiButton par1) {
        switch (par1.id) {
            case -1:
                PacketHandler.sendToServer(Mappings.Update.create(tile, targetid, Mappings.Type.Add, blocks.current));
                break;
            case -2:
                showParent();
                break;
        }
    }

    @Override
    public void drawScreen(final int i, final int j, final float k) {
        drawDefaultBackground();
        this.blocks.drawScreen(i, j, k);
        final String title = I18n.format("tof.selectfluid");
        this.fontRendererObj.drawStringWithShadow(title,
                (this.width - this.fontRendererObj.getStringWidth(title)) / 2, 8, 0xFFFFFF);
        super.drawScreen(i, j, k);
    }
}
