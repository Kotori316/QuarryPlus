package com.yogpc.qp.machines.marker;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.misc.IndexedButton;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
public class ScreenFlexMarker extends AbstractContainerScreen<ContainerMarker> {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/flex_marker.png");
    private final TileFlexMarker marker;
    private static final int yOffsetCenter = 35;
    private static final int yOffsetLR = 55;
    private static final int yOffsetBottom = 78;
    private static final int buttonWidth = 20;
    private static final int buttonHeight = 14;

    public ScreenFlexMarker(ContainerMarker containerMarker, Inventory inventory, Component component) {
        super(containerMarker, inventory, component);
        //217, 188
        this.imageWidth = 217;
        this.imageHeight = 220;
        this.inventoryLabelY = this.imageHeight - 96 + 2; // y position of text, inventory
        marker = (TileFlexMarker) containerMarker.player.level().getBlockEntity(containerMarker.pos);
    }

    @Override
    public void init() {
        super.init();
        Component[] mp = Stream.of("--", "-", "+", "++").map(Component::literal).toArray(Component[]::new);
        int w = buttonWidth;
        int h = buttonHeight;
        int top = 16;
        AtomicInteger buttonIndex = new AtomicInteger(0);

        // UP
        for (int j = 0; j < mp.length; j++) {
            addRenderableWidget(Button.builder(mp[j], this::actionPerformed)
                .pos(this.getGuiLeft() + imageWidth / 2 + (j - mp.length / 2) * w, this.getGuiTop() + top)
                .size(w, h)
                .build(IndexedButton.builder(buttonIndex.getAndIncrement())));
        }
        // Left
        for (int j = 0; j < mp.length; j++) {
            addRenderableWidget(Button.builder(mp[j], this::actionPerformed)
                .pos(this.getGuiLeft() + 8 + j * w, this.getGuiTop() + top + yOffsetLR)
                .size(w, h)
                .build(IndexedButton.builder(buttonIndex.getAndIncrement())));
        }
        // Center
        for (int j = 0; j < mp.length; j++) {
            addRenderableWidget(Button.builder(mp[j], this::actionPerformed)
                .pos(this.getGuiLeft() + imageWidth / 2 + (j - mp.length / 2) * w, this.getGuiTop() + top + yOffsetCenter)
                .size(w, h)
                .build(IndexedButton.builder(buttonIndex.getAndIncrement())));
        }
        // Right
        for (int j = 0; j < mp.length; j++) {
            addRenderableWidget(Button.builder(mp[j], this::actionPerformed)
                .pos(this.getGuiLeft() + imageWidth - 8 + (j - mp.length) * w, this.getGuiTop() + top + yOffsetLR)
                .size(w, h)
                .build(IndexedButton.builder(buttonIndex.getAndIncrement())));
        }
        // Down
        for (int j = 0; j < mp.length; j++) {
            addRenderableWidget(Button.builder(mp[j], this::actionPerformed)
                .pos(this.getGuiLeft() + imageWidth / 2 + (j - mp.length / 2) * w, this.getGuiTop() + top + yOffsetBottom)
                .size(w, h)
                .build(IndexedButton.builder(buttonIndex.getAndIncrement())));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        int pX = getGuiLeft();
        int pY = getGuiTop();
        graphics.blit(LOCATION, pX, pY, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        var s = Component.translatable(TileFlexMarker.Movable.UP.transName);
        graphics.drawString(font, s, (this.imageWidth - font.width(s)) / 2, 6, 0x404040, false);
        s = Component.translatable(TileFlexMarker.Movable.FORWARD.transName);
        graphics.drawString(font, s, (this.imageWidth - font.width(s)) / 2, 6 + yOffsetCenter, 0x404040, false);
        s = Component.translatable(TileFlexMarker.Movable.LEFT.transName);
        graphics.drawString(font, s, 8 + buttonWidth * 2 - font.width(s) / 2, 6 + yOffsetLR, 0x404040, false);
        s = Component.translatable(TileFlexMarker.Movable.RIGHT.transName);
        graphics.drawString(font, s, imageWidth - 8 - buttonWidth * 2 - font.width(s) / 2, 6 + yOffsetLR, 0x404040, false);
        s = Component.translatable(TileFlexMarker.Movable.DOWN.transName);
        graphics.drawString(font, s, (this.imageWidth - font.width(s)) / 2, 6 + yOffsetBottom, 0x404040, false);

        marker.getArea().ifPresent(area -> {
            var start = "(%d, %d, %d)".formatted(area.minX(), area.minY(), area.minZ());
            var end = "(%d, %d, %d)".formatted(area.maxX(), area.maxY(), area.maxZ());
            var x = this.imageWidth - Math.max(font.width(start), font.width(end)) - 10;
            var heightOffset = buttonHeight + 12;
            graphics.drawString(font, start, x, 6 + heightOffset + yOffsetBottom, 0x404040, false);
            graphics.drawString(font, end, x, 6 + heightOffset + yOffsetBottom + 10, 0x404040, false);

            var minPos = new BlockPos(area.minX(), area.minY(), area.minZ());
            var maxPos = new BlockPos(area.maxX(), area.maxY(), area.maxZ());
            String distanceUp = String.valueOf(TileFlexMarker.Movable.UP.distanceFromOrigin(marker.getBlockPos(), minPos, maxPos, marker.direction));
            String distanceLeft = String.valueOf(TileFlexMarker.Movable.LEFT.distanceFromOrigin(marker.getBlockPos(), minPos, maxPos, marker.direction));
            String distanceForward = String.valueOf(TileFlexMarker.Movable.FORWARD.distanceFromOrigin(marker.getBlockPos(), minPos, maxPos, marker.direction));
            String distanceRight = String.valueOf(TileFlexMarker.Movable.RIGHT.distanceFromOrigin(marker.getBlockPos(), minPos, maxPos, marker.direction));
            String distanceDown = String.valueOf(TileFlexMarker.Movable.DOWN.distanceFromOrigin(marker.getBlockPos(), minPos, maxPos, marker.direction));
            graphics.drawString(font, distanceUp, (this.imageWidth - font.width(distanceUp)) / 2, 6 + heightOffset, 0x404040, false);
            graphics.drawString(font, distanceLeft, 8 + buttonWidth * 2 - font.width(distanceLeft) / 2, 6 + heightOffset + yOffsetLR, 0x404040, false);
            graphics.drawString(font, distanceForward, (this.imageWidth - font.width(distanceForward)) / 2, 6 + heightOffset + yOffsetCenter, 0x404040, false);
            graphics.drawString(font, distanceRight, imageWidth - 8 - buttonWidth * 2 - font.width(distanceRight) / 2, 6 + heightOffset + yOffsetLR, 0x404040, false);
            graphics.drawString(font, distanceDown, (this.imageWidth - font.width(distanceDown)) / 2, 6 + heightOffset + yOffsetBottom, 0x404040, false);
        });
    }

    public void actionPerformed(Button button) {
        if (button instanceof IndexedButton indexedButton) {
            int id = indexedButton.id();
            if (id >= 0) {
                final int[] amounts = {-16, -1, 1, 16};
                int amount = amounts[id % 4];
                TileFlexMarker.Movable movable = TileFlexMarker.Movable.valueOf(id / 4);
                FlexMarkerMessage message = new FlexMarkerMessage(getMenu().player.level(), getMenu().pos, movable, amount);
                PacketHandler.sendToServer(message);
            }
        }
    }
}
