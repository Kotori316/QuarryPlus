**This mod requires a library, [Scalable Cat's Force](https://minecraft.curseforge.com/projects/scalable-cats-force).**

## Version 13.3.3
* Added Fuel Module, which supply 10 RF/t every tick without resources.
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
