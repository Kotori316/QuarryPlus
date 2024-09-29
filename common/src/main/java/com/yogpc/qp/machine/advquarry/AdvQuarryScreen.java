package com.yogpc.qp.machine.advquarry;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.Area;
import com.yogpc.qp.machine.misc.IndexedButton;
import com.yogpc.qp.machine.misc.SmallCheckBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.List;

public class AdvQuarryScreen extends AbstractContainerScreen<AdvQuarryContainer> {
    private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "textures/gui/adv_quarry.png");
    private SmallCheckBox areaFrameCheckBox;
    private SmallCheckBox chunkByChunkCheckBox;
    private SmallCheckBox startCheckBox;
    private final List<MovablePosition> movablePositions = new ArrayList<>();

    public AdvQuarryScreen(AdvQuarryContainer c, Inventory inventory, Component component) {
        super(c, inventory, component);
        this.imageWidth = c.imageWidth;
        this.imageHeight = c.imageHeight;
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
        var range = getMenu().quarry.getArea();
        if (range != null) {
            var chunkPos = new ChunkPos(getMenu().quarry.getBlockPos());
            for (MovablePosition movablePosition : movablePositions) {
                graphics.drawString(this.font, String.valueOf(movablePosition.distance(chunkPos, range) / 16), movablePosition.baseX, movablePosition.baseY, 0x404040, false);
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        movablePositions.add(new MovablePosition(Direction.NORTH, 77, 18));
        movablePositions.add(new MovablePosition(Direction.SOUTH, 77, 62));
        movablePositions.add(new MovablePosition(Direction.WEST, 24, 40));
        movablePositions.add(new MovablePosition(Direction.EAST, 131, 40));
        for (MovablePosition movablePosition : movablePositions) {
            for (DiffPosition diffPosition : DiffPosition.values()) {
                this.addRenderableWidget(
                    Button.builder(diffPosition.text, areaChange(movablePosition, diffPosition))
                        .pos(leftPos + movablePosition.buttonX(diffPosition), topPos + movablePosition.buttonY())
                        .size(12, 8)
                        .build()
                );
            }
        }

        this.addRenderableWidget(new IndexedButton(8, leftPos + 118, topPos + 58, 50, 12, Component.literal("Start"), this::startQuarryOnPress));
        if (!PlatformAccess.getAccess().platformName().equalsIgnoreCase("fabric")) {
            this.addRenderableWidget(new IndexedButton(9, leftPos + 8, topPos + 58, 50, 12, Component.literal("Modules"), this::openModuleOnPress));
        }

        areaFrameCheckBox = new SmallCheckBox(leftPos + 8, topPos + 72, 100, 10, 10, 10,
            Component.literal("Area Frame"), getMenu().quarry.workConfig.placeAreaFrame(), this::checkBoxOnPress);
        this.addRenderableWidget(areaFrameCheckBox);
        chunkByChunkCheckBox = new SmallCheckBox(leftPos + 8, topPos + 83, 100, 10, 10, 10,
            Component.literal("Chunk by Chunk"), getMenu().quarry.workConfig.chunkByChunk(), this::checkBoxOnPress);
        this.addRenderableWidget(chunkByChunkCheckBox);
        startCheckBox = new SmallCheckBox(leftPos + 8, topPos + 94, 100, 10, 10, 10,
            Component.literal("Ready to Start"), getMenu().quarry.workConfig.startImmediately(), this::checkBoxOnPress);
        this.addRenderableWidget(startCheckBox);
    }

    private void startQuarryOnPress(Button b) {
        var quarry = getMenu().quarry;
        if (quarry.currentState == AdvQuarryState.WAITING) {
            quarry.workConfig = quarry.workConfig.startSoonConfig();
            startCheckBox.setSelected(quarry.workConfig.startImmediately());
            PlatformAccess.getAccess().packetHandler().sendToServer(new AdvActionActionMessage(quarry, AdvActionActionMessage.Action.QUICK_START));
            PlatformAccess.getAccess().packetHandler().sendToServer(new AdvActionSyncMessage(quarry)); // To sync work config change
        }
    }

    private void openModuleOnPress(Button b) {
        var quarry = getMenu().quarry;
        PlatformAccess.getAccess().packetHandler().sendToServer(new AdvActionActionMessage(quarry, AdvActionActionMessage.Action.MODULE_INV));
    }

    private void checkBoxOnPress(Button b) {
        var quarry = getMenu().quarry;
        if (quarry.currentState == AdvQuarryState.WAITING) {
            boolean placeAreaFrame = areaFrameCheckBox.isSelected();
            boolean chunkByChunk = chunkByChunkCheckBox.isSelected();
            boolean startImmediately = startCheckBox.isSelected();
            quarry.workConfig = new WorkConfig(startImmediately, placeAreaFrame, chunkByChunk);
            PlatformAccess.getAccess().packetHandler().sendToServer(new AdvActionSyncMessage(quarry));
        }
    }

    private Button.OnPress areaChange(MovablePosition movablePosition, DiffPosition diffPosition) {
        return b -> {
            var quarry = getMenu().quarry;
            if (quarry.currentState != AdvQuarryState.WAITING) return;
            var direction = movablePosition.direction;
            var increase = diffPosition.sign;
            var shift = Screen.hasShiftDown();
            var ctrl = Screen.hasControlDown();
            int t;
            if (shift && ctrl) t = 1024 * increase;
            else if (shift) t = 256 * increase;
            else if (ctrl) t = 64 * increase;
            else t = 16 * increase;

            var range = quarry.getArea();
            if (range == null) return;
            Area newRange = switch (direction.getAxis()) {
                case X -> {
                    switch (direction.getAxisDirection()) {
                        case POSITIVE -> {
                            var e = range.maxX();
                            if (range.minX() < e + t)
                                yield new Area(range.minX(), range.minY(), range.minZ(), e + t, range.maxY(), range.maxZ(), range.direction());
                            else yield range;
                        }
                        case NEGATIVE -> {
                            var e = range.minX();
                            if (range.maxX() > e - t)
                                yield new Area(e - t, range.minY(), range.minZ(), range.maxX(), range.maxY(), range.maxZ(), range.direction());
                            else yield range;
                        }
                        default -> {
                            yield range;
                        }
                    }
                }
                case Z -> {
                    switch (direction.getAxisDirection()) {
                        case POSITIVE -> {
                            var e = range.maxZ();
                            if (range.minZ() < e + t)
                                yield new Area(range.minX(), range.minY(), range.minZ(), range.maxX(), range.maxY(), e + t, range.direction());
                            else yield range;
                        }
                        case NEGATIVE -> {
                            var e = range.minZ();
                            if (range.maxZ() > e - t)
                                yield new Area(range.minX(), range.minY(), e - t, range.maxX(), range.maxY(), range.maxZ(), range.direction());
                            else yield range;
                        }
                        default -> {
                            yield range;
                        }
                    }
                }
                default -> range;
            };
            quarry.setArea(newRange);
            PlatformAccess.getAccess().packetHandler().sendToServer(new AdvActionSyncMessage(quarry));
        };
    }

    /**
     * @param baseX position of text X
     * @param baseY position of text Y
     */
    private record MovablePosition(Direction direction, int baseX, int baseY) {
        int buttonX(DiffPosition diffPosition) {
            return switch (diffPosition) {
                case PLUS -> plusButtonX();
                case MINUS -> minusButtonX();
            };
        }

        int minusButtonX() {
            return baseX - 13;
        }

        int plusButtonX() {
            return baseX + 22;
        }

        int buttonY() {
            return baseY - 1;
        }

        double distance(ChunkPos quarryChunk, Area area) {
            return switch (direction) {
                case NORTH -> quarryChunk.getMinBlockZ() - area.minZ() - 1;
                case SOUTH -> area.maxZ() - quarryChunk.getMaxBlockZ() - 1;
                case WEST -> quarryChunk.getMinBlockX() - area.minX() - 1;
                case EAST -> area.maxX() - quarryChunk.getMaxBlockX() - 1;
                case null, default -> 0;
            };
        }
    }

    private enum DiffPosition {
        PLUS(Component.literal("+"), 1),
        MINUS(Component.literal("-"), -1),
        ;
        private final Component text;
        private final int sign;

        DiffPosition(Component text, int sign) {
            this.text = text;
            this.sign = sign;
        }
    }
}
