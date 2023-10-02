package com.yogpc.qp.machines.advquarry;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.misc.IndexedButton;
import com.yogpc.qp.machines.misc.SmallCheckBox;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.ChunkPos;

public class AdvQuarryScreen extends AbstractContainerScreen<AdvQuarryMenu> implements Button.OnPress {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/adv_quarry.png");
    private SmallCheckBox areaFrameCheckBox;
    private SmallCheckBox chunkByChunkCheckBox;
    private SmallCheckBox startCheckBox;

    public AdvQuarryScreen(AdvQuarryMenu c, Inventory inventory, Component component) {
        super(c, inventory, component);
        this.imageWidth = c.imageWidth;
        this.imageHeight = c.imageHeight;
        this.inventoryLabelY = this.imageHeight - 96 + 2; // y position of text, inventory
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
        this.addRenderableWidget(new IndexedButton(0, getGuiLeft() + 99, getGuiTop() + 17, 12, 8, plus, this));
        this.addRenderableWidget(new IndexedButton(1, getGuiLeft() + 64, getGuiTop() + 17, 12, 8, minus, this));
        this.addRenderableWidget(new IndexedButton(2, getGuiLeft() + 99, getGuiTop() + 61, 12, 8, plus, this));
        this.addRenderableWidget(new IndexedButton(3, getGuiLeft() + 64, getGuiTop() + 61, 12, 8, minus, this));
        this.addRenderableWidget(new IndexedButton(4, getGuiLeft() + 46, getGuiTop() + 39, 12, 8, plus, this));
        this.addRenderableWidget(new IndexedButton(5, getGuiLeft() + 11, getGuiTop() + 39, 12, 8, minus, this));
        this.addRenderableWidget(new IndexedButton(6, getGuiLeft() + 153, getGuiTop() + 39, 12, 8, plus, this));
        this.addRenderableWidget(new IndexedButton(7, getGuiLeft() + 118, getGuiTop() + 39, 12, 8, minus, this));

        this.addRenderableWidget(new IndexedButton(8, getGuiLeft() + 118, getGuiTop() + 58, 50, 12, Component.literal("Start"), this));
        this.addRenderableWidget(new IndexedButton(9, getGuiLeft() + 8, getGuiTop() + 58, 50, 12, Component.literal("Modules"), this));

        areaFrameCheckBox = new SmallCheckBox(getGuiLeft() + 8, getGuiTop() + 72, 60, 10, 10, 10,
            Component.literal("Area Frame"), getMenu().quarry.workConfig.placeAreaFrame(), this);
        this.addRenderableWidget(areaFrameCheckBox);
        chunkByChunkCheckBox = new SmallCheckBox(getGuiLeft() + 8, getGuiTop() + 83, 60, 10, 10, 10,
            Component.literal("Chunk by Chunk"), getMenu().quarry.workConfig.chunkByChunk(), this);
        this.addRenderableWidget(chunkByChunkCheckBox);
        startCheckBox = new SmallCheckBox(getGuiLeft() + 8, getGuiTop() + 94, 60, 10, 10, 10,
            Component.literal("Ready to Start"), getMenu().quarry.workConfig.startImmediately(), this);
        this.addRenderableWidget(startCheckBox);
    }

    @Override
    public void onPress(Button b) {
        var tile = getMenu().quarry;
        if (b instanceof IndexedButton button) {
            if (button.id() == 8) {
                if (tile.getAction() == AdvQuarryAction.Waiting.WAITING) {
                    tile.workConfig = tile.workConfig.startSoonConfig();
                    startCheckBox.setSelected(tile.workConfig.startImmediately());
                    PacketHandler.sendToServer(new AdvActionMessage(tile, AdvActionMessage.Actions.QUICK_START));
                }
            } else if (button.id() == 9) {
                PacketHandler.sendToServer(new AdvActionMessage(tile, AdvActionMessage.Actions.MODULE_INV));
                //      onClose()
            } else if (tile.getAction() == AdvQuarryAction.Waiting.WAITING) {
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
                    PacketHandler.sendToServer(new AdvActionMessage(tile, AdvActionMessage.Actions.CHANGE_RANGE, newRange));
                }
            }
        }
        if (b instanceof SmallCheckBox) {
            boolean placeAreaFrame = areaFrameCheckBox.isSelected();
            boolean chunkByChunk = chunkByChunkCheckBox.isSelected();
            boolean startImmediately = startCheckBox.isSelected();
            WorkConfig workConfig = new WorkConfig(startImmediately, placeAreaFrame, chunkByChunk);
            tile.workConfig = workConfig;
            PacketHandler.sendToServer(new AdvActionMessage(tile, AdvActionMessage.Actions.SYNC, workConfig));
        }
    }

}
