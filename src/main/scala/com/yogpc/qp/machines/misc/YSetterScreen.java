package com.yogpc.qp.machines.misc;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.PacketHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

@Environment(EnvType.CLIENT)
public class YSetterScreen extends AbstractContainerScreen<YSetterContainer> {
    private static final ResourceLocation texture = new ResourceLocation(QuarryPlus.modID, "textures/gui/adv_pump.png");
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
            graphics.drawString(this.font, level, (this.imageWidth - font.width(level)) / 2, tp + 23, 0x404040, false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        graphics.blit(texture, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void init() {
        super.init();
        final int width = 40;
        addRenderableWidget(new IndexedButton(0, this.leftPos + this.imageWidth / 2 - width / 2, this.topPos + tp, width, 20, Component.literal("+"), b -> changeDigY(true)));
        addRenderableWidget(new IndexedButton(1, this.leftPos + this.imageWidth / 2 - width / 2, this.topPos + tp + 33, width, 20, Component.literal("-"), b -> changeDigY(false)));
    }

    private void changeDigY(boolean plus) {
        if (getMenu().yAccessor != null) {
            var count = (plus ? 1 : -1) * (Screen.hasControlDown() ? 10 : 1);
            var topLimit = getMenu().yAccessor.getLimitTop();
            if (count + getMenu().yAccessor.getDigMinY() < topLimit) {
                getMenu().yAccessor.setDigMinY(count + getMenu().yAccessor.getDigMinY());
                PacketHandler.sendToServer(getMenu().yAccessor.makeMessage());
            }
        }
    }
}
