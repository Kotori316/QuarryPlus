package com.yogpc.qp.neoforge.machine.quarry;

import com.yogpc.qp.machine.GeneralScreenHandler;
import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.module.ModuleContainer;
import com.yogpc.qp.machine.quarry.QuarryBlock;
import com.yogpc.qp.machine.quarry.QuarryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class QuarryBlockNeoForge extends QuarryBlock {
    public QuarryBlockNeoForge() {
        super(QuarryItemNeoForge::new);
    }

    @Override
    protected QpBlock createBlock(Properties properties) {
        return new QuarryBlockNeoForge();
    }

    @Override
    protected void openGui(ServerPlayer player, Level level, BlockPos pos, QuarryEntity quarry) {
        player.openMenu(new GeneralScreenHandler<>(quarry, ModuleContainer::new), pos);
    }
}
