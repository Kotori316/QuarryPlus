package com.yogpc.qp.compat;

/*import buildcraft.api.tools.IToolWrench;*/

import cofh.api.item.IToolHammer;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.ModAPIManager;

public class BuildcraftHelper {

    public static boolean isWrench(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) {
        if (ModAPIManager.INSTANCE.hasAPI(QuarryPlus.Optionals.COFH_item)) {
            if (wrench != null && IToolHammer.class.isInstance(wrench.getItem())) {
                IToolHammer wrenchItem = (IToolHammer) wrench.getItem();
                if (wrenchItem.isUsable(wrench, player, rayTrace.getBlockPos())) {
                    wrenchItem.toolUsed(wrench, player, rayTrace.getBlockPos());
                    return true;
                }
            }
        }
        /*if (ModAPIManager.INSTANCE.hasAPI(QuarryPlus.Optionals.Buildcraft_tools)) {
            if (IToolWrench.class.isInstance(wrench.getItem())) {
                IToolWrench wrenchItem = (IToolWrench) wrench.getItem();
                if (wrenchItem.canWrench(player, hand, wrench, rayTrace)) {
                    wrenchItem.wrenchUsed(player, hand, wrench, rayTrace);
                    return true;
                }
            }
        }*/
        return false;
    }

    @net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.Buildcraft_facades)
    public static void disableFacade() {
        /*if (Loader.isModLoaded(QuarryPlus.Optionals.IC2_modID))
            Optional.ofNullable(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("ic2:te"))).ifPresent(FacadeAPI::disableBlock);
        QuarryPlusI.blockList().forEach(FacadeAPI::disableBlock);*/
    }
}
