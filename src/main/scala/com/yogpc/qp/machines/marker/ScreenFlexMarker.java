package com.yogpc.qp.machines.marker;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.misc.IndexedButton;
import com.yogpc.qp.packet.FlexMarkerMessage;
import com.yogpc.qp.packet.PacketHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

@Environment(EnvType.CLIENT)
public class ScreenFlexMarker extends AbstractContainerScreen<ContainerMarker> implements Button.OnPress {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/flex_marker.png");
    private static final int upSide = 1;
    private static final int center = 3;
    private static final int downSide = 1;
    private static final int[] amounts = {-16, -1, 1, 16};
    private final TileFlexMarker marker;
    private static final int yOffsetCenter = 45;
    private static final int yOffsetBottom = 90;

    public ScreenFlexMarker(ContainerMarker containerMarker, Inventory inv, Component component) {
        super(containerMarker, inv, component);
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
        int w = Stream.of(mp).mapToInt(font::width).max().orElseThrow();
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
                    this.leftPos + imageWidth / 2 - 4 * w * center / 2 + i * w * mp.length + w * j, this.topPos + top + yOffsetCenter, w, h, mp[j], this));
            }
        }
        for (int i = 0; i < downSide; i++) {
            for (int j = 0; j < mp.length; j++) {
                addRenderableWidget(new IndexedButton(counter.getAndIncrement(),
                    this.leftPos + imageWidth / 2 - 4 * w * downSide / 2 + w * j, this.topPos + top + yOffsetBottom, w, h, mp[j], this));
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics);
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
        Component s = Component.translatable(TileFlexMarker.Movable.UP.transName);
        graphics.drawString(this.font, s, (this.imageWidth - font.width(s)) / 2, 6, 0x404040, false);
        s = Component.translatable(TileFlexMarker.Movable.FORWARD.transName);
        graphics.drawString(this.font, s, (this.imageWidth - font.width(s)) / 2, 6 + yOffsetCenter, 0x404040, false);
        s = Component.translatable(TileFlexMarker.Movable.LEFT.transName);
        graphics.drawString(this.font, s, (this.imageWidth - font.width(s)) / 2 - 40, 6 + yOffsetCenter, 0x404040, false);
        s = Component.translatable(TileFlexMarker.Movable.RIGHT.transName);
        graphics.drawString(this.font, s, (this.imageWidth - font.width(s)) / 2 + 40, 6 + yOffsetCenter, 0x404040, false);
        s = Component.translatable(TileFlexMarker.Movable.DOWN.transName);
        graphics.drawString(this.font, s, (this.imageWidth - font.width(s)) / 2, 6 + yOffsetBottom, 0x404040, false);

        marker.getArea().ifPresent(area -> {
            var start = "(%d, %d, %d)".formatted(area.minX(), area.minY(), area.minZ());
            var end = "(%d, %d, %d)".formatted(area.maxX(), area.maxY(), area.maxZ());
            var x = this.imageWidth - Math.max(font.width(start), font.width(end)) - 10;
            graphics.drawString(this.font, start, x, 6 + yOffsetBottom + 5, 0x404040, false);
            graphics.drawString(this.font, end, x, 6 + yOffsetBottom + 15, 0x404040, false);

            var minPos = new BlockPos(area.minX(), area.minY(), area.minZ());
            var maxPos = new BlockPos(area.maxX(), area.maxY(), area.maxZ());
            String distanceUp = String.valueOf(TileFlexMarker.Movable.UP.distanceFromOrigin(marker.getBlockPos(), minPos, maxPos, marker.direction));
            String distanceLeft = String.valueOf(TileFlexMarker.Movable.LEFT.distanceFromOrigin(marker.getBlockPos(), minPos, maxPos, marker.direction));
            String distanceForward = String.valueOf(TileFlexMarker.Movable.FORWARD.distanceFromOrigin(marker.getBlockPos(), minPos, maxPos, marker.direction));
            String distanceRight = String.valueOf(TileFlexMarker.Movable.RIGHT.distanceFromOrigin(marker.getBlockPos(), minPos, maxPos, marker.direction));
            String distanceDown = String.valueOf(TileFlexMarker.Movable.DOWN.distanceFromOrigin(marker.getBlockPos(), minPos, maxPos, marker.direction));
            graphics.drawString(this.font, distanceUp, (this.imageWidth - font.width(distanceUp)) / 2, 6 + 32, 0x404040, false);
            graphics.drawString(this.font, distanceLeft, (this.imageWidth - font.width(distanceLeft)) / 2 - 40, 6 + 32 + yOffsetCenter, 0x404040, false);
            graphics.drawString(this.font, distanceForward, (this.imageWidth - font.width(distanceForward)) / 2, 6 + 32 + yOffsetCenter, 0x404040, false);
            graphics.drawString(this.font, distanceRight, (this.imageWidth - font.width(distanceRight)) / 2 + 40, 6 + 32 + yOffsetCenter, 0x404040, false);
            graphics.drawString(this.font, distanceDown, (this.imageWidth - font.width(distanceDown)) / 2, 6 + 32 + yOffsetBottom, 0x404040, false);
        });
    }

    @Override
    public void onPress(Button button) {
        if (!(button instanceof IndexedButton ib)) return;
        int id = ib.id();
        if (id >= 0) {
            TileFlexMarker.Movable movable = TileFlexMarker.Movable.valueOf(id / 4);
            FlexMarkerMessage message = new FlexMarkerMessage(getMenu().player.level(), getMenu().pos, movable, amounts[id % 4]);
            PacketHandler.sendToServer(message);
        }
    }
}
