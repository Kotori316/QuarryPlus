package com.yogpc.qp.compat;

//import buildcraft.api.tools.IToolWrench;

import cofh.api.item.IToolHammer;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.ModAPIManager;

public class BuildCraftHelper {

    public static boolean isWrench(EntityPlayer player, ItemStack wrench, BlockPos pos) {
        if (ModAPIManager.INSTANCE.hasAPI(QuarryPlus.Optionals.COFH_item)) {
            if (IToolHammer.class.isInstance(wrench.getItem())) {
                IToolHammer wrenchItem = (IToolHammer) wrench.getItem();
                if (wrenchItem.isUsable(wrench, player, pos)) {
                    wrenchItem.toolUsed(wrench, player, pos);
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
}
