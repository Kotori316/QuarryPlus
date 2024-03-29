package com.yogpc.qp.machines.placer;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.CheckerLog;
import com.yogpc.qp.machines.InvUtils;
import com.yogpc.qp.machines.QuarryFakePlayer;
import com.yogpc.qp.packet.ClientSync;
import com.yogpc.qp.packet.ClientSyncMessage;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class PlacerTile extends BlockEntity implements
    Container,
    CheckerLog,
    ClientSync,
    MenuProvider {
    public static final String KEY_ITEM = "items";
    public static final String KEY_LAST_PLACED = "last_placed";
    public static final String KEY_RS_MODE = "redstone_mode";
    public static final Map<Direction, Vec3> DIRECTION_VEC3D_MAP;

    static {
        EnumMap<Direction, Vec3> map = new EnumMap<>(Direction.class);
        map.put(Direction.DOWN, new Vec3(0.5, 0, 0.5));
        map.put(Direction.UP, new Vec3(0.5, 1, 0.5));
        map.put(Direction.NORTH, new Vec3(0.5, 0.5, 0));
        map.put(Direction.SOUTH, new Vec3(0.5, 0.5, 1));
        map.put(Direction.EAST, new Vec3(1, 0.5, 0.5));
        map.put(Direction.WEST, new Vec3(0, 0.5, 0.5));
        DIRECTION_VEC3D_MAP = Collections.unmodifiableMap(map);
    }

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
    private int lastPlacedIndex = 0;
    public RedstoneMode redstoneMode = RedstoneMode.PULSE;
    private final IItemHandler itemHandler = new InvWrapper(this);

    public PlacerTile(BlockPos pos, BlockState state) {
        super(Holder.PLACER_TYPE, pos, state);
    }

    protected PlacerTile(BlockEntityType<?> entityType, BlockPos pos, BlockState state) {
        super(entityType, pos, state);
    }

    // -------------------- Place --------------------

    public void tick() {
        if (level != null && !level.isClientSide && redstoneMode.isAlways()) {
            if (redstoneMode.shouldWork(this::isPowered)) {
                var placed = placeBlock();
                if (!placed)
                    breakBlock();
            }
        }
    }

    protected BlockPos getTargetPos() {
        return getBlockPos().relative(getMachineFacing());
    }

    protected Direction getMachineFacing() {
        return getBlockState().getValue(FACING);
    }

    protected boolean isPowered() {
        return PlacerBlock.isPoweredToWork(level, getBlockPos(), getMachineFacing());
    }

    void breakBlock() {
        if (level == null || !redstoneMode.canBreak()) return;
        BlockPos pos = getTargetPos();
        BlockState state = level.getBlockState(pos);
        if (state.getDestroySpeed(level, pos) < 0) return; // Unbreakable.
        Player fake = QuarryFakePlayer.get(((ServerLevel) level));
        fake.setItemInHand(InteractionHand.MAIN_HAND, getSilkPickaxe());
        List<ItemStack> drops = InvUtils.getBlockDrops(state, ((ServerLevel) level), pos, level.getBlockEntity(pos), fake, fake.getMainHandItem());
        level.removeBlock(pos, false);
        drops.stream().map(s -> ItemHandlerHelper.insertItem(this.itemHandler, s, false)) // Return not-inserted items.
            .filter(Predicate.not(ItemStack::isEmpty)).forEach(s -> Block.popResource(level, getBlockPos(), s));
    }

    /**
     * @return Whether the placement succeeded.
     */
    boolean placeBlock() {
        if (isEmpty() || !redstoneMode.canPlace()) return false;
        Direction facing = getMachineFacing();
        BlockPos pos = getTargetPos();
        Vec3 hitPos = DIRECTION_VEC3D_MAP.get(facing.getOpposite()).add(pos.getX(), pos.getY(), pos.getZ());
        BlockHitResult rayTrace = new BlockHitResult(hitPos, facing.getOpposite(), pos, false);
        Player fake = QuarryFakePlayer.get(((ServerLevel) level));

        AtomicBoolean result = new AtomicBoolean(false);
        findEntry(inventory,
            i -> tryPlaceItem(i, fake, rayTrace),
            lastPlacedIndex).ifPresent(i -> {
            if (!getItem(i).isEmpty())
                this.lastPlacedIndex = i;
            else
                this.lastPlacedIndex = findEntry(inventory, s -> !s.isEmpty() && s.getItem() instanceof BlockItem, i).orElse(0);
            setChanged();
            sendPacket();
            result.set(true);
        });
        fake.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        return result.get();
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

    static boolean tryPlaceItem(ItemStack stack, Player fake, BlockHitResult rayTrace) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        if (item instanceof BlockItem blockItem) {
            fake.setItemInHand(InteractionHand.MAIN_HAND, stack);
            BlockPlaceContext context = new BlockPlaceContext(new UseOnContext(fake, InteractionHand.MAIN_HAND, rayTrace));
            return blockItem.place(context).consumesAction();
        } else {
            return false;
        }
    }

    public int getLastPlacedIndex() {
        return lastPlacedIndex;
    }

    void sendPacket() {
        if (level != null && !level.isClientSide)
            PacketHandler.sendToClient(new ClientSyncMessage(this), level);
    }

    private static ItemStack getSilkPickaxe() {
        ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
        stack.enchant(Enchantments.SILK_TOUCH, 1);
        return stack;
    }

    // -------------------- NBT --------------------

    @Override
    protected void saveAdditional(CompoundTag compound) {
        compound.put(KEY_ITEM, ContainerHelper.saveAllItems(new CompoundTag(), inventory));
        toClientTag(compound);
        super.saveAdditional(compound);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        ContainerHelper.loadAllItems(compound.getCompound(KEY_ITEM), inventory);
        fromClientTag(compound);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.serializeNBT();
    }

    @Override
    public void fromClientTag(CompoundTag compound) {
        lastPlacedIndex = compound.getInt(KEY_LAST_PLACED);
        try {
            redstoneMode = RedstoneMode.valueOf(compound.getString(KEY_RS_MODE));
        } catch (IllegalArgumentException e) {
            QuarryPlus.LOGGER.error("Illegal name(%s) was passed to placer mode.".formatted(compound.getString(KEY_RS_MODE)), e);
            redstoneMode = RedstoneMode.PULSE;
        }
    }

    @Override
    public CompoundTag toClientTag(CompoundTag compound) {
        compound.putInt(KEY_LAST_PLACED, lastPlacedIndex);
        compound.putString(KEY_RS_MODE, redstoneMode.name());
        return compound;
    }

    // -------------------- Capability --------------------
    private final LazyOptional<IItemHandler> itemHandlerOpt = LazyOptional.of(() -> itemHandler);

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER)
            return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, itemHandlerOpt);
        return super.getCapability(cap, side);
    }

    // -------------------- Inventory --------------------

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public List<? extends Component> getDebugLogs() {
        return Stream.of(
            "RS Mode: " + redstoneMode.toString(),
            "Last Placed: " + getLastPlacedIndex(),
            "Target: " + getTargetPos(),
            "Inv: " + inventory.stream().filter(Predicate.not(ItemStack::isEmpty)).count()
        ).map(Component::literal).collect(Collectors.toList());
    }

    @Override
    public int getContainerSize() {
        return 9;
    }

    @Override
    public boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int index) {
        if (index >= 0 && index < getContainerSize())
            return inventory.get(index);
        else
            return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return ContainerHelper.removeItem(inventory, index, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(inventory, index);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index >= 0 && index < getContainerSize())
            inventory.set(index, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && getBlockPos().distToCenterSqr(player.position()) < 64;
    }

    @Override
    public void clearContent() {
        inventory.clear();
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return true;
    }

    // -------------------- Container --------------------

    @Override
    public PlacerContainer createMenu(int id, Inventory p, Player player) {
        return new PlacerContainer(id, player, getBlockPos(), getClass());
    }

    // -------------------- RedstoneMode --------------------

    public void cycleRedstoneMode() {
        this.redstoneMode = RedstoneMode.cycle(redstoneMode);
        if (level != null && !level.isClientSide) {
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
//        ALWAYS_RS_IGNORE(RS_IGNORE_ID, true, true),
//        ALWAYS_PLACE_ONLY(RS_IGNORE_ID, true, false),
//        ALWAYS_BREAK_ONLY(RS_IGNORE_ID, false, true),
//        ALWAYS_RS_ON(RS_ON_ID, true, true),
//        ALWAYS_RS_ON_PLACE_ONLY(RS_ON_ID, true, false),
//        ALWAYS_RS_ON_BREAK_ONLY(RS_ON_ID, false, true),
//        ALWAYS_RS_OFF(RS_OFF_ID, true, true),
//        ALWAYS_RS_OFF_PLACE_ONLY(RS_OFF_ID, true, false),
//        ALWAYS_RS_OFF_BREAK_ONLY(RS_OFF_ID, false, true),
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
