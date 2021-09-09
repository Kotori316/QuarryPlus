# QuarryPlus for 1.17.1
===========

[![](https://github.com/Kotori316/QuarryPlus/workflows/Build%20Check%20and%20Publish/badge.svg)](https://github.com/Kotori316/QuarryPlus/actions)

[![](http://cf.way2muchnoise.eu/versions/additional-enchanted-miner.svg)](https://www.curseforge.com/minecraft/mc-mods/additional-enchanted-miner)
[![](http://cf.way2muchnoise.eu/full_additional-enchanted-miner_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/additional-enchanted-miner)

CurseForge - https://www.curseforge.com/minecraft/mc-mods/additional-enchanted-miner

## Finished Work

* Marker
  * Renderer
  * MarkerPlus, Flexible Marker, Chunk Marker
* Workbench
  * JEI support
* Enchantment Mover
  * JEI support
* Miningwell
* QuarryFrame
* Quarry
  * Renderer
  * Enchantment
* PumpPlus
* Advanced Pump
  * Enchantment
* Some modules
* Exp Pump

## Planned (Future work)

* Book Enchantment Mover
* Spawner Controller
* Chunk Destroyer
* Placer Plus
* Mini Quarry
* Replacer

## Maven repo

See [here](https://dev.azure.com/Kotori316/minecraft/_packaging?_a=package&feed=mods%40Local&package=com.kotori316%3Aadditionalenchantedminer&protocolType=maven&view=versions)
to get other versions.

```groovy
repositories {
    maven {
        name "Kotori316 Azure Maven"
        url = uri("https://pkgs.dev.azure.com/Kotori316/minecraft/_packaging/mods/maven/v1")
    }
}
dependencies {
    // https://dev.azure.com/Kotori316/minecraft/_packaging?_a=package&feed=mods%40Local&package=com.kotori316%3Aadditionalenchantedminer&protocolType=maven&view=versions
    implementation(fg.deobf("com.kotori316:AdditionalEnchantedMiner:VERSION".toLowerCase(Locale.ROOT)))
}
```