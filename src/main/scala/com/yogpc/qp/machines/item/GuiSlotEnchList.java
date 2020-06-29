package com.yogpc.qp.machines.item;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.yogpc.qp.machines.base.QuarryBlackList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;

public class GuiSlotEnchList extends ExtendedList<GuiSlotEnchList.Entry> {

    private final GuiEnchList parent;

    public GuiSlotEnchList(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn, GuiEnchList parent) {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.parent = parent;
        refreshList();
    }

    public void refreshList() {
        this.func_230963_j_();
        parent.buildModList(this::func_230513_b_, Entry::new);
    }

    public class Entry extends ExtendedList.AbstractListEntry<Entry> {

        private final QuarryBlackList.Entry data;

        public Entry(QuarryBlackList.Entry data) {
            this.data = data;
        }

        public QuarryBlackList.Entry getData() {
            return data;
        }

        @Override
        @SuppressWarnings("IntegerDivisionInFloatingPointContext")
        public void func_230432_a_(MatrixStack m, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_render_8_, float partialTicks) {
            String name = data.toString();
            Minecraft.getInstance().fontRenderer.func_238405_a_(m, name,
                (GuiSlotEnchList.this.parent.field_230708_k_ * 3 / 5 - Minecraft.getInstance().fontRenderer.getStringWidth(name)) / 2, top + 2, 0xFFFFFF);
        }

        @Override
        public boolean func_231044_a_(double mouseX, double mouseY, int button) {
            GuiSlotEnchList.this.func_241215_a_(this);
            return false;
        }
    }
}
