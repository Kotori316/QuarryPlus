package com.yogpc.qp.machines.controller;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.IHandleButton;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.controller.SetEntity;
import com.yogpc.qp.utils.Holder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiController extends Screen implements IHandleButton {
    private GuiSlotEntities slot;
    private TextFieldWidget search;
    List<ResourceLocation> names;
    @javax.annotation.Nonnull
    private final List<ResourceLocation> allEntities;
    ResourceLocation dim;
    final int xc, yc, zc;

    public GuiController(final ResourceLocation d, final int x, final int y, final int z, final List<ResourceLocation> allEntities) {
        super(new TranslationTextComponent(Holder.blockController().getTranslationKey()));
        this.dim = d;
        this.xc = x;
        this.yc = y;
        this.zc = z;
        this.allEntities = allEntities;
        names = allEntities.stream().sorted(Comparator.comparing(ResourceLocation::getNamespace)).collect(Collectors.toList());
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        int width = this.field_230708_k_;
        int height = this.field_230709_l_;
        this.slot = new GuiSlotEntities(this.getMinecraft(), width, height, 30, height - 60, 18, this);
        this.field_230705_e_.add(slot);
        func_231035_a_(slot);
        func_230480_a_(new IHandleButton.Button(-1, width / 2 - 125, height - 26, 250, 20, new TranslationTextComponent(TranslationKeys.DONE), this));
        this.search = new TextFieldWidget(field_230712_o_, width / 2 - 125, height - 56, 250, 20, new StringTextComponent(""));
        this.field_230705_e_.add(search);
        search.setCanLoseFocus(true);
        search.setResponder(this::searchEntities);
    }

    @Override
    public void func_230430_a_(MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        if (slot != null) {
            this.slot.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
            this.search.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
        }
        super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
        func_238472_a_(matrixStack, this.field_230712_o_, new TranslationTextComponent(TranslationKeys.YOG_SPAWNER_SETTING), this.field_230708_k_ / 2, 8, 0xFFFFFF);
    }

    @Override
    public void func_231023_e_() {
        super.func_231023_e_();
        if (!this.getMinecraft().player.isAlive())
            this.getMinecraft().player.closeScreen();
    }

    @Override
    public boolean func_231046_a_(int keyCode, int scanCode, int modifiers) {
        InputMappings.Input mouseKey = InputMappings.getInputByCode(keyCode, scanCode);
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || (!search.func_230999_j_() && this.getMinecraft().gameSettings.keyBindInventory.isActiveAndMatches(mouseKey))) {
            // func_230999_j_ isFocused
            this.func_231175_as__();
            return true;
        }
        return super.func_231046_a_(keyCode, scanCode, modifiers);
    }

    @Override
    public void actionPerformed(IHandleButton.Button button) {
        if (button.id == -1) {
            GuiSlotEntities.Entry selected = slot.func_230958_g_();
            if (selected != null) {
                PacketHandler.sendToServer(SetEntity.create(dim, new BlockPos(xc, yc, zc), selected.location));
            }
            getMinecraft().player.closeScreen();
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
                    .sorted(Comparator.comparing(ResourceLocation::getNamespace)).collect(Collectors.toList());
            } catch (PatternSyntaxException e) {
                collect = allEntities.stream().filter(l -> l.toString().contains(text))
                    .sorted(Comparator.comparing(ResourceLocation::getNamespace)).collect(Collectors.toList());
            }
        } else {
            collect = allEntities.stream().sorted(Comparator.comparing(ResourceLocation::getNamespace)).collect(Collectors.toList());
        }
        names = collect;
        this.slot.refreshList();
        this.slot.func_241215_a_(null); // setSelected
    }
}
