package com.yogpc.qp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

@Immutable
public class BlockData {
    public static final String Name_NBT = "name";
    public static final String Meta_NBT = "meta";
    public final ResourceLocation name;
    public final int meta;

    public BlockData(final String n, final int m) {
        this.name = new ResourceLocation(n);
        this.meta = m;
    }

    public BlockData(ResourceLocation name, int meta) {
        this.name = name;
        this.meta = meta;
    }

    @Nonnull
    public static BlockData of(NBTTagCompound compound) {
        if (compound == null) {
            return Invalid;
        }
        return new BlockData(compound.getString(Name_NBT), compound.getInteger(Meta_NBT));
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof BlockData && this.name.equals(((BlockData) o).name) && (this.meta == ((BlockData) o).meta
                || this.meta == OreDictionary.WILDCARD_VALUE || ((BlockData) o).meta == OreDictionary.WILDCARD_VALUE);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Nullable
    public NBTTagCompound toNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString(Name_NBT, name.toString());
        compound.setInteger(Meta_NBT, meta);
        return compound;
    }

    @Override
    public String toString() {
        return name + "@" + meta;
    }

    public String getLocalizedName() {
        final StringBuilder sb = new StringBuilder();
        sb.append(name);
        if (meta != OreDictionary.WILDCARD_VALUE) {
            sb.append(":");
            sb.append(meta);
        }
        sb.append("  ");
        Block value = ForgeRegistries.BLOCKS.getValue(name);
        sb.append(value != null ? value.getLocalizedName() : name.toString());
        return sb.toString();
    }

    public static final BlockData Invalid = new BlockData((ResourceLocation) null, 0) {
        @Override
        public boolean equals(Object o) {
            return false;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public NBTTagCompound toNBT() {
            return null;
        }

        @Override
        public String toString() {
            return "BlockData@Invaild";
        }

        @Override
        public String getLocalizedName() {
            return "Unknown:Dummy";
        }
    };
}
