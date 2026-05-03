package com.yogpc.qp.machine.marker;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public final class FlexibleMarkerScreen extends AbstractContainerScreen<MarkerContainer> {
    private static final Identifier LOCATION = Identifier.fromNamespaceAndPath(QuarryPlus.modID, "textures/gui/flex_marker.png");
    private final FlexibleMarkerEntity marker;
    private static final int yOffsetCenter = 35;
    private static final int yOffsetLR = 55;
    private static final int yOffsetBottom = 78;
    private static final int buttonWidth = 20;
    private static final int buttonHeight = 14;
    private final List<MovablePosition> movablePositions = new ArrayList<>();
    private final List<DiffPosition> diffPositions = new ArrayList<>();

    public FlexibleMarkerScreen(MarkerContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 217, 220);
        this.inventoryLabelY = this.imageHeight - 96 + 2;
        marker = (FlexibleMarkerEntity) playerInventory.player.level().getBlockEntity(menu.pos);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        int pX = leftPos;
        int pY = topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, LOCATION, pX, pY, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void init() {
        super.init();
        movablePositions.add(new MovablePosition(FlexibleMarkerEntity.Movable.UP, this.imageWidth / 2, 6));
        movablePositions.add(new MovablePosition(FlexibleMarkerEntity.Movable.FORWARD, this.imageWidth / 2, 6 + yOffsetCenter));
        movablePositions.add(new MovablePosition(FlexibleMarkerEntity.Movable.LEFT, 8 + buttonWidth * 2, 6 + yOffsetLR));
        movablePositions.add(new MovablePosition(FlexibleMarkerEntity.Movable.RIGHT, this.imageWidth - 8 - buttonWidth * 2, 6 + yOffsetLR));
        movablePositions.add(new MovablePosition(FlexibleMarkerEntity.Movable.DOWN, this.imageWidth / 2, 6 + yOffsetBottom));

        diffPositions.add(new DiffPosition(Component.literal("--"), -buttonWidth * 2, 0, -16));
        diffPositions.add(new DiffPosition(Component.literal("-"), -buttonWidth, 0, -1));
        diffPositions.add(new DiffPosition(Component.literal("+"), +0, 0, +1));
        diffPositions.add(new DiffPosition(Component.literal("++"), buttonWidth, 0, +16));

        for (MovablePosition movablePosition : movablePositions) {
            for (DiffPosition diffPosition : diffPositions) {
                addRenderableWidget(
                    Button.builder(diffPosition.title, onPress(movablePosition, diffPosition.amount))
                        .pos(leftPos + movablePosition.baseX + diffPosition.x, topPos + movablePosition.baseY + diffPosition.y + 10)
                        .size(buttonWidth, buttonHeight)
                        .tooltip(Tooltip.create(Component.translatable(movablePosition.movable.transName).append(String.format(" %+d", diffPosition.amount))))
                        .build()
                );
            }
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractLabels(guiGraphics, mouseX, mouseY);
        final var heightOffset = buttonHeight + 12;

        for (var movablePosition : movablePositions) {
            {
                // Label
                var text = Component.translatable(movablePosition.movable.transName);
                var textLength = font.width(text);
                guiGraphics.text(font, text, movablePosition.baseX - textLength / 2, movablePosition.baseY, ARGB.opaque(0x404040), false);
            }
            {
                // distance
                int distance = movablePosition.movable.distanceFromOrigin(marker.getBlockPos(), marker.min, marker.max, marker.direction);
                var text = String.valueOf(distance);
                var textLength = font.width(text);
                guiGraphics.text(font, text, movablePosition.baseX - textLength / 2, movablePosition.baseY + heightOffset, ARGB.opaque(0x404040), false);
            }
        }
    }

    private record MovablePosition(FlexibleMarkerEntity.Movable movable, int baseX, int baseY) {
    }

    private record DiffPosition(Component title, int x, int y, int amount) {
    }

    Button.OnPress onPress(MovablePosition movablePosition, int amount) {
        return button ->
            PlatformAccess.getAccess().packetHandler().sendToServer(new FlexibleMarkerMessage(marker, movablePosition.movable, amount));
    }
}
