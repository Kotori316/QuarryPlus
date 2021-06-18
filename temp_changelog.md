**This mod requires a library, [Scalable Cat's Force](https://www.curseforge.com/minecraft/mc-mods/scalable-cats-force).**
## Version 16.1.7
* Added log to collect information of Array error of PumpPlus.

## Version 16.1.6
* Restore the setting of Workbench when world is loaded.
* Add workbench bonus(5FE/t) to avoid report of energy connection and internal energy showing. Close #113

## Version 16.1.5
* Fixed the internal energy of Solid Fuel Quarry goes to under 0.
* Added French lang file.
* Update to Minecraft 1.16.5

## Version 16.1.4
* Added CraftTweaker script support. See document in "src/main/scala/com/yogpc/qp/integration/crafttweaker"
* Send a message if you try to use quarry in chunks protected by FTBChunks.
* Fixed a problem that Solid Fuel Quarry just removes blocks protected with break events.

## Version 16.1.3
* Build with the new forge.

## Version 16.1.2
* Fixed a bug that ChunkDestroyer collected stones.
* Removed `enableEncryption` from FakePlayer.

## Version 16.1.1
* Respect the result of break event in MiniQuarry.
* Fixed a bug that quarry removes blocks that an event canceled to break. Fix #103

## Version 16.1.0
* Build with the forge for 1.16.3
* Fixed Mini Quarry didn't work.
* Update library, requiring Scalable Cat's Force 2.13.3-build-3
* Update lang file.

## Version 16.0.8
* Fixed a side loading error shown in server launching.
* Fixed workbench plus create items even materials not supplied. Close #99

## Version 16.0.7
* Forge update to 1.16.1-32.0.98 and added license.
* Internal changes.
* Fix that quarry stopped in building frames if it tries to create a frame in water.

## Version 16.0.6
* Update Chinese lang file
* Also, updated for other languages.

## Version 16.0.5
* Add enchanted editors in the creative tab.
* Not to show message if template fails to add entry.
* Tells players that List Editor should be enchanted with the same enchantments as the machine. Close #94.

## Version 16.0.4
* REQUIRES Forge newer than 32.0.57
* Now, mods loads ignore list of Chunk Destroyer and disallow list of mining machine from data pack.
* Fixed an issue that workbench plus had no recipe output in the dedicated server.

## Version 16.0.3
* The first release version for Minecraft 1.16.1.
  * **Features**
    * QuarryPlus
    * Marker Plus
      * Also, 2 other markers, previously provided in other mod.
    * Workbench Plus, to craft items in this mod.
    * Solid Fuel Quarry
    * Modules
    * Advanced Pump
    * Chunk Destroyer
      * The list of removed blocks can't be changed due to forge limitation.
      * This will be improved.
    * List Editor
    * List Template
      * There is a strange part in its GUI.
    * Mini Quarry
    * Placer Plus
    * Enchantment Mover
  * The list of ignore blocks(previously, black list) can't be edited now due to forge limitation.
* Fixed module saw wrong dimension.
* Fixed NPE when opening module GUI of Adv Pump.
* Fixed quarryplus didn't show area when placed into the world.
* Fixed an arrow in GUI of book mover didn't work.

## Version 16.0.2-SNAPSHOT
* Re-added support of JEI.
* You can move enchantment from Netherite tools.
* Recipe of status checker changed.

## Version 16.0.1-SNAPSHOT
* Added GUI of Spawner Controller, ListEditor and Template.
  * GUI of template has something strange, but how to remove it?

## Version 16.0.0-SNAPSHOT
* First version for Minecraft 1.16.1
* Controller and some items doesn't work in this build.
* Be careful, we don't provide ways to supply energy.
