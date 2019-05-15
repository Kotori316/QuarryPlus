package com.yogpc.qp.machines.controller;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.controller.AvailableEntities;
import com.yogpc.qp.utils.Holder;
import cpw.mods.modlauncher.api.INameMappingService;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

//@net.minecraftforge.fml.common.Optional.Interface(iface = "cofh.api.block.IDismantleable", modid = QuarryPlus.Optionals.COFH_modID)
public class BlockController extends Block /*implements IDismantleable*/ {
    private static final Field logic_spawnDelay;
    public static final scala.Symbol SYMBOL = scala.Symbol.apply("SpawnerController");
    public final ItemBlock itemBlock;

    static {
        String fieldName = ObfuscationReflectionHelper.remapName(INameMappingService.Domain.FIELD, "field_98286_b");
        Field field;
        try {
            field = MobSpawnerBaseLogic.class.getDeclaredField(fieldName);
            field.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        logic_spawnDelay = field;
    }

    public BlockController() {
        super(Properties.create(Material.CIRCUITS)
            .hardnessAndResistance(1.0f));
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.controller);
        setDefaultState(getStateContainer().getBaseState().with(QPBlock.WORKING(), false));
        itemBlock = new ItemBlock(this, new Item.Properties().group(Holder.tab()));
        itemBlock.setRegistryName(QuarryPlus.modID, QuarryPlus.Names.controller);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder) {
        builder.add(QPBlock.WORKING());
    }

    private static Optional<MobSpawnerBaseLogic> getSpawner(World world, BlockPos pos) {
        return Stream.of(EnumFacing.values()).map(pos::offset).map(world::getTileEntity).filter(TileEntityMobSpawner.class::isInstance)
            .map(tileEntity -> ((TileEntityMobSpawner) tileEntity).getSpawnerBaseLogic()).findFirst();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (super.onBlockActivated(state, worldIn, pos, player, hand, side, hitX, hitY, hitZ)) return true;
        if (!player.isSneaking()) {
            if (!worldIn.isRemote) {
                if (true /*!Config.content().disableMapJ().get(SYMBOL)*/) {
                    List<EntityType<?>> entries = ForgeRegistries.ENTITIES.getValues().stream().filter(e ->
                        !Modifier.isAbstract(e.getEntityClass().getModifiers())
                            && !Config.common().spawnerBlacklist().contains(e.getRegistryName())).collect(Collectors.toList());

                    PacketHandler.sendToClient(AvailableEntities.create(pos, worldIn, entries), worldIn);
                } else {
                    player.sendStatusMessage(new TextComponentString("Spawner Controller is disabled."), true);
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
            IBlockState state = logic.getWorld().getBlockState(logic.getSpawnerPosition());
            logic.getWorld().notifyBlockUpdate(logic.getSpawnerPosition(), state, state, 3);
        });
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!worldIn.isRemote /*&& !Config.content().disableMapJ().get(SYMBOL)*/) {
            boolean r = worldIn.isBlockPowered(pos);
            boolean m = state.get(QPBlock.WORKING());
            if (r && !m) {
                getSpawner(worldIn, pos).ifPresent(logic -> {
                    try {
                        logic_spawnDelay.setInt(logic, 0);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return;
                    }
                    FakePlayer fakePlayer = FakePlayerFactory.getMinecraft((WorldServer) worldIn);
                    fakePlayer.setWorld(logic.getWorld());
                    fakePlayer.setPosition(logic.getSpawnerPosition().getX(), logic.getSpawnerPosition().getY(), logic.getSpawnerPosition().getZ());
                    logic.getWorld().playerEntities.add(fakePlayer);
                    logic.tick();
                    logic.getWorld().playerEntities.remove(fakePlayer);
                });
                worldIn.setBlockState(pos, state.with(QPBlock.WORKING(), true));
            } else if (!r && m) {
                worldIn.setBlockState(pos, state.with(QPBlock.WORKING(), false));
            }
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
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
