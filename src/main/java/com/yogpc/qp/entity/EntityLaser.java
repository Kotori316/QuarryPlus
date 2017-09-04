package com.yogpc.qp.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class EntityLaser extends Entity {
    public static final int DRILL = 0;
    public static final int DRILL_HEAD = 1;
    public static final int BLUE_LASER = 2;
    public static final int RED_LASER = 3;

    public double iSize, jSize, kSize;
    public final int texture;

    @SuppressWarnings("unused")
    public EntityLaser(World worldIn) {
        this(worldIn, 0, 0, 0, 0, 0, 0, DRILL);
    }

    public EntityLaser(final World world, final double x, final double y, final double z,
                       final double iSize, final double jSize, final double kSize, final int tex) {
        super(world);
        this.preventEntitySpawning = false;
        this.noClip = true;
        this.isImmuneToFire = true;
        this.iSize = iSize;
        this.jSize = jSize;
        this.kSize = kSize;
        setPositionAndRotation(x, y, z, 0, 0);
        this.motionX = 0.0;
        this.motionY = 0.0;
        this.motionZ = 0.0;
        this.texture = tex;
    }

    @Override
    public void setPosition(final double x, final double y, final double z) {
        super.setPosition(x, y, z);
        setEntityBoundingBox(new AxisAlignedBB(this.posX, this.posY, this.posZ, this.posX + this.iSize, this.posY + this.jSize, this.posZ + this.kSize));
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
}
