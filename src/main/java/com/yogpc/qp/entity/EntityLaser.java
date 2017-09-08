package com.yogpc.qp.entity;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

/**
 * TODO remove to change tileentity render.
 */
@Deprecated
public class EntityLaser extends Entity {
    public static final String NAME = "quarrylaser";
    public double iSize, jSize, kSize;
    public final LaserType texture;
    public final boolean isLink;

    @SuppressWarnings("unused")
    public EntityLaser(World worldIn) {
        this(worldIn, 0, 0, 0, 0, 0, 0, LaserType.DRILL, false);
        width = 0.1f;
        height = .1f;
    }

    public EntityLaser(final World world, final double x, final double y, final double z,
                       final double xSize, final double ySize, final double zSize, final LaserType tex, boolean isLink) {
        super(world);
        this.preventEntitySpawning = false;
        this.noClip = true;
        this.isImmuneToFire = true;
        this.isLink = isLink;
        this.iSize = xSize;
        this.jSize = ySize;
        this.kSize = zSize;
        this.motionX = 0.0;
        this.motionY = 0.0;
        this.motionZ = 0.0;
        setSize(.1f, .1f);
        setPositionAndRotation(x, y, z, 0, 0);
        this.texture = tex;
        if (Config.content().debug())
            QuarryPlus.LOGGER.info(String.format("Client = %s Laser spwaned at %s, %s, %s size %s, %s, %s", world.isRemote, x, y, x, xSize, ySize, zSize));
    }

    @Override
    public void setPosition(final double x, final double y, final double z) {
        super.setPosition(x, y, z);
        if (isLink)
            setEntityBoundingBox(new AxisAlignedBB(this.posX, this.posY, this.posZ, this.posX + this.iSize, this.posY + this.jSize, this.posZ + this.kSize));
        else
            setEntityBoundingBox(
                    new AxisAlignedBB(posX - iSize, posY - jSize, posZ - kSize, posX + iSize, posY + jSize, posZ + kSize)
            );
    }

    @Override
    public void move(MoverType type, double x, double y, double z) {
        setPosition(this.posX + x, this.posY + y, this.posZ + z);
    }

    @Override
    protected void entityInit() {
    }

    @Override
    protected void readEntityFromNBT(final NBTTagCompound data) {
        this.iSize = data.getDouble("iSize");
        this.jSize = data.getDouble("jSize");
        this.kSize = data.getDouble("kSize");
    }

    @Override
    protected void writeEntityToNBT(final NBTTagCompound data) {
        data.setDouble("iSize", this.iSize);
        data.setDouble("jSize", this.jSize);
        data.setDouble("kSize", this.kSize);
    }

    @Override
    public void setDead() {
        super.setDead();
        if (Config.content().debug())
            QuarryPlus.LOGGER.info(world.isRemote + " " + toString() + " " + getEntityBoundingBox());
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
    }

    @Override
    public boolean writeToNBTOptional(NBTTagCompound compound) {
        return false;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

}
