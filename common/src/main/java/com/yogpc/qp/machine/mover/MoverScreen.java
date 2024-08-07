package com.yogpc.qp.machine.mover;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.misc.IndexedButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class MoverScreen extends AbstractContainerScreen<MoverContainer> implements Button.OnPress {
    private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "textures/gui/mover.png");
    private IndexedButton enchantmentMoveButton;
    private int currentIndex = 0;

    public MoverScreen(MoverContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // 176, 186
        this.imageWidth = 176;
        this.imageHeight = 186;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int pX = leftPos;
        int pY = topPos;
        guiGraphics.blit(LOCATION, pX, pY, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        final var width = 120;
        enchantmentMoveButton = new IndexedButton(1, leftPos + (imageWidth - width) / 2, topPos + 38, width, 20, Component.empty(), this);
        this.addRenderableWidget(enchantmentMoveButton);
    }

    @Override
    public void onPress(Button button) {
        var list = getMenu().entity.movableEnchantments;
        if (!button.active || list.isEmpty()) {
            return;
        }
        if (button == enchantmentMoveButton) {
            var enchantment = list.get(Math.floorMod(currentIndex, list.size()));
            enchantment.unwrapKey().ifPresentOrElse(
                key -> PlatformAccess.getAccess().packetHandler().sendToServer(new MoverMessage(getMenu().entity, key)),
                () -> QuarryPlus.LOGGER.warn("No enchantment key found for {}", enchantment)
            );
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        var list = getMenu().entity.movableEnchantments;
        if (!list.isEmpty()) {
            enchantmentMoveButton.setMessage(list.get(Math.floorMod(currentIndex, list.size())).value().description());
        } else {
            enchantmentMoveButton.setMessage(Component.empty());
        }
    }
}
