# QuarryPlus for 1.17.1

This program is licensed under GNU LESSER GENERAL PUBLIC LICENSE.

Copyright (C) 2012, 2013 yogpstop, Copyright (C) 2017-2021 Kotori316

===========

[![](https://github.com/Kotori316/QuarryPlus/workflows/Build%20Check%20and%20Publish/badge.svg)](https://github.com/Kotori316/QuarryPlus/actions)

[![](http://cf.way2muchnoise.eu/versions/additional-enchanted-miner.svg)](https://www.curseforge.com/minecraft/mc-mods/additional-enchanted-miner)
[![](http://cf.way2muchnoise.eu/full_additional-enchanted-miner_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/additional-enchanted-miner)

CurseForge - https://www.curseforge.com/minecraft/mc-mods/additional-enchanted-miner

Requires [Scalable Cat's Force](https://www.curseforge.com/minecraft/mc-mods/scalable-cats-force/files/3488867).

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
* Placer Plus
* Replacer
* Book Enchantment Mover
* Chunk Destroyer
  * Enchantment
* Spawner Controller

## Planned (Future work)

* Mini Quarry

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