package com.yogpc.qp.render

import com.yogpc.qp.QuarryPlus
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

import scala.collection.mutable

object Sprites {

    val instance = this
    private val map = mutable.Map.empty[Symbol, TextureAtlasSprite]

    def getMap = map.toMap

    @SubscribeEvent
    def registerTexture(event: TextureStitchEvent.Pre): Unit = {
        val textureMap = event.getMap
        LaserType.values().foreach(laserType => map.put(laserType.symbol, textureMap.registerSprite(laserType.location())))
        val put_F = (name: Symbol) => map.put(name, textureMap.registerSprite(new ResourceLocation(QuarryPlus.modID, "entities/" + name)))
        List('laser_1, 'laser_2, 'laser_3, 'laser_4, 'yellow, 'stripes_h, 'stripes_v, 'stripes_b, 'stripes_refinery).foreach(put_F)
    }
}
