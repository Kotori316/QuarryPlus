package com.yogpc.qp.fabric.machine.quarry;

import com.yogpc.qp.fabric.ExtendedGeneralScreenHandler;
import com.yogpc.qp.machine.GeneralScreenHandler;
import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.quarry.QuarryEntity;
import com.yogpc.qp.machine.quarry.SolidFuelQuarryBlock;
import com.yogpc.qp.machine.quarry.SolidFuelQuarryMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class SolidFuelQuarryBlockFabric extends SolidFuelQuarryBlock {
    public SolidFuelQuarryBlockFabric() {
        super(QuarryItemFabric::new);
    }

    @Override
    protected QpBlock createBlock(Properties properties) {
        return new SolidFuelQuarryBlockFabric();
    }

    @Override
    protected void openGui(ServerPlayer player, Level level, BlockPos pos, QuarryEntity quarry) {
        player.openMenu(new ExtendedGeneralScreenHandler<>(new GeneralScreenHandler<>(quarry, SolidFuelQuarryMenu::new)));
    }
}
