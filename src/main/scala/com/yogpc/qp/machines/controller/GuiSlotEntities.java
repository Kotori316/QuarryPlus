package com.yogpc.qp.machines.controller;
/*
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiSlotEntities extends ExtendedList<GuiSlotEntities.Entry> {

    private final GuiController parent;

    public GuiSlotEntities(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn, GuiController parent) {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.parent = parent;
        this.refreshList();
    }

    public void refreshList() {
        this.func_230963_j_(); // clear
        parent.buildModList(this::func_230513_b_, Entry::new); // add
    }

    public class Entry extends ExtendedList.AbstractListEntry<Entry> {
        public final ResourceLocation location;

        public Entry(ResourceLocation location) {
            this.location = location;
        }

        @Override
        public void func_230432_a_(MatrixStack matrix, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_render_8_, float partialTicks) {
            String name = location.toString();
            Minecraft minecraft = Minecraft.getInstance();

            assert minecraft.currentScreen != null;
            minecraft.fontRenderer.func_238405_a_(matrix, name,
                (minecraft.currentScreen.field_230708_k_ - minecraft.fontRenderer.getStringWidth(name)) >> 1,
                top + 2, 0xFFFFFF);
        }

        @Override
        public boolean func_231044_a_(double mouseX, double mouseY, int button) {
            GuiSlotEntities.this.func_241215_a_(this); // setSelected
            return false;
        }
    }
}
*/