package com.yogpc.qp.modules;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.tile.APowerTile;
import com.yogpc.qp.tile.ExpPumpModule;
import com.yogpc.qp.tile.HasStorage;
import com.yogpc.qp.tile.IModule;
import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import scala.Symbol;

public class ItemExpPumpModule extends Item implements IDisabled, IModuleItem {
    public static final String Key_xp = "xp";

    public ItemExpPumpModule() {
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.exppumpModule);
        setUnlocalizedName(QuarryPlus.Names.exppumpModule);
        setCreativeTab(QuarryPlusI.creativeTab());
    }

    @Override
    public Symbol getSymbol() {
        return Symbol.apply("ModuleExpPump");
    }

    @Override
    public <T extends APowerTile> Function<T, IModule> getModule(ItemStack stack) {
        int xp = getXp(stack).orElse(0);
        return t -> {
            ExpPumpModule module = ExpPumpModule.fromTile(t, value -> stack.setTagInfo(Key_xp, new NBTTagInt(value)));
            module.xp_$eq(xp);
            return module;
        };
    }

    private Optional<Integer> getXp(ItemStack stack) {
        return Optional.ofNullable(stack.getTagCompound())
            .map(tag -> tag.getInteger(Key_xp))
            .filter(i -> i > 0);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        getXp(stack)
            .ifPresent(integer -> tooltip.add("xp: " + integer));
    }

    @SuppressWarnings("deprecation")
    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (!playerIn.isSneaking()) {
            ItemStack stack = playerIn.getHeldItem(handIn);
            int xp = getXp(stack).orElse(0);
            if (xp > 0) {
                Optional.ofNullable(stack.getTagCompound()).ifPresent(t -> t.removeTag(Key_xp));
                stack.setTagCompound(Optional.ofNullable(stack.getTagCompound()).filter(t -> !t.hasNoTags()).orElse(null));
                if (!worldIn.isRemote) {
                    EntityXPOrb orb = new EntityXPOrb(worldIn, playerIn.posX, playerIn.posY, playerIn.posZ, xp);
                    worldIn.spawnEntity(orb);
                }
                return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
            }
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }
}
