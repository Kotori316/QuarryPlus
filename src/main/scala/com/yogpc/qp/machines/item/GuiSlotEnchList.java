package com.yogpc.qp.machines.item;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.yogpc.qp.machines.base.QuarryBlackList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.list.ExtendedList;

public class GuiSlotEnchList extends ExtendedList<GuiSlotEnchList.Entry> {

    private final GuiEnchList parent;

    public GuiSlotEnchList(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn, GuiEnchList parent) {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.parent = parent;
        refreshList();
    }

    public void refreshList() {
        this.clearEntries();
        parent.buildModList(this::addEntry, Entry::new);
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
        public void render(MatrixStack m, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_render_8_, float partialTicks) {
            String name = data.toString();
            FontRenderer font = minecraft.fontRenderer;
            font.drawString(m, name,
                (GuiSlotEnchList.this.parent.width * 3 / 5 - font.getStringWidth(name)) / 2, top + 2, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            GuiSlotEnchList.this.setSelected(this);
            return false;
        }
    }
}
