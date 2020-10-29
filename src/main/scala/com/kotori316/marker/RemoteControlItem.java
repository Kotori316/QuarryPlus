package com.kotori316.marker;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.yogpc.qp.machines.base.Area;
import com.yogpc.qp.machines.base.IMarker;
import com.yogpc.qp.machines.base.IRemotePowerOn;
import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RemoteControlItem extends Item {
    private static final Logger LOGGER = LogManager.getLogger(RemoteControlItem.class);
    public static final String NBT_AREA = "area";
    public static final String NBT_REMOTE_POS = "remote_pos";
    public static final String NAME = "remote_controller";

    public RemoteControlItem() {
        super(new Properties().group(Marker.ITEM_GROUP));
        setRegistryName(Marker.modID, NAME);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        if (Caps.isQuarryModLoaded()) {
            World world = context.getWorld();
            if (world.isRemote) // 1.15.2 code.
                return ActionResultType.PASS;
            TileEntity tileEntity = world.getTileEntity(context.getPos());
            if (tileEntity != null) {
                Optional<IMarker> maybeMarker = Caps.markerCapability().flatMap(c -> tileEntity.getCapability(c, context.getFace()).filter(IMarker::hasLink));
                Optional<IRemotePowerOn> maybeRemoteControllable = Caps.remotePowerOnCapability().flatMap(c -> tileEntity.getCapability(c, context.getFace()).resolve());
                if (maybeMarker.isPresent()) {
                    if (world.getServer() != null) {
                        maybeMarker.map(m -> Area.posToArea(m.min(), m.max(), world.getDimensionKey()))
                            .ifPresent(area -> {
                                setArea(stack, Area.areaToNbt().apply(area)); // Save
                                LOGGER.debug("New area set: {}", area);
                                maybeMarker.ifPresent(m -> m.removeFromWorldWithItem().forEach(i -> {
                                    if (context.getPlayer() != null && !context.getPlayer().inventory.addItemStackToInventory(i)) {
                                        context.getPlayer().dropItem(i, false);
                                    }
                                })); // Drop item

                                getRemotePos(stack)
                                    .flatMap(p -> Optional.ofNullable(world.getServer()).map(w -> w.getWorld(p.getDimension())).map(w -> w.getTileEntity(p.getPos())))
                                    .flatMap(t -> Caps.remotePowerOnCapability().map(c -> t.getCapability(c, context.getFace())))
                                    .ifPresent(l -> l.ifPresent(r -> {
                                        LOGGER.debug("Send start request to {} with {}", r, area);
                                        r.setAndStart(area);
                                        stack.removeChildTag(NBT_AREA);
                                    }));
                            });
                        Optional.ofNullable(context.getPlayer()).ifPresent(p ->
                            p.sendStatusMessage(new TranslationTextComponent("chat.flexiblemarker.area"), false));
                    }
                    return ActionResultType.SUCCESS;
                } else if (maybeRemoteControllable.isPresent()) {
                    Optional<Area> optionalArea = getArea(stack);
                    if (optionalArea.isPresent()) {
                        maybeRemoteControllable.ifPresent(r -> {
                            r.setAndStart(optionalArea.get());
                            LOGGER.debug("Send start request to {} with {}", r, optionalArea.get());
                            stack.removeChildTag(NBT_AREA);
                        });
                    } else {
                        GlobalPos pos = GlobalPos.getPosition(world.getDimensionKey(), tileEntity.getPos());
                        setRemotePos(stack, pos);
                        LOGGER.debug("New remote pos set {}.", pos);
                        Optional.ofNullable(context.getPlayer()).ifPresent(p ->
                            p.sendStatusMessage(new TranslationTextComponent("chat.flexiblemarker.pos", convertPosText.apply(pos)), false));
                        maybeRemoteControllable.ifPresent(IRemotePowerOn::startWaiting);
                    }
                    return ActionResultType.SUCCESS;
                } else {
                    return super.onItemUseFirst(stack, context);
                }
            } else {
                if (context.hasSecondaryUseForPlayer() && stack.hasTag()) {
                    stack.removeChildTag(NBT_REMOTE_POS);
                    stack.removeChildTag(NBT_AREA);
                    LOGGER.debug("Reset controller setting.");
                    Optional.ofNullable(context.getPlayer()).ifPresent(p ->
                        p.sendStatusMessage(new TranslationTextComponent("chat.flexiblemarker.reset"), false));
                    return ActionResultType.SUCCESS;
                } else {
                    return super.onItemUseFirst(stack, context);
                }
            }
        } else
            return super.onItemUseFirst(stack, context);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.addAll(areaText(stack));
        tooltip.addAll(remotePosText(stack));
    }

    public static final Function<GlobalPos, ITextComponent> convertPosText = p ->
        new TranslationTextComponent("tooltip.flexiblemarker.remote_pos", p.getPos().getX(), p.getPos().getY(), p.getPos().getZ(), p.getDimension().getLocation());

    public static List<? extends ITextComponent> areaText(ItemStack stack) {
        if (Caps.isQuarryModLoaded())
            return getArea(stack)
                .map(AreaComponent.convertAreaText)
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
        else
            return Collections.emptyList();
    }

    private static class AreaComponent {
        public static final Function<Area, ITextComponent> convertAreaText = a ->
            new TranslationTextComponent("tooltip.flexiblemarker.area", a.xMin(), a.yMin(), a.zMin(), a.xMax(), a.yMax(), a.zMax());

        private static Optional<Area> getAreaInternal(ItemStack stack) {
            return Optional.ofNullable(stack.getChildTag(NBT_AREA))
                .map(Area::areaLoad);
        }
    }

    public static Optional<Area> getArea(ItemStack stack) {
        if (Caps.isQuarryModLoaded() && !stack.isEmpty()) {
            return AreaComponent.getAreaInternal(stack);
        } else {
            return Optional.empty();
        }
    }

    public static void setArea(ItemStack stack, CompoundNBT areaNBT) {
        stack.setTagInfo(NBT_AREA, areaNBT);
    }

    public static List<? extends ITextComponent> remotePosText(ItemStack stack) {
        return getRemotePos(stack).map(convertPosText)
            .map(Collections::singletonList)
            .orElse(Collections.emptyList());
    }

    public static Optional<GlobalPos> getRemotePos(ItemStack stack) {
        if (Caps.isQuarryModLoaded() && !stack.isEmpty())
            return Optional.ofNullable(stack.getTag())
                .filter(t -> t.contains(NBT_REMOTE_POS))
                .filter(t -> !t.contains(NBT_REMOTE_POS, Constants.NBT.TAG_LONG))
                .flatMap(t -> GlobalPos.CODEC.parse(NBTDynamicOps.INSTANCE, t.get(NBT_REMOTE_POS)).result());
        else {
            return Optional.empty();
        }
    }

    public static void setRemotePos(ItemStack stack, GlobalPos pos) {
        GlobalPos.CODEC.encodeStart(NBTDynamicOps.INSTANCE, pos).result()
            .ifPresent(p -> stack.setTagInfo(NBT_REMOTE_POS, p));
    }
}
