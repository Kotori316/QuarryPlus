package com.yogpc.qp.machines.marker;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.Marker16Message;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

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
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.renderTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, LOCATION);
        this.blit(matrices, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(PoseStack matrices, int mouseX, int mouseY) {
        super.renderLabels(matrices, mouseX, mouseY);
        font.draw(matrices, "Size", (this.imageWidth - font.width("Size")) / 2f, 6, 0x404040);
        String sizeText = Integer.toString(marker.getSize() / CHUNK);
        font.draw(matrices, sizeText, (this.imageWidth - font.width(sizeText)) / 2f, 15 + 23, 0x404040);
        String yMaxText = Integer.toString(marker.max().getY());
        String yMinText = Integer.toString(marker.min().getY());
        font.draw(matrices, yMaxText, (this.imageWidth - font.width(yMaxText)) / 2f + 10 + BUTTON_WIDTH, 15 + 23, 0x404040);
        font.draw(matrices, yMinText, (this.imageWidth - font.width(yMinText)) / 2f - 10 - BUTTON_WIDTH, 15 + 23, 0x404040);
    }

    @Override
    public void init() {
        super.init();
        final int tp = 15;
        final int middle = leftPos + this.imageWidth / 2;
        this.addRenderableWidget(new Button(middle - BUTTON_WIDTH / 2, topPos + tp, BUTTON_WIDTH, 20, new TextComponent("+"), this));
        this.addRenderableWidget(new Button(middle - BUTTON_WIDTH / 2, topPos + tp + 33, BUTTON_WIDTH, 20, new TextComponent("-"), this));
        this.addRenderableWidget(new Button(middle + BUTTON_WIDTH / 2 + 10, topPos + tp, BUTTON_WIDTH, 20, new TextComponent("Top+"), this));
        this.addRenderableWidget(new Button(middle + BUTTON_WIDTH / 2 + 10, topPos + tp + 33, BUTTON_WIDTH, 20, new TextComponent("Top-"), this));
        this.addRenderableWidget(new Button(middle - BUTTON_WIDTH / 2 - 10 - BUTTON_WIDTH, topPos + tp, BUTTON_WIDTH, 20, new TextComponent("Bottom+"), this));
        this.addRenderableWidget(new Button(middle - BUTTON_WIDTH / 2 - 10 - BUTTON_WIDTH, topPos + tp + 33, BUTTON_WIDTH, 20, new TextComponent("Bottom-"), this));

    }

    @Override
    public void onPress(Button button) {
        int size = marker.getSize();
        int yMin = marker.min().getY(), yMax = marker.max().getY();
        int n;
        if (Screen.hasShiftDown()) { // Shift
            n = 16;
        } else if (Screen.hasControlDown()) {
            n = 4;
        } else {
            n = 1;
        }
        switch (children().indexOf(button)) {
            case 0: // Plus
                size = marker.getSize() + CHUNK;
                break;
            case 1: // Minus
                if (marker.getSize() > CHUNK) {
                    size = marker.getSize() - CHUNK;
                } else {
                    size = marker.getSize();
                }
                break;
            case 2:
                yMax = marker.max().getY() + n;
                break;
            case 3:
                yMax = Math.max(marker.max().getY() - n, yMin);
                break;
            case 4:
                yMin = Math.min(marker.min().getY() + n, yMax);
                break;
            case 5:
                yMin = marker.min().getY() - n;
                break;
        }
        PacketHandler.sendToServer(new Marker16Message(marker.getLevel(), marker.getBlockPos(), size, yMax, yMin));
    }
}
