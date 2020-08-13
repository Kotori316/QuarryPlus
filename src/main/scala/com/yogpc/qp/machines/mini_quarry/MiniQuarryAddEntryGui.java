package com.yogpc.qp.machines.mini_quarry;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.IHandleButton;
import com.yogpc.qp.machines.base.QuarryBlackList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.InputMappings;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

public class MiniQuarryAddEntryGui extends Screen implements IHandleButton {
    @javax.annotation.Nonnull
    private final Screen parent;
    private final Consumer<QuarryBlackList.Entry> callback;
    private EntryList list;
    private TextFieldWidget textField;

    protected MiniQuarryAddEntryGui(Screen parent, Consumer<QuarryBlackList.Entry> callback) {
        super(parent.getTitle()); // title
        this.parent = parent;
        this.callback = callback;
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = 80;
        int width = this.width;
        int height = this.height;
        textField = new TextFieldWidget(font, width / 2 - 125, height - 56, 250, 20, new StringTextComponent(""));
        textField.setMaxStringLength(512);
        list = new EntryList(this.getMinecraft(), width, height, 30, height - 70, 18, this, this::getEntries);
        addButton(new Button(1, width / 2 - buttonWidth / 2, height - 35, buttonWidth, 20, I18n.format(TranslationKeys.ADD), this));
        this.children.add(list);
        this.children.add(textField);
        this.setFocusedDefault(list);
        textField.setCanLoseFocus(true);
        textField.setResponder(s -> {
            list.updateList();
            list.setScrollAmount(list.getScrollAmount()); // Scroll
        });
    }

    @Override
    public void render(MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        list.render(matrixStack, mouseX, mouseY, partialTicks);
        textField.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        String title = I18n.format(TranslationKeys.NEW_ENTRY);
        drawCenteredString(matrixStack, this.font, title, this.width / 2, 8, 0xFFFFFF);
    }

    private Pair<Kind, List<ResourceLocation>> getEntries() {
        String filterText = textField == null ? "" : textField.getText();
        if (filterText.startsWith("#")) {
            String f = filterText.substring(1); // Remove first #
            return Pair.of(Kind.TAG, BlockTags.getCollection().getRegisteredTags()
                .stream().filter(r -> r.toString().contains(f)).sorted().collect(Collectors.toList()));
        } else {
            return Pair.of(Kind.BLOCK, ForgeRegistries.BLOCKS.getKeys().stream()
                .filter(r -> r.toString().contains(filterText)).sorted().collect(Collectors.toList()));
        }
    }

    @Override
    public void actionPerformed(Button button) {
        if (button.id == 1) {
            LocationEntry entry = list.getSelected();
            if (entry != null) {
                ResourceLocation location = entry.getData();
                switch (entry.getKind()) {
                    case BLOCK:
                        callback.accept(new QuarryBlackList.Name(location));
                        break;
                    case TAG:
                        callback.accept(new QuarryBlackList.Tag(location));
                        break;
                    default:
                        QuarryPlus.LOGGER.warn("Not registered kind {} for {}.", entry.getKind(), location);
                }
            } else {
                String maybePredicate = textField.getText();
                if (!maybePredicate.isEmpty()) {
                    try {
                        (new BlockStateParser(new StringReader(maybePredicate), true)).parse(true);
                        callback.accept(new QuarryBlackList.VanillaBlockPredicate(maybePredicate));
                    } catch (CommandSyntaxException e) {
                        QuarryPlus.LOGGER.debug("Invalid predicate {} was parsed but not added. Got {}.", maybePredicate, e);
                    }
                }
            }
            this.closeScreen();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputMappings.Input mouseKey = InputMappings.getInputByCode(keyCode, scanCode);
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || (!textField.isFocused() && this.getMinecraft().gameSettings.keyBindInventory.isActiveAndMatches(mouseKey))) {
            // isFocused isFocused
            this.closeScreen();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void closeScreen() { // OnClose
        getMinecraft().displayGuiScreen(parent);
    }

    static class EntryList extends ExtendedList<LocationEntry> {

        private final Screen parent;
        private final Supplier<Pair<Kind, List<ResourceLocation>>> entriesSupplier;

        public EntryList(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn, Screen parent, Supplier<Pair<Kind, List<ResourceLocation>>> entriesSupplier) {
            super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
            this.parent = parent;
            this.entriesSupplier = entriesSupplier;
            updateList();
        }

        public void updateList() {
            this.clearEntries();
            Pair<Kind, List<ResourceLocation>> kindListPair = entriesSupplier.get();
            kindListPair.getValue().stream().map(e -> new LocationEntry(e, this.parent, this::setSelected, kindListPair.getKey())).forEach(this::addEntry);
        }

    }

    static class LocationEntry extends ExtendedList.AbstractListEntry<LocationEntry> {

        private final ResourceLocation data;
        private final Screen parent;
        private final Consumer<LocationEntry> setSelected;
        private final Kind kind;

        LocationEntry(ResourceLocation data, Screen parent, Consumer<LocationEntry> setSelected, Kind kind) {
            this.data = data;
            this.parent = parent;
            this.setSelected = setSelected;
            this.kind = kind;
        }

        public ResourceLocation getData() {
            return data;
        }

        public Kind getKind() {
            return kind;
        }

        @Override
        @SuppressWarnings("IntegerDivisionInFloatingPointContext")
        public void render(MatrixStack m, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_render_8_, float partialTicks) {
            String name = (kind == Kind.TAG ? "#" : "") + data.toString();
            Minecraft.getInstance().fontRenderer.drawString(m, name,
                (parent.width - Minecraft.getInstance().fontRenderer.getStringWidth(name)) / 2, top + 2, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            setSelected.accept(this);
            return false;
        }
    }

    private enum Kind {
        BLOCK, TAG
    }
}
