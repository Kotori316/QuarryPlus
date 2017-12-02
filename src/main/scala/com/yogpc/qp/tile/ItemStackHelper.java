package com.yogpc.qp.tile;

import java.util.List;

import com.yogpc.qp.NonNullList;
import com.yogpc.qp.version.VersionUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

/**
 * Copied from net.minecraft.inventory.ItemStackHelper
 * 1.12.2
 */
public class ItemStackHelper {
    public static ItemStack getAndSplit(List<ItemStack> stacks, int index, int amount) {
        return index >= 0 && index < stacks.size() && VersionUtil.nonEmpty(stacks.get(index)) && amount > 0 ? stacks.get(index).splitStack(amount) : VersionUtil.empty();
    }

    public static ItemStack getAndRemove(List<ItemStack> stacks, int index) {
        return index >= 0 && index < stacks.size() ? stacks.set(index, VersionUtil.empty()) : VersionUtil.empty();
    }

    public static NBTTagCompound saveAllItems(NBTTagCompound tag, NonNullList<ItemStack> list) {
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < list.size(); ++i) {
            ItemStack itemstack = list.get(i);

            if (VersionUtil.nonEmpty(itemstack)) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                itemstack.writeToNBT(nbttagcompound);
                nbttaglist.appendTag(nbttagcompound);
            }
        }

        if (!nbttaglist.hasNoTags()) {
            tag.setTag("Items", nbttaglist);
        }

        return tag;
    }

    public static void loadAllItems(NBTTagCompound tag, NonNullList<ItemStack> list) {
        NBTTagList nbttaglist = tag.getTagList("Items", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;

            if (j < list.size()) {
                list.set(j, VersionUtil.fromNBTTag(nbttagcompound));
            }
        }
    }
}
