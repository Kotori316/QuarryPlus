package com.yogpc.qp.machines.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.PacketHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class YSetterScreen extends HandledScreen<YSetterContainer> {
    private static final Identifier texture = new Identifier(QuarryPlus.modID, "textures/gui/adv_pump.png");
    private static final int tp = 15;

    public YSetterScreen(YSetterContainer handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        super.drawForeground(matrices, mouseX, mouseY);
        var level = String.valueOf(getScreenHandler().yAccessor != null ? getScreenHandler().yAccessor.getDigMinY() + 1 : 0);
        this.textRenderer.draw(matrices, level, ((float) this.backgroundWidth - textRenderer.getWidth(level)) / 2, tp + 23, 0x404040);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);
        this.drawTexture(matrices, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    protected void init() {
        super.init();
        final int width = 40;
        addDrawableChild(new ButtonWidget(this.x + this.backgroundWidth / 2 - width / 2, this.y + tp, width, 20, new LiteralText("+"), b -> changeDigY(true)));
        addDrawableChild(new ButtonWidget(this.x + this.backgroundWidth / 2 - width / 2, this.y + tp + 33, width, 20, new LiteralText("-"), b -> changeDigY(false)));
    }

    private void changeDigY(boolean plus) {
        if (getScreenHandler().yAccessor != null) {
            var count = (plus ? 1 : -1) * (Screen.hasControlDown() ? 10 : 1);
            var topLimit = getScreenHandler().yAccessor.getLimitTop();
            if (count + getScreenHandler().yAccessor.getDigMinY() < topLimit) {
                getScreenHandler().yAccessor.setDigMinY(count + getScreenHandler().yAccessor.getDigMinY());
                PacketHandler.sendToServer(getScreenHandler().yAccessor.makeMessage());
            }
        }
    }
}
