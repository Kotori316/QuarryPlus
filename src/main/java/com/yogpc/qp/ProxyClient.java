package com.yogpc.qp;

import java.util.Map;
import java.util.WeakHashMap;

import com.yogpc.qp.entity.EntityLaser;
import com.yogpc.qp.render.RenderEntityLaser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ProxyClient extends ProxyCommon {
    private int key = 0;

    static {
        for (final Key k : Key.values())
            if (k.name != null) {
                k.binding = new KeyBinding(k.name, k.id, "key.yoglib");
                ClientRegistry.registerKeyBinding((KeyBinding) k.binding);
            }
    }

    public ProxyClient() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void keyUpdate(final TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START)
            return;
        final int prev = this.key;
        this.key = 0;
        final GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if (currentScreen == null || currentScreen.allowUserInput)
            for (final Key k : Key.values())
                if (k.binding instanceof KeyBinding) {
                    if (GameSettings.isKeyDown((KeyBinding) k.binding))
                        this.key |= 1 << k.ordinal();
                } else
                    switch (k) {
                        case forward:
                            if (GameSettings.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindForward))
                                this.key |= 1 << k.ordinal();
                            break;
                        case mode:
                            break;
                        case jump:
                            if (GameSettings.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindJump))
                                this.key |= 1 << k.ordinal();
                            break;
                    }
        if (this.key != prev) {
            PacketHandler.sendPacketToServer(new YogpstopPacket(this.key));
            this.setKeys(Minecraft.getMinecraft().player, this.key);
        }
    }

    @Override
    public EntityPlayer getPacketPlayer(final INetHandler inh) {
        if (inh instanceof NetHandlerPlayServer)
            return ((NetHandlerPlayServer) inh).playerEntity;
        return Minecraft.getMinecraft().player;
    }

    private final Map<EntityPlayer, Integer> keys = new WeakHashMap<>();

    @Override
    public boolean getKey(final EntityPlayer p, final Key k) {
        return this.keys.containsKey(p) && (this.keys.get(p) & 1 << k.ordinal()) != 0;
    }

    @Override
    public int keysToInt(final EntityPlayer p) {
        return this.keys.getOrDefault(p, 0);
    }

    @Override
    public void setKeys(final EntityPlayer p, final int r) {
        this.keys.put(p, r);
    }

    @Override
    public int addNewArmourRendererPrefix(final String s) {
        return 0;
    }

    @Override
    public void removeEntity(final Entity e) {
        e.world.removeEntity(e);
        if (e.world.isRemote)
            ((WorldClient) e.world).removeEntityFromWorld(e.getEntityId());
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getMinecraft().world;
    }

    @Override
    public void registerTextures() {
        RenderingRegistry.registerEntityRenderingHandler(EntityLaser.class, RenderEntityLaser::new);
//        ClientRegistry.bindTileEntitySpecialRenderer(TileRefinery.class, RenderRefinery.INSTANCE);
    }
}
