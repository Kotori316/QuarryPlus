package com.yogpc.qp.machines.mini_quarry;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.misc.IndexedButton;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class MiniQuarryScreen extends AbstractContainerScreen<MiniQuarryMenu> implements Button.OnPress {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/quarry_module.png");

    public MiniQuarryScreen(MiniQuarryMenu c, Inventory inventory, Component component) {
        super(c, inventory, component);
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
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new IndexedButton(1, getGuiLeft() + 70, getGuiTop() + 50, 60, 20, Component.literal("List"), this));
    }

    @Override
    public void onPress(Button b) {
        if (b instanceof IndexedButton button) {
            if (button.id() == 1) {
                PacketHandler.sendToServer(new MiniRequestListMessage(getMenu().miniQuarry));
            }
        }
    }
}
