package com.yogpc.qp.machines.misc;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class YSetterScreen extends AbstractContainerScreen<YSetterContainer> {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/adv_pump.png");
    private static final int tp = 15;

    public YSetterScreen(YSetterContainer handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        if (getMenu().yAccessor != null) {
            var level = String.valueOf(getMenu().yAccessor.getDigMinY() + 1);
            graphics.drawString(font, level, (this.imageWidth - font.width(level)) / 2, tp + 23, 0x404040, false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        int pX = getGuiLeft();
        int pY = getGuiTop();
        graphics.blit(LOCATION, pX, pY, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void init() {
        super.init();
        final int width = 40;
        addRenderableWidget(Button.builder(Component.literal("+"), b -> changeDigY(true))
                .pos(this.getGuiLeft() + this.imageWidth / 2 - width / 2, this.getGuiTop() + tp)
                .size(width, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("-"), b -> changeDigY(false))
                .pos(this.getGuiLeft() + this.imageWidth / 2 - width / 2, this.getGuiTop() + tp + 33)
                .size(width, 20)
                .build());
    }

    private void changeDigY(boolean plus) {
        var accessor = getMenu().yAccessor;
        if (accessor != null) {
            int n = Screen.hasShiftDown() ? 16 : Screen.hasControlDown() ? 4 : 1;
            var count = (plus ? 1 : -1) * n;
            var topLimit = accessor.getLimitTop();
            if (count + accessor.getDigMinY() < topLimit) {
                accessor.setDigMinY(count + accessor.getDigMinY());
                PacketHandler.sendToServer(accessor.makeMessage());
            }
        }
    }
}
