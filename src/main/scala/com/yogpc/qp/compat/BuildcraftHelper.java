package com.yogpc.qp.compat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;

public class BuildcraftHelper {

    public static boolean isWrench(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) {
        /*if (Loader.isModLoaded(QuarryPlus.Optionals.COFH_modID)) {
            if (wrench.getItem() instanceof IToolHammer) {
                IToolHammer wrenchItem = (IToolHammer) wrench.getItem();
                if (wrenchItem.isUsable(wrench, player, rayTrace.getBlockPos())) {
                    wrenchItem.toolUsed(wrench, player, rayTrace.getBlockPos());
                    return true;
                }
            }
        }
        // TODO change to net.minecraftforge.fml.common.ModAPIManager
        if (Loader.isModLoaded(QuarryPlus.Optionals.Buildcraft_tools)) {
            if (wrench.getItem() instanceof IToolWrench) {
                IToolWrench wrenchItem = (IToolWrench) wrench.getItem();
                if (wrenchItem.canWrench(player, hand, wrench, rayTrace)) {
                    wrenchItem.wrenchUsed(player, hand, wrench, rayTrace);
                    return true;
                }
            }
        }*/
        return wrench.getItem() == Items.STICK;
    }
//
//    @net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.Buildcraft_facades)
//    public static void disableFacade() {
//        if (Loader.isModLoaded(QuarryPlus.Optionals.IC2_modID))
//            Optional.ofNullable(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("ic2:te")))
//                .filter(Predicate.isEqual(Blocks.AIR).negate())
//                .ifPresent(FacadeAPI::disableBlock);
//        QuarryPlusI.blockList().forEach(FacadeAPI::disableBlock);
//    }
}
