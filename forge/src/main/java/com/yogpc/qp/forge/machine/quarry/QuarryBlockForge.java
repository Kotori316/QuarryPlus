package com.yogpc.qp.forge.machine.quarry;

import com.yogpc.qp.machine.GeneralScreenHandler;
import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.module.ModuleContainer;
import com.yogpc.qp.machine.quarry.QuarryBlock;
import com.yogpc.qp.machine.quarry.QuarryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class QuarryBlockForge extends QuarryBlock {
    public QuarryBlockForge() {
        super(QuarryItemForge::new);
    }

    @Override
    protected QpBlock createBlock(Properties properties) {
        return new QuarryBlockForge();
    }

    @Override
    protected void openGui(ServerPlayer player, Level level, BlockPos pos, QuarryEntity quarry) {
        player.openMenu(new GeneralScreenHandler<>(quarry, ModuleContainer::new), pos);
    }
}
