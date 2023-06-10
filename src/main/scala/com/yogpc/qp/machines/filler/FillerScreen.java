package com.yogpc.qp.machines.filler;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.misc.IndexedButton;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.concurrent.atomic.AtomicInteger;

public final class FillerScreen extends AbstractContainerScreen<FillerMenu> implements Button.OnPress {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/filler.png");

    public FillerScreen(FillerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        var id = new AtomicInteger(0);
        this.addRenderableWidget(new IndexedButton(id.getAndIncrement(), this.getGuiLeft() + this.getXSize() - 60 - 8, this.getGuiTop() + 7,
                60, 20, Component.literal("FillAll"), this));
        this.addRenderableWidget(new IndexedButton(id.getAndIncrement(), this.getGuiLeft() + this.getXSize() - 60 - 8, this.getGuiTop() + 7 + 20,
                60, 20, Component.literal("FillBox"), this));
        // this.addRenderableWidget(new IndexedButton(id.getAndIncrement(), this.getGuiLeft() + this.getXSize() - 60 - 8, this.getGuiTop() + 7 + 40,
        //     60, 20, Component.literal("Pillar"), this));
        // this.addRenderableWidget(new IndexedButton(id.getAndIncrement(), this.getGuiLeft() + 110, this.getGuiTop() + this.getYSize() - 97,
        //     60, 14, Component.literal("Modules"), this));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
        int pX = getGuiLeft();
        int pY = getGuiTop();
        graphics.blit(LOCATION, pX, pY, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public void onPress(Button pButton) {
        if (pButton instanceof IndexedButton indexedButton) {
            switch (indexedButton.getIndex()) {
                case 0 -> PacketHandler.sendToServer(new FillerButtonMessage(menu.filler, FillerEntity.Action.BOX));
                case 1 -> PacketHandler.sendToServer(new FillerButtonMessage(menu.filler, FillerEntity.Action.WALL));
                case 2 -> PacketHandler.sendToServer(new FillerButtonMessage(menu.filler, FillerEntity.Action.PILLAR));
                default ->
                        QuarryPlus.LOGGER.error("Unknown button({}, {}) is pushed in {}", indexedButton, indexedButton.getIndex(), this);
            }
        } else {
            QuarryPlus.LOGGER.error("Unknown button({}) is pushed in {}", pButton, this);
        }
    }
}
