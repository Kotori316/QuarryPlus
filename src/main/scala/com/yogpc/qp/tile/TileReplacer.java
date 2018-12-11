package com.yogpc.qp.tile;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.block.BlockPump;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.Pair;
import scala.Symbol;

import static com.yogpc.qp.tile.IAttachment.Attachments.REPLACER;
import static jp.t2v.lab.syntax.MapStreamSyntax.byEntry;
import static jp.t2v.lab.syntax.MapStreamSyntax.byValue;
import static jp.t2v.lab.syntax.MapStreamSyntax.keyToAny;
import static jp.t2v.lab.syntax.MapStreamSyntax.not;
import static jp.t2v.lab.syntax.MapStreamSyntax.values;

public class TileReplacer extends APacketTile implements IAttachment {

    public static final Symbol SYMBOL = Symbol.apply("Replacer");
    private EnumFacing facing;
    private boolean loading = false;
    private IBlockState toReplaceState = Blocks.AIR.getDefaultState();

    @Override
    protected Symbol getSymbol() {
        return SYMBOL;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (loading) {
            loading = false;
            connection();
        }
    }

    public void neighborChanged() {
        connection();
    }

    public void onPlaced() {
        connection();
    }

    private void connection() {
        if (hasWorld() && !world.isRemote && !machineDisabled) {
            EnumFacing enumFacing = Stream.of(EnumFacing.VALUES)
                .map(f -> Pair.of(f, world.getTileEntity(pos.offset(f))))
                .filter(byValue(IAttachable.class::isInstance))
                .map(values(IAttachable.class::cast))
                .filter(byEntry((facing, t) -> t.connect(facing.getOpposite(), REPLACER)))
                .map(keyToAny(Function.identity()))
                .findFirst()
                .orElse(null);
            setConnectTo(enumFacing);
            IBlockState state = world.getBlockState(pos.up());
            if (Item.getItemFromBlock(state.getBlock()) == Items.AIR ||
                state.getBlock().hasTileEntity(state) ||
                state.getMaterial() == Material.CIRCUITS ||
                TilePump.isLiquid(state)) {
                // Blocks should not be replaced with TileEntities.
                // Material.CIRCUITS is for blocks which isn't normal.
                // Liquid block cause crash.
                toReplaceState = QuarryPlusI.dummyBlock().getDefaultState();
            } else {
                toReplaceState = state;
            }
        }
    }

    @Override
    public void setConnectTo(@Nullable EnumFacing facing) {
        this.facing = facing;
        if (hasWorld()) {
            IBlockState state = world.getBlockState(pos);
            if (facing != null ^ state.getValue(BlockPump.CONNECTED)) {
                world.setBlockState(pos, state.withProperty(BlockPump.CONNECTED, facing != null));
                validate();
                world.setTileEntity(pos, this);
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        compound.setString("facing", Optional.ofNullable(facing).map(EnumFacing::name).orElse(""));
        loading = true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        facing = Optional.of(compound.getString("facing")).filter(not(String::isEmpty)).map(EnumFacing::byName).orElse(null);
        return super.writeToNBT(compound);
    }

    public IBlockState getReplaceState() {
        return toReplaceState;
    }
}
