package com.yogpc.qp.compat;

import java.util.Optional;

import buildcraft.api.facades.FacadeAPI;
import buildcraft.api.tools.IToolWrench;
import cofh.api.item.IToolHammer;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModAPIManager;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class BuildcraftHelper {

    public static boolean isWrench(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) {
        if (ModAPIManager.INSTANCE.hasAPI(QuarryPlus.Optionals.COFH_item)) {
            if (IToolHammer.class.isInstance(wrench.getItem())) {
                IToolHammer wrenchItem = (IToolHammer) wrench.getItem();
                if (wrenchItem.isUsable(wrench, player, rayTrace.getBlockPos())) {
                    wrenchItem.toolUsed(wrench, player, rayTrace.getBlockPos());
                    return true;
                }
            }
        }
        if (ModAPIManager.INSTANCE.hasAPI(QuarryPlus.Optionals.Buildcraft_tools)) {
            if (IToolWrench.class.isInstance(wrench.getItem())) {
                IToolWrench wrenchItem = (IToolWrench) wrench.getItem();
                if (wrenchItem.canWrench(player, hand, wrench, rayTrace)) {
                    wrenchItem.wrenchUsed(player, hand, wrench, rayTrace);
                    return true;
                }
            }
        }
        return false;
    }

    @net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.Buildcraft_facades)
    public static void disableIC2Facade() {
        if (Loader.isModLoaded(QuarryPlus.Optionals.IC2_modID))
            Optional.ofNullable(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("ic2:te"))).ifPresent(FacadeAPI::disableBlock);

    }
}
