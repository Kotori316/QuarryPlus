package com.yogpc.qp.machine.mover;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.misc.IndexedButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public final class MoverScreen extends AbstractContainerScreen<MoverContainer> {
    private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "textures/gui/mover.png");
    private static final WidgetSprites PAGE_FORWARD_SPRITES = new WidgetSprites(
        ResourceLocation.withDefaultNamespace("recipe_book/page_forward"), ResourceLocation.withDefaultNamespace("recipe_book/page_forward_highlighted")
    );
    private static final WidgetSprites PAGE_BACKWARD_SPRITES = new WidgetSprites(
        ResourceLocation.withDefaultNamespace("recipe_book/page_backward"), ResourceLocation.withDefaultNamespace("recipe_book/page_backward_highlighted")
    );
    private IndexedButton enchantmentMoveButton;
    private StateSwitchingButton forwardButton;
    private StateSwitchingButton backwardButton;
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
        this.addRenderableWidget(enchantmentMoveButton = new IndexedButton(1, leftPos + (imageWidth - width) / 2, topPos + 38, width, 20, Component.empty(), this::onPress));
        this.addRenderableWidget(backwardButton = new ArrowButton(leftPos + (imageWidth - 12) / 2 - 20, enchantmentMoveButton.getY() + enchantmentMoveButton.getHeight() + 8, 12, 17, false, PAGE_BACKWARD_SPRITES, this::onPress));
        this.addRenderableWidget(forwardButton = new ArrowButton(leftPos + (imageWidth - 12) / 2 + 20, enchantmentMoveButton.getY() + enchantmentMoveButton.getHeight() + 8, 12, 17, false, PAGE_FORWARD_SPRITES, this::onPress));

        enchantmentMoveButton.setTooltip(Tooltip.create(Component.literal("Move this enchantment")));
        backwardButton.setTooltip(Tooltip.create(Component.literal("Previous")));
        forwardButton.setTooltip(Tooltip.create(Component.literal("Next")));
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

    private static final class ArrowButton extends StateSwitchingButton {
        private final Consumer<ArrowButton> onPress;

        public ArrowButton(int x, int y, int width, int height, boolean initialState, @Nullable WidgetSprites sprites, Consumer<ArrowButton> onPress) {
            super(x, y, width, height, initialState);
            this.onPress = onPress;
            this.initTextureValues(sprites);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            super.onClick(mouseX, mouseY);
            onPress.accept(this);
        }

        @Override
        public boolean isHoveredOrFocused() {
            return isHovered();
        }
    }
}
