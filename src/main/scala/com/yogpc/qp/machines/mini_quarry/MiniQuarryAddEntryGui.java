package com.yogpc.qp.machines.mini_quarry;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.IHandleButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

public class MiniQuarryAddEntryGui extends Screen implements IHandleButton {
    @javax.annotation.Nonnull
    private final Screen parent;
    private final Consumer<ResourceLocation> callback;
    private EntryList list;
    private TextFieldWidget textField;

    protected MiniQuarryAddEntryGui(Screen parent, Consumer<ResourceLocation> callback) {
        super(parent.getTitle());
        this.parent = parent;
        this.callback = callback;
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = 80;
        textField = new TextFieldWidget(font, this.width / 2 - 125, this.height - 56, 250, 20, "");
        list = new EntryList(this.minecraft, this.width, this.height, 30, this.height - 70, 18, this, this::getEntries);
        addButton(new Button(1, width / 2 - buttonWidth / 2, height - 35, buttonWidth, 20, I18n.format(TranslationKeys.ADD), this));
        this.children.add(list);
        this.children.add(textField);
        this.setFocused(list);
        textField.setCanLoseFocus(true);
        textField.setResponder(s -> {
            list.updateList();
            list.setScrollAmount(list.getScrollAmount());
        });
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks) {
        list.render(mouseX, mouseY, partialTicks);
        textField.render(mouseX, mouseY, partialTicks);
        super.render(mouseX, mouseY, partialTicks);
        String title = "New Entry";
        drawCenteredString(this.font, title, this.width / 2, 8, 0xFFFFFF);
    }

    private List<ResourceLocation> getEntries() {
        String filterText = textField == null ? "" : textField.getText();
        return ForgeRegistries.BLOCKS.getKeys().stream().filter(r -> r.toString().contains(filterText)).sorted().collect(Collectors.toList());
    }

    @Override
    public void actionPerformed(Button button) {
        if (button.id == 1) {
            LocationEntry entry = list.getSelected();
            if (entry != null) {
                ResourceLocation location = entry.getData();
                callback.accept(location);
                this.onClose();
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputMappings.Input mouseKey = InputMappings.getInputByCode(keyCode, scanCode);
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || (!textField.isFocused() && this.getMinecraft().gameSettings.keyBindInventory.isActiveAndMatches(mouseKey))) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        getMinecraft().displayGuiScreen(parent);
    }

    static class EntryList extends ExtendedList<LocationEntry> {

        private final Screen parent;
        private final Supplier<List<ResourceLocation>> entriesSupplier;

        public EntryList(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn, Screen parent, Supplier<List<ResourceLocation>> entriesSupplier) {
            super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
            this.parent = parent;
            this.entriesSupplier = entriesSupplier;
            updateList();
        }

        public void updateList() {
            this.clearEntries();
            entriesSupplier.get().stream().map(e -> new LocationEntry(e, this.parent, this::setSelected)).forEach(this::addEntry);
        }

    }

    static class LocationEntry extends ExtendedList.AbstractListEntry<LocationEntry> {

        private final ResourceLocation data;
        private final Screen parent;
        private final Consumer<LocationEntry> setSelected;

        LocationEntry(ResourceLocation data, Screen parent, Consumer<LocationEntry> setSelected) {
            this.data = data;
            this.parent = parent;
            this.setSelected = setSelected;
        }

        public ResourceLocation getData() {
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

}
