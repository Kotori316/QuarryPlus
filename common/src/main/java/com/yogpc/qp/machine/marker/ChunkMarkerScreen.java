package com.yogpc.qp.machine.marker;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.misc.IndexedButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.concurrent.atomic.AtomicInteger;

public final class ChunkMarkerScreen extends AbstractContainerScreen<MarkerContainer> implements Button.OnPress {
    private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "textures/gui/marker.png");
    private static final int CHUNK = 16;
    private final ChunkMarkerEntity marker;
    private static final int BUTTON_WIDTH = 44;

    public ChunkMarkerScreen(MarkerContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        marker = (ChunkMarkerEntity) playerInventory.player.level().getBlockEntity(menu.pos);
        //217, 188
        this.imageWidth = 217;
        this.imageHeight = 188;
        this.inventoryLabelY = this.imageHeight - 96 + 2; // y position of text, inventory
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        int pX = leftPos;
        int pY = topPos;
        graphics.blit(LOCATION, pX, pY, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(font, "Size", (this.imageWidth - font.width("Size")) / 2, 6, 0x404040, false);
        String sizeText = Integer.toString(marker.size / CHUNK);
        graphics.drawString(font, sizeText, (this.imageWidth - font.width(sizeText)) / 2, 15 + 23, 0x404040, false);
        String yMaxText = Integer.toString(marker.maxY);
        String yMinText = Integer.toString(marker.minY);
        graphics.drawString(font, yMaxText, (this.imageWidth - font.width(yMaxText)) / 2 + 10 + BUTTON_WIDTH, 15 + 23, 0x404040, false);
        graphics.drawString(font, yMinText, (this.imageWidth - font.width(yMinText)) / 2 - 10 - BUTTON_WIDTH, 15 + 23, 0x404040, false);
    }

    @Override
    public void init() {
        super.init();
        final int tp = 15;
        final int middle = leftPos + this.imageWidth / 2;
        var id = new AtomicInteger(0);
        this.addRenderableWidget(new IndexedButton(id.getAndIncrement(), middle - BUTTON_WIDTH / 2, topPos + tp,
            BUTTON_WIDTH, 20, Component.literal("+"), this));
        this.addRenderableWidget(new IndexedButton(id.getAndIncrement(), middle - BUTTON_WIDTH / 2, topPos + tp + 33,
            BUTTON_WIDTH, 20, Component.literal("-"), this));
        this.addRenderableWidget(new IndexedButton(id.getAndIncrement(), middle + BUTTON_WIDTH / 2 + 10, topPos + tp,
            BUTTON_WIDTH, 20, Component.literal("Top+"), this));
        this.addRenderableWidget(new IndexedButton(id.getAndIncrement(), middle + BUTTON_WIDTH / 2 + 10, topPos + tp + 33,
            BUTTON_WIDTH, 20, Component.literal("Top-"), this));
        this.addRenderableWidget(new IndexedButton(id.getAndIncrement(), middle - BUTTON_WIDTH / 2 - 10 - BUTTON_WIDTH, topPos + tp,
            BUTTON_WIDTH, 20, Component.literal("Bottom+"), this));
        this.addRenderableWidget(new IndexedButton(id.getAndIncrement(), middle - BUTTON_WIDTH / 2 - 10 - BUTTON_WIDTH, topPos + tp + 33,
            BUTTON_WIDTH, 20, Component.literal("Bottom-"), this));
    }

    @Override
    public void onPress(Button button) {
        int size = marker.size;
        int yMin = marker.minY;
        int yMax = marker.maxY;
        int n;
        if (Screen.hasShiftDown()) { // Shift
            n = 16;
        } else if (Screen.hasControlDown()) { // Ctrl
            n = 4;
        } else {
            n = 1;
        }
        if (button instanceof IndexedButton indexedButton) {
            switch (indexedButton.getIndex()) {
                case 0 -> size = marker.size + CHUNK; // Plus
                case 1 -> { // Minus
                    if (marker.size > CHUNK) {
                        size = marker.size - CHUNK;
                    } else {
                        size = marker.size;
                    }
                }
                case 2 -> yMax = marker.maxY + n;
                case 3 -> yMax = Math.max(marker.maxY - n, yMin);
                case 4 -> yMin = Math.min(marker.minY + n, yMax);
                case 5 -> yMin = marker.minY - n;
            }
        }
        this.marker.size = size;
        this.marker.minY = yMin;
        this.marker.maxY = yMax;
        PlatformAccess.getAccess().packetHandler().sendToServer(new ChunkMarkerMessage(marker));
    }
}
