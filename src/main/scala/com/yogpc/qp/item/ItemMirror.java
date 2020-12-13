/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.yogpc.qp.item;

import java.lang.reflect.Method;
import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.version.VersionUtil;
import javax.annotation.Nonnull;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.end.DragonFightManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ItemMirror extends ItemFood {

    public static final int Dimension_Meta = 1;
    public static final int OverWorld_Meta = 2;
    private static final Method UPDATE_PLAYERS
        = ObfuscationReflectionHelper.findMethod(DragonFightManager.class, "func_186100_j", Void.TYPE);

    public ItemMirror() {
        super(0, 0, false);
        setHasSubtypes(true);
        setUnlocalizedName(QuarryPlus.Names.mirror);
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.mirror);
        setCreativeTab(QuarryPlusI.creativeTab());
        setAlwaysEdible();
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP playerMP = ((EntityPlayerMP) player);
            playerMP.dismountRidingEntity();
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (stack.getItemDamage() == OverWorld_Meta) {
                if (playerMP.dimension != 0) {
                    changeDimension(playerMP, server, 0);
                }
            } else if (!playerMP.getServerWorld().provider.canRespawnHere()) {
                if (stack.getItemDamage() == Dimension_Meta) {
                    changeDimension(playerMP, server, playerMP.getServerWorld().provider.getRespawnDimension(playerMP));
                } else {
                    return;
                }
            }
            //Player World has changed.
            BlockPos c = getBedLocation(playerMP, playerMP.dimension);
            playerMP.getServerWorld().getChunkProvider().provideChunk(c.getX() >> 4, c.getZ() >> 4);
            playerMP.setPositionAndUpdate(c.getX() + 0.5D, c.getY() + 0.1D, c.getZ() + 0.5D);
        }
    }

    private static void changeDimension(EntityPlayerMP playerMP, MinecraftServer server, int dimensionIn) {
        int preDim = playerMP.dimension;
        server.getPlayerList().transferPlayerToDimension(playerMP, dimensionIn, new DummyTeleporter(playerMP));
        if (server.getWorld(preDim).provider instanceof WorldProviderEnd) {
            WorldProviderEnd providerEnd = (WorldProviderEnd) server.getWorld(preDim).provider;
            Optional.ofNullable(providerEnd.getDragonFightManager()).ifPresent(dragonFightManager -> {
                try {
                    UPDATE_PLAYERS.invoke(dragonFightManager);
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    private BlockPos getBedLocation(EntityPlayerMP playerMP, int dim) {
        return Optional.ofNullable(playerMP.getBedLocation(dim))
            .map(p -> EntityPlayerMP.getBedSpawnLocation(playerMP.getServerWorld(), p, playerMP.isSpawnForced(dim)))
            .orElseGet(() -> playerMP.getServerWorld().provider.getRandomizedSpawnPoint());
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        ItemStack itemStack = super.onItemUseFinish(stack, worldIn, entityLiving);
        VersionUtil.setCountForce(itemStack, VersionUtil.getCount(itemStack) + 1); //prevent stack size from being shrink.
        return itemStack;
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
            case OverWorld_Meta:
                return "item.overworldmirror";
            case Dimension_Meta:
                return "item.dimensionmirror";
        }
        return super.getUnlocalizedName(is);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            items.add(new ItemStack(this, 1, 0));
            items.add(new ItemStack(this, 1, Dimension_Meta));
            items.add(new ItemStack(this, 1, OverWorld_Meta));
        }
    }

    private static class DummyTeleporter extends Teleporter {

        public DummyTeleporter(EntityPlayerMP playerMP) {
            super(playerMP.getServerWorld());
        }

        @Override
        public void placeInPortal(Entity entityIn, float rotationYaw) {
            int i = MathHelper.floor(entityIn.posX);
            int j = MathHelper.floor(entityIn.posY) - 1;
            int k = MathHelper.floor(entityIn.posZ);
            entityIn.setLocationAndAngles((double) i, (double) j, (double) k, entityIn.rotationYaw, 0.0F);
            entityIn.motionX = 0.0D;
            entityIn.motionY = 0.0D;
            entityIn.motionZ = 0.0D;
        }

        @Override
        public boolean placeInExistingPortal(Entity entityIn, float rotationYaw) {
            return false;
        }

        @Override
        public boolean makePortal(Entity entityIn) {
            return true;
        }

        @Override
        public void removeStalePortalLocations(long worldTime) {
        }
    }
}
