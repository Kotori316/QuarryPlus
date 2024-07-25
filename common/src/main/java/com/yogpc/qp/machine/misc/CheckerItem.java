package com.yogpc.qp.machine.misc;

import com.yogpc.qp.InCreativeTabs;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.PowerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public abstract class CheckerItem extends Item implements InCreativeTabs {
    public static final String NAME = "status_checker";
    public final ResourceLocation name = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, NAME);

    public CheckerItem() {
        super(new Properties());
    }

    protected final InteractionResult outputLog(Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof PowerEntity e) {
            if (!level.isClientSide()) {
                e.checkerLogs().forEach(c -> player.displayClientMessage(c, false));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
