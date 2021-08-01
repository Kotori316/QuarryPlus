package com.yogpc.qp.machines.marker;

import com.mojang.blaze3d.systems.RenderSystem;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.Marker16Message;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Screen16Marker extends HandledScreen<ContainerMarker> implements ButtonWidget.PressAction {
    private static final Identifier LOCATION = new Identifier(QuarryPlus.modID, "textures/gui/marker.png");
    private static final int CHUNK = 16;
    private final Tile16Marker marker;
    private static final int BUTTON_WIDTH = 40;

    public Screen16Marker(ContainerMarker containerMarker, PlayerInventory inv, Text component) {
        super(containerMarker, inv, component);
        this.marker = ((Tile16Marker) inv.player.getEntityWorld().getBlockEntity(containerMarker.pos));
        //217, 188
        this.backgroundWidth = 217;
        this.backgroundHeight = 188;
        this.playerInventoryTitleY = this.backgroundHeight - 96 + 2; // y position of text, inventory
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, LOCATION);
        this.drawTexture(matrices, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        super.drawForeground(matrices, mouseX, mouseY);
        textRenderer.draw(matrices, "Size", (this.backgroundWidth - textRenderer.getWidth("Size")) / 2f, 6, 0x404040);
        String sizeText = Integer.toString(marker.getSize() / CHUNK);
        textRenderer.draw(matrices, sizeText, (this.backgroundWidth - textRenderer.getWidth(sizeText)) / 2f, 15 + 23, 0x404040);
        String yMaxText = Integer.toString(marker.max().getY());
        String yMinText = Integer.toString(marker.min().getY());
        textRenderer.draw(matrices, yMaxText, (this.backgroundWidth - textRenderer.getWidth(yMaxText)) / 2f + 10 + BUTTON_WIDTH, 15 + 23, 0x404040);
        textRenderer.draw(matrices, yMinText, (this.backgroundWidth - textRenderer.getWidth(yMinText)) / 2f - 10 - BUTTON_WIDTH, 15 + 23, 0x404040);
    }

    @Override
    public void init() {
        super.init();
        final int tp = 15;
        final int middle = x + this.backgroundWidth / 2;
        this.addDrawableChild(new ButtonWidget(middle - BUTTON_WIDTH / 2, y + tp, BUTTON_WIDTH, 20, new LiteralText("+"), this));
        this.addDrawableChild(new ButtonWidget(middle - BUTTON_WIDTH / 2, y + tp + 33, BUTTON_WIDTH, 20, new LiteralText("-"), this));
        this.addDrawableChild(new ButtonWidget(middle + BUTTON_WIDTH / 2 + 10, y + tp, BUTTON_WIDTH, 20, new LiteralText("Top+"), this));
        this.addDrawableChild(new ButtonWidget(middle + BUTTON_WIDTH / 2 + 10, y + tp + 33, BUTTON_WIDTH, 20, new LiteralText("Top-"), this));
        this.addDrawableChild(new ButtonWidget(middle - BUTTON_WIDTH / 2 - 10 - BUTTON_WIDTH, y + tp, BUTTON_WIDTH, 20, new LiteralText("Bottom+"), this));
        this.addDrawableChild(new ButtonWidget(middle - BUTTON_WIDTH / 2 - 10 - BUTTON_WIDTH, y + tp + 33, BUTTON_WIDTH, 20, new LiteralText("Bottom-"), this));

    }

    @Override
    public void onPress(ButtonWidget button) {
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
        PacketHandler.sendToServer(new Marker16Message(marker.getWorld(), marker.getPos(), size, yMax, yMin));
    }
}
