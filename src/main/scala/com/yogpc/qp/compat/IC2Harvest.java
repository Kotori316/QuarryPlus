package com.yogpc.qp.compat;

import com.yogpc.qp.block.ADismCBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class IC2Harvest {

    // IC2 hack to drop blocks when removed by NEW Wrench.
    @SubscribeEvent
    public static void canHarvestBlock(PlayerEvent.HarvestCheck event) {
        if (event.canHarvest()) return; // Do nothing.
        if (event.getTargetBlock().getBlock() instanceof ADismCBlock) {
            // Our block!
            ItemStack stack = event.getEntityPlayer().inventory.getStackInSlot(event.getEntityPlayer().inventory.currentItem);
            if (!stack.isEmpty() && stack.getItem() == InvUtils.ic2_wrench_new) {
                event.setCanHarvest(true);
            }
        }
    }
}
