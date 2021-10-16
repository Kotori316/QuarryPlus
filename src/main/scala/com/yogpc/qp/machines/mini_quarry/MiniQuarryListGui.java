package com.yogpc.qp.machines.mini_quarry;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.machines.misc.IndexedButton;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class MiniQuarryListGui extends Screen implements Button.OnPress {
    final List<BlockStatePredicate> whiteList;
    final List<BlockStatePredicate> blackList;
    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    EntryList list;
    boolean whiteListFlag = true;

    public MiniQuarryListGui(MiniQuarryTile tile, Collection<BlockStatePredicate> whiteList, Collection<BlockStatePredicate> blackList) {
        super(tile.getDisplayName());
        this.whiteList = whiteList.stream().sorted(Comparator.comparing(Object::toString)).collect(Collectors.toList());
        this.blackList = blackList.stream().sorted(Comparator.comparing(Object::toString)).collect(Collectors.toList());
        pos = tile.getBlockPos();
        dim = PacketHandler.getDimension(tile);
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = 80;
        int width = this.width;
        int height = this.height;
        addRenderableWidget(new IndexedButton(0, width / 3 - buttonWidth / 2, height - 35, buttonWidth, 20, new TranslatableComponent("quarryplus.gui.blacklist"), this));
        addRenderableWidget(new IndexedButton(1, width / 3 * 2 - buttonWidth / 2, height - 35, buttonWidth, 20, new TranslatableComponent("gui.done"), this));
        addRenderableWidget(new IndexedButton(2, width / 2 - buttonWidth, height - 60, buttonWidth, 20, new TranslatableComponent("quarryplus.gui.new_entry"), this));
        addRenderableWidget(new IndexedButton(3, width / 2, height - 60, buttonWidth, 20, new TranslatableComponent("selectWorld.delete"), this));
        list = new EntryList(this.getMinecraft(), width, height, 30, height - 70, 18, this, this::getEntries);
        addRenderableWidget(list); // Add?
        this.setInitialFocus(list); // setFocus
    }

    @Override
    public void render(PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        list.render(matrixStack, mouseX, mouseY, partialTicks);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        TranslatableComponent listName = new TranslatableComponent(whiteListFlag ? "quarryplus.gui.whitelist" : "quarryplus.gui.blacklist");
        TranslatableComponent title = new TranslatableComponent("quarryplus.gui.of", listName, super.getTitle().getString());
        drawCenteredString(matrixStack, this.font, title, this.width / 2, 8, 0xFFFFFF);
    }

    public List<BlockStatePredicate> getEntries() {
        if (whiteListFlag)
            return whiteList;
        else
            return blackList;
    }

    public void setWhiteListFlag(boolean whiteListFlag) {
        this.whiteListFlag = whiteListFlag;
        list.updateList();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
        if (this.getMinecraft().options.keyInventory.isActiveAndMatches(mouseKey)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onPress(Button button) {
        if (button instanceof IndexedButton indexedButton) {
            switch (indexedButton.id()) {
                case 0: // Change list
                    setWhiteListFlag(!whiteListFlag);
                    button.setMessage(new TranslatableComponent(whiteListFlag ? "quarryplus.gui.whitelist" : "quarryplus.gui.blacklist"));
                    break;
                case 1:
                    onClose();
                    break;
                case 2: // New Entry
                    getMinecraft().pushGuiLayer(new MiniQuarryAddEntryGui(this,
                        e -> {
                            getEntries().add(e);
                            list.updateList();
                            PacketHandler.sendToServer(new MiniListSyncMessage(pos, dim, blackList, whiteList));
                        }));
                    break;
                case 3: // Delete
                    MiniQuarryListEntry selected = list.getSelected(); // getSelected
                    if (selected != null) {
                        BlockStatePredicate data = selected.getData();
                        if (!MiniQuarryTile.defaultBlackList().contains(data)) {
                            getEntries().remove(data);
                            list.updateList();
                            PacketHandler.sendToServer(new MiniListSyncMessage(pos, dim, blackList, whiteList));
                        }
                    }
                    break;
            }
        }
    }

    private static class EntryList extends ObjectSelectionList<MiniQuarryListEntry> {

        private final Screen parent;
        private final Supplier<List<BlockStatePredicate>> entriesSupplier;

        public EntryList(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn, Screen parent, Supplier<List<BlockStatePredicate>> entriesSupplier) {
            super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
            this.parent = parent;
            this.entriesSupplier = entriesSupplier;
            updateList();
        }

        public void updateList() {
            this.clearEntries(); // clear
            entriesSupplier.get().stream().map(e -> new MiniQuarryListEntry(e, this.parent, this::setSelected)).forEach(this::addEntry); // addEntry
        }

    }

    private static class MiniQuarryListEntry extends ObjectSelectionList.Entry<MiniQuarryListEntry> {

        private final BlockStatePredicate data;
        private final Screen parent;
        private final Consumer<MiniQuarryListEntry> setSelected;

        MiniQuarryListEntry(BlockStatePredicate data, Screen parent, Consumer<MiniQuarryListEntry> setSelected) {
            this.data = data;
            this.parent = parent;
            this.setSelected = setSelected;
        }

        public BlockStatePredicate getData() {
            return data;
        }

        @Override
        @SuppressWarnings("IntegerDivisionInFloatingPointContext")
        public void render(PoseStack m, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_render_8_, float partialTicks) {
            //render
            String name = data.toString();
            Minecraft.getInstance().font.draw(m, name,
                (parent.width - Minecraft.getInstance().font.width(name)) / 2, top + 2, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            // Mouse clicked
            setSelected.accept(this);
            return false;
        }

        @Override
        public Component getNarration() {
            return new TranslatableComponent("narrator.select", data);
        }
    }
}


