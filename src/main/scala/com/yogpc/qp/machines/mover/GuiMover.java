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

package com.yogpc.qp.machines.mover;

import java.util.Optional;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.IHandleButton;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.mover.MoverMessage;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiMover extends ContainerScreen<ContainerMover> implements IHandleButton {
    private static final ResourceLocation gui = new ResourceLocation(QuarryPlus.modID, "textures/gui/mover.png");

    private final BlockPos pos;

    public GuiMover(ContainerMover containerMarker, PlayerInventory inv, ITextComponent component) {
        super(containerMarker, inv, component);
        this.pos = containerMarker.pos;
    }

    @Override
    public void init() {
        super.init();
        final int i = this.width - this.xSize >> 1;
        final int j = this.height - this.ySize >> 1;
        addButton(new IHandleButton.Button(1, i + 27, j + 18, 122, 18, I18n.format(TranslationKeys.UP), this));
        addButton(new IHandleButton.Button(2, i + 27, j + 36, 122, 18, "", this));
        addButton(new IHandleButton.Button(3, i + 27, j + 54, 122, 18, I18n.format(TranslationKeys.DOWN), this));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
        this.font.drawString(I18n.format(TranslationKeys.mover), 8, 6, 0x404040);
        this.font.drawString(I18n.format(TranslationKeys.CONTAINER_INVENTORY), 8, 72, 0x404040);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.getMinecraft().getTextureManager().bindTexture(gui);
        blit(this.width - this.xSize >> 1, this.height - this.ySize >> 1, 0, 0, this.xSize, this.ySize);
    }

    @Override
    public void tick() {
        super.tick();
        Optional<Enchantment> enchantment = this.container.getEnchantment();
        this.buttons.get(1).setMessage(enchantment.map(e -> I18n.format(e.getName())).orElse(""));
    }

    @Override
    public void actionPerformed(IHandleButton.Button button) {
        if (!button.active)
            return;

        switch (button.id) {
            case 2:
                PacketHandler.sendToServer(MoverMessage.Move.create(pos, container.windowId));
                break;
            case 1:
                PacketHandler.sendToServer(MoverMessage.Cursor.create(pos, container.windowId, ContainerMover.D.UP));
                break;
            case 3:
                PacketHandler.sendToServer(MoverMessage.Cursor.create(pos, container.windowId, ContainerMover.D.DOWN));
                break;
        }
    }
}
