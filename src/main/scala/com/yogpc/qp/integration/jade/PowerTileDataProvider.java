package com.yogpc.qp.integration.jade;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.PowerTile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

enum PowerTileDataProvider implements IServerDataProvider<BlockEntity>, IBlockComponentProvider {
    INSTANCE;

    /**
     * In this method, I will send these data.
     * <ul>
     *     <li>Current Energy</li>
     *     <li>Max Energy</li>
     * </ul>
     */
    @Override
    public void appendServerData(CompoundTag data, ServerPlayer serverPlayer, Level level, BlockEntity tile, boolean b) {
        if (tile instanceof PowerTile powerTile) {
            data.putLong("currentEnergy", powerTile.getEnergy());
            data.putLong("maxEnergy", powerTile.getMaxEnergy());
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof PowerTile) {
            var tag = accessor.getServerData();
            long actualCurrentEnergy = tag.contains("currentEnergy", Tag.TAG_LONG) ? tag.getLong("currentEnergy") : 0;
            long currentEnergy = QuarryPlus.config.common.debug ? actualCurrentEnergy : Math.max(0L, actualCurrentEnergy);
            long maxEnergy = tag.contains("maxEnergy", Tag.TAG_LONG) ? tag.getLong("maxEnergy") : 0;
            String percent = String.format("Energy: %.1f%%", 100d * currentEnergy / maxEnergy);
            String energy = String.format("%d / %d FE", currentEnergy / PowerTile.ONE_FE, maxEnergy / PowerTile.ONE_FE);
            tooltip.add(Component.literal(percent));
            tooltip.add(Component.literal(energy));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation(QuarryPlus.modID, "jade_plugin");
    }
}
