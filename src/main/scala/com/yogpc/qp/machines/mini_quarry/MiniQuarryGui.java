package com.yogpc.qp.machines.mini_quarry;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.base.IHandleButton;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.mini_quarry.MiniRenderBoxMessage;
import com.yogpc.qp.packet.mini_quarry.MiniRequestListMessage;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class MiniQuarryGui extends ContainerScreen<MiniQuarryContainer> implements IHandleButton {

    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/quarry_module.png");
    private final MiniQuarryContainer container;

    public MiniQuarryGui(MiniQuarryContainer c, PlayerInventory i, ITextComponent t) {
        super(c, i, t);
        container = c;
    }

    @Override
    public void func_230430_a_(MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        this.func_230446_a_(matrixStack);// back ground
        super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
        this.func_230459_a_(matrixStack, mouseX, mouseY); // render tooltip
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void func_230450_a_(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.getMinecraft().getTextureManager().bindTexture(LOCATION);
        this.func_238474_b_(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void func_231160_c_() {
        super.func_231160_c_();
        func_230480_a_(new IHandleButton.Button(0, guiLeft + 8, guiTop + 50, 60, 20, "RenderBox", this));
        func_230480_a_(new IHandleButton.Button(1, guiLeft + 70, guiTop + 50, 60, 20, "List", this));
    }

    @Override
    public void actionPerformed(Button button) {
        switch (button.id) {
            case 0:
                boolean renderBox = !container.tile.renderBox();
                container.tile.renderBox_$eq(renderBox);
                PacketHandler.sendToServer(MiniRenderBoxMessage.create(container.tile, renderBox));
                break;
            case 1:
//                this.onClose();
                PacketHandler.sendToServer(MiniRequestListMessage.create(container.tile));
                break;
        }
    }
}
