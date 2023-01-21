package com.yogpc.qp.machines.module;

import java.util.List;
import java.util.Optional;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.ItemKey;
import com.yogpc.qp.machines.QPItem;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FilterModuleItem extends QPItem implements QuarryModuleProvider.Item {
    public static final String NAME = "filter_module";
    public static final String KEY_ITEMS = "filter_items";

    public FilterModuleItem() {
        super(new ResourceLocation(QuarryPlus.modID, NAME), new Properties().tab(Holder.TAB).stacksTo(1));
    }

    @Override
    public QuarryModule getModule(@NotNull ItemStack stack) {
        var tagList = Optional.ofNullable(stack.getTag())
            .map(t -> t.getList(KEY_ITEMS, Tag.TAG_COMPOUND))
            .orElse(null);
        return new FilterModule(tagList);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof ModuleInventory.HasModuleInventory) {
            // Trying to open machine GUI
            return InteractionResult.PASS;
        }
        if (!context.getLevel().isClientSide && context.getPlayer() instanceof ServerPlayer) {
            NetworkHooks.openScreen((ServerPlayer) context.getPlayer(), new InteractionObject(context.getItemInHand()));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> tooltips, TooltipFlag pIsAdvanced) {
        super.appendHoverText(stack, pLevel, tooltips, pIsAdvanced);
        tooltips.add(Component.translatable("quarryplus.tooltip.filter_module_1"));
        tooltips.add(Component.translatable("quarryplus.tooltip.filter_module_2"));
        var keys = FilterModule.getFromTag(Optional.ofNullable(stack.getTag())
                .map(t -> t.getList(KEY_ITEMS, Tag.TAG_COMPOUND)).orElse(null))
            .stream()
            .map(ItemKey::getId)
            .map(ResourceLocation::toString);
        keys.map(s -> "  " + s)
            .map(Component::literal)
            .forEach(tooltips::add);
    }

    private void addKey(ItemStack filterItem, ItemKey keyToAdd) {
        var tag = filterItem.getOrCreateTag();
        var list = tag.getList(KEY_ITEMS, Tag.TAG_COMPOUND);
        list.add(keyToAdd.createNbt());
        tag.put(KEY_ITEMS, list);
    }

    private record InteractionObject(ItemStack stack) implements MenuProvider {
        @Override
        public Component getDisplayName() {
            return stack.getHoverName();
        }

        @Override
        public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
            return new FilterModuleMenu(pContainerId, pPlayer, stack);
        }
    }
}
