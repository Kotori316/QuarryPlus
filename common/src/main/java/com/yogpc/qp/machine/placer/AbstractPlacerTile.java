package com.yogpc.qp.machine.placer;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.QpEntity;
import com.yogpc.qp.packet.ClientSync;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class AbstractPlacerTile extends QpEntity
    implements ClientSync, Container {

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

    private final SimpleContainer container = new PlacerContainer(this, getContainerSize());
    private int lastPlacedIndex = 0;
    @NotNull
    public RedStoneMode redstoneMode = RedStoneMode.PULSE;

    protected AbstractPlacerTile(BlockPos pos, BlockState blockState) {
        super(pos, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        toClientTag(output);
        container.storeAsItemList(output.list("container", ItemStack.CODEC));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        fromClientTag(input);
        container.fromItemList(input.listOrEmpty("container", ItemStack.CODEC));
    }

    @Override
    public void fromClientTag(ValueInput input) {
        lastPlacedIndex = input.getIntOr("lastPlacedIndex", 0);
        redstoneMode = input.getString("mode").map(RedStoneMode::valueOf).orElse(RedStoneMode.PULSE);
    }

    @Override
    public ValueOutput toClientTag(ValueOutput output) {
        output.putInt("lastPlacedIndex", lastPlacedIndex);
        output.putString("mode", redstoneMode.name());
        return output;
    }

    protected abstract BlockPos getTargetPos();

    protected abstract Direction getMachineFacing();

    @Override
    public Stream<MutableComponent> checkerLogs() {
        return Stream.concat(
            super.checkerLogs(),
            Stream.of(
                detail(ChatFormatting.GREEN, "redstoneMode", redstoneMode.toString()),
                detail(ChatFormatting.GREEN, "Target", getTargetPos().toShortString()),
                detail(ChatFormatting.GREEN, "Facing", String.valueOf(getMachineFacing())),
                detail(ChatFormatting.GREEN, "isPowered", String.valueOf(isPowered())),
                detail(ChatFormatting.GREEN, "lastPlacedIndex in inv", String.valueOf(getLastPlacedIndex()))
            )
        );
    }

    protected boolean isPowered() {
        assert level != null;
        return Arrays.stream(Direction.values()).filter(Predicate.isEqual(getMachineFacing()).negate())
            .anyMatch(f -> level.hasSignal(getBlockPos().relative(f), f)) || level.hasNeighborSignal(getBlockPos().above());
    }

    void breakBlock() {
        if (level == null || !redstoneMode.canBreak()) return;
        BlockPos pos = getTargetPos();
        // Not to remove the placer itself
        if (pos.equals(getBlockPos())) return;
        BlockState state = level.getBlockState(pos);
        if (state.getDestroySpeed(level, pos) < 0) return; // Unbreakable.
        Player fake = PlatformAccess.getAccess().mining().getQuarryFakePlayer(this, (ServerLevel) level, pos);
        var pickaxe = getSilkPickaxe();
        fake.setItemInHand(InteractionHand.MAIN_HAND, pickaxe);
        List<ItemStack> drops = Block.getDrops(state, (ServerLevel) level, pos, level.getBlockEntity(pos), fake, pickaxe);
        level.removeBlock(pos, false);
        drops.stream().map(container::addItem) // Return not-inserted items.
            .filter(Predicate.not(ItemStack::isEmpty)).forEach(s -> Block.popResource(level, getBlockPos(), s));
    }

    /**
     * @return Whether the placement succeeded.
     */
    boolean placeBlock() {
        if (isEmpty() || !redstoneMode.canPlace()) return false;
        Direction facing = getMachineFacing();
        BlockPos pos = getTargetPos();
        // Not to remove the placer itself
        if (pos.equals(getBlockPos())) return false;
        Vec3 hitPos = DIRECTION_VEC3D_MAP.get(facing.getOpposite()).add(pos.getX(), pos.getY(), pos.getZ());
        BlockHitResult rayTrace = new BlockHitResult(hitPos, facing.getOpposite(), pos, false);
        Player fake = PlatformAccess.getAccess().mining().getQuarryFakePlayer(this, (ServerLevel) level, pos);

        AtomicBoolean result = new AtomicBoolean(false);
        findEntry(container.getItems(),
            i -> tryPlaceItem(i, fake, rayTrace),
            lastPlacedIndex).ifPresent(i -> {
            if (!getItem(i).isEmpty()) {
                this.lastPlacedIndex = i;
            } else {
                this.lastPlacedIndex = findEntry(container.getItems(), s -> !s.isEmpty() && s.getItem() instanceof BlockItem, i).orElse(0);
            }
            setChanged();
            syncToClient();
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

    protected ItemStack getSilkPickaxe() {
        assert level != null;
        ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
        stack.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), 1);
        return stack;
    }

    // Container

    @Override
    public int getContainerSize() {
        return 9;
    }

    @Override
    public boolean isEmpty() {
        return container.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return container.getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return container.removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return container.removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        container.setItem(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return getBlockPos().closerToCenterThan(player.position(), 8);
    }

    @Override
    public void clearContent() {
        container.clearContent();
    }

    public void cycleRedStoneMode() {
        this.redstoneMode = RedStoneMode.cycle(redstoneMode);
        if (level != null && !level.isClientSide) {
            syncToClient();
        }
    }

    private static final class PlacerContainer extends SimpleContainer {
        private final QpEntity parent;

        public PlacerContainer(QpEntity parent, int size) {
            super(size);
            this.parent = parent;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            parent.setChanged();
        }
    }

    public enum RedStoneMode {
        PULSE(true, true),
        PULSE_PLACE_ONLY(true, false),
        PULSE_BREAK_ONLY(false, true),
        ;
        private final boolean placeEnabled;
        private final boolean breakEnabled;

        RedStoneMode(boolean placeEnabled, boolean breakEnabled) {
            this.placeEnabled = placeEnabled;
            this.breakEnabled = breakEnabled;
        }

        @Override
        public String toString() {
            return name().replace('_', ' ');
        }

        public boolean canPlace() {
            return placeEnabled;
        }

        public boolean canBreak() {
            return breakEnabled;
        }

        public static RedStoneMode cycle(RedStoneMode now) {
            RedStoneMode[] modes = values();
            for (int i = 0; i < modes.length; i++) {
                RedStoneMode mode = modes[i];
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
