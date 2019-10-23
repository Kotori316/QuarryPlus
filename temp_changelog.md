**This mod requires a library, [Scalable Cat's Force](https://www.curseforge.com/minecraft/mc-mods/scalable-cats-force).**

## Version 14.1.1
- Fixed items in workbench slots has incorrect count. (Dedicated server)
- Use vanilla data pack system for workbench recipe. Sync recipes with client.
- Extracted dummy recipe to object
- Working on an issue of server recipe loading
- Use sand stone tag in exclude list of ChunkDestroyer.
- Fixed waring of texture.
- Forge version up to 28.1.61

## Version 14.1.0
- Added -  GUI of Status Checker for new quarry. [#69](https://github.com/Kotori316/QuarryPlus/issues/69)

## Version 14.0.9
- Update for new Forge.
- Update for new Scala (**Requires 2.13.1 version of library.**)
- Added option to stop machines with RS signal. (Need to change config option `EnableRSontrol`)

## Version 14.0.8
- Fixed - a crash of WorkbenchPlus in dedicated server due to an access to client only method. (#64)

## Version 14.0.7
- **Made `NoEnergy` true in default.** There a few mods that can provide FE or RF with Forge.
This change will reduce confusion of not working machines.
- Internal changes.

## Version 14.0.6
- Fixed - the amount of received or used energy was not treated correctly.
- Improvement - Solid Fuel Quarry drops fuel item when removed.
- Improvement - Solid Fuel Quarry shows fuel count on its GUI.
- Improvement - Workbench act as receiving energy for 10 seconds when NoEnergy is true.
- Fixed - Solid Fuel Quarry consumed not only lava but also bucket.

## Version 14.0.5
- Fixed - Crashes of loading client-only classes in multi player server (Dedicated Server). #63
- Improvement - Show y level of the machine on its GUI.

## Version 14.0.4
- Improvement - Collect items and exp when removing liquid
- Reduced log
- Fixed - A bug where New Quarry skips many blocks when world is loaded.

## Version 14.0.3
* Improved the action of disabled machines

## Version 14.0.2
* Added Chunk Destroyer, with module system.

## Version 14.0.1
* Added Advanced Pump
* Internal changes

## Version 14.0.0
* Update for Minecraft 1.14.4

## Version 13.3.3
* Added Fuel Module, which supply 40 RF/t every tick without resources.
* Localized message shown up when you restart quarry.
* Added Creative Energy Source Module.

## Version 13.3.2
* Added Torch Module, which automatically places torch if the floor is dark enough monsters can spawn.
* Refactoring.
* Support chain breaking of MCAssistant. (Imported from 1.12.2)

## Version 13.3.1
Ported fixes from 1.12.2
* Fixed - fluid is replacer to dummy block.
* Log name
* Not to replace fluid
* Faster fluid removal
* Configurable modules
* Breaking sound
* Fixed - NPE when pump module is loaded.
* Y Setter for new quarry
* Fixed that onActivate is called in client.
* Fixed quarry drill head went to (0, 0, 0).
* Changed order of breaking blocks
* Fix NEP if unbreaking isn't provided. Fix (0, 0, 0) block is removed by quarry.
* Fixed step is 0 when x size or z side of area is 1. #60
* drops enchanted item when removed

## Version 13.3.0
* Added new quarry and module system. To enhance old quarry function, you need to place block next to the machine.
In module system, you can put a module item to quarry inventory to enhance the function.
* Changed version. (mc version).(major).(minor)

## Version 2.0.4
* Added signature to Jar file.
* Added List Template.
* Respect stack size of the result of WorkbenchRecipe.

## Version 2.0.3
* Refactoring to use cats.
* Added GUI of ListEditor
* Changed to beta.

## Version 2.0.2
* Update forge.
* You can use status checker to Spawner Controller to know mob name spawned by adjacent spawner.
* Implemented JEI plugin for BookMover and EnchantMover.
* Added AdvQuarry(ChunkDestroyer).
* Added AdvPump(Advanced Pump)
* Removed limit of enchantment. You can add Fortune and Silktouch at the same time to machines when BookMover is enabled.

## Version 2.0.1
* Added function of chunk loading to QuarryPlus, Solid Fuel Quarry and MarkerPlus.
This chunk loading uses vanilla method, World#func_212414_b(int, int, boolean). See source of /forceload (net.minecraft.command.impl.ForceLoadCommand).
* Refactoring.

## Version 2.0.0
* Fixed a crash of loading machine.
* Fixed recipes weren't registered.
* Quarry is now slower than before.

## Version 1.0.7
* Added replacer
* Improved validation of Workbench recipe

## Version 1.0.6
* Update for Minecraft 1.13.2
