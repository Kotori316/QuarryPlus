package com.yogpc.qp.machine.advquarry;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.misc.IndexedButton;
import com.yogpc.qp.machine.misc.SmallCheckBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.ChunkPos;

public class AdvQuarryScreen extends AbstractContainerScreen<AdvQuarryContainer> implements Button.OnPress {
    private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "textures/gui/adv_quarry.png");
    private SmallCheckBox areaFrameCheckBox;
    private SmallCheckBox chunkByChunkCheckBox;
    private SmallCheckBox startCheckBox;

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
            double north = chunkPos.getMinBlockZ() - range.minZ() - 1;
            double south = range.maxZ() - chunkPos.getMaxBlockZ() - 1;
            double east = range.maxX() - chunkPos.getMaxBlockX() - 1;
            double west = chunkPos.getMinBlockX() - range.minX() - 1;
            graphics.drawString(this.font, String.valueOf(north / 16), 77, 18, 0x404040, false);
            graphics.drawString(this.font, String.valueOf(south / 16), 77, 62, 0x404040, false);
            graphics.drawString(this.font, String.valueOf(west / 16), 24, 40, 0x404040, false);
            graphics.drawString(this.font, String.valueOf(east / 16), 131, 40, 0x404040, false);
        }
    }

    @Override
    protected void init() {
        super.init();
        var plus = Component.literal("+");
        var minus = Component.literal("-");
        this.addRenderableWidget(new IndexedButton(0, leftPos + 99, topPos + 17, 12, 8, plus, this));
        this.addRenderableWidget(new IndexedButton(1, leftPos + 64, topPos + 17, 12, 8, minus, this));
        this.addRenderableWidget(new IndexedButton(2, leftPos + 99, topPos + 61, 12, 8, plus, this));
        this.addRenderableWidget(new IndexedButton(3, leftPos + 64, topPos + 61, 12, 8, minus, this));
        this.addRenderableWidget(new IndexedButton(4, leftPos + 46, topPos + 39, 12, 8, plus, this));
        this.addRenderableWidget(new IndexedButton(5, leftPos + 11, topPos + 39, 12, 8, minus, this));
        this.addRenderableWidget(new IndexedButton(6, leftPos + 153, topPos + 39, 12, 8, plus, this));
        this.addRenderableWidget(new IndexedButton(7, leftPos + 118, topPos + 39, 12, 8, minus, this));

        this.addRenderableWidget(new IndexedButton(8, leftPos + 118, topPos + 58, 50, 12, Component.literal("Start"), this));
        if (!PlatformAccess.getAccess().platformName().equalsIgnoreCase("fabric")) {
            this.addRenderableWidget(new IndexedButton(9, leftPos + 8, topPos + 58, 50, 12, Component.literal("Modules"), this::openModuleOnPress));
        }

        areaFrameCheckBox = new SmallCheckBox(leftPos + 8, topPos + 72, 100, 10, 10, 10,
            Component.literal("Area Frame"), getMenu().quarry.workConfig.placeAreaFrame(), this);
        this.addRenderableWidget(areaFrameCheckBox);
        chunkByChunkCheckBox = new SmallCheckBox(leftPos + 8, topPos + 83, 100, 10, 10, 10,
            Component.literal("Chunk by Chunk"), getMenu().quarry.workConfig.chunkByChunk(), this);
        this.addRenderableWidget(chunkByChunkCheckBox);
        startCheckBox = new SmallCheckBox(leftPos + 8, topPos + 94, 100, 10, 10, 10,
            Component.literal("Ready to Start"), getMenu().quarry.workConfig.startImmediately(), this::startQuarryOnPress);
        this.addRenderableWidget(startCheckBox);
    }

    private void startQuarryOnPress(Button b) {
        var quarry = getMenu().quarry;
        if (quarry.currentState == AdvQuarryState.WAITING) {
            quarry.workConfig = quarry.workConfig.startSoonConfig();
            startCheckBox.setSelected(quarry.workConfig.startImmediately());
            PlatformAccess.getAccess().packetHandler().sendToServer(new AdvActionActionMessage(quarry, AdvActionActionMessage.Action.QUICK_START));
        }
    }

    private void openModuleOnPress(Button b) {
        var quarry = getMenu().quarry;
        PlatformAccess.getAccess().packetHandler().sendToServer(new AdvActionActionMessage(quarry, AdvActionActionMessage.Action.MODULE_INV));
    }

    @Override
    public void onPress(Button b) {
        /*var tile = getMenu().quarry;
        if (b instanceof IndexedButton button) {
            if (tile.getAction() == AdvQuarryAction.Waiting.WAITING) {
                var direction = Direction.from3DDataValue(button.id() / 2 + 2);
                var increase = button.id() % 2 == 0 ? 1 : -1;
                var shift = Screen.hasShiftDown();
                var ctrl = Screen.hasControlDown();
                int t;
                if (shift && ctrl) t = 1024 * increase;
                else if (shift) t = 256 * increase;
                else if (ctrl) t = 64 * increase;
                else t = 16 * increase;

                var range = tile.getArea();
                if (range != null) {
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
                        default -> tile.getArea();
                    };
                    PacketHandler.sendToServer(new AdvActionSyncMessage(tile, AdvActionSyncMessage.Actions.CHANGE_RANGE, newRange));
                }
            }
        }
        if (b instanceof SmallCheckBox) {
            boolean placeAreaFrame = areaFrameCheckBox.isSelected();
            boolean chunkByChunk = chunkByChunkCheckBox.isSelected();
            boolean startImmediately = startCheckBox.isSelected();
            WorkConfig workConfig = new WorkConfig(startImmediately, placeAreaFrame, chunkByChunk);
            tile.workConfig = workConfig;
            PacketHandler.sendToServer(new AdvActionSyncMessage(tile, AdvActionSyncMessage.Actions.SYNC, workConfig));
        }*/
    }

}
