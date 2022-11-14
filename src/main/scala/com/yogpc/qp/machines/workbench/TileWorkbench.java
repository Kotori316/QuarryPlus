package com.yogpc.qp.machines.workbench;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.CheckerLog;
import com.yogpc.qp.machines.InvUtils;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.TileMessage;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

public class TileWorkbench extends PowerTile implements Container, MenuProvider, CheckerLog {
    final List<ItemStack> ingredientInventory = NonNullList.withSize(27, ItemStack.EMPTY);
    final List<ItemStack> selectionInventory = NonNullList.withSize(18, ItemStack.EMPTY);
    public List<WorkbenchRecipe> recipesList = Collections.emptyList();
    private WorkbenchRecipe currentRecipe = WorkbenchRecipe.dummyRecipe();
    private final ItemHandler itemHandler = new ItemHandler();
    public boolean workContinue;
    private Runnable initRecipeTask = null;
    private final List<Player> openPlayers = new ArrayList<>();

    public TileWorkbench(BlockPos pos, BlockState state) {
        super(Holder.WORKBENCH_TYPE, pos, state);
    }

    public void tick() {
        if (!enabled || level == null || level.isClientSide) return;
        if (this.initRecipeTask != null) {
            this.initRecipeTask.run();
            this.initRecipeTask = null;
        }
        if (currentRecipe.hasContent() && currentRecipe.getRequiredEnergy() <= getEnergy()) {
            // Enough energy is collected. Create the item.
            useEnergy(currentRecipe.getRequiredEnergy(), Reason.WORKBENCH, true);
            ItemStack created = currentRecipe.getOutput(ingredientInventory);
            ItemStack toSpawnInWorld = InvUtils.injectToNearTile(level, getBlockPos(), created);
            if (!toSpawnInWorld.isEmpty()) {
                Containers.dropItemStack(level, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), toSpawnInWorld);
            }
            currentRecipe.consumeItems(ingredientInventory);
            updateRecipeOutputs();
            setCurrentRecipe(workContinue ? currentRecipe.getId() : WorkbenchRecipe.dummyRecipe().getId());
        } else if (QuarryPlus.config.common.noEnergy.get()) {
            addEnergy(currentRecipe.getRequiredEnergy() / 200, false);
        } else {
            addEnergy(5 * ONE_FE, false);
        }
        if (!openPlayers.isEmpty()) {
            PacketHandler.sendToClient(new TileMessage(this), level);
        }
    }

    // Overrides of BlockEntity

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide && this.initRecipeTask != null) {
            var server = level.getServer();
            if (server != null) { // Must be true, as this is in the server world.
                server.tell(new TickTask(server.getTickCount(), this.initRecipeTask));
                this.initRecipeTask = null;
            }
        }
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        ListTag list = nbt.getList("Items", Tag.TAG_COMPOUND);
        list.stream().mapMulti(MapMulti.cast(CompoundTag.class)).forEach(nbtTagCompound -> {
            var slot = nbtTagCompound.getByte("Slot");
            var stack = ItemStack.of(nbtTagCompound);
            stack.setCount(nbtTagCompound.getInt("Count"));
            ingredientInventory.set(slot, stack);
        });
        var recipeLocation = new ResourceLocation(nbt.getString("recipe"));
        initRecipeTask = () -> {
            updateRecipeOutputs();
            setCurrentRecipe(recipeLocation);
        };
    }

    @Override
    public void saveNbtData(CompoundTag nbt) {
        ListTag items = new ListTag();
        for (int i = 0; i < ingredientInventory.size(); i++) {
            var stack = ingredientInventory.get(i);
            if (!stack.isEmpty()) {
                var compoundNBT = new CompoundTag();
                compoundNBT.putByte("Slot", (byte) i);
                stack.save(compoundNBT);
                compoundNBT.remove("Count");
                compoundNBT.putInt("Count", stack.getCount());
                items.add(compoundNBT);
            }
        }
        nbt.put("Items", items);
        nbt.putString("recipe", currentRecipe.getId().toString());
    }

    // Implementation of Inventory.
    @Override
    public int getContainerSize() {
        return 27 + 18;
    }

    @Override
    public boolean isEmpty() {
        return ingredientInventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int index) {
        if (ingredientInventory.size() <= index && index < getContainerSize()) {
            return selectionInventory.get(index - ingredientInventory.size());
        }
        return ingredientInventory.get(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        if (ingredientInventory.size() <= index && index < getContainerSize())
            return ContainerHelper.removeItem(selectionInventory, index - ingredientInventory.size(), count);
        else {
            ItemStack stack = ContainerHelper.removeItem(ingredientInventory, index, count);
            updateRecipeOutputs();
            return stack;
        }
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        if (ingredientInventory.size() <= index && index < getContainerSize())
            return ContainerHelper.takeItem(selectionInventory, index - ingredientInventory.size());
        return ContainerHelper.takeItem(ingredientInventory, index);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (ingredientInventory.size() <= index && index < getContainerSize()) {
            selectionInventory.set(index - ingredientInventory.size(), stack);
        } else {
            ingredientInventory.set(index, stack);
            updateRecipeOutputs();
        }
    }

    @Override
    public int getMaxStackSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void clearContent() {
        ingredientInventory.clear();
        updateRecipeOutputs();
    }

    @Override
    public List<? extends Component> getDebugLogs() {
        return Stream.of(
            "%sRecipe:%s %s".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, currentRecipe),
            "%sWorkContinue:%s %b".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, workContinue),
            "%sRecipe List:%s %s".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, recipesList),
            energyString()
        ).map(Component::literal).toList();
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this, player);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        updateRecipeOutputs();
    }

    // Workbench Methods.
    private void updateRecipeOutputs() {
        if (level != null && !level.isClientSide) {
            updateRecipeList();
            if (!recipesList.contains(currentRecipe)) {
                if (currentRecipe.hasContent()) {
                    setCurrentRecipe(WorkbenchRecipe.dummyRecipe().getId());
                    //Finish work
                    logUsage();
                }
            }
            PacketHandler.sendToClient(new TileMessage(this), level);
        }
    }

    @VisibleForTesting
    void updateRecipeList() {
        recipesList = WorkbenchRecipe.getRecipeFinder().getRecipes(ingredientInventory);
        selectionInventory.clear();
        for (int i = 0; i < recipesList.size(); i++) {
            setItem(ingredientInventory.size() + i, recipesList.get(i).getResultItem());
        }
    }

    /**
     * Set the recipe. You should update recipeList before invoking this method.
     * This method checks if the recipe exists in the recipe list.
     * If not, dummy recipe(no output) is set as current recipe.
     * If exists, the recipe given as parameter is set.
     *
     * @param recipeName the id of recipe.
     */
    public void setCurrentRecipe(ResourceLocation recipeName) {
        this.currentRecipe = recipesList.stream().filter(r -> recipeName.equals(r.getId()))
            .findFirst().orElse(WorkbenchRecipe.dummyRecipe());
        setMaxEnergy(Math.max(this.currentRecipe.getRequiredEnergy(), 5));
        if (QuarryPlus.config.common.noEnergy.get()) {
            setEnergy(0, true);
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(() -> itemHandler));
        }
        return super.getCapability(cap, side);
    }

    public WorkbenchRecipe getRecipe() {
        return currentRecipe;
    }

    public int getProgressScaled(int scale) {
        if (currentRecipe.hasContent())
            return (int) (getEnergy() * scale / currentRecipe.getRequiredEnergy());
        else
            return 0;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ContainerWorkbench(id, player, getBlockPos());
    }

    @Override
    public void startOpen(Player player) {
        openPlayers.add(player);
    }

    @Override
    public void stopOpen(Player player) {
        openPlayers.remove(player);
    }

    private class ItemHandler implements IItemHandlerModifiable {

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            setItem(slot, stack);
        }

        @Override
        public int getSlots() {
            return ingredientInventory.size();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return TileWorkbench.this.getItem(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty())
                return ItemStack.EMPTY;
            ItemStack inSlot = getStackInSlot(slot).copy();
            if (!inSlot.isEmpty()) {
                if (ItemHandlerHelper.canItemStacksStack(inSlot, stack)) {
                    if (!simulate) {
                        inSlot.grow(stack.getCount());
                        setStackInSlot(slot, inSlot);
                        setChanged();
                    }
                    return ItemStack.EMPTY;
                } else {
                    return stack;
                }
            } else {
                if (!simulate) {
                    setStackInSlot(slot, stack.copy());
                    setChanged();
                }
                return ItemStack.EMPTY;
            }
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return getMaxStackSize();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return canPlaceItem(slot, stack);
        }
    }
}
