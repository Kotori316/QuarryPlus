# QuarryPlus for 1.21

This program is licensed under GNU LESSER GENERAL PUBLIC LICENSE.

Copyright (C) 2012, 2013 yogpstop, Copyright (C) 2017-2024 Kotori316

===========

[![](https://github.com/Kotori316/QuarryPlus/workflows/Build%20Check%20and%20Publish/badge.svg)](https://github.com/Kotori316/QuarryPlus/actions)

[![](https://cf.way2muchnoise.eu/versions/282837.svg)](https://www.curseforge.com/minecraft/mc-mods/additional-enchanted-miner)
[![](https://cf.way2muchnoise.eu/full_282837.svg)](https://www.curseforge.com/minecraft/mc-mods/additional-enchanted-miner)

CurseForge - https://www.curseforge.com/minecraft/mc-mods/additional-enchanted-miner

Requires [Scalable Cat's Force](https://www.curseforge.com/minecraft/mc-mods/scalable-cats-force).

## Maven repo

See [here](https://maven.kotori316.com) to get other versions.

```groovy
repositories {
    maven {
        name "Kotori316"
        url = uri("https://maven.kotori316.com")
    }
}
dependencies {
    // since 1.20.4 See https://maven.kotori316.com/com/kotori316
    // "AdditionalEnchantedMiner" is for Forge. For fabric and NeoForge, use the version with suffix.
    implementation(fg.deobf("com.kotori316:AdditionalEnchantedMiner:21.0.x".toLowerCase(Locale.ROOT)))
}
```
