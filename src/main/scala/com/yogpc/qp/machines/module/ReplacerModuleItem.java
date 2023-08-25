package com.yogpc.qp.machines.module;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ReplacerModuleItem extends QPItem implements QuarryModuleProvider.Item {
    public static final String NAME = "replacer_module";
    public static final String KEY_STATE = "state";

    public ReplacerModuleItem() {
        super(new ResourceLocation(QuarryPlus.modID, NAME), new Properties());
    }

    @Override
    public ReplacerModule getModule(@NotNull ItemStack stack) {
        BlockState state = Optional.ofNullable(stack.getTagElement(KEY_STATE))
            .flatMap(this::getStateFromTag)
            .orElse(Holder.BLOCK_DUMMY_REPLACER.defaultBlockState());
        return new ReplacerModule(state);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (!context.getLevel().isClientSide) {
            if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
                // Remove setting
                stack.removeTagKey(KEY_STATE);
                context.getPlayer().displayClientMessage(Component.literal("Replacer Module: Setting removed."), false);
            } else {
                if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof ModuleInventory.HasModuleInventory)
                    // Maybe trying to open the machine.
                    return InteractionResult.PASS;
                BlockState state = context.getLevel().getBlockState(context.getClickedPos());
                if (ReplacerModule.rejects.stream().anyMatch(t -> t.test(state)))
                    // The block can't be used as replacing block.
                    return InteractionResult.PASS;
                BlockState.CODEC.encodeStart(NbtOps.INSTANCE, state).resultOrPartial(s -> {
                    // What happened?
                    stack.removeTagKey(KEY_STATE);
                    QuarryPlus.LOGGER.warn("Error in encoding state to NBT. {}, {}", state, s);
                }).ifPresent(stateTag -> stack.addTagElement(KEY_STATE, stateTag));
                if (context.getPlayer() != null)
                    context.getPlayer().displayClientMessage(Component.translatable("quarryplus.chat.replacer_module", state.getBlock().getName()), false);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, level, list, flag);
        Optional.ofNullable(stack.getTagElement(KEY_STATE))
            .flatMap(tag -> BlockState.CODEC.parse(NbtOps.INSTANCE, tag).result())
            .ifPresent(state -> {
                list.add(state.getBlock().getName());
                state.getValues().forEach((k, v) -> list.add(Component.literal(String.format("  %s: %s", k.getName(), v))));
            });
    }

    Optional<BlockState> getStateFromTag(CompoundTag tag) {
        try {
            Predicate<BlockState> accept = ReplacerModule.rejects
                .stream().reduce(s -> false, Predicate::or).negate();
            var result = BlockState.CODEC.parse(NbtOps.INSTANCE, tag);
            return result.result()
                .filter(accept);
        } catch (Exception e) {
            QuarryPlus.LOGGER.debug("Error in getting replace block of ReplacerModule.", e);
            return Optional.empty();
        }
    }
}
