package com.yogpc.qp.machines.misc;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.packet.ClientSync;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public class CreativeGeneratorTile extends PowerTile implements MenuProvider, ClientSync {
    long sendEnergy = ONE_FE * 100000L;

    public CreativeGeneratorTile(BlockPos pos, BlockState state) {
        super(Holder.CREATIVE_GENERATOR_TYPE, pos, state);
        setEnergy(getMaxEnergy(), false);
    }

    @Override
    public void saveNbtData(CompoundTag nbt) {
        nbt.putLong("sendEnergy", sendEnergy);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        sendEnergy = nbt.getLong("sendEnergy");
    }

    static final BlockEntityTicker<CreativeGeneratorTile> TICKER = (world, pos, state, tile) ->
        Arrays.stream(Direction.values())
            .map(pos::relative)
            .map(world::getBlockEntity)
            .mapMulti(MapMulti.cast(PowerTile.class))
            .forEach(t -> t.addEnergy(tile.sendEnergy, false));

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CreativeGeneratorMenu(id, player, getBlockPos());
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        saveAdditional(tag);
        return tag;
    }
}
