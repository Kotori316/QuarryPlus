package com.yogpc.qp.machines.mini_quarry;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.yogpc.qp.machines.base.IHandleButton;
import com.yogpc.qp.machines.base.QuarryBlackList;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.mini_quarry.MiniListSyncMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.math.BlockPos;

public class MiniQuarryListGui extends Screen implements IHandleButton {
    final List<QuarryBlackList.Entry> whiteList;
    final List<QuarryBlackList.Entry> blackList;
    private final BlockPos pos;
    private final int dim;
    EntryList list;
    boolean whiteListFlag = true;

    public MiniQuarryListGui(MiniQuarryTile tile, Collection<QuarryBlackList.Entry> whiteList, Collection<QuarryBlackList.Entry> blackList) {
        super(tile.getDisplayName());
        this.whiteList = whiteList.stream().sorted(Comparator.comparing(Object::toString)).collect(Collectors.toList());
        this.blackList = blackList.stream().sorted(Comparator.comparing(Object::toString)).collect(Collectors.toList());
        pos = tile.getPos();
        dim = IMessage.getDimId(tile.getWorld());
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = 80;
        addButton(new Button(0, width / 3 - buttonWidth / 2, height - 35, buttonWidth, 20, "BlackList", this));
        addButton(new Button(1, width / 3 * 2 - buttonWidth / 2, height - 35, buttonWidth, 20, I18n.format("gui.done"), this));
        addButton(new Button(2, width / 2 - buttonWidth, height - 60, buttonWidth, 20, "New Entry", this));
        addButton(new Button(3, width / 2, height - 60, buttonWidth, 20, I18n.format("selectWorld.delete"), this));
        list = new EntryList(this.minecraft, this.width, this.height, 30, this.height - 70, 18, this, this::getEntries);
        this.children.add(list);
        this.setFocused(list);
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks) {
        list.render(mouseX, mouseY, partialTicks);
        super.render(mouseX, mouseY, partialTicks);
        String title = (whiteListFlag ? "WhiteList" : "BlackList") + " of " + super.getTitle().getFormattedText();
        drawCenteredString(this.font, title, this.width / 2, 8, 0xFFFFFF);
    }

    public List<QuarryBlackList.Entry> getEntries() {
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
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
        InputMappings.Input mouseKey = InputMappings.getInputByCode(keyCode, scanCode);
        assert this.minecraft != null;
        if (this.minecraft.gameSettings.keyBindInventory.isActiveAndMatches(mouseKey)) {
            assert this.minecraft.player != null;
            this.minecraft.player.closeScreen();
            return true; // Forge MC-146650: Needs to return true when the key is handled.
        }
        return false;
    }

    @Override
    public void actionPerformed(Button button) {
        switch (button.id) {
            case 0: // Change list
                setWhiteListFlag(!whiteListFlag);
                button.setMessage(whiteListFlag ? "BlackList" : "WhiteList");
                break;
            case 1:
                assert this.minecraft != null;
                assert this.minecraft.player != null;
                this.minecraft.player.closeScreen();
                break;
            case 2: // New Entry
                getMinecraft().displayGuiScreen(new MiniQuarryAddEntryGui(this,
                    e -> {
                        getEntries().add(e);
                        list.updateList();
                        PacketHandler.sendToServer(MiniListSyncMessage.create(pos, dim, blackList, whiteList));
                    }));
                break;
            case 3: // Delete
                MiniQuarryListEntry selected = list.getSelected();
                if (selected != null) {
                    QuarryBlackList.Entry data = selected.getData();
                    if (!MiniQuarryTile.defaultBlackList().contains(data)) {
                        getEntries().remove(data);
                        list.updateList();
                        PacketHandler.sendToServer(MiniListSyncMessage.create(pos, dim, blackList, whiteList));
                    }
                }
                break;
        }
    }
}

class EntryList extends ExtendedList<MiniQuarryListEntry> {

    private final Screen parent;
    private final Supplier<List<QuarryBlackList.Entry>> entriesSupplier;

    public EntryList(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn, Screen parent, Supplier<List<QuarryBlackList.Entry>> entriesSupplier) {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.parent = parent;
        this.entriesSupplier = entriesSupplier;
        updateList();
    }

    public void updateList() {
        this.clearEntries();
        entriesSupplier.get().stream().map(e -> new MiniQuarryListEntry(e, this.parent, this::setSelected)).forEach(this::addEntry);
    }

}

class MiniQuarryListEntry extends ExtendedList.AbstractListEntry<MiniQuarryListEntry> {

    private final QuarryBlackList.Entry data;
    private final Screen parent;
    private final Consumer<MiniQuarryListEntry> setSelected;

    MiniQuarryListEntry(QuarryBlackList.Entry data, Screen parent, Consumer<MiniQuarryListEntry> setSelected) {
        this.data = data;
        this.parent = parent;
        this.setSelected = setSelected;
    }

    public QuarryBlackList.Entry getData() {
        return data;
    }

    @Override
    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    public void render(int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_render_8_, float partialTicks) {
        String name = data.toString();
        Minecraft.getInstance().fontRenderer.drawStringWithShadow(name,
            (parent.width - Minecraft.getInstance().fontRenderer.getStringWidth(name)) / 2, top + 2, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        setSelected.accept(this);
        return false;
    }
}
