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

import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.container.ContainerMover;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.enchantment.MoverMessage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiMover extends GuiContainer {
    private static final ResourceLocation gui = new ResourceLocation(QuarryPlus.modID, "textures/gui/mover.png");

    private final BlockPos pos;

    public GuiMover(final EntityPlayer player, final World world, final int x, final int y, final int z) {
        super(new ContainerMover(player.inventory, world, x, y, z));
        pos = new BlockPos(x, y, z);
    }

    @Override
    public void initGui() {
        super.initGui();
        final int i = this.width - this.xSize >> 1;
        final int j = this.height - this.ySize >> 1;
        this.buttonList.add(new GuiButton(1, i + 27, j + 18, 122, 18, "UP"));
        this.buttonList.add(new GuiButton(2, i + 27, j + 36, 122, 18, ""));
        this.buttonList.add(new GuiButton(3, i + 27, j + 54, 122, 18, "Down"));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
        this.fontRenderer.drawString(I18n.format("tile.enchantmover.name"), 8, 6, 0x404040);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, 72, 0x404040);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(gui);
        drawTexturedModalRect(this.width - this.xSize >> 1, this.height - this.ySize >> 1, 0, 0, this.xSize, this.ySize);
    }

    @Override
    protected void actionPerformed(final GuiButton button) {
        if (!button.enabled)
            return;

        switch (button.id) {
            case 2:
                PacketHandler.sendToServer(MoverMessage.Move.create(pos, inventorySlots.windowId));
                break;
            case 1:
                PacketHandler.sendToServer(MoverMessage.Cursor.create(pos, inventorySlots.windowId, ContainerMover.D.UP));
                break;
            case 3:
                PacketHandler.sendToServer(MoverMessage.Cursor.create(pos, inventorySlots.windowId, ContainerMover.D.DOUN));
                break;
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        Optional<Enchantment> enchantment = ((ContainerMover) this.inventorySlots).getEnchantment();
        this.buttonList.get(1).displayString = enchantment.map(e -> I18n.format(e.getName())).orElse("");
    }
}
