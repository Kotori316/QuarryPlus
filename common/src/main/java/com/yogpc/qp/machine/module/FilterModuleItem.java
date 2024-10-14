package com.yogpc.qp.machine.module;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryDataComponents;
import com.yogpc.qp.machine.GeneralScreenHandler;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.QpItem;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public final class FilterModuleItem extends QpItem implements QuarryModuleProvider.Item {
    public static final String NAME = ConverterModule.FilterModule.NAME;

    public FilterModuleItem() {
        super(new Properties(), NAME);
    }

    @Override
    public QuarryModule getModule(@NotNull ItemStack stack) {
        Set<MachineStorage.ItemKey> targets = Set.copyOf(stack.getOrDefault(QuarryDataComponents.ITEM_KEY_LIST_COMPONENT, List.of()));
        return new ConverterModule.FilterModule(targets);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        var stack = player.getItemInHand(usedHand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            PlatformAccess.getAccess().openGui(serverPlayer, new GeneralScreenHandler<>(player.getOnPos(), stack.getHoverName(),
                (syncId, inventory, pos) -> new FilterModuleContainer(syncId, inventory, stack)));
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("quarryplus.tooltip.filter_module_1"));
        tooltipComponents.add(Component.translatable("quarryplus.tooltip.filter_module_2"));

        var itemList = stack.get(QuarryDataComponents.ITEM_KEY_LIST_COMPONENT);
        if (itemList != null && !itemList.isEmpty()) {
            if (Screen.hasShiftDown()) {
                itemList.stream()
                    .map(MachineStorage.ItemKey::item)
                    .filter(Predicate.isEqual(Items.AIR).negate())
                    .map(BuiltInRegistries.ITEM::getKey)
                    .map("  %s"::formatted)
                    .map(Component::literal)
                    .forEach(tooltipComponents::add);
            } else {
                var first = Component.literal("  %s".formatted(BuiltInRegistries.ITEM.getKey(itemList.getFirst().item())));
                tooltipComponents.add(first);
                if (itemList.size() > 1) {
                    tooltipComponents.add(Component.literal("  ...(%s)".formatted(itemList.size() - 1)));
                }
            }
        }
    }
}
