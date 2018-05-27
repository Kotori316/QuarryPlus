package com.yogpc.qp.block;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import cofh.api.block.IDismantleable;
import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.compat.InvUtils;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.controller.AvailableEntities;
import com.yogpc.qp.version.VersionUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import static com.yogpc.qp.block.ADismCBlock.ACTING;

@net.minecraftforge.fml.common.Optional.Interface(iface = "cofh.api.block.IDismantleable", modid = QuarryPlus.Optionals.COFH_block)
public class BlockController extends Block implements IDismantleable {
    private static final Field logic_spawnDelay = ReflectionHelper.findField(MobSpawnerBaseLogic.class, "spawnDelay", "field_98286_b");
    public final ItemBlock itemBlock;

    public BlockController() {
        super(Material.CIRCUITS);
        setUnlocalizedName(QuarryPlus.Names.controller);
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.controller);
        setHardness(1.0f);
        setCreativeTab(QuarryPlusI.creativeTab());
        setDefaultState(getBlockState().getBaseState().withProperty(ACTING, false));
        itemBlock = new ItemBlock(this);
        itemBlock.setRegistryName(QuarryPlus.modID, QuarryPlus.Names.controller);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ACTING);
    }

    private static Optional<MobSpawnerBaseLogic> getSpawner(World world, BlockPos pos) {
        return Stream.of(EnumFacing.VALUES).map(pos::offset).map(world::getTileEntity).filter(TileEntityMobSpawner.class::isInstance)
            .map(tileEntity -> ((TileEntityMobSpawner) tileEntity).getSpawnerBaseLogic()).findFirst();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, ItemStack s, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (InvUtils.isDebugItem(playerIn, hand)) return true;
        if (!playerIn.isSneaking()) {
            if (!worldIn.isRemote) {
                if (!Config.content().disableController()) {
                    List<String> entries = new ArrayList<>();
                    for (Map.Entry<Class<? extends Entity>, String> entry : EntityList.CLASS_TO_NAME.entrySet()) {
                        Class<? extends Entity> entityClass = entry.getKey();
                        if (entityClass == null || Modifier.isAbstract(entityClass.getModifiers()) || Config.content().spawnerBlacklist().contains(entry.getValue())) {
                            continue;
                        }
                        entries.add(entry.getValue());
                    }
                    PacketHandler.sendToClient(AvailableEntities.create(pos, worldIn.provider.getDimension(), entries), (EntityPlayerMP) playerIn);
                } else {
                    VersionUtil.sendMessage(playerIn, new TextComponentString("Spawner Controller is disabled."), true);
                }
            }
            return true;
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, s, facing, hitX, hitY, hitZ);
    }

    public static void setSpawnerEntity(World world, BlockPos pos, String name) {
        getSpawner(world, pos).ifPresent(logic -> {
            if (!Config.content().spawnerBlacklist().contains(name))
                logic.setEntityName(name);
            Optional.ofNullable(logic.getSpawnerWorld().getTileEntity(logic.getSpawnerPosition())).ifPresent(TileEntity::markDirty);
            IBlockState state = logic.getSpawnerWorld().getBlockState(logic.getSpawnerPosition());
            logic.getSpawnerWorld().notifyBlockUpdate(logic.getSpawnerPosition(), state, state, 3);
        });
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn/*, BlockPos fromPos*/) {
        if (!worldIn.isRemote && !Config.content().disableController()) {
            boolean r = worldIn.isBlockPowered(pos);
            boolean m = state.getValue(ACTING);
            if (r && !m) {
                getSpawner(worldIn, pos).ifPresent(logic -> {
                    try {
                        logic_spawnDelay.setInt(logic, 0);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return;
                    }
                    FakePlayer fakePlayer = FakePlayerFactory.getMinecraft((WorldServer) worldIn);
                    fakePlayer.setWorld(logic.getSpawnerWorld());
                    fakePlayer.setPosition(logic.getSpawnerPosition().getX(), logic.getSpawnerPosition().getY(), logic.getSpawnerPosition().getZ());
                    logic.getSpawnerWorld().playerEntities.add(fakePlayer);
                    logic.updateSpawner();
                    logic.getSpawnerWorld().playerEntities.remove(fakePlayer);
                });
                worldIn.setBlockState(pos, state.withProperty(ACTING, true));
            } else if (!r && m) {
                worldIn.setBlockState(pos, state.withProperty(ACTING, false));
            }
        }
        super.neighborChanged(state, worldIn, pos, blockIn/*, fromPos*/);
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(ACTING, meta == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(ACTING) ? 1 : 0;
    }

    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.COFH_block)
    public ArrayList<ItemStack> dismantleBlock(World world, BlockPos pos, IBlockState state, EntityPlayer player, boolean returnDrops) {
        return ADismCBlock.dismantle(world, pos, state, returnDrops);
    }

    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.COFH_block)
    public boolean canDismantle(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return true;
    }
}
