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

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.pump.Mappings;
import com.yogpc.qp.tile.TilePump;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiP_List extends GuiScreenA implements GuiYesNoCallback {
    private static final byte CHANGE_ID = -4;
    private static final byte DONE_ID = -1;
    private static final byte COPY_ID = -5;
    private static final byte ADDMANUAL_ID = -2;
    private static final byte ADDFROMLIST_ID = -3;

    private GuiP_SlotList oreslot;
    private GuiButton delete, top, up, down, bottom;
    private final TilePump tile;
    EnumFacing dir;

    public GuiP_List(final byte id, final TilePump tq) {
        super(null);
        this.dir = EnumFacing.getFront(id);
        this.tile = tq;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.add(new GuiButton(CHANGE_ID, this.width / 2 - 160, this.height - 26, 100, 20,
                I18n.format("pp.change")));
        this.buttonList.add(new GuiButton(DONE_ID, this.width / 2 - 50, this.height - 26, 100, 20,
                I18n.format("gui.done")));
        this.buttonList.add(new GuiButton(COPY_ID, this.width / 2 + 60, this.height - 26, 100, 20,
                I18n.format("pp.copy")));
        this.buttonList.add(new GuiButton(ADDMANUAL_ID, this.width * 2 / 3 + 10, 45, 100, 20,
                I18n.format("tof.addnewore") + "(" + I18n.format("tof.manualinput") + ")"));
        this.buttonList.add(new GuiButton(ADDFROMLIST_ID, this.width * 2 / 3 + 10, 20, 100, 20,
                I18n.format("tof.addnewore") + "(" + I18n.format("tof.fromlist") + ")"));

        this.buttonList.add(this.delete =
                new GuiButton(Mappings.Type.Remove.getId(), this.width * 2 / 3 + 10, 70, 100, 20,
                        I18n.format("selectServer.delete")));
        this.buttonList.add(this.top =
                new GuiButton(Mappings.Type.Top.getId(), this.width * 2 / 3 + 10, 95, 100, 20,
                        I18n.format("tof.top")));
        this.buttonList.add(this.up =
                new GuiButton(Mappings.Type.Up.getId(), this.width * 2 / 3 + 10, 120, 100, 20,
                        I18n.format("tof.up")));
        this.buttonList.add(this.down =
                new GuiButton(Mappings.Type.Down.getId(), this.width * 2 / 3 + 10, 145, 100, 20,
                        I18n.format("tof.down")));
        this.buttonList.add(this.bottom =
                new GuiButton(Mappings.Type.Bottom.getId(), this.width * 2 / 3 + 10, 170, 100, 20,
                        I18n.format("tof.bottom")));
        this.oreslot = new GuiP_SlotList(this.mc, this.width * 3 / 5, this.height, 30, this.height - 30, this, this.tile.mapping.get(dir));
    }

    @Override
    public void actionPerformed(final GuiButton guiButton) {
        switch (guiButton.id) {
            case DONE_ID:
                showParent();
                break;
            case ADDMANUAL_ID:
                this.mc.displayGuiScreen(new GuiP_Manual(this, this.dir, this.tile));
                break;
            case ADDFROMLIST_ID:
                this.mc.displayGuiScreen(new GuiP_SelectBlock(this, this.tile, this.dir));
                break;
            case CHANGE_ID:
            case COPY_ID:
                this.mc.displayGuiScreen(new GuiP_SelectSide(this.tile, this, guiButton.id == COPY_ID));
                break;
            case 1://Mappings.Type.Remove.getId():
                String name = this.tile.mapping.get(dir).get(this.oreslot.currentore);
                if (FluidRegistry.isFluidRegistered(name))
                    name = FluidRegistry.getFluid(name).getLocalizedName(FluidRegistry.getFluidStack(name, 0));
                this.mc.displayGuiScreen(new GuiYesNo(this, I18n.format("tof.deletefluidsure"), name, guiButton.id));
                break;
            default:
                //TODO client change
                Mappings.Type typeId = Mappings.Type.fromID(guiButton.id);
                PacketHandler.sendToServer(Mappings.Update.create(tile, dir, typeId, this.tile.mapping.get(dir).get(this.oreslot.currentore)));
                break;
        }
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        drawDefaultBackground();
        if (oreslot != null)
            this.oreslot.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(
                this.fontRendererObj,
                I18n.format("pp.list.setting")
                        + I18n.format("FD." + dir), this.width / 2, 8, 0xFFFFFF);
        if (this.tile.mapping.get(dir).isEmpty()) {
            this.delete.enabled = false;
            this.top.enabled = false;
            this.up.enabled = false;
            this.down.enabled = false;
            this.bottom.enabled = false;
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void confirmClicked(final boolean result, final int id) {
        if (result) {
            if (Config.content().debug()) {
                QuarryPlus.LOGGER.info("GUIP_LIST callback id = " + id);
            }
            //TODO client change
            PacketHandler.sendToServer(Mappings.Update.create(tile, dir, Mappings.Type.fromID(id), this.tile.mapping.get(dir).get(this.oreslot.currentore)));
        }
        this.mc.displayGuiScreen(this);
    }
}
