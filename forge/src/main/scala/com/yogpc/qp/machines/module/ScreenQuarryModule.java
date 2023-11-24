package com.yogpc.qp.machines.module;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.misc.YAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

public class ScreenQuarryModule extends AbstractContainerScreen<ContainerQuarryModule> {

    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/quarry_module.png");
    @Nullable
    private final YAccessor yGetter;

    public ScreenQuarryModule(ContainerQuarryModule c, Inventory inventory, Component component) {
        super(c, inventory, component);
        this.yGetter = YAccessor.get(c.blockEntity);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        int pX = getGuiLeft();
        int pY = getGuiTop();
        graphics.blit(LOCATION, pX, pY, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(font, "Modules", this.titleLabelX, this.titleLabelY + 10, 0x404040, false);
        if (yGetter != null) {
            graphics.drawString(font, "Y: " + (yGetter.getDigMinY() + 1), 120, this.titleLabelY, 0x404040, false);
        }
    }
}
