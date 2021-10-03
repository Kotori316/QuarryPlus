package com.yogpc.qp.machines.controller;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.misc.IndexedButton;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiController extends Screen implements Button.OnPress {
    private static final Comparator<ResourceLocation> RESOURCE_LOCATION_COMPARATOR =
        Comparator.comparing(ResourceLocation::getNamespace).thenComparing(ResourceLocation::getPath);
    private GuiSlotEntities slot;
    private EditBox search;
    List<ResourceLocation> names;
    @javax.annotation.Nonnull
    private final List<ResourceLocation> allEntities;
    private final ResourceKey<Level> dim;
    private final BlockPos pos;

    public GuiController(final ResourceKey<Level> dim, final BlockPos pos, final List<ResourceLocation> allEntities) {
        super(Holder.BLOCK_CONTROLLER.getName());
        this.dim = dim;
        this.pos = pos;
        this.allEntities = allEntities;
        names = allEntities.stream().sorted(RESOURCE_LOCATION_COMPARATOR).collect(Collectors.toList());
    }

    @Override
    public void init() {
        super.init();
        int width = this.width;
        int height = this.height;
        this.slot = new GuiSlotEntities(this.getMinecraft(), width, height, 30, height - 60, 18, this);
        this.addRenderableWidget(slot);
        setInitialFocus(slot);
        addRenderableWidget(new IndexedButton(-1, width / 2 - 125, height - 26, 250, 20, new TranslatableComponent("gui.done"), this));
        this.search = new EditBox(font, width / 2 - 125, height - 56, 250, 20, new TextComponent("edit box"));
        this.addRenderableWidget(search);
        search.setCanLoseFocus(true);
        search.setResponder(this::searchEntities);
    }

    @Override
    public void render(PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        if (slot != null) {
            this.slot.render(matrixStack, mouseX, mouseY, partialTicks);
            this.search.render(matrixStack, mouseX, mouseY, partialTicks);
        }
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, this.font, new TranslatableComponent("yog.spawner.setting"), this.width / 2, 8, 0xFFFFFF);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getMinecraft().player != null && !this.getMinecraft().player.isAlive())
            this.getMinecraft().player.closeContainer();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || (!search.isFocused() && this.getMinecraft().options.keyInventory.isActiveAndMatches(mouseKey))) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onPress(Button button) {
        if (button instanceof IndexedButton indexedButton && indexedButton.id() == -1) {
            GuiSlotEntities.Entry selected = slot.getSelected();
            if (selected != null) {
                PacketHandler.sendToServer(new SetSpawnerEntityMessage(pos, dim, selected.location));
            }
            if (this.getMinecraft().player != null)
                getMinecraft().player.closeContainer();
        }
    }

    public void buildModList(Consumer<GuiSlotEntities.Entry> modListViewConsumer, Function<ResourceLocation, GuiSlotEntities.Entry> newEntry) {
        names.stream().map(newEntry).forEach(modListViewConsumer);
    }

    public void searchEntities(String text) {
        List<ResourceLocation> collect;
        if (!text.isEmpty()) {
            try {
                Pattern pattern = Pattern.compile(text);
                collect = allEntities.stream().filter(l -> pattern.matcher(l.toString()).find())
                    .sorted(RESOURCE_LOCATION_COMPARATOR).collect(Collectors.toList());
            } catch (PatternSyntaxException e) {
                collect = allEntities.stream().filter(l -> l.toString().contains(text))
                    .sorted(RESOURCE_LOCATION_COMPARATOR).collect(Collectors.toList());
            }
        } else {
            collect = allEntities.stream().sorted(RESOURCE_LOCATION_COMPARATOR).collect(Collectors.toList());
        }
        if (names.size() != collect.size())
            this.slot.setScrollAmount(0);
        names = collect;
        this.slot.refreshList();
        this.slot.setSelected(null); // setSelected
    }
}
