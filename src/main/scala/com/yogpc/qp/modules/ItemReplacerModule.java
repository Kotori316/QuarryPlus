package com.yogpc.qp.modules;

import java.util.Optional;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.tile.APowerTile;
import com.yogpc.qp.tile.HasStorage;
import com.yogpc.qp.tile.IModule;
import com.yogpc.qp.tile.ReplacerModule;
import com.yogpc.qp.utils.NBTBuilder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import scala.Symbol;

public class ItemReplacerModule extends Item implements IDisabled, IModuleItem {
    public static final String Key_state = "state";
    private final Gson gson = new Gson();

    public ItemReplacerModule() {
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.replacerModule);
        setUnlocalizedName(QuarryPlus.Names.replacerModule);
        setCreativeTab(QuarryPlusI.creativeTab());
    }

    @Override
    public Symbol getSymbol() {
        return Symbol.apply("ModuleReplacer");
    }

    @SuppressWarnings("deprecation")
    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }

    @Override
    public <T extends APowerTile> Function<T, IModule> getModule(ItemStack stack) {
        IBlockState state = Optional.ofNullable(stack.getTagCompound())
            .map(tag -> tag.getString(Key_state))
            .map(s -> gson.fromJson(s, JsonObject.class))
            .flatMap(NBTBuilder::getStateFromJson)
            .orElse(QuarryPlusI.dummyBlock().getDefaultState());
        return t -> ReplacerModule.apply(state);
    }
}
