package com.yogpc.qp.machines.pb;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.APacketTile;
import com.yogpc.qp.machines.base.HasInv;
import com.yogpc.qp.machines.base.IDebugSender;
import com.yogpc.qp.machines.quarry.QuarryFakePlayer;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.TileMessage;
import com.yogpc.qp.utils.Holder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class PlacerTile extends APacketTile implements
    ITickableTileEntity,
    HasInv,
    IDebugSender,
    INamedContainerProvider {
    public static final scala.Symbol SYMBOL = scala.Symbol.apply("Placer");
    public static final String KEY_ITEM = "items";
    public static final String KEY_LAST_PLACED = "last_placed";
    public static final String KEY_RS_MODE = "redstone_mode";
    public static final Map<Direction, Vec3d> DIRECTION_VEC3D_MAP;

    static {
        EnumMap<Direction, Vec3d> map = new EnumMap<>(Direction.class);
        map.put(Direction.DOWN, new Vec3d(0.5, 0, 0.5));
        map.put(Direction.UP, new Vec3d(0.5, 1, 0.5));
        map.put(Direction.NORTH, new Vec3d(0.5, 0.5, 0));
        map.put(Direction.SOUTH, new Vec3d(0.5, 0.5, 1));
        map.put(Direction.EAST, new Vec3d(1, 0.5, 0.5));
        map.put(Direction.WEST, new Vec3d(0, 0.5, 0.5));
        DIRECTION_VEC3D_MAP = Collections.unmodifiableMap(map);
    }

    private NonNullList<ItemStack> inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
    private int lastPlacedIndex = 0;
    public RedstoneMode redstoneMode = RedstoneMode.PULSE;
    private final IItemHandler itemHandler = new InvWrapper(this);

    public PlacerTile() {
        super(Holder.placerType());
    }

    // -------------------- Place --------------------

    @Override
    public void tick() {
        if (world != null && !world.isRemote && redstoneMode.isAlways()) {
            Direction facing = getBlockState().get(FACING);
            if (redstoneMode.shouldWork(() -> PlacerBlock.isPoweredToWork(world, getPos(), facing))) {
                if (world.isAirBlock(getPos().offset(facing))) {
                    placeBlock();
                } else {
                    breakBlock();
                }
            }
        }
    }

    public void breakBlock() {
        if (world == null || !redstoneMode.canBreak()) return;
        Direction facing = getBlockState().get(FACING);
        BlockPos pos = getPos().offset(facing);
        BlockState state = world.getBlockState(pos);
        if (state.getBlockHardness(world, pos) < 0) return; // Unbreakable.
        PlayerEntity fake = QuarryFakePlayer.get(((ServerWorld) world), getPos());
        fake.setHeldItem(Hand.MAIN_HAND, getSilkPickaxe());
        List<ItemStack> drops = Block.getDrops(state, ((ServerWorld) world), pos, world.getTileEntity(pos), fake, fake.getHeldItemMainhand());
        world.removeBlock(pos, false);
        drops.stream().map(s -> ItemHandlerHelper.insertItem(this.itemHandler, s, false)) // Return not-inserted items.
            .filter(s -> !s.isEmpty()).forEach(s -> Block.spawnAsEntity(world, getPos(), s));
    }

    public void placeBlock() {
        if (isEmpty() || !redstoneMode.canPlace()) return;
        Direction facing = getBlockState().get(FACING);
        BlockPos pos = getPos().offset(facing);
        Vec3d hitPos = DIRECTION_VEC3D_MAP.get(facing.getOpposite()).add(pos.getX(), pos.getY(), pos.getZ());
        BlockRayTraceResult rayTrace = new BlockRayTraceResult(hitPos, facing.getOpposite(), pos, false);
        PlayerEntity fake = QuarryFakePlayer.get(((ServerWorld) world), getPos());

        findEntry(inventory,
            i -> isItemPlaceable(i, fake, rayTrace),
            lastPlacedIndex).ifPresent(i -> {
            if (!getStackInSlot(i).isEmpty())
                this.lastPlacedIndex = i;
            else
                this.lastPlacedIndex = findEntry(inventory, s -> !s.isEmpty() && s.getItem() instanceof BlockItem, i).orElse(0);
            markDirty();
            sendPacket();
        });
        fake.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
    }

    // -------------------- Utility --------------------

    public static <T> OptionalInt findEntry(List<T> check, Predicate<T> filter, int startIndex) {
        int listSize = check.size();
        if (startIndex >= listSize)
            return OptionalInt.empty();
        return findEntryInternal(check, filter, startIndex, startIndex, listSize);
    }

    private static <T> OptionalInt findEntryInternal(List<T> check, Predicate<T> filter, int startIndex, int index, int listSize) {
        T value = check.get(index);
        if (filter.test(value))
            return OptionalInt.of(index);
        if (index == startIndex - 1 || (startIndex == 0 && index == listSize - 1)) {
            // last search
            return OptionalInt.empty();
        } else {
            int next = index + 1 == listSize ? 0 : index + 1;
            return findEntryInternal(check, filter, startIndex, next, listSize);
        }
    }

    public static boolean isItemPlaceable(ItemStack stack, PlayerEntity fake, BlockRayTraceResult rayTrace) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        if (!(item instanceof BlockItem)) {
            return false;
        } else {
            BlockItem blockItem = (BlockItem) item;
            fake.setHeldItem(Hand.MAIN_HAND, stack);
            BlockItemUseContext context = new BlockItemUseContext(new ItemUseContext(fake, Hand.MAIN_HAND, rayTrace));
            return blockItem.tryPlace(context).isSuccess();
        }
    }

    public int getLastPlacedIndex() {
        return lastPlacedIndex;
    }

    void sendPacket() {
        PacketHandler.sendToClient(TileMessage.create(this), world);
    }

    private static ItemStack getSilkPickaxe() {
        ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
        stack.addEnchantment(Enchantments.SILK_TOUCH, 1);
        return stack;
    }
    // -------------------- NBT --------------------

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.put(KEY_ITEM, ItemStackHelper.saveAllItems(new CompoundNBT(), inventory));
        compound.putInt(KEY_LAST_PLACED, lastPlacedIndex);
        compound.putString(KEY_RS_MODE, redstoneMode.name());
        return super.write(compound);
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        ItemStackHelper.loadAllItems(compound.getCompound(KEY_ITEM), inventory);
        lastPlacedIndex = compound.getInt(KEY_LAST_PLACED);
        try {
            redstoneMode = RedstoneMode.valueOf(compound.getString(KEY_RS_MODE));
        } catch (IllegalArgumentException e) {
            QuarryPlus.LOGGER.error("Illegal name was passed to placer mode.", e);
            redstoneMode = RedstoneMode.PULSE;
        }
    }

    // -------------------- Inventory --------------------

    @Override
    public ITextComponent getName() {
        return new TranslationTextComponent(getDebugName());
    }

    @Override
    public String getDebugName() {
        return TranslationKeys.placer;
    }

    @Override
    public List<? extends ITextComponent> getDebugMessages() {
        return Collections.emptyList();
    }

    @Override
    public int getSizeInventory() {
        return 9;
    }

    @Override
    public boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        if (index >= 0 && index < getSizeInventory())
            return inventory.get(index);
        else
            return ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return ItemStackHelper.getAndSplit(inventory, index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(inventory, index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index >= 0 && index < getSizeInventory())
            inventory.set(index, stack);
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return player.getDistanceSq(getPos().getX(), getPos().getY(), getPos().getZ()) <= 64;
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    // -------------------- Container --------------------

    @Override
    public PlacerContainer createMenu(int id, PlayerInventory p, PlayerEntity player) {
        return new PlacerContainer(id, player, getPos());
    }

    // -------------------- RedstoneMode --------------------

    public void cycleRedstoneMode() {
        this.redstoneMode = RedstoneMode.cycle(redstoneMode);
        if (world != null && !world.isRemote) {
            sendPacket();
        }
    }

    private static final int PULSE_ID = 0;
    private static final int RS_IGNORE_ID = 1;
    private static final int RS_ON_ID = 2;
    private static final int RS_OFF_ID = 3;

    public enum RedstoneMode {
        PULSE(PULSE_ID, true, true),
        PULSE_PLACE_ONLY(PULSE_ID, true, false),
        PULSE_BREAK_ONLY(PULSE_ID, false, true),
        ALWAYS_RS_IGNORE(RS_IGNORE_ID, true, true),
        ALWAYS_PLACE_ONLY(RS_IGNORE_ID, true, false),
        ALWAYS_BREAK_ONLY(RS_IGNORE_ID, false, true),
        ALWAYS_RS_ON(RS_ON_ID, true, true),
        ALWAYS_RS_ON_PLACE_ONLY(RS_ON_ID, true, false),
        ALWAYS_RS_ON_BREAK_ONLY(RS_ON_ID, false, true),
        ALWAYS_RS_OFF(RS_OFF_ID, true, true),
        ALWAYS_RS_OFF_PLACE_ONLY(RS_OFF_ID, true, false),
        ALWAYS_RS_OFF_BREAK_ONLY(RS_OFF_ID, false, true),
        ;
        private final int modeID;
        private final boolean placeEnabled;
        private final boolean breakEnabled;

        RedstoneMode(int modeID, boolean placeEnabled, boolean breakEnabled) {
            this.modeID = modeID;
            this.placeEnabled = placeEnabled;
            this.breakEnabled = breakEnabled;
        }

        @Override
        public String toString() {
            return name().replace('_', ' ');
        }

        public boolean isAlways() {
            return this.modeID == RS_IGNORE_ID || this.modeID == RS_ON_ID || this.modeID == RS_OFF_ID;
        }

        public boolean isPulse() {
            return this.modeID == PULSE_ID;
        }

        public boolean canPlace() {
            return placeEnabled;
        }

        public boolean canBreak() {
            return breakEnabled;
        }

        public boolean isRsOn() {
            return this.modeID == RS_ON_ID;
        }

        public boolean isRsOff() {
            return this.modeID == RS_OFF_ID;
        }

        public boolean shouldWork(BooleanSupplier powered) {
            if (isRsOn())
                return powered.getAsBoolean();
            else if (isRsOff())
                return !powered.getAsBoolean();
            else
                return true;
        }

        public static RedstoneMode cycle(RedstoneMode now) {
            RedstoneMode[] modes = values();
            for (int i = 0; i < modes.length; i++) {
                RedstoneMode mode = modes[i];
                if (mode == now) {
                    if (i + 1 == modes.length)
                        return modes[0];
                    else
                        return modes[i + 1];
                }
            }
            return modes[0];
        }
    }
}
