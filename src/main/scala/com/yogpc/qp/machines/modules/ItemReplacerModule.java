package com.yogpc.qp.machines.modules;

import java.util.Optional;
import java.util.function.Function;

import cats.Eval;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.base.APowerTile;
import com.yogpc.qp.machines.base.HasStorage;
import com.yogpc.qp.machines.base.IDisabled;
import com.yogpc.qp.machines.base.IModule;
import com.yogpc.qp.machines.replacer.ReplacerModule;
import com.yogpc.qp.utils.Holder;
import com.yogpc.qp.utils.NBTBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import scala.Symbol;

public class ItemReplacerModule extends Item implements IDisabled, IModuleItem {
    public static final String Key_state = "state";
    private final Gson gson = new Gson();

    public ItemReplacerModule() {
        super(new Item.Properties().group(Holder.tab()).rarity(Rarity.UNCOMMON));
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.replacerModule);
    }

    @Override
    public Symbol getSymbol() {
        return Symbol.apply("ModuleReplacer");
    }

    @Override
    public <T extends APowerTile & HasStorage> Function<T, IModule> getModule(ItemStack stack) {
        BlockState state = Optional.ofNullable(stack.getTag())
            .map(tag -> tag.getString(Key_state))
            .map(s -> gson.fromJson(s, JsonObject.class))
            .flatMap(NBTBuilder::getStateFromJson)
            .orElse(Holder.blockDummy().getDefaultState());
        return t -> new ReplacerModule(Eval.now(state));
    }
}
