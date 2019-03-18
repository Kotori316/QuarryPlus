# Version 1.0.5
* Sneak and right click with AE's wrench and other mods's one to remove machine.
* Reduced log spam.

# Version 1.0.4
* Add NoFrameMode to ChunkDestroyer. The mode skips building frame and start breaking blocks immediately.
* Add some tooltips to attachment machines. e.g. pumps
* Removed duplicated code.

# Version 1.0.3
* Add a new item, "Quarry Y Setter", which allows you to stop quarry work at a specific y level.
* Machines with ability of changing its texture when working have comparator output. (When working 15, otherwise 0)

# Version 1.0.2
* Support BuildCraft 7.99.21 and newer. Close [#47](https://github.com/Kotori316/QuarryPlus/issues/47).

# Version 1.0.1
* Working to fix a bug where QuarryPlus stops its work while removing flowing liquid with PumpPlus in frame mode.
If you have this problem, right click the PumpPlus with a stick or wrench to change PumpPlus's mode.
* Made quarry head moving much faster. Change FastQuarryHeadMove in config to true.

# Version 1.0.0
* Now you can modify recipes of WorkbenchPlus. Copy file from `config/quarryplus/defaultrecipes` to `config/quarryplus/recipes` and change `UseHardCodeRecipe` in config to false.
* Made Frame mode work faster.

# Version 0.11.2
* Fixed a crash when you shift-clicked item in WorkbenchPlus.
* Added a condition quarryplus:machine_enabled, which checks if a machine which has the name in "value" field is enabled.
* Fixed an issue where you can't craft items with WorkbenchPlus due to recreating tile every ticks.

# Version 0.11.1
* Now SolidQuarry doesn't push fuels to hopper. Close [#43](https://github.com/Kotori316/QuarryPlus/issues/43).

# Version 0.11.0
* Remove pipes when MiningwellPlus is removed. Close [#42](https://github.com/Kotori316/QuarryPlus/issues/42)
* Fixed java.lang.NullPointerException at EnergyDebug#getTime when tile is loaded.
* Config now outputs quarryplus_noDigBlocks.json.

# Version 0.10.9
* Fixed a problem where quarry didn't find a marker which implemented buildcraft.api.core.IAreaProvider.
* Changed logic to search markers.

# Version 0.10.8
* Internal changes

# Version 0.10.7
* Fixed a crash due to NullPointerException in FakePlayer. [#41](https://github.com/Kotori316/QuarryPlus/issues/41)
* Updated for new JEI newer than 4.12.0
