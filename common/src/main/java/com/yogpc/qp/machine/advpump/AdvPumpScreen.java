package com.yogpc.qp.machine.advpump;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.misc.IndexedButton;
import com.yogpc.qp.machine.misc.SmallCheckBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AdvPumpScreen extends AbstractContainerScreen<AdvPumpContainer> {
    private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "textures/gui/adv_pump_machine.png");
    private SmallCheckBox placeFrameCheckBox;
    private SmallCheckBox deleteFluidCheckBox;

    public AdvPumpScreen(AdvPumpContainer c, Inventory inventory, Component component) {
        super(c, inventory, component);
        this.imageWidth = c.imageWidth;
        this.imageHeight = c.imageHeight;
        this.inventoryLabelY = this.imageHeight - 96 + 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        graphics.blit(LOCATION, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void init() {
        super.init();
        placeFrameCheckBox = new SmallCheckBox(leftPos + 8, topPos + 20, 140, 10, 10, 10,
            Component.translatable("quarryplus.gui.adv_pump.frame"), getMenu().pump.placeFrame, this::checkBoxOnPress);
        this.addRenderableWidget(placeFrameCheckBox);
        deleteFluidCheckBox = new SmallCheckBox(leftPos + 8, topPos + 31, 140, 10, 10, 10,
            Component.translatable("quarryplus.gui.adv_pump.delete"), getMenu().pump.deleteFluid, this::checkBoxOnPress);
        this.addRenderableWidget(deleteFluidCheckBox);
        if (!PlatformAccess.getAccess().platformName().equalsIgnoreCase("fabric")) {
            this.addRenderableWidget(new IndexedButton(0, leftPos + 8, topPos + 45, 50, 12, Component.translatable("quarryplus.gui.adv_pump.modules"), this::openModuleOnPress));
        }
    }

    private void checkBoxOnPress(Button b) {
        var pump = getMenu().pump;
        var placeFrame = placeFrameCheckBox.isSelected();
        var deleteFluid = deleteFluidCheckBox.isSelected();
        pump.placeFrame = placeFrame;
        pump.deleteFluid = deleteFluid;
        PlatformAccess.getAccess().packetHandler().sendToServer(new AdvPumpSettingsMessage(pump));
    }

    private void openModuleOnPress(Button b) {
        PlatformAccess.getAccess().packetHandler().sendToServer(new AdvPumpOpenModuleMessage(getMenu().pump));
    }
}
