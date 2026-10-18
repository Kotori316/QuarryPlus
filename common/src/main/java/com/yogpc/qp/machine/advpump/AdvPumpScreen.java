package com.yogpc.qp.machine.advpump;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.misc.IndexedButton;
import com.yogpc.qp.machine.misc.SmallCheckBox;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class AdvPumpScreen extends AbstractContainerScreen<AdvPumpContainer> {
    private static final Identifier LOCATION = Identifier.fromNamespaceAndPath(QuarryPlus.modID, "textures/gui/adv_pump.png");
    private SmallCheckBox placeFrameCheckBox;
    private SmallCheckBox deleteFluidCheckBox;
    private SmallCheckBox searchDownwardCheckBox;

    public AdvPumpScreen(AdvPumpContainer c, Inventory inventory, Component component) {
        super(c, inventory, component, c.imageWidth, c.imageHeight);
        this.inventoryLabelY = this.imageHeight - 96 + 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, LOCATION, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
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
        searchDownwardCheckBox = new SmallCheckBox(leftPos + 8, topPos + 42, 140, 10, 10, 10,
            Component.translatable("quarryplus.gui.adv_pump.search_downward"), getMenu().pump.searchDownward, this::checkBoxOnPress);
        this.addRenderableWidget(searchDownwardCheckBox);
        if (!PlatformAccess.getAccess().platformName().equalsIgnoreCase("fabric")) {
            this.addRenderableWidget(new IndexedButton(0, leftPos + 8, topPos + 56, 50, 12, Component.translatable("quarryplus.gui.adv_pump.modules"), this::openModuleOnPress));
        }
    }

    private void checkBoxOnPress(Button b) {
        var pump = getMenu().pump;
        pump.placeFrame = placeFrameCheckBox.isSelected();
        pump.deleteFluid = deleteFluidCheckBox.isSelected();
        pump.searchDownward = searchDownwardCheckBox.isSelected();
        PlatformAccess.getAccess().packetHandler().sendToServer(new AdvPumpSettingsMessage(pump));
    }

    private void openModuleOnPress(Button b) {
        PlatformAccess.getAccess().packetHandler().sendToServer(new AdvPumpOpenModuleMessage(getMenu().pump));
    }
}
