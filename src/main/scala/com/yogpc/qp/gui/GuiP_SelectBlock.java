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

import java.io.IOException;

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
    private static final int DONE_Id = -1;
    private static final int CANCEL_Id = -2;
    private GuiP_SlotBlockList blocks;
    private final TilePump tile;
    private final EnumFacing targetId;

    public GuiP_SelectBlock(final GuiScreen parent, final TilePump tilePump, EnumFacing id) {
        super(parent);
        this.tile = tilePump;
        this.targetId = id;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.blocks = new GuiP_SlotBlockList(this.mc, this.width, this.height, 24, this.height - 32, this, this.tile.mapping.get(targetId));
        this.buttonList.add(new GuiButton(DONE_Id, this.width / 2 - 150, this.height - 26, 140, 20, I18n.format(TranslationKeys.DONE)));
        this.buttonList.add(new GuiButton(CANCEL_Id, this.width / 2 + 10, this.height - 26, 140, 20, I18n.format(TranslationKeys.CANCEL)));
    }

    @Override
    public void actionPerformed(final GuiButton par1) {
        if (par1.id == DONE_Id) {
            PacketHandler.sendToServer(Mappings.Update.create(tile, targetId, Mappings.Type.Add, blocks.current));
            tile.mapping.get(targetId).add(blocks.current);
        }
        showParent();
    }

    /**
     * Handles mouse input.
     */
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.blocks.handleMouseInput();
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        drawDefaultBackground();
        this.blocks.drawScreen(mouseX, mouseY, partialTicks);
        final String title = I18n.format(TranslationKeys.SELECT_FLUID);
        this.fontRenderer.drawStringWithShadow(title,
            (this.width - this.fontRenderer.getStringWidth(title)) / 2, 8, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
