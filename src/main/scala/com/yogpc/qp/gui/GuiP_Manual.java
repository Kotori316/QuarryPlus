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
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiP_Manual extends GuiScreenA implements GuiYesNoCallback {
    private GuiTextField blockid;
    private final EnumFacing targetid;
    private final TilePump tile;

    public GuiP_Manual(final GuiScreen parents, EnumFacing facing, final TilePump tq) {
        super(parents);
        this.targetid = facing;
        this.tile = tq;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.add(new GuiButton(-1, this.width / 2 - 150, this.height - 26, 140, 20,
                I18n.format(TranslationKeys.DONE)));
        this.buttonList.add(new GuiButton(-2, this.width / 2 + 10, this.height - 26, 140, 20,
                I18n.format(TranslationKeys.CANCEL)));
        this.blockid = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 50, 50, 100, 20);
        this.blockid.setFocused(true);
    }

    @Override
    public void actionPerformed(final GuiButton par1) {
        switch (par1.id) {
            case -1:
                String name = this.blockid.getText();
                if (name.isEmpty())
                    return;
                if (this.tile.mapping.get(targetid).contains(name)) {
                    if (FluidRegistry.isFluidRegistered(name))
                        name = FluidRegistry.getFluid(name).getLocalizedName(FluidRegistry.getFluidStack(name, 0));
                    this.mc.displayGuiScreen(new GuiError(this, I18n.format(TranslationKeys.ALREADY_REGISTERED_ERROR), name));
                    return;
                }
                if (FluidRegistry.isFluidRegistered(name))
                    name = FluidRegistry.getFluid(name).getLocalizedName(FluidRegistry.getFluidStack(name, 0));
                this.mc.displayGuiScreen(new GuiYesNo(this, I18n.format(TranslationKeys.ADD_FLUID_SURE), name, -1));
                break;
            case -2:
                showParent();
                break;
        }
    }

    @Override
    public void confirmClicked(final boolean par1, final int par2) {
        if (par1) {
            PacketHandler.sendToServer(Mappings.Update.create(tile, targetid, Mappings.Type.Add, blockid.getText()));
            tile.mapping.get(targetid).add(blockid.getText());
        }
        showParent();
    }

    @Override
    protected void keyTyped(final char typedChar, final int keyCode) {
        if (this.blockid.isFocused())
            this.blockid.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(final int par1, final int par2, final int par3) throws IOException {
        super.mouseClicked(par1, par2, par3);
        this.blockid.mouseClicked(par1, par2, par3);
    }

    @Override
    public void drawScreen(final int i, final int j, final float k) {
        drawDefaultBackground();
        drawCenteredString(this.fontRendererObj, I18n.format(TranslationKeys.SELECT_FLUID), this.width / 2, 8, 0xFFFFFF);
        String fluidId = I18n.format(TranslationKeys.FLUID_ID);
        this.fontRendererObj.drawStringWithShadow(fluidId, this.width / 2 - 60 - this.fontRendererObj.getStringWidth(fluidId), 50, 0xFFFFFF);
        this.blockid.drawTextBox();
        super.drawScreen(i, j, k);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.blockid.updateCursorCounter();
    }
}
