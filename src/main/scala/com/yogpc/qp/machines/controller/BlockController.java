package com.yogpc.qp.machines.controller;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.base.APacketTile;
import com.yogpc.qp.machines.base.IDisabled;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.controller.AvailableEntities;
import com.yogpc.qp.utils.Holder;
import cpw.mods.modlauncher.api.INameMappingService;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import scala.Symbol;

import static jp.t2v.lab.syntax.MapStreamSyntax.streamCast;

//@net.minecraftforge.fml.common.Optional.Interface(iface = "cofh.api.block.IDismantleable", modid = QuarryPlus.Optionals.COFH_modID)
public class BlockController extends Block implements IDisabled /*IDismantleable*/ {
    private static final Field logic_spawnDelay;
    private static final Method logic_getEntityID;
    public static final scala.Symbol SYMBOL = scala.Symbol.apply("SpawnerController");
    public final BlockItem itemBlock;

    static {
        String fieldName = ObfuscationReflectionHelper.remapName(INameMappingService.Domain.FIELD, "field_98286_b");
        Field field;
        try {
            field = AbstractSpawner.class.getDeclaredField(fieldName);
            field.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        logic_spawnDelay = field;
        logic_getEntityID = ObfuscationReflectionHelper.findMethod(AbstractSpawner.class, "func_190895_g");
    }

    public BlockController() {
        super(Properties.create(Material.MISCELLANEOUS)
            .hardnessAndResistance(1.0f));
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.controller);
        setDefaultState(getStateContainer().getBaseState().with(QPBlock.WORKING(), false));
        itemBlock = new BlockItem(this, new Item.Properties().group(Holder.tab()));
        itemBlock.setRegistryName(QuarryPlus.modID, QuarryPlus.Names.controller);
    }

    @Override
    public Item asItem() {
        return itemBlock;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(QPBlock.WORKING());
    }

    private static Optional<AbstractSpawner> getSpawner(World world, BlockPos pos) {
        return Stream.of(Direction.values()).map(pos::offset).map(world::getTileEntity)
            .flatMap(streamCast(MobSpawnerTileEntity.class))
            .map(MobSpawnerTileEntity::getSpawnerBaseLogic).findFirst();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
                                    Hand hand, BlockRayTraceResult hit) {
        if (super.onBlockActivated(state, worldIn, pos, player, hand, hit)) return true;
        if (!player.isSneaking()) {
            if (!worldIn.isRemote) {
                if (player.getHeldItem(hand).getItem() == Holder.itemStatusChecker()) {
                    getSpawner(worldIn, pos)
                        .flatMap(logic -> Optional.ofNullable(APacketTile.invoke(logic_getEntityID, ResourceLocation.class, logic)))
                        .map(ResourceLocation::toString)
                        .map(s -> "Spawner Mob: " + s)
                        .map(StringTextComponent::new)
                        .ifPresent(s -> player.sendStatusMessage(s, false));
                } else if (enabled()) {
                    List<EntityType<?>> entries = ForgeRegistries.ENTITIES.getValues().stream().filter(e ->
                        !Config.common().spawnerBlacklist().contains(e.getRegistryName())).collect(Collectors.toList());

                    PacketHandler.sendToClient(AvailableEntities.create(pos, worldIn, entries), worldIn);
                } else {
                    player.sendStatusMessage(new StringTextComponent("Spawner Controller is disabled."), true);
                }
            }
            return true;
        }
        return false;
    }

    public static void setSpawnerEntity(World world, BlockPos pos, ResourceLocation name) {
        getSpawner(world, pos).ifPresent(logic -> {
            Optional.of(name)
                .filter(n -> !Config.common().spawnerBlacklist().contains(n))
                .map(ForgeRegistries.ENTITIES::getValue)
                .ifPresent(logic::setEntityType);
            Optional.ofNullable(logic.getWorld().getTileEntity(logic.getSpawnerPosition())).ifPresent(TileEntity::markDirty);
            BlockState state = logic.getWorld().getBlockState(logic.getSpawnerPosition());
            logic.getWorld().notifyBlockUpdate(logic.getSpawnerPosition(), state, state, 3);
        });
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isRemote && !Config.common().disabled().apply(SYMBOL).get()) {
            boolean powered = worldIn.isBlockPowered(pos);
            boolean m = state.get(QPBlock.WORKING());
            if (powered && !m) {
                getSpawner(worldIn, pos).ifPresent(logic -> {
                    try {
                        logic_spawnDelay.setInt(logic, 0);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return;
                    }
                    FakePlayer fakePlayer = FakePlayerFactory.getMinecraft((ServerWorld) worldIn);
                    fakePlayer.setWorld(logic.getWorld());
                    fakePlayer.setPosition(logic.getSpawnerPosition().getX(), logic.getSpawnerPosition().getY(), logic.getSpawnerPosition().getZ());
//                    logic.getWorld().players.add(fakePlayer);
                    logic.tick();
//                    logic.getWorld().players.remove(fakePlayer);
                });
            }
            worldIn.setBlockState(pos, state.with(QPBlock.WORKING(), powered));
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
    }

    @Override
    public Symbol getSymbol() {
        return SYMBOL;
    }

    @Override
    public boolean defaultDisableMachine() {
        return true;
    }

    /*
    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.COFH_modID)
    public ArrayList<ItemStack> dismantleBlock(World world, BlockPos pos, IBlockState state, EntityPlayer player, boolean returnDrops) {
        return ADismCBlock.dismantle(world, pos, state, returnDrops);
    }

    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.COFH_modID)
    public boolean canDismantle(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return true;
    }*/
}
