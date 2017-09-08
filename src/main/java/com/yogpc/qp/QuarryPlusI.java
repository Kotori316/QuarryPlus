package com.yogpc.qp;

import com.yogpc.qp.block.BlockBreaker;
import com.yogpc.qp.block.BlockController;
import com.yogpc.qp.block.BlockFrame;
import com.yogpc.qp.block.BlockLaser;
import com.yogpc.qp.block.BlockMarker;
import com.yogpc.qp.block.BlockMiningWell;
import com.yogpc.qp.block.BlockMover;
import com.yogpc.qp.block.BlockPlacer;
import com.yogpc.qp.block.BlockPlainPipe;
import com.yogpc.qp.block.BlockPump;
import com.yogpc.qp.block.BlockQuarry;
import com.yogpc.qp.block.BlockRefinery;
import com.yogpc.qp.block.BlockWorkbench;
import com.yogpc.qp.entity.EntityLaser;
import com.yogpc.qp.entity.LaserType;
import com.yogpc.qp.gui.GuiHandler;
import com.yogpc.qp.item.ItemMirror;
import com.yogpc.qp.item.ItemQuarryDebug;
import com.yogpc.qp.item.ItemTool;
import com.yogpc.qp.render.RenderEntityLaser;
import com.yogpc.qp.tile.TileMarker;
import com.yogpc.qp.tile.WorkbenchRecipes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class QuarryPlusI {
    public static final QuarryPlusI INSANCE = new QuarryPlusI();

    private QuarryPlusI() {
    }

    public static final CreativeTabs ct = new CreativeTabQuarryPlus();

    public static final BlockQuarry blockQuarry = new BlockQuarry();
    public static final BlockMarker blockMarker = new BlockMarker();
    public static final BlockMover blockMover = new BlockMover();
    public static final BlockMiningWell blockMiningWell = new BlockMiningWell();
    public static final BlockPump blockPump = new BlockPump();
    public static final BlockRefinery blockRefinery = new BlockRefinery();
    public static final BlockPlacer blockPlacer = new BlockPlacer();
    public static final BlockBreaker blockBreaker = new BlockBreaker();
    public static final BlockLaser blockLaser = new BlockLaser();
    public static final BlockPlainPipe blockPlainPipe = new BlockPlainPipe();
    public static final BlockFrame blockFrame = new BlockFrame();
    public static final BlockWorkbench workbench = new BlockWorkbench();
    public static final BlockController controller = new BlockController();
    public static final Item itemTool = new ItemTool();
    public static final Item magicmirror = new ItemMirror();
    public static final Item debugItem = new ItemQuarryDebug();
//    public static final Item armor = new ItemArmorElectric();

    public static final int guiIdWorkbench = 1;
    public static final int guiIdMover = 2;
    public static final int guiIdFList = 3;
    public static final int guiIdSList = 4;
    public static final int guiIdPlacer = 5;

    @SubscribeEvent
    public void onWorldUnload(final WorldEvent.Unload event) {
        final TileMarker.Link[] la = TileMarker.linkList.toArray(new TileMarker.Link[TileMarker.linkList.size()]);
        for (final TileMarker.Link l : la)
            if (l.w == event.getWorld())
                l.removeConnection(false);
        final TileMarker.Laser[] lb = TileMarker.laserList.toArray(new TileMarker.Laser[TileMarker.laserList.size()]);
        for (final TileMarker.Laser l : lb)
            if (l.w == event.getWorld())
                l.destructor();
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void loadTextures(final TextureStitchEvent.Pre evt) {
        RenderEntityLaser.icons = new TextureAtlasSprite[]{
                evt.getMap().registerSprite(new ResourceLocation(LaserType.RED_LASER.location().toString())),
                evt.getMap().registerSprite(new ResourceLocation(LaserType.BLUE_LASER.location().toString())),
                evt.getMap().registerSprite(new ResourceLocation(LaserType.DRILL.location().toString())),
                evt.getMap().registerSprite(new ResourceLocation(LaserType.DRILL_HEAD.location().toString())),
        };
    }

    public static void preInit(final FMLPreInitializationEvent event) {
        Config.setConfigFile(event.getSuggestedConfigurationFile());
        ForgeChunkManager.setForcedChunkLoadingCallback(QuarryPlus.INSTANCE, new ChunkLoadingHandler());
        EntityRegistry.registerModEntity(new ResourceLocation(QuarryPlus.modID, EntityLaser.NAME), EntityLaser.class, EntityLaser.NAME,
                0, QuarryPlus.INSTANCE, 16 * 17, 20, false);
        QuarryPlus.proxy.registerTextures();
        MinecraftForge.EVENT_BUS.register(INSANCE);
        MinecraftForge.EVENT_BUS.register(Loot.instance());
        MinecraftForge.EVENT_BUS.register(Config.instance());
        NetworkRegistry.INSTANCE.registerGuiHandler(QuarryPlus.INSTANCE, new GuiHandler());
    }

    public static void init() {
        com.yogpc.qp.packet.PacketHandler.init();
        PacketHandler.channels = NetworkRegistry.INSTANCE.newChannel(QuarryPlus.Mod_Name, new YogpstopPacketCodec(), new PacketHandler());
        GameRegistry.addRecipe(new ItemStack(workbench, 1),
                "III", "GDG", "RRR",
                'D', Blocks.DIAMOND_BLOCK, 'R', Items.REDSTONE,
                'I', Blocks.IRON_BLOCK, 'G', Blocks.GOLD_BLOCK);
        WorkbenchRecipes.registerRecipes();
        PacketHandler.registerStaticHandler(BlockController.class);
        PacketHandler.registerStaticHandler(TileMarker.class);
    }
}
