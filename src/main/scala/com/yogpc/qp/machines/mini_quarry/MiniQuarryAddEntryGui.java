package com.yogpc.qp.machines.mini_quarry;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.misc.IndexedButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

public class MiniQuarryAddEntryGui extends Screen implements Button.OnPress {
    private final Consumer<BlockStatePredicate> callback;
    private EntryList list;
    private EditBox textField;

    protected MiniQuarryAddEntryGui(Screen parent, Consumer<BlockStatePredicate> callback) {
        super(parent.getTitle()); // title
        this.callback = callback;
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = 80;
        int width = this.width;
        int height = this.height;
        textField = new EditBox(font, width / 2 - 125, height - 56, 250, 20, new TextComponent(""));
        textField.setMaxLength(512);
        list = new EntryList(this.getMinecraft(), width, height, 30, height - 70, 18, this, this::getEntries);
        IndexedButton button = new IndexedButton(1, width / 2 - buttonWidth / 2, height - 35, buttonWidth, 20, new TranslatableComponent("tof.add_new_ore"), this);

        this.addRenderableWidget(list);
        this.addRenderableWidget(textField);
        this.addRenderableWidget(button);
        this.setInitialFocus(list);
        textField.setCanLoseFocus(true);
        textField.setResponder(s -> {
            list.updateList();
            list.setScrollAmount(list.getScrollAmount()); // Scroll
        });
    }

    @Override
    public void render(PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        list.render(matrixStack, mouseX, mouseY, partialTicks);
        //textField.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, this.font, new TranslatableComponent("quarryplus.gui.new_entry"), this.width / 2, 8, 0xFFFFFF);
    }

    private Pair<Kind, List<ResourceLocation>> getEntries() {
        String filterText = textField == null ? "" : textField.getValue();
        if (filterText.startsWith("#")) {
            String f = filterText.substring(1); // Remove first #
            return Pair.of(Kind.TAG, BlockTags.getAllTags().getAvailableTags()
                .stream().filter(r -> r.toString().contains(f)).sorted().collect(Collectors.toList()));
        } else {
            return Pair.of(Kind.BLOCK, ForgeRegistries.BLOCKS.getKeys().stream()
                .filter(r -> r.toString().contains(filterText)).sorted().collect(Collectors.toList()));
        }
    }

    @Override
    public void onPress(Button button) {
        if (button instanceof IndexedButton indexedButton) {
            if (indexedButton.id() == 1) {
                LocationEntry entry = list.getSelected();
                if (entry != null) {
                    ResourceLocation location = entry.getData();
                    switch (entry.getKind()) {
                        case BLOCK:
                            callback.accept(BlockStatePredicate.name(location));
                            break;
                        case TAG:
                            callback.accept(BlockStatePredicate.tag(location));
                            break;
                        default:
                            QuarryPlus.LOGGER.warn("Not registered kind {} for {}.", entry.getKind(), location);
                    }
                } else {
                    String maybePredicate = textField.getValue();
                    if (!maybePredicate.isEmpty()) {
                        try {
                            (new BlockStateParser(new StringReader(maybePredicate), true)).parse(true);
                            callback.accept(BlockStatePredicate.predicateString(maybePredicate));
                        } catch (CommandSyntaxException e) {
                            QuarryPlus.LOGGER.debug("Invalid predicate {} was parsed but not added. Got {}.", maybePredicate, e);
                        }
                    }
                }
                this.onClose();
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || (!textField.isFocused() && this.getMinecraft().options.keyInventory.isActiveAndMatches(mouseKey))) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    static class EntryList extends ObjectSelectionList<LocationEntry> {

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

    static class LocationEntry extends ObjectSelectionList.Entry<LocationEntry> {

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
        public void render(PoseStack m, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_render_8_, float partialTicks) {
            String name = (kind == Kind.TAG ? "#" : "") + data.toString();
            // drawCenteredString(m, Minecraft.getInstance().font, name, parent.width, top + 1, 0xFFFFFF);
            Minecraft.getInstance().font.draw(m, name,
                (parent.width - Minecraft.getInstance().font.width(name)) / 2, top + 2, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            setSelected.accept(this);
            return false;
        }

        @Override
        public Component getNarration() {
            return new TranslatableComponent("narrator.select", data);
        }
    }

    private enum Kind {
        BLOCK, TAG
    }
}
