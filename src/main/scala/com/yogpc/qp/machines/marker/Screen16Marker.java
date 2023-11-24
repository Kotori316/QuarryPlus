package com.yogpc.qp.machines.marker;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.misc.IndexedButton;
import com.yogpc.qp.packet.Marker16Message;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.concurrent.atomic.AtomicInteger;

public class Screen16Marker extends AbstractContainerScreen<ContainerMarker> implements Button.OnPress {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/marker.png");
    private static final int CHUNK = 16;
    private final Tile16Marker marker;
    private static final int BUTTON_WIDTH = 40;

    public Screen16Marker(ContainerMarker containerMarker, Inventory inv, Component component) {
        super(containerMarker, inv, component);
        this.marker = ((Tile16Marker) inv.player.getCommandSenderWorld().getBlockEntity(containerMarker.pos));
        //217, 188
        this.imageWidth = 217;
        this.imageHeight = 188;
        this.inventoryLabelY = this.imageHeight - 96 + 2; // y position of text, inventory
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        graphics.blit(LOCATION, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(this.font, "Size", (this.imageWidth - font.width("Size")) / 2, 6, 0x404040, false);
        String sizeText = Integer.toString(marker.getSize() / CHUNK);
        graphics.drawString(this.font, sizeText, (this.imageWidth - font.width(sizeText)) / 2, 15 + 23, 0x404040, false);
        String yMaxText = Integer.toString(marker.max().getY());
        String yMinText = Integer.toString(marker.min().getY());
        graphics.drawString(this.font, yMaxText, (this.imageWidth - font.width(yMaxText)) / 2 + 10 + BUTTON_WIDTH, 15 + 23, 0x404040, false);
        graphics.drawString(this.font, yMinText, (this.imageWidth - font.width(yMinText)) / 2 - 10 - BUTTON_WIDTH, 15 + 23, 0x404040, false);
    }

    @Override
    public void init() {
        super.init();
        final int tp = 15;
        final int middle = leftPos + this.imageWidth / 2;
        var id = new AtomicInteger();
        this.addRenderableWidget(new IndexedButton(id.getAndIncrement(), middle - BUTTON_WIDTH / 2, topPos + tp, BUTTON_WIDTH, 20, Component.literal("+"), this));
        this.addRenderableWidget(new IndexedButton(id.getAndIncrement(), middle - BUTTON_WIDTH / 2, topPos + tp + 33, BUTTON_WIDTH, 20, Component.literal("-"), this));
        this.addRenderableWidget(new IndexedButton(id.getAndIncrement(), middle + BUTTON_WIDTH / 2 + 10, topPos + tp, BUTTON_WIDTH, 20, Component.literal("Top+"), this));
        this.addRenderableWidget(new IndexedButton(id.getAndIncrement(), middle + BUTTON_WIDTH / 2 + 10, topPos + tp + 33, BUTTON_WIDTH, 20, Component.literal("Top-"), this));
        this.addRenderableWidget(new IndexedButton(id.getAndIncrement(), middle - BUTTON_WIDTH / 2 - 10 - BUTTON_WIDTH, topPos + tp, BUTTON_WIDTH, 20, Component.literal("Bottom+"), this));
        this.addRenderableWidget(new IndexedButton(id.getAndIncrement(), middle - BUTTON_WIDTH / 2 - 10 - BUTTON_WIDTH, topPos + tp + 33, BUTTON_WIDTH, 20, Component.literal("Bottom-"), this));
    }

    @Override
    public void onPress(Button b) {
        if (!(b instanceof IndexedButton)) return;
        int size = marker.getSize();
        int yMin = marker.min().getY(), yMax = marker.max().getY();
        int n;
        if (Screen.hasShiftDown()) { // Shift
            n = 16;
        } else if (Screen.hasControlDown()) { // Ctrl
            n = 4;
        } else {
            n = 1;
        }
        switch (((IndexedButton) b).getIndex()) {
            case 0 -> size = marker.getSize() + CHUNK; // Plus
            case 1 -> { // Minus
                if (marker.getSize() > CHUNK) {
                    size = marker.getSize() - CHUNK;
                } else {
                    size = marker.getSize();
                }
            }
            case 2 -> yMax = marker.max().getY() + n;
            case 3 -> yMax = Math.max(marker.max().getY() - n, yMin);
            case 4 -> yMin = Math.min(marker.min().getY() + n, yMax);
            case 5 -> yMin = marker.min().getY() - n;
        }
        PacketHandler.sendToServer(new Marker16Message(marker.getLevel(), marker.getBlockPos(), size, yMax, yMin));
    }
}
