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
import java.util.Collections;
import java.util.List;

import com.yogpc.qp.BlockData;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.container.ContainerEnchList;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.enchantment.EnchantmentMessage;
import com.yogpc.qp.tile.TileBasic;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiEnchList extends GuiContainer implements GuiYesNoCallback {
    public static final int Toggle_id = 10, Remove_id = 12;
    private GuiSlotEnchList slot;
    private final TileBasic tile;
    private final Enchantment target;

    /**
     * @param ench must be either Fortune or Silktouch.
     */
    public GuiEnchList(Enchantment ench, final TileBasic tq, EntityPlayer player) {
        super(new ContainerEnchList(tq, player));
        this.target = ench;
        this.tile = tq;
    }

    public boolean include() {
        if (this.target == Enchantments.FORTUNE)
            return this.tile.fortuneInclude;
        return this.tile.silktouchInclude;
    }

    private List<BlockData> getBlockDataList(Enchantment enchantment) {
        if (enchantment == Enchantments.SILK_TOUCH) {
            return tile.silktouchList;
        } else if (enchantment == Enchantments.FORTUNE) {
            return tile.fortuneList;
        } else {
            QuarryPlus.LOGGER.error(String.format("GuiEnchList target is %s", enchantment));
            return Collections.emptyList();
        }
    }

    @Override
    public void initGui() {
        this.xSize = this.width;
        this.ySize = this.height;
        super.initGui(); // must be here!
//        PacketHandler.sendToServer(BlockListRequestMessage.create(inventorySlots.windowId));
        this.buttonList.add(new GuiButton(-1,
            this.width / 2 - 125, this.height - 26, 250, 20, I18n.format(TranslationKeys.DONE)));
        this.buttonList.add(new GuiButton(Toggle_id,
            this.width * 2 / 3 + 10, 140, 100, 20, ""));
        this.buttonList.add(new GuiButton(Remove_id,
            this.width * 2 / 3 + 10, 110, 100, 20, I18n.format(TranslationKeys.DELETE)));
        this.slot = new GuiSlotEnchList(this.mc, this.width * 3 / 5, this.height - 60, 30, this.height - 30,
            this, getBlockDataList(target));
    }

    /**
     * Handles mouse input.
     */
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.slot.handleMouseInput();
    }

    @Override
    public void actionPerformed(final GuiButton par1) {
        switch (par1.id) {
            case -1:
                this.mc.thePlayer.closeScreen();
                break;
            case Remove_id:
                this.mc.displayGuiScreen(new GuiYesNo(this, I18n.format(TranslationKeys.DELETE_BLOCK_SURE),
                    getBlockDataList(target).get(this.slot.currentore()).getLocalizedName(), par1.id));
                break;
            default: //maybe toggle
                PacketHandler.sendToServer(EnchantmentMessage.create(tile, EnchantmentMessage.Type.Toggle, target, BlockData.Invalid()));
                break;
        }
    }

    @Override
    public void confirmClicked(final boolean result, final int id) {
        if (result) {
            final BlockData bd = this.slot.target().get(this.slot.currentore());
            PacketHandler.sendToServer(EnchantmentMessage.create(tile, EnchantmentMessage.Type.Remove, target, bd));

            getBlockDataList(target).remove(bd);
            slot.target().remove(bd);
        }
        this.mc.displayGuiScreen(this);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final float k, final int i, final int j) {
        if (slot != null)
            this.slot.drawScreen(i, j, k);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final int i, final int j) {
        drawCenteredString(this.fontRendererObj, I18n.format(TranslationKeys.QP_ENABLE_LIST, I18n.format(this.target.getName())),
            this.xSize / 2, 8, 0xFFFFFF);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.buttonList.get(1).displayString = I18n.format(include() ? TranslationKeys.TOF_INCLUDE : TranslationKeys.TOF_EXCLUDE);
        this.buttonList.get(2).enabled = !getBlockDataList(target).isEmpty();
    }
}
