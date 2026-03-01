package com.yogpc.qp.machine.mover;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.misc.IndexedButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class MoverScreen extends AbstractContainerScreen<MoverContainer> {
    private static final Identifier LOCATION = Identifier.fromNamespaceAndPath(QuarryPlus.modID, "textures/gui/mover.png");
    private static final WidgetSprites PAGE_FORWARD_SPRITES = new WidgetSprites(
        Identifier.withDefaultNamespace("recipe_book/page_forward"), Identifier.withDefaultNamespace("recipe_book/page_forward_highlighted")
    );
    private static final WidgetSprites PAGE_BACKWARD_SPRITES = new WidgetSprites(
        Identifier.withDefaultNamespace("recipe_book/page_backward"), Identifier.withDefaultNamespace("recipe_book/page_backward_highlighted")
    );
    private IndexedButton enchantmentMoveButton;
    private ArrowButton forwardButton;
    private ArrowButton backwardButton;
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
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int pX = leftPos;
        int pY = topPos;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, LOCATION, pX, pY, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        final var width = 120;
        this.addRenderableWidget(enchantmentMoveButton = new IndexedButton(1, leftPos + (imageWidth - width) / 2, topPos + 38, width, 20, Component.empty(), this::onPress));
        this.addRenderableWidget(backwardButton = new ArrowButton(leftPos + (imageWidth - 12) / 2 - 20, enchantmentMoveButton.getY() + enchantmentMoveButton.getHeight() + 8, 12, 17, false, PAGE_BACKWARD_SPRITES, this::onPress));
        this.addRenderableWidget(forwardButton = new ArrowButton(leftPos + (imageWidth - 12) / 2 + 20, enchantmentMoveButton.getY() + enchantmentMoveButton.getHeight() + 8, 12, 17, false, PAGE_FORWARD_SPRITES, this::onPress));
        backwardButton.setState(true);
        forwardButton.setState(true);

        enchantmentMoveButton.setTooltip(Tooltip.create(Component.translatable("quarryplus.gui.mover.move_enchantment")));
        backwardButton.setTooltip(Tooltip.create(Component.translatable("quarryplus.gui.mover.previous")));
        forwardButton.setTooltip(Tooltip.create(Component.translatable("quarryplus.gui.mover.next")));
    }

    public void onPress(AbstractWidget button) {
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
        } else if (button == forwardButton) {
            currentIndex = Math.floorMod(currentIndex + 1, list.size());
        } else if (button == backwardButton) {
            currentIndex = Math.floorMod(currentIndex - 1, list.size());
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

    private static final class ArrowButton extends ImageButton {

        private boolean state;

        public ArrowButton(int x, int y, int width, int height, boolean initialState, WidgetSprites sprites, Button.OnPress onPress) {
            super(x, y, width, height, sprites, onPress);
            this.state = initialState;
        }

        public void setState(boolean state) {
            this.state = state;
        }

        @Override
        public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            var spriteIdentifier = this.sprites.get(state, isHovered());
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, spriteIdentifier, this.getX(), this.getY(), this.width, this.height);
        }
    }
}
