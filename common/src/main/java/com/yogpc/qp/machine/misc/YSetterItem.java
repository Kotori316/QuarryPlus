package com.yogpc.qp.machine.misc;

import com.yogpc.qp.InCreativeTabs;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class YSetterItem extends Item implements InCreativeTabs {
    public static final String NAME = "y_setter";
    public final ResourceLocation name = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, NAME);

    public YSetterItem() {
        super(new Properties());
    }

    protected final InteractionResult interact(@NotNull Level level, @NotNull BlockPos pos, @Nullable Player player) {
        var entity = level.getBlockEntity(pos);
        var accessor = YAccessor.get(entity);
        if (entity == null || accessor == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            // accessor.entity().syncToClient();
            openGui(player, entity.getBlockPos(), entity.getBlockState().getBlock().getName());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    protected abstract void openGui(Player player, BlockPos pos, Component text);

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
