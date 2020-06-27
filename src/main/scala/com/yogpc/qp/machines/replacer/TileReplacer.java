package com.yogpc.qp.machines.replacer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.APacketTile;
import com.yogpc.qp.machines.base.IAttachable;
import com.yogpc.qp.machines.base.IAttachment;
import com.yogpc.qp.machines.base.IDebugSender;
import com.yogpc.qp.machines.base.IModule;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.machines.pump.TilePump;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.tuple.Pair;
import scala.Symbol;

import static com.yogpc.qp.machines.base.IAttachment.Attachments.REPLACER;
import static jp.t2v.lab.syntax.MapStreamSyntax.always_false;
import static jp.t2v.lab.syntax.MapStreamSyntax.byEntry;
import static jp.t2v.lab.syntax.MapStreamSyntax.byValue;
import static jp.t2v.lab.syntax.MapStreamSyntax.not;
import static jp.t2v.lab.syntax.MapStreamSyntax.values;

public class TileReplacer extends APacketTile implements IAttachment, IDebugSender, ITickableTileEntity {

    public static final Symbol SYMBOL = Symbol.apply("Replacer");
    private static final List<Predicate<BlockState>> rejects = new ArrayList<>(Arrays.asList(
        state -> state.getBlock().hasTileEntity(state),
        state -> state.getMaterial() == Material.MISCELLANEOUS,
        TilePump::isLiquid,
        always_false()
    ));
    private Direction facing;
    private boolean loading = false;
    private final ReplacerModule module = ReplacerModule.apply(this);

    public TileReplacer() {
        super(Holder.replacerType());
    }

    public void neighborChanged() {
        connection();
    }

    public void onPlaced() {
        connection();
    }

    private void connection() {
        if (hasWorld() && !Objects.requireNonNull(world).isRemote && enabled()) {
            Map.Entry<Direction, IAttachable> entry = Stream.of(Direction.values())
                .map(f -> Pair.of(f, world.getTileEntity(pos.offset(f))))
                .filter(byValue(IAttachable.class::isInstance))
                .map(values(IAttachable.class::cast))
                .filter(byEntry((facing, t) -> t.connect(facing.getOpposite(), REPLACER)))
                .findFirst()
                .orElse(Pair.of(null, IAttachable.dummy));
            if (entry.getKey() == null || entry.getValue().connectAttachment(entry.getKey().getOpposite(), REPLACER, false))
                setConnectTo(entry.getKey());
        }
    }

    @Override
    public void setConnectTo(@Nullable Direction facing) {
        this.facing = facing;
        if (hasWorld()) {
            BlockState state = Objects.requireNonNull(world).getBlockState(getPos());
            if (facing != null ^ state.get(QPBlock.WORKING())) {
                world.setBlockState(getPos(), state.with(QPBlock.WORKING(), facing != null));
            }
        }
    }

    @Override
    public IModule getModule() {
        return this.module;
    }

    @Override
    public void func_230337_a_(BlockState state, CompoundNBT compound) {
        super.func_230337_a_(state, compound);
        facing = Optional.of(compound.getString("facing")).filter(not(String::isEmpty)).map(Direction::byName).orElse(null);
        loading = true;
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putString("facing", Optional.ofNullable(facing).map(Direction::name).orElse(""));
        return super.write(compound);
    }

    public BlockState getReplaceState() {
        // Blocks should not be replaced with TileEntities.
        // Material.CIRCUITS is for blocks which isn't normal.
        // Liquid block cause crash.
        Predicate<BlockState> accept = rejects.stream().reduce(always_false(), Predicate::or).negate();
        return Optional.ofNullable(world).map(world -> world.getBlockState(pos.up()))
            .filter(s -> !s.isAir(world, pos.up())) // Avoid air. Written here to use world and pos.
            .filter(accept)
            .orElse(Holder.blockDummy().getDefaultState());
    }

    @Override
    public String getDebugName() {
        return TranslationKeys.replacer;
    }

    @Override
    public List<? extends ITextComponent> getDebugMessages() {
        return Stream.of(
            "Connect: " + facing,
            "toReplaceState: " + getReplaceState(),
            "Module: " + module
        ).map(StringTextComponent::new).collect(Collectors.toList());
    }

    @Override
    public void tick() {
        if (loading) {
            loading = false;
            connection();
        }
    }
}
