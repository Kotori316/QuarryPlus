package com.yogpc.qp.machines.modules;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import com.mojang.serialization.Dynamic;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.base.APowerTile;
import com.yogpc.qp.machines.base.HasStorage;
import com.yogpc.qp.machines.base.IDisabled;
import com.yogpc.qp.machines.base.IModule;
import com.yogpc.qp.machines.base.QPItem;
import com.yogpc.qp.machines.quarry.ContainerQuarryModule;
import com.yogpc.qp.machines.replacer.ReplacerModule;
import com.yogpc.qp.machines.replacer.TileReplacer;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import scala.Symbol;

import static jp.t2v.lab.syntax.MapStreamSyntax.always_false;

public class ItemReplacerModule extends QPItem implements IDisabled, IModuleItem {
    public static final String Key_state = "state";

    public ItemReplacerModule() {
        super(QuarryPlus.Names.replacerModule, p -> p.rarity(Rarity.UNCOMMON));
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        if (!context.getWorld().isRemote) {
            if (context.getPlayer() != null && context.getPlayer().isSneaking()) {
                stack.removeChildTag(Key_state);
                context.getPlayer().sendMessage(new StringTextComponent("Replacer Module: Setting removed."), Util.DUMMY_UUID);
            } else {
                if (context.getWorld().getTileEntity(context.getPos()) instanceof ContainerQuarryModule.HasModuleInventory)
                    // Maybe trying to open the quarry.
                    return ActionResultType.PASS;
                BlockState state = context.getWorld().getBlockState(context.getPos());
                Predicate<BlockState> deny = TileReplacer.rejects.stream().reduce(always_false(), Predicate::or);
                if (deny.test(state))
                    return ActionResultType.PASS;
                BlockState.CODEC.encodeStart(NBTDynamicOps.INSTANCE, state).resultOrPartial(s -> {
                    // What happened?
                    stack.removeChildTag(Key_state);
                    QuarryPlus.LOGGER.warn("Error in encoding state to NBT. {}, {}", state, s);
                }).ifPresent(stateTag -> stack.setTagInfo(Key_state, stateTag));
                if (context.getPlayer() != null)
                    context.getPlayer().sendMessage(new TranslationTextComponent("Replacer Module: %s.", state.getBlock().getTranslatedName()), Util.DUMMY_UUID);
            }
            return ActionResultType.SUCCESS;
        } else {
            return ActionResultType.PASS;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        Optional.ofNullable(stack.getChildTag(Key_state))
            .map(c -> new Dynamic<>(NBTDynamicOps.INSTANCE, c))
            .flatMap(d -> BlockState.CODEC.parse(d).result())
            .ifPresent(state -> {
                tooltip.add(state.getBlock().getTranslatedName());
                state.getValues().forEach((k, v) -> tooltip.add(new StringTextComponent(String.format("  %s: %s", k.getName(), v))));
            });
    }

    @Override
    public Symbol getSymbol() {
        return Symbol.apply("ModuleReplacer");
    }

    @Override
    public <T extends APowerTile & HasStorage & ContainerQuarryModule.HasModuleInventory> Function<T, IModule> getModule(ItemStack stack) {
        Predicate<BlockState> accept = TileReplacer.rejects.stream().reduce(always_false(), Predicate::or).negate();
        BlockState state = Optional.ofNullable(stack.getChildTag(Key_state))
            .map(c -> new Dynamic<>(NBTDynamicOps.INSTANCE, c))
            .flatMap(d -> {
                try {
                    return BlockState.CODEC.parse(d).result();
                } catch (Exception e) {
                    QuarryPlus.LOGGER.debug("Error in getting replace block of ReplaceModule.", e);
                    return Optional.empty();
                }
            })
            .filter(accept)
            .orElse(Holder.blockDummy().getDefaultState());
        return t -> ReplacerModule.apply(state);
    }
}
