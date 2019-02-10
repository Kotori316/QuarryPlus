# Version 0.11.2
* Fixed a crash when you shift-clicked item in WorkbenchPlus.
* Added a condition quarryplus:machine_enabled, which checks if a machine which has the name in "value" field is enabled.
* Fixed an issue where you can't craft items with WorkbenchPlus due to recreating tile every ticks.

# Version 0.11.1
Changes
* Now SolidQuarry doesn't push fuels to hopper. Close [#43](https://github.com/Kotori316/QuarryPlus/issues/43).

# Version 0.11.0
Changes
* Remove pipes when MiningwellPlus is removed. Close [#42](https://github.com/Kotori316/QuarryPlus/issues/42)
* Fixed java.lang.NullPointerException at EnergyDebug#getTime when tile is loaded.
* Config now outputs quarryplus_noDigBlocks.json.

# Version 0.10.9
Changes
* Fixed a problem where quarry didn't find a marker which implemented buildcraft.api.core.IAreaProvider.
* Changed logic to search markers.

# Version 0.10.8
Changes
* Internal changes

# Version 0.10.7
Changes
* Fixed a crash due to NullPointerException in FakePlayer. [#41](https://github.com/Kotori316/QuarryPlus/issues/41)
* Updated for new JEI newer than 4.12.0
