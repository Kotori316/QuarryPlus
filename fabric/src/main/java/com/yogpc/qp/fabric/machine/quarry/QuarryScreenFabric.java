package com.yogpc.qp.fabric.machine.quarry;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.misc.SmallCheckBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class QuarryScreenFabric extends AbstractContainerScreen<QuarryMenuFabric> {
    private static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "textures/gui/adv_pump.png");
    private SmallCheckBox removeFluids;

    public QuarryScreenFabric(QuarryMenuFabric abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float f, int i, int j) {
        graphics.blit(texture, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void init() {
        super.init();
        removeFluids = new SmallCheckBox(leftPos + 8, topPos + 20, 80, 10, 10, 10,
            Component.nullToEmpty("Remove fluids"), this.menu.quarry.shouldRemoveFluid, this::onPressFluidButton);
        addRenderableWidget(removeFluids);
    }

    private void onPressFluidButton(Button button) {
        if (button == removeFluids) {
            this.menu.quarry.shouldRemoveFluid = removeFluids.isSelected();
            PlatformAccess.getAccess().packetHandler().sendToServer(new QuarryConfigSyncMessage(
                this.menu.quarry,
                removeFluids.isSelected()
            ));
        }
    }
}
