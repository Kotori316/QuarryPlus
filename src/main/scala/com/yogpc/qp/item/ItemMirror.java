package com.yogpc.qp.item;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMirror extends ItemFood {
    public ItemMirror() {
        super(0, 0, false);
        setHasSubtypes(true);
        setUnlocalizedName(QuarryPlus.Names.mirror);
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.mirror);
        setCreativeTab(QuarryPlusI.ct);
        setAlwaysEdible();
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            if (stack.getItemDamage() == 2) {
                if (player.dimension != 0) {
                    player.changeDimension(0);
                }
            } else if (!player.world.provider.canRespawnHere()) {
                if (stack.getItemDamage() == 1) {
                    player.changeDimension(player.world.provider.getRespawnDimension((EntityPlayerMP) player));
                } else {
                    return;
                }
            }
            BlockPos c = player.getBedLocation(player.dimension);
            if (c != null)
                c = EntityPlayer.getBedSpawnLocation(player.world, c, player.isSpawnForced(player.dimension));
            else
                c = player.world.provider.getRandomizedSpawnPoint();
            player.setPositionAndUpdate(c.getX() + 0.5D, c.getY() + 0.1D, c.getZ() + 0.5D);
        }
    }

    @Override
    public int getMaxItemUseDuration(final ItemStack i) {
        return 100;
    }

    @Override
    public EnumAction getItemUseAction(final ItemStack i) {
        return EnumAction.EAT;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        playerIn.setActiveHand(handIn);
        return ActionResult.newResult(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }

    @Override
    public String getUnlocalizedName(final ItemStack is) {
        switch (is.getItemDamage()) {
            case 2:
                return "item.overworldmirror";
            case 1:
                return "item.dimensionmirror";
        }
        return "item.magicmirror";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
        subItems.add(new ItemStack(this, 1, 0));
        subItems.add(new ItemStack(this, 1, 1));
        subItems.add(new ItemStack(this, 1, 2));
    }

}
