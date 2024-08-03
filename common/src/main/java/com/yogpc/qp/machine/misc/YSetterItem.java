package com.yogpc.qp.machine.misc;

import com.yogpc.qp.InCreativeTabs;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public abstract class YSetterItem extends Item implements InCreativeTabs {
    public static final String NAME = "y_setter";
    public final ResourceLocation name = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, NAME);

    public YSetterItem() {
        super(new Properties());
    }

    protected static class YSetterScreenHandler implements MenuProvider {
        protected final BlockPos pos;
        protected final Component text;

        protected YSetterScreenHandler(BlockPos pos, Component text) {
            this.pos = pos;
            this.text = text;
        }

        @Override
        public Component getDisplayName() {
            return text;
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
            return new YSetterContainer(i, inventory, pos);
        }
    }
}
