package com.yogpc.qp.machines.modules;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.base.APowerTile;
import com.yogpc.qp.machines.base.HasStorage;
import com.yogpc.qp.machines.base.IDisabled;
import com.yogpc.qp.machines.base.IModule;
import com.yogpc.qp.machines.exppump.ExpPumpModule;
import com.yogpc.qp.machines.quarry.ContainerQuarryModule;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import scala.Symbol;

public class ItemExpPumpModule extends Item implements IDisabled, IModuleItem {
    public static final String Key_xp = "xp";

    public ItemExpPumpModule() {
        super(new Item.Properties().group(Holder.tab()).rarity(Rarity.UNCOMMON));
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.exppumpModule);
    }

    @Override
    public Symbol getSymbol() {
        return Symbol.apply("ModuleExpPump");
    }

    @Override
    public <T extends APowerTile & HasStorage & ContainerQuarryModule.HasModuleInventory> Function<T, IModule> getModule(ItemStack stack) {
        int xp = Optional.ofNullable(stack.getTag())
            .map(tag -> tag.get(Key_xp))
            .flatMap(NBTDynamicOps.INSTANCE::getNumberValue)
            .map(Number::intValue)
            .orElse(0);
        return t -> {
            ExpPumpModule module = ExpPumpModule.fromTile(t, value -> stack.setTagInfo(Key_xp, NBTDynamicOps.INSTANCE.createInt(value)));
            module.xp_$eq(xp);
            return module;
        };
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        Optional.ofNullable(stack.getTag())
            .map(tag -> tag.get(Key_xp))
            .flatMap(NBTDynamicOps.INSTANCE::getNumberValue)
            .map(Number::intValue)
            .ifPresent(integer -> tooltip.add(new StringTextComponent("xp: " + integer)));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (!playerIn.isSneaking()) {
            ItemStack stack = playerIn.getHeldItem(handIn);
            int xp = Optional.ofNullable(stack.getTag())
                .map(tag -> tag.get(Key_xp))
                .flatMap(NBTDynamicOps.INSTANCE::getNumberValue)
                .map(Number::intValue)
                .orElse(0);
            if (xp > 0) {
                stack.removeChildTag(Key_xp);
                if (!worldIn.isRemote) {
                    ExperienceOrbEntity orb = new ExperienceOrbEntity(worldIn, playerIn.posX, playerIn.posY, playerIn.posZ, xp);
                    worldIn.addEntity(orb);
                }
                return ActionResult.newResult(ActionResultType.SUCCESS, stack);
            }
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }
}
