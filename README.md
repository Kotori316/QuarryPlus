# QuarryPlus for 1.20

This program is licensed under GNU LESSER GENERAL PUBLIC LICENSE.

Copyright (C) 2012, 2013 yogpstop, Copyright (C) 2017-2023 Kotori316

===========

[![](https://github.com/Kotori316/QuarryPlus/workflows/Build%20Check%20and%20Publish/badge.svg)](https://github.com/Kotori316/QuarryPlus/actions)

[![](https://cf.way2muchnoise.eu/versions/282837.svg)](https://www.curseforge.com/minecraft/mc-mods/additional-enchanted-miner)
[![](https://cf.way2muchnoise.eu/full_282837.svg)](https://www.curseforge.com/minecraft/mc-mods/additional-enchanted-miner)

CurseForge - https://www.curseforge.com/minecraft/mc-mods/additional-enchanted-miner

Requires [Scalable Cat's Force](https://www.curseforge.com/minecraft/mc-mods/scalable-cats-force).

## Finished Work

* Marker
  * Renderer
  * ~Marker Plus~, Flexible Marker, Chunk Marker
* Waterlogged Marker
  * ~Marker Plus~, Flexible Marker, Chunk Marker
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
* Remote Placer
* Replacer
* Book Enchantment Mover
* Chunk Destroyer
  * Enchantment
* Spawner Controller
* Mini Quarry
* Filler

## Maven repo

See [here](https://dev.azure.com/Kotori316/minecraft/_artifacts/feed/mods/maven/com.kotori316%2Fadditionalenchantedminer/versions)
to get other versions.

```groovy
repositories {
    maven {
        name "Kotori316 Azure Maven"
        url = uri("https://pkgs.dev.azure.com/Kotori316/minecraft/_packaging/mods/maven/v1")
    }
}
dependencies {
    // https://dev.azure.com/Kotori316/minecraft/_artifacts/feed/mods/maven/com.kotori316%2Fadditionalenchantedminer/versions
    implementation(fg.deobf("com.kotori316:AdditionalEnchantedMiner:VERSION".toLowerCase(Locale.ROOT)))
}
```
