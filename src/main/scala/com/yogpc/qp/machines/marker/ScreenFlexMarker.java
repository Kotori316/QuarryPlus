package com.yogpc.qp.machines.marker;

import java.util.stream.Stream;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ScreenFlexMarker extends AbstractContainerScreen<ContainerMarker> {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/marker.png");
    private static final int upSide = 1;
    private static final int center = 3;
    private static final int downSide = 1;
    private static final int[] amounts = {-16, -1, 1, 16};
    private final TileFlexMarker marker;

    public ScreenFlexMarker(ContainerMarker containerMarker, Inventory inventory, Component component) {
        super(containerMarker, inventory, component);
        //217, 188
        this.imageWidth = 217;
        this.imageHeight = 188;
        this.inventoryLabelY = this.imageHeight - 96 + 2; // y position of text, inventory
        marker = (TileFlexMarker) containerMarker.player.getLevel().getBlockEntity(containerMarker.pos);
    }

    @Override
    public void init() {
        super.init();
        TextComponent[] mp = Stream.of("--", "-", "+", "++").map(TextComponent::new).toArray(TextComponent[]::new);
        int w = 10;
        int h = 20;
        int top = 16;

        for (int i = 0; i < upSide; i++) {
            for (int j = 0; j < mp.length; j++) {
                addRenderableWidget(new Button(this.getGuiLeft() + imageWidth / 2 - 4 * w * upSide / 2 + w * j, this.getGuiTop() + top, w, h, mp[j], this::actionPerformed));
            }
        }
        for (int i = 0; i < center; i++) {
            for (int j = 0; j < mp.length; j++) {
                addRenderableWidget(new Button(this.getGuiLeft() + imageWidth / 2 - 4 * w * center / 2 + i * w * mp.length + w * j, this.getGuiTop() + top + 35, w, h, mp[j], this::actionPerformed));
            }
        }
        for (int i = 0; i < downSide; i++) {
            for (int j = 0; j < mp.length; j++) {
                addRenderableWidget(new Button(this.getGuiLeft() + imageWidth / 2 - 4 * w * downSide / 2 + w * j, this.getGuiTop() + top + 70, w, h, mp[j], this::actionPerformed));
            }
        }
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
        this.blit(matrices, getGuiLeft(), getGuiTop(), 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(PoseStack matrices, int mouseX, int mouseY) {
        super.renderLabels(matrices, mouseX, mouseY);
        var s = new TranslatableComponent(TileFlexMarker.Movable.UP.transName);
        this.font.draw(matrices, s, ((float) this.imageWidth - font.width(s)) / 2, 6, 0x404040);
        s = new TranslatableComponent(TileFlexMarker.Movable.FORWARD.transName);
        this.font.draw(matrices, s, ((float) this.imageWidth - font.width(s)) / 2, 6 + 35, 0x404040);
        s = new TranslatableComponent(TileFlexMarker.Movable.LEFT.transName);
        this.font.draw(matrices, s, ((float) this.imageWidth - font.width(s)) / 2 - 40, 6 + 35, 0x404040);
        s = new TranslatableComponent(TileFlexMarker.Movable.RIGHT.transName);
        this.font.draw(matrices, s, ((float) this.imageWidth - font.width(s)) / 2 + 40, 6 + 35, 0x404040);
        s = new TranslatableComponent(TileFlexMarker.Movable.DOWN.transName);
        this.font.draw(matrices, s, ((float) this.imageWidth - font.width(s)) / 2, 6 + 70, 0x404040);

        marker.getArea().ifPresent(area -> {
            var start = "(%d, %d, %d)".formatted(area.minX(), area.minY(), area.minZ());
            var end = "(%d, %d, %d)".formatted(area.maxX(), area.maxY(), area.maxZ());
            var x = (float) this.imageWidth - Math.max(font.width(start), font.width(end)) - 10;
            font.draw(matrices, start, x, 6 + 75, 0x404040);
            font.draw(matrices, end, x, 6 + 85, 0x404040);
        });
    }

    public void actionPerformed(Button button) {
        int id = super.children().indexOf(button);
        if (id >= 0) {
            TileFlexMarker.Movable movable = TileFlexMarker.Movable.valueOf(id / 4);
            FlexMarkerMessage message = new FlexMarkerMessage(getMenu().player.level, getMenu().pos, movable, amounts[id % 4]);
            PacketHandler.sendToServer(message);
        }
    }
}
