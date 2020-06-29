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

import com.mojang.blaze3d.matrix.MatrixStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.IHandleButton;
import com.yogpc.qp.machines.base.ScreenUtil;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.mover.MoverMessage;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
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
    public void func_231160_c_() {
        super.func_231160_c_();
        func_230480_a_(new IHandleButton.Button(1, guiLeft + 27, guiTop + 18, 122, 18, new TranslationTextComponent(TranslationKeys.UP), this));
        func_230480_a_(new IHandleButton.Button(2, guiLeft + 27, guiTop + 36, 122, 18, "", this));
        func_230480_a_(new IHandleButton.Button(3, guiLeft + 27, guiTop + 54, 122, 18, new TranslationTextComponent(TranslationKeys.DOWN), this));
    }

    @Override
    protected void func_230451_b_(MatrixStack matrixStack, final int mouseX, final int mouseY) {
        super.func_230451_b_(matrixStack, mouseX, mouseY);
    }

    @Override
    public void func_230430_a_(MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        this.func_230446_a_(matrixStack);// back ground
        super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
        this.func_230459_a_(matrixStack, mouseX, mouseY); // render tooltip
    }

    @Override
    protected void func_230450_a_(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        ScreenUtil.color4f();
        this.getMinecraft().getTextureManager().bindTexture(gui);
        this.func_238474_b_(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    public void func_231023_e_() {
        super.func_231023_e_();
        Optional<Enchantment> enchantment = this.container.getEnchantment();
        this.field_230710_m_.get(1).func_238482_a_(enchantment.map(e -> new TranslationTextComponent(e.getName())).orElse(new TranslationTextComponent("")));
    }

    @Override
    public void actionPerformed(IHandleButton.Button button) {
        if (!button.field_230693_o_)
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
