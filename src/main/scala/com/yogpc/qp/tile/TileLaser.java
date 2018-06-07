/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp.tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import buildcraft.api.mj.ILaserTarget;
import buildcraft.api.mj.MjAPI;
import com.yogpc.qp.PowerManager;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.block.BlockLaser;
import com.yogpc.qp.gui.TranslationKeys;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.laser.LaserAverageMessage;
import com.yogpc.qp.packet.laser.LaserMessage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The plus machine of {@link buildcraft.silicon.tile.TileLaser}
 */
public class TileLaser extends APowerTile implements IEnchantableTile, IDebugSender {
    private int ticks;

    /**
     * To target table.
     */
    public Vec3d[] lasers = new Vec3d[0];
    private final List<BlockPos> targets = new ArrayList<>();

    protected byte unbreaking;
    protected byte fortune;
    protected byte efficiency;
    protected boolean silktouch;

    public TileLaser() {
        PowerManager.configureLaser(this, this.efficiency, this.unbreaking);
        bcLoaded = Loader.isModLoaded(QuarryPlus.Optionals.Buildcraft_modID);
    }

    @Override
    public void update() {
        super.update();
        if (!bcLoaded || getWorld().isRemote)
            return;

        ticks += 1;
        targets.removeIf(pos1 -> !(getWorld().getTileEntity(pos1) instanceof ILaserTarget));

        if (ticks % (10 + world.rand.nextInt(20)) == 0 || targets.isEmpty()) {
            findTable();
        }

        if (ticks % (5 + world.rand.nextInt(10)) == 0 || targets.isEmpty()) {
            updateLaser();
            PacketHandler.sendToAround(LaserMessage.create(this), getWorld(), getPos());
        }

        if (!targets.isEmpty()) {
            long maxPower = (long) (PowerManager.simurateEnergyLaser(this, this.unbreaking, this.fortune, this.silktouch, this.efficiency) * MjAPI.MJ);
            List<ILaserTarget> targetList = targets.stream()
                .map(getWorld()::getTileEntity)
                .map(ILaserTarget.class::cast)
                .filter(t -> !Objects.requireNonNull(t).isInvalidTarget() && t.getRequiredLaserPower() > 0)
                .collect(Collectors.toList());
            if (!targetList.isEmpty()) {
                long each = maxPower / targetList.size();
                targetList.forEach(iLaserTarget -> {
                    long joules = Math.min(each, iLaserTarget.getRequiredLaserPower());
                    long excess = iLaserTarget.receiveLaserPower(joules);
                    PowerManager.useEnergyLaser(this, (double) (joules - excess) / MjAPI.MJ, this.unbreaking, this.fortune, this.silktouch, false);
                });
                pushPower(each / MjAPI.MJ);
            }
        }

        if (ticks % 20 == 0 /*&& !getWorld().isRemote*/) {
            PacketHandler.sendToAround(LaserAverageMessage.create(this), getWorld(), getPos());
        }
    }

    private void updateLaser() {
        if (!targets.isEmpty()) {
            Vec3d[] vec3ds = new Vec3d[targets.size()];
            for (int i = 0; i < targets.size(); i++) {
                BlockPos targetPos = targets.get(i);
                vec3ds[i] = new Vec3d(targetPos).addVector(
                    (5 + getWorld().rand.nextInt(6) + 0.5) / 16D,
                    9 / 16D,
                    (5 + getWorld().rand.nextInt(6) + 0.5) / 16D
                );
            }
            lasers = vec3ds;
        } else {
            lasers = new Vec3d[0];
        }
    }

    protected void findTable() {
        removeLaser();
        EnumFacing facing = getWorld().getBlockState(getPos()).getValue(BlockLaser.FACING);

        int minX = getPos().getX() - 5 * (this.fortune + 1);
        int minY = getPos().getY() - 5 * (this.fortune + 1);
        int minZ = getPos().getZ() - 5 * (this.fortune + 1);
        int maxX = getPos().getX() + 5 * (this.fortune + 1);
        int maxY = getPos().getY() + 5 * (this.fortune + 1);
        int maxZ = getPos().getZ() + 5 * (this.fortune + 1);

        switch (facing) {
            case WEST:
                maxX = getPos().getX();
                break;
            case EAST:
                minX = getPos().getX();
                break;
            case DOWN:
                maxY = getPos().getY();
                break;
            case UP:
                minY = getPos().getY();
                break;
            case NORTH:
                maxZ = getPos().getZ();
                break;
            default:
            case SOUTH:
                minZ = getPos().getZ();
                break;
        }

        this.targets.clear();
        BlockPos.getAllInBoxMutable(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ)).forEach(mutableBlockPos -> {
            TileEntity tileEntity = getWorld().getTileEntity(mutableBlockPos);
            if (ILaserTarget.class.isInstance(tileEntity)) {
                ILaserTarget target = (ILaserTarget) tileEntity;
                if (!target.isInvalidTarget() && target.getRequiredLaserPower() > 0) {
                    targets.add(mutableBlockPos.toImmutable());
                }
            }
        });

        if (this.targets.isEmpty())
            return;
        if (!this.silktouch) {
            BlockPos laserTarget = this.targets.get(getWorld().rand.nextInt(this.targets.size()));
            this.targets.clear();
            this.targets.add(laserTarget);
        }
        lasers = new Vec3d[targets.size()];
    }

    protected void removeLaser() {
        if (this.lasers != null)
            for (int i = 0; i < this.lasers.length; i++)
                this.lasers[i] = null;
    }

    private final double[] tp = new double[100];
    public double pa;
    private int pi = 0;

    private void pushPower(final double received) {
        this.pa -= this.tp[this.pi];
        this.pa += received;
        this.tp[this.pi] = received;
        this.pi++;

        if (this.pi == this.tp.length)
            this.pi = 0;
    }

    public static final ResourceLocation[] LASER_TEXTURES = new ResourceLocation[]{
        new ResourceLocation(QuarryPlus.modID, "textures/entities/laser_1.png"),
        new ResourceLocation(QuarryPlus.modID, "textures/entities/laser_2.png"),
        new ResourceLocation(QuarryPlus.modID, "textures/entities/laser_3.png"),
        new ResourceLocation(QuarryPlus.modID, "textures/entities/laser_4.png"),
        new ResourceLocation(QuarryPlus.modID, "textures/entities/stripes.png")};

    public ResourceLocation getTexture() {
        final double avg = getAvg();

        if (avg <= 1.0)
            return LASER_TEXTURES[0];
        else if (avg <= 2.0)
            return LASER_TEXTURES[1];
        else if (avg <= 3.0)
            return LASER_TEXTURES[2];
        else
            return LASER_TEXTURES[3];
    }

    public double getAvg() {
        return this.pa / 100;
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbttc) {
        super.readFromNBT(nbttc);
        this.fortune = nbttc.getByte("fortune");
        this.efficiency = nbttc.getByte("efficiency");
        this.unbreaking = nbttc.getByte("unbreaking");
        this.silktouch = nbttc.getBoolean("silktouch");
        PowerManager.configureLaser(this, this.efficiency, this.unbreaking);
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbttc) {
        nbttc.setByte("fortune", this.fortune);
        nbttc.setByte("efficiency", this.efficiency);
        nbttc.setByte("unbreaking", this.unbreaking);
        nbttc.setBoolean("silktouch", this.silktouch);
        return super.writeToNBT(nbttc);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        removeLaser();
    }

    @Override
    protected boolean isWorking() {
        return !targets.isEmpty();
    }

    @Override
    public Map<Integer, Integer> getEnchantments() {
        final Map<Integer, Integer> ret = new HashMap<>();
        if (this.efficiency > 0)
            ret.put(EfficiencyID, (int) this.efficiency);
        if (this.fortune > 0)
            ret.put(FortuneID, (int) this.fortune);
        if (this.unbreaking > 0)
            ret.put(UnbreakingID, (int) this.unbreaking);
        if (this.silktouch)
            ret.put(SilktouchID, 1);
        return ret;
    }

    @Override
    public void setEnchantent(final short id, final short val) {
        if (id == EfficiencyID)
            this.efficiency = (byte) val;
        else if (id == FortuneID)
            this.fortune = (byte) val;
        else if (id == UnbreakingID)
            this.unbreaking = (byte) val;
        else if (id == SilktouchID)
            this.silktouch = val > 0;
    }

    @Override
    public void G_reinit() {
        PowerManager.configureLaser(this, this.efficiency, this.unbreaking);
    }

    @Override
    public List<ITextComponent> getDebugmessages() {
        List<ITextComponent> list = new ArrayList<>();
        list.add(toComponentString.apply("Targets"));
        targets.stream()
            .map(pos1 -> String.format("x=%d, y=%d, z=%d", pos1.getX(), pos1.getY(), pos1.getZ()))
            .reduce(combiner).map(toComponentString)
            .ifPresent(list::add);
        list.add(toComponentString.apply("Lasers"));
        Stream.of(lasers).filter(nonNull)
            .map(pos1 -> String.format("x=%s, y=%s, z=%s", pos1.x, pos1.y, pos1.z))
            .reduce(combiner).map(toComponentString)
            .ifPresent(list::add);
        return list;
    }

    @Override
    public String getDebugName() {
        return TranslationKeys.laser;
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        double minX = getPos().getX();
        double minY = getPos().getY();
        double minZ = getPos().getZ();

        double maxX = getPos().getX() + 1;
        double maxY = getPos().getY() + 1;
        double maxZ = getPos().getZ() + 1;
        /*if (this.lasers != null) //neccesary?
            for (final Vec3d p : this.lasers) {
                if (p == null)
                    continue;
                final double xn = p.x - 0.0625;
                final double xx = xn + 0.125;
                final double zn = p.z - 0.0625;
                final double zx = zn + 0.125;
                if (xn < minX)
                    minX = xn;
                if (xx > maxX)
                    maxX = xx;
                if (p.y < minY)
                    minY = p.y;
                if (p.y > maxY)
                    maxY = p.y;
                if (zn < minZ)
                    minZ = zn;
                if (zx > maxZ)
                    maxZ = zx;
            }*/
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
