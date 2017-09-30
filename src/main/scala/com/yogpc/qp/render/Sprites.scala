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
        LaserType.values().foreach(laserType => map.put(laserType.symbol, event.getMap.registerSprite(laserType.location())))
        map.put('laser_1, event.getMap.registerSprite(new ResourceLocation(QuarryPlus.modID, "entities/laser_1")))
        map.put('laser_2, event.getMap.registerSprite(new ResourceLocation(QuarryPlus.modID, "entities/laser_2")))
        map.put('laser_3, event.getMap.registerSprite(new ResourceLocation(QuarryPlus.modID, "entities/laser_3")))
        map.put('laser_4, event.getMap.registerSprite(new ResourceLocation(QuarryPlus.modID, "entities/laser_4")))
        //        map.put('stripes, event.getMap.registerSprite(new ResourceLocation(QuarryPlus.modID, "entities/stripes")))
        map.put('stripes_h, event.getMap.registerSprite(new ResourceLocation(QuarryPlus.modID, "entities/stripes_h")))
        map.put('stripes_v, event.getMap.registerSprite(new ResourceLocation(QuarryPlus.modID, "entities/stripes_v")))
        map.put('stripes_b, event.getMap.registerSprite(new ResourceLocation(QuarryPlus.modID, "entities/stripes_b")))
    }
}
