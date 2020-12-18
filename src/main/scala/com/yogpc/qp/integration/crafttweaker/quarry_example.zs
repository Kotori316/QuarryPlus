import mods.quarryplus.WorkbenchPlus;
import crafttweaker.api.item.IIngredient;

WorkbenchPlus.INSTANCE.addSingleInputRecipe(
   "convert_glass", // recipe name
   <tag:items:forge:stained_glass>, // input
   <item:minecraft:glass>, // output
   1000 // energy
);

WorkbenchPlus.INSTANCE.addMultiInputRecipe(
   "get_more_blaze_powder", // recipe name
   [<item:minecraft:blaze_rod>, <item:minecraft:magma_cream>], // inputs
   <item:minecraft:blaze_powder> * 5, // output
   1000 // energy
);

var inputs = [
  [<item:minecraft:coal>, <item:minecraft:charcoal>] as IIngredient[],
  [<tag:items:minecraft:logs>] as IIngredient[]
] as IIngredient[][];
WorkbenchPlus.INSTANCE.addRecipe(
   "get_more_torch", // recipe name
   inputs,
   <item:minecraft:torch> * 64, // output
   1000 // energy
);
