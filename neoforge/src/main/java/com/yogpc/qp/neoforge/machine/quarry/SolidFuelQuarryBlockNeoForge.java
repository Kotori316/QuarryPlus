package com.yogpc.qp.neoforge.machine.quarry;

import com.yogpc.qp.machine.GeneralScreenHandler;
import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.quarry.QuarryEntity;
import com.yogpc.qp.machine.quarry.SolidFuelQuarryBlock;
import com.yogpc.qp.machine.quarry.SolidFuelQuarryMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class SolidFuelQuarryBlockNeoForge extends SolidFuelQuarryBlock {
    public SolidFuelQuarryBlockNeoForge() {
        super(QuarryItemNeoForge::new);
    }

    @Override
    protected QpBlock createBlock(Properties properties) {
        return new SolidFuelQuarryBlockNeoForge();
    }

    @Override
    protected void openGui(ServerPlayer player, Level level, BlockPos pos, QuarryEntity quarry) {
        player.openMenu(new GeneralScreenHandler<>(quarry, SolidFuelQuarryMenu::new), pos);
    }
}
