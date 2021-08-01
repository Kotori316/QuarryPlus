package com.yogpc.qp.machines.marker;

import java.util.stream.Stream;

import com.mojang.blaze3d.systems.RenderSystem;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.FlexMarkerMessage;
import com.yogpc.qp.packet.PacketHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ScreenFlexMarker extends HandledScreen<ContainerMarker> {
    private static final Identifier LOCATION = new Identifier(QuarryPlus.modID, "textures/gui/marker.png");
    private static final int upSide = 1;
    private static final int center = 3;
    private static final int downSide = 1;
    private static final int[] amounts = {-16, -1, 1, 16};

    public ScreenFlexMarker(ContainerMarker containerMarker, PlayerInventory inv, Text component) {
        super(containerMarker, inv, component);
        //217, 188
        this.backgroundWidth = 217;
        this.backgroundHeight = 188;
        this.playerInventoryTitleY = this.backgroundHeight - 96 + 2; // y position of text, inventory
    }

    @Override
    public void init() {
        super.init();
        LiteralText[] mp = Stream.of("--", "-", "+", "++").map(LiteralText::new).toArray(LiteralText[]::new);
        int w = 10;
        int h = 20;
        int top = 16;

        for (int i = 0; i < upSide; i++) {
            for (int j = 0; j < mp.length; j++) {
                addDrawableChild(new ButtonWidget(this.x + backgroundWidth / 2 - 4 * w * upSide / 2 + w * j, this.y + top, w, h, mp[j], this::actionPerformed));
            }
        }
        for (int i = 0; i < center; i++) {
            for (int j = 0; j < mp.length; j++) {
                addDrawableChild(new ButtonWidget(this.x + backgroundWidth / 2 - 4 * w * center / 2 + i * w * mp.length + w * j, this.y + top + 35, w, h, mp[j], this::actionPerformed));
            }
        }
        for (int i = 0; i < downSide; i++) {
            for (int j = 0; j < mp.length; j++) {
                addDrawableChild(new ButtonWidget(this.x + backgroundWidth / 2 - 4 * w * downSide / 2 + w * j, this.y + top + 70, w, h, mp[j], this::actionPerformed));
            }
        }
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
        var s = new TranslatableText(TileFlexMarker.Movable.UP.transName);
        this.textRenderer.draw(matrices, s, ((float) this.backgroundWidth - textRenderer.getWidth(s)) / 2, 6, 0x404040);
        s = new TranslatableText(TileFlexMarker.Movable.FORWARD.transName);
        this.textRenderer.draw(matrices, s, ((float) this.backgroundWidth - textRenderer.getWidth(s)) / 2, 6 + 35, 0x404040);
        s = new TranslatableText(TileFlexMarker.Movable.LEFT.transName);
        this.textRenderer.draw(matrices, s, ((float) this.backgroundWidth - textRenderer.getWidth(s)) / 2 - 40, 6 + 35, 0x404040);
        s = new TranslatableText(TileFlexMarker.Movable.RIGHT.transName);
        this.textRenderer.draw(matrices, s, ((float) this.backgroundWidth - textRenderer.getWidth(s)) / 2 + 40, 6 + 35, 0x404040);
        s = new TranslatableText(TileFlexMarker.Movable.DOWN.transName);
        this.textRenderer.draw(matrices, s, ((float) this.backgroundWidth - textRenderer.getWidth(s)) / 2, 6 + 70, 0x404040);
    }

    public void actionPerformed(ButtonWidget button) {
        int id = super.children().indexOf(button);
        if (id >= 0) {
            TileFlexMarker.Movable movable = TileFlexMarker.Movable.valueOf(id / 4);
            FlexMarkerMessage message = new FlexMarkerMessage(getScreenHandler().player.world, getScreenHandler().pos, movable, amounts[id % 4]);
            PacketHandler.sendToServer(message);
        }
    }
}
