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

import java.util.LinkedList;

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
public class GuiP_SelectSide extends GuiScreenA {
    private final TilePump tile;
    private final boolean copy;
    private final EnumFacing to;

    public GuiP_SelectSide(final TilePump ptile, final GuiP_List pparent, final boolean pcopy) {
        this(pparent, ptile, pcopy, pparent.dir);
    }

    public GuiP_SelectSide(GuiScreen pparent, TilePump tile, boolean copy, EnumFacing to) {
        super(pparent);
        this.tile = tile;
        this.copy = copy;
        this.to = to;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.add(new GuiButton(EnumFacing.UP.ordinal(), this.width / 2 - 50,
            this.height / 2 - 60, 100, 20, I18n.format(TranslationKeys.UP)));
        this.buttonList.add(new GuiButton(EnumFacing.DOWN.ordinal(), this.width / 2 - 50,
            this.height / 2 + 40, 100, 20, I18n.format(TranslationKeys.DOWN)));
        this.buttonList.add(new GuiButton(EnumFacing.SOUTH.ordinal(), this.width / 2 - 50,
            this.height / 2 + 15, 100, 20, I18n.format(TranslationKeys.SOUTH)));
        this.buttonList.add(new GuiButton(EnumFacing.NORTH.ordinal(), this.width / 2 - 50,
            this.height / 2 - 35, 100, 20, I18n.format(TranslationKeys.NORTH)));
        this.buttonList.add(new GuiButton(EnumFacing.EAST.ordinal(), this.width / 2 + 40,
            this.height / 2 - 10, 100, 20, I18n.format(TranslationKeys.EAST)));
        this.buttonList.add(new GuiButton(EnumFacing.WEST.ordinal(), this.width / 2 - 140,
            this.height / 2 - 10, 100, 20, I18n.format(TranslationKeys.WEST)));
    }

    @Override
    public void actionPerformed(final GuiButton button) {
        if (this.copy) {
            LinkedList<String> list = tile.mapping.get(EnumFacing.getFront(button.id));
            PacketHandler.sendToServer(Mappings.Copy.create(tile, to, list));
            tile.mapping.put(to, list);
            showParent();
        } else {
            mc.displayGuiScreen(new GuiP_List(button.id, tile));
//            this.mc.displayGuiScreen(new GuiP_SelectSide(parent, tile, false, EnumFacing.getFront(button.id)));
        }
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(this.fontRenderer,
            I18n.format(this.copy ? TranslationKeys.COPY_SELECT : TranslationKeys.SET_SELECT), this.width / 2, 8, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

}
