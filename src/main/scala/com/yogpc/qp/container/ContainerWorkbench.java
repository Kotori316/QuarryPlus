package com.yogpc.qp.container;

import java.util.Objects;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.TileMessage;
import com.yogpc.qp.tile.TileWorkbench;
import com.yogpc.qp.version.VersionUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.yogpc.qp.version.VersionUtil.empty;
import static com.yogpc.qp.version.VersionUtil.getCount;
import static com.yogpc.qp.version.VersionUtil.isEmpty;
import static com.yogpc.qp.version.VersionUtil.nonEmpty;

public class ContainerWorkbench extends Container {

    private final TileWorkbench tile;
    private final EntityPlayer player;
    private static final int sourceSlot = 27;
    private static final int recipeSlot = 18;
    private static final int playerSlot = 45;

    public ContainerWorkbench(final EntityPlayer player, final TileWorkbench tw) {
        this.tile = tw;
        this.player = player;
        int row;
        int col;

        //0-26
        for (row = 0; row < 3; ++row)
            for (col = 0; col < 9; ++col)
                addSlotToContainer(new SlotUnlimited(tw, col + row * 9, 8 + col * 18, 18 + row * 18));

        //27-44
        for (row = 0; row < 2; ++row)
            for (col = 0; col < 9; ++col)
                addSlotToContainer(new SlotWorkbench(tw, col + row * 9 + sourceSlot, 8 + col * 18, 90 + row * 18));

        //45-62
        for (row = 0; row < 3; ++row)
            for (col = 0; col < 9; ++col)
                addSlotToContainer(new Slot(player.inventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));

        //63-71
        for (col = 0; col < 9; ++col)
            addSlotToContainer(new Slot(player.inventory, col, 8 + col * 18, 198));
    }

    @Override
    public boolean canInteractWith(final EntityPlayer playerIn) {
        return this.tile.isUsableByPlayer(playerIn);
    }

    /**
     * @param index The index of clicked slot, the source.
     */
    @Override
    public ItemStack transferStackInSlot(final EntityPlayer playerIn, final int index) {
        if (sourceSlot <= index && index < sourceSlot + recipeSlot)
            return empty();
        ItemStack src = empty();
        final Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            final ItemStack remain = slot.getStack();
            src = Objects.requireNonNull(remain).copy();
            if (index < sourceSlot) {
                //To inventory
                if (src.isStackable()) {
                    if (!mergeItemStack(remain, sourceSlot + recipeSlot, sourceSlot + recipeSlot + playerSlot, true))
                        return empty();
                } else {
                    for (int i = sourceSlot + recipeSlot + playerSlot - 1; i >= sourceSlot + recipeSlot && nonEmpty(remain); i--) {
                        Slot destinationSlot = inventorySlots.get(i);

                        if (!destinationSlot.getHasStack()) {
                            //Just move
                            int maxSize = Math.min(slot.getSlotStackLimit(), remain.getMaxStackSize());
                            destinationSlot.putStack(remain.splitStack(maxSize));
                        } else {
                            ItemStack dest = destinationSlot.getStack();
                            if (areStackable(dest, remain)) {
                                int newSize = VersionUtil.getCount(dest) + VersionUtil.getCount(remain);
                                int maxSize = Math.min(slot.getSlotStackLimit(), remain.getMaxStackSize());

                                if (newSize <= maxSize) {
                                    VersionUtil.setCount(remain, 0);
                                    VersionUtil.setCount(dest, newSize);
                                    slot.onSlotChanged();
                                } else if (VersionUtil.getCount(dest) < maxSize) {
                                    VersionUtil.shrink(remain, maxSize - VersionUtil.getCount(dest));
                                    VersionUtil.setCount(dest, maxSize);
                                    slot.onSlotChanged();
                                }
                            }
                        }
                    }
                    if (nonEmpty(remain))
                        return empty();
                }
            } else if (!n_mergeItemStack(remain))
                //To workbench
                return empty();
            if (isEmpty(remain))
                slot.putStack(empty());
            else
                slot.onSlotChanged();
            if (getCount(remain) == getCount(src))
                return empty();
            VersionUtil.onTake(slot, playerIn, remain);
        }
        return src;
    }

    /**
     * Called on server side. (!world.isRemote must be true.)
     *
     * @param listener player?
     */
    @Override
    public void addListener(IContainerListener listener) {
        if (this.listeners.contains(listener)) {
            throw new IllegalArgumentException("Listener already listening");
        } else {
            this.listeners.add(listener);
            // This method send byte as stack count.
            //listener.sendAllContents(this, this.getInventory());
            if (listener instanceof EntityPlayerMP) {
                EntityPlayerMP playerMP = (EntityPlayerMP) listener;
                PacketHandler.sendToClient(TileMessage.create(tile), playerMP);
            }
            this.detectAndSendChanges();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        tile.setField(id, data);
    }

    @Override
    public void detectAndSendChanges() {
        //super.detectAndSendChanges();
        for (int i = 45; i < this.inventorySlots.size(); ++i) {
            ItemStack itemstack = this.inventorySlots.get(i).getStack();
            ItemStack itemstack1 = this.inventoryItemStacks.get(i);

            if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
//                boolean clientStackChanged = !ItemStack.areItemStacksEqualUsingNBTShareTag(itemstack1, itemstack);
                itemstack1 = VersionUtil.isEmpty(itemstack) ? VersionUtil.empty() : Objects.requireNonNull(itemstack).copy();
                this.inventoryItemStacks.set(i, itemstack1);

//                if (clientStackChanged)
                for (IContainerListener listener : this.listeners) {
                    listener.sendSlotContents(this, i, itemstack1);
                }
            }
        }
        if (!tile.getWorld().isRemote)
            PacketHandler.sendToClient(TileMessage.create(tile), (EntityPlayerMP) player);

        listeners.forEach(listener -> listener.sendAllWindowProperties(this, tile));
    }

    /**
     * @param slotId      slot id
     * @param dragType    0 = left click, 1 right click, 2 middle click.
     * @param clickTypeIn NORMAL->PICKUP, Shift click->QUICK_MOVE, Middle click->CLONE
     * @param player      the player
     * @return ???
     */
    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        if (sourceSlot <= slotId && slotId < sourceSlot + recipeSlot && clickTypeIn == ClickType.PICKUP) {
            int index = slotId - sourceSlot;
            if (index < tile.recipesList.size()) {
                if (dragType == 0) {
                    if (index == tile.getRecipeIndex()) {
                        tile.workcontinue = !tile.workcontinue;
                    } else {
                        tile.setCurrentRecipeIndex(index);
                    }
                } else if (dragType == 1) {
                    tile.setCurrentRecipeIndex(-1);
                }
            }
            return empty();
        } else if (0 <= slotId && slotId < sourceSlot && clickTypeIn == ClickType.PICKUP) {

            InventoryPlayer inventoryplayer = player.inventory;
            ItemStack itemstack = empty();

            Slot slot = this.inventorySlots.get(slotId);

            if (slot != null) {
                ItemStack slotStack = slot.getStack();
                ItemStack playerStack = inventoryplayer.getItemStack();

                if (nonEmpty(slotStack)) {
                    itemstack = Objects.requireNonNull(slotStack).copy();
                }

                if (isEmpty(slotStack)) {
                    //put TO workbench.
                    if (nonEmpty(playerStack) && slot.isItemValid(playerStack)) {
                        int l2 = dragType == 0 ? getCount(playerStack) : 1;

                        if (l2 > slot.getItemStackLimit(playerStack)) {
                            l2 = slot.getItemStackLimit(playerStack);
                        }
                        slot.putStack(Objects.requireNonNull(playerStack).splitStack(l2));
                        if (VersionUtil.isEmpty(playerStack)) {
                            inventoryplayer.setItemStack(VersionUtil.empty());
                        }
                    }
                } else {
                    if (isEmpty(playerStack)) {
                        //take itemstack FROM workbench
                        if (isEmpty(slotStack)) {
                            slot.putStack(empty());
                            inventoryplayer.setItemStack(empty());
                        } else {
                            int k2;
                            if (dragType == 0) {
                                k2 = Math.min(getCount(slotStack), Objects.requireNonNull(slotStack).getMaxStackSize());
                            } else {
                                k2 = Math.min((getCount(slotStack) + 1) / 2, Objects.requireNonNull(slotStack).getMaxStackSize());
                            }
                            inventoryplayer.setItemStack(slot.decrStackSize(k2));

                            if (isEmpty(slotStack)) {
                                slot.putStack(empty());
                            }

                            VersionUtil.onTake(slot, player, inventoryplayer.getItemStack());
                        }
                    } else {
                        //put TO workbench.
                        if (areStackable(slotStack, playerStack)) {
                            int j2 = dragType == 0 ? getCount(playerStack) : 1;

                            VersionUtil.shrink(playerStack, j2);
                            if (VersionUtil.isEmpty(playerStack))//1.10.2
                                inventoryplayer.setItemStack(VersionUtil.empty());
                            VersionUtil.grow(slotStack, j2);
                        }
                        //Swiching items. not need!
                        /*else if (playerStack.getCount() <= slot.getItemStackLimit(playerStack)) {

                            slot.putStack(playerStack);
                            inventoryplayer.setItemStack(slotStack);
                        }*/
                    }
                }
                slot.onSlotChanged();
            }
            return itemstack;
        } else
            return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    protected boolean n_mergeItemStack(ItemStack stack) {
        boolean flag = false;

        for (int i = 0; i < sourceSlot && nonEmpty(stack); i++) {
            Slot slot = this.inventorySlots.get(i);
            ItemStack itemstack = slot.getStack();

            if (nonEmpty(itemstack) && areStackable(stack, itemstack)) {
                int j = getCount(itemstack) + getCount(stack);
                int maxSize = slot.getSlotStackLimit();// ignore limit of stack. Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());

                if (j <= maxSize) {
                    VersionUtil.setCount(stack, 0);
                    VersionUtil.setCount(itemstack, j);
                    slot.onSlotChanged();
                    flag = true;
                } else if (getCount(itemstack) < maxSize) {
                    //come?
                    if (Config.content().debug())
                        QuarryPlus.LOGGER.info("ContainerWorkbench#mergeItemStack itemstack.getCount() < maxSize");
                    VersionUtil.shrink(stack, maxSize - getCount(itemstack));
                    VersionUtil.setCount(itemstack, maxSize);
                    slot.onSlotChanged();
                    flag = true;
                }
            }
        }
        if (nonEmpty(stack)) {
            for (int i = 0; i < sourceSlot; i++) {
                Slot slot1 = this.inventorySlots.get(i);
                ItemStack itemstack1 = slot1.getStack();

                if (isEmpty(itemstack1) && slot1.isItemValid(stack)) {
                    //NEVER
                    /* if (stack.getCount() > slot1.getSlotStackLimit()) {
                        slot1.putStack(stack.splitStack(slot1.getSlotStackLimit()));
                    } else */
                    slot1.putStack(stack.splitStack(getCount(stack)));

                    slot1.onSlotChanged();
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    private static boolean areStackable(ItemStack stack1, ItemStack stack2) {
        return nonEmpty(stack2) && stack1.getItem() == stack2.getItem() &&
            (!stack1.getHasSubtypes() || stack1.getMetadata() == stack2.getMetadata()) && ItemStack.areItemStackTagsEqual(stack1, stack2);
    }
}
