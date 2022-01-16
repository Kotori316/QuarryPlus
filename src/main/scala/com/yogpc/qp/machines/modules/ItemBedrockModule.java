package com.yogpc.qp.machines.modules;

import java.util.List;
import java.util.function.Function;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.APowerTile;
import com.yogpc.qp.machines.base.HasStorage;
import com.yogpc.qp.machines.base.IModule;
import com.yogpc.qp.machines.base.QPItem;
import com.yogpc.qp.machines.quarry.ContainerQuarryModule;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import scala.Symbol;

public class ItemBedrockModule extends QPItem implements IModuleItem {
    public ItemBedrockModule() {
        super(QuarryPlus.Names.bedrockModule, p -> p.rarity(Rarity.UNCOMMON));
    }

    @Override
    public <T extends APowerTile & HasStorage & ContainerQuarryModule.HasModuleInventory> Function<T, IModule> getModule(ItemStack stack) {
        return RemoveBedrockModule::new;
    }

    @Override
    public Symbol getSymbol() {
        return Symbol.apply("ModuleRemoveBedrock");
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(new TranslationTextComponent(TranslationKeys.BEDROCK_MODULE_DESCRIPTION));
    }
}
