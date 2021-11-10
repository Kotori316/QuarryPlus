package com.yogpc.qp.machines.marker;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.misc.IndexedButton;
import com.yogpc.qp.packet.FlexMarkerMessage;
import com.yogpc.qp.packet.PacketHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

@Environment(EnvType.CLIENT)
public class ScreenFlexMarker extends AbstractContainerScreen<ContainerMarker> implements Button.OnPress {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/marker.png");
    private static final int upSide = 1;
    private static final int center = 3;
    private static final int downSide = 1;
    private static final int[] amounts = {-16, -1, 1, 16};

    public ScreenFlexMarker(ContainerMarker containerMarker, Inventory inv, Component component) {
        super(containerMarker, inv, component);
        //217, 188
        this.imageWidth = 217;
        this.imageHeight = 188;
        this.inventoryLabelY = this.imageHeight - 96 + 2; // y position of text, inventory
    }

    @Override
    public void init() {
        super.init();
        TextComponent[] mp = Stream.of("--", "-", "+", "++").map(TextComponent::new).toArray(TextComponent[]::new);
        int w = 10;
        int h = 20;
        int top = 16;
        AtomicInteger counter = new AtomicInteger();

        for (int i = 0; i < upSide; i++) {
            for (int j = 0; j < mp.length; j++) {
                addRenderableWidget(new IndexedButton(counter.getAndIncrement(),
                    this.leftPos + imageWidth / 2 - 4 * w * upSide / 2 + w * j, this.topPos + top, w, h, mp[j], this));
            }
        }
        for (int i = 0; i < center; i++) {
            for (int j = 0; j < mp.length; j++) {
                addRenderableWidget(new IndexedButton(counter.getAndIncrement(),
                    this.leftPos + imageWidth / 2 - 4 * w * center / 2 + i * w * mp.length + w * j, this.topPos + top + 35, w, h, mp[j], this));
            }
        }
        for (int i = 0; i < downSide; i++) {
            for (int j = 0; j < mp.length; j++) {
                addRenderableWidget(new IndexedButton(counter.getAndIncrement(),
                    this.leftPos + imageWidth / 2 - 4 * w * downSide / 2 + w * j, this.topPos + top + 70, w, h, mp[j], this));
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
        this.blit(matrices, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(PoseStack matrices, int mouseX, int mouseY) {
        TranslatableComponent s = new TranslatableComponent(TileFlexMarker.Movable.UP.transName);
        this.font.draw(matrices, s, ((float) this.imageWidth - font.width(s)) / 2, 6, 0x404040);
        s = new TranslatableComponent(TileFlexMarker.Movable.FORWARD.transName);
        this.font.draw(matrices, s, ((float) this.imageWidth - font.width(s)) / 2, 6 + 35, 0x404040);
        s = new TranslatableComponent(TileFlexMarker.Movable.LEFT.transName);
        this.font.draw(matrices, s, ((float) this.imageWidth - font.width(s)) / 2 - 40, 6 + 35, 0x404040);
        s = new TranslatableComponent(TileFlexMarker.Movable.RIGHT.transName);
        this.font.draw(matrices, s, ((float) this.imageWidth - font.width(s)) / 2 + 40, 6 + 35, 0x404040);
        s = new TranslatableComponent(TileFlexMarker.Movable.DOWN.transName);
        this.font.draw(matrices, s, ((float) this.imageWidth - font.width(s)) / 2, 6 + 70, 0x404040);
    }

    @Override
    public void onPress(Button button) {
        if (!(button instanceof IndexedButton ib)) return;
        int id = ib.id();
        if (id >= 0) {
            TileFlexMarker.Movable movable = TileFlexMarker.Movable.valueOf(id / 4);
            FlexMarkerMessage message = new FlexMarkerMessage(getMenu().player.level, getMenu().pos, movable, amounts[id % 4]);
            PacketHandler.sendToServer(message);
        }
    }
}
