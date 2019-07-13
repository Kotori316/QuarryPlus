package com.yogpc.qp.machines.exppump;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.APacketTile;
import com.yogpc.qp.machines.base.APowerTile;
import com.yogpc.qp.machines.base.IAttachable;
import com.yogpc.qp.machines.base.IAttachment;
import com.yogpc.qp.machines.base.IDebugSender;
import com.yogpc.qp.machines.base.IEnchantableTile;
import com.yogpc.qp.machines.base.IModule;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import static jp.t2v.lab.syntax.MapStreamSyntax.byValue;
import static jp.t2v.lab.syntax.MapStreamSyntax.values;
import static net.minecraft.state.properties.BlockStateProperties.ENABLED;

public class TileExpPump extends APacketTile implements IEnchantableTile, IDebugSender, IAttachment {
    @Nullable
    private EnumFacing mConnectTo;
    private ExpPumpModule module = ExpPumpModule.apply(v1 -> true, () -> this.unbreaking);

    private int fortune = 0;
    private int unbreaking = 0;
    private boolean silktouch = false;

    public TileExpPump() {
        super(Holder.expPumpTileType());
    }

    @Override
    public void setConnectTo(@Nullable EnumFacing connectTo) {
        mConnectTo = connectTo;
        if (hasWorld()) {
            IBlockState state = world.getBlockState(getPos());
            if (!working() == state.get(QPBlock.WORKING())) {
                world.setBlockState(pos, state.with(QPBlock.WORKING(), working()));
            }
        }
    }

    @Override
    public IModule getModule() {
        return module;
    }

    @Override
    public String getDebugName() {
        return TranslationKeys.exppump;
    }

    @Override
    public List<? extends ITextComponent> getDebugMessages() {
        return Stream.of(
            "Connection -> " + mConnectTo,
            Stream.of("Unbreaking -> " + unbreaking, "Fortune -> " + fortune, "Silktouch -> " + silktouch).reduce(combiner).get(),
            "XpAmount -> " + module.xp())
            .map(toComponentString)
            .collect(Collectors.toList());
    }

    @Override
    public void onLoad() {
        refreshConnection();
    }

    @Override
    public void G_ReInit() {
        refreshConnection();
    }

    private void refreshConnection() {
        if (hasWorld() && !world.isRemote) {
            Map.Entry<EnumFacing, IAttachable> entry = Stream.of(EnumFacing.values())
                .map(f -> Pair.of(f, world.getTileEntity(pos.offset(f))))
                .filter(byValue(t -> t instanceof IAttachable))
                .map(values(t -> ((IAttachable) t)))
                .filter(pair ->
                    pair.getValue().connect(pair.getKey().getOpposite(), Attachments.EXP_PUMP)
                )
                .findFirst()
                .orElse(Pair.of(null, IAttachable.dummy));

            if (entry.getKey() == null || entry.getValue().connectAttachment(entry.getKey().getOpposite(), Attachments.EXP_PUMP, false))
                setConnectTo(entry.getKey());
        }
    }

    public void addXp(int amount) {
        module.xp_$eq(module.xp() + amount);
        if (module.xp() > 0 ^ world.getBlockState(pos).get(ENABLED)) {
            IBlockState state = world.getBlockState(pos).with(ENABLED, module.xp() > 0);
            world.setBlockState(pos, state);
        }
    }

    public long getEnergyUse(int amount) {
        return amount * 10 * APowerTile.MicroJtoMJ / (1 + unbreaking);
    }

    public void onActivated(World worldIn, BlockPos pos, EntityPlayer playerIn) {
        if (module.xp() > 0) {
            int xp = EntityXPOrb.getXPSplit(module.xp());
            EntityXPOrb orb = new EntityXPOrb(worldIn, playerIn.posX, playerIn.posY, playerIn.posZ, xp);
            worldIn.spawnEntity(orb);
            addXp(-xp);
        }
    }

    public void onBreak(World worldIn) {
        if (module.xp() > 0) {
            EntityXPOrb xpOrb = new EntityXPOrb(worldIn, pos.getX(), pos.getY(), pos.getZ(), module.xp());
            worldIn.spawnEntity(xpOrb);
        }
    }

    @Override
    public void read(NBTTagCompound compound) {
        super.read(compound);
        int connectID = compound.getByte("mConnectTo");
        mConnectTo = connectID < 0 ? null : EnumFacing.byIndex(connectID);
        module.xp_$eq(compound.getInt("xpAmount"));
        this.silktouch = compound.getBoolean("silktouch");
        this.fortune = compound.getByte("fortune");
        this.unbreaking = compound.getByte("unbreaking");
    }

    @Override
    public NBTTagCompound write(NBTTagCompound compound) {
        compound.putByte("mConnectTo", Optional.ofNullable(mConnectTo).map(Enum::ordinal).orElse(-1).byteValue());
        compound.putInt("xpAmount", module.xp());
        compound.putBoolean("silktouch", this.silktouch);
        compound.putByte("fortune", (byte) this.fortune);
        compound.putByte("unbreaking", (byte) this.unbreaking);
        return super.write(compound);
    }

    @Nonnull
    @Override
    public Map<ResourceLocation, Integer> getEnchantments() {
        Map<ResourceLocation, Integer> map = new HashMap<>();
        if (fortune > 0) map.put(FortuneID, fortune);
        if (unbreaking > 0) map.put(UnbreakingID, unbreaking);
        if (silktouch) map.put(SilktouchID, 1);
        return map;
    }

    @Override
    public void setEnchantment(ResourceLocation id, short val) {
        if (id.equals(FortuneID)) {
            fortune = val;
        } else if (id.equals(UnbreakingID)) {
            unbreaking = val;
        } else if (id.equals(SilktouchID)) {
            silktouch = val > 0;
        }
    }

    public boolean working() {
        return mConnectTo != null;
    }

    public int getXpAmount() {
        return module.xp();
    }

}
