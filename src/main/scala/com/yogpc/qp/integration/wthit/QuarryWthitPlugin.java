package com.yogpc.qp.integration.wthit;

import com.yogpc.qp.machines.PowerTile;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;

@SuppressWarnings("unused")
public final class QuarryWthitPlugin implements IWailaPlugin {
    @Override
    public void register(IRegistrar registrar) {
        PowerTileDataProvider provider = new PowerTileDataProvider();
        registrar.addComponent(provider, TooltipPosition.BODY, PowerTile.class);
        registrar.addBlockData(provider, PowerTile.class);
    }
}
