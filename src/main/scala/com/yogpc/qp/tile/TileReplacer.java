package com.yogpc.qp.tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.block.BlockPump;
import com.yogpc.qp.compat.InvUtils;
import com.yogpc.qp.gui.TranslationKeys;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.apache.commons.lang3.tuple.Pair;
import scala.Symbol;

import static com.yogpc.qp.tile.IAttachment.Attachments.REPLACER;
import static jp.t2v.lab.syntax.MapStreamSyntax.*;

public class TileReplacer extends APacketTile implements IAttachment, IDebugSender {

    public static final Symbol SYMBOL = Symbol.apply("Replacer");
    private static final List<Predicate<IBlockState>> rejects = new ArrayList<>(Arrays.asList(
        state -> Item.getItemFromBlock(state.getBlock()) == Items.AIR,
        state -> state.getBlock().hasTileEntity(state),
        state -> state.getMaterial() == Material.CIRCUITS,
        TilePump::isLiquid,
        always_false()
    ));
    private EnumFacing facing;
    private boolean loading = false;
    private IBlockState toReplaceState = Blocks.AIR.getDefaultState();
    private final ReplacerModule module = ReplacerModule.apply(this);

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
                .filter(accept)
                .orElse(QuarryPlusI.dummyBlock().getDefaultState());
        }
    }

    @Override
    public void setConnectTo(@Nullable EnumFacing facing) {
        this.facing = facing;
        if (hasWorld()) {
            IBlockState state = world.getBlockState(pos);
            if (facing != null ^ state.getValue(BlockPump.CONNECTED)) {
                InvUtils.setNewState(world, pos, this, state.withProperty(BlockPump.CONNECTED, facing != null));
            }
        }
    }

    @Override
    public IModule getModule() {
        return this.module;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        facing = Optional.of(compound.getString("facing")).filter(not(String::isEmpty)).map(EnumFacing::byName).orElse(null);
        loading = true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setString("facing", Optional.ofNullable(facing).map(EnumFacing::name).orElse(""));
        return super.writeToNBT(compound);
    }

    public IBlockState getReplaceState() {
        return toReplaceState;
    }

    @Override
    public String getDebugName() {
        return TranslationKeys.replacer;
    }

    @Override
    public List<? extends ITextComponent> getDebugMessages() {
        return Stream.of(
            "Connect: " + facing,
            "toReplaceState: " + toReplaceState,
            "Module: " + module
        ).map(TextComponentString::new).collect(Collectors.toList());
    }
}
