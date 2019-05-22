package com.yogpc.qp.machines.replacer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.yogpc.qp.machines.base.APacketTile;
import com.yogpc.qp.machines.base.IAttachable;
import com.yogpc.qp.machines.base.IAttachment;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.machines.pump.TilePump;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.Pair;
import scala.Symbol;

import static com.yogpc.qp.machines.base.IAttachment.Attachments.REPLACER;
import static jp.t2v.lab.syntax.MapStreamSyntax.always_false;
import static jp.t2v.lab.syntax.MapStreamSyntax.byEntry;
import static jp.t2v.lab.syntax.MapStreamSyntax.byValue;
import static jp.t2v.lab.syntax.MapStreamSyntax.not;
import static jp.t2v.lab.syntax.MapStreamSyntax.values;

public class TileReplacer extends APacketTile implements IAttachment {

    public static final Symbol SYMBOL = Symbol.apply("Replacer");
    private static final List<Predicate<IBlockState>> rejects = new ArrayList<>(Arrays.asList(
        state -> state.getBlock().hasTileEntity(state),
        state -> state.getMaterial() == Material.CIRCUITS,
        TilePump::isLiquid,
        always_false()
    ));
    private EnumFacing facing;
    private boolean loading = false;
    private IBlockState toReplaceState = Blocks.AIR.getDefaultState();

    public TileReplacer() {
        super(Holder.replacerType());
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
        if (hasWorld() && !world.isRemote && enabled()) {
            Map.Entry<EnumFacing, IAttachable> entry = Stream.of(EnumFacing.values())
                .map(f -> Pair.of(f, world.getTileEntity(pos.offset(f))))
                .filter(byValue(IAttachable.class::isInstance))
                .map(values(IAttachable.class::cast))
                .filter(byEntry((facing, t) -> t.connect(facing.getOpposite(), REPLACER)))
                .findFirst()
                .orElse(Pair.of(null, IAttachable.dummy));
            if (entry.getKey() == null || entry.getValue().connectAttachment(entry.getKey().getOpposite(), REPLACER, false))
                setConnectTo(entry.getKey());

            // Blocks should not be replaced with TileEntities.
            // Material.CIRCUITS is for blocks which isn't normal.
            // Liquid block cause crash.
            Predicate<IBlockState> accept = rejects.stream().reduce(always_false(), Predicate::or).negate();
            toReplaceState = Optional.of(world.getBlockState(pos.up()))
                .filter(s -> !s.isAir(world, pos.up())) // Avoid air. Written here to use world and pos.
                .filter(accept)
                .orElse(Holder.blockDummy().getDefaultState());
        }
    }

    @Override
    public void setConnectTo(@Nullable EnumFacing facing) {
        this.facing = facing;
        if (hasWorld()) {
            IBlockState state = world.getBlockState(getPos());
            if (facing != null ^ state.get(QPBlock.WORKING())) {
                world.setBlockState(getPos(), state.with(QPBlock.WORKING(), facing != null));
            }
        }
    }

    @Override
    public void read(NBTTagCompound compound) {
        super.read(compound);
        compound.setString("facing", Optional.ofNullable(facing).map(EnumFacing::name).orElse(""));
        loading = true;
    }

    @Override
    public NBTTagCompound write(NBTTagCompound compound) {
        facing = Optional.of(compound.getString("facing")).filter(not(String::isEmpty)).map(EnumFacing::byName).orElse(null);
        return super.write(compound);
    }

    public IBlockState getReplaceState() {
        return toReplaceState;
    }

}
