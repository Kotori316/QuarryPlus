# Craft Tweaker Integration

Files in this package adds the way to modify recipes of WorkbenchPlus. WorkbenchPlus is used to create some machines in
this mod, and you can add/remove recipes via json in data packs. Now you can use CraftTweaker to change the recipe with
powerful dynamic features CT provides. For users who want to know how to use CraftTweaker,
see [wiki](https://docs.blamejared.com/1.16/en/index/).

## The instance to access WorkbenchPlus recipes

```groovy
import mods.quarryplus.WorkbenchPlus;

var workbench = WorkbenchPlus.INSTANCE;
// Also you can access with this way
// var workbench = <recipetype:quarryplus:workbench_recipe>;
```

The instance implements [`IRecipeManager`](https://docs.blamejared.com/1.16/en/recipes/recipe_managers/). The basic
methods to get recipes or remove recipes are available via the instance.

## How to add Workbench recipe

I added some methods to add recipes. You can also use `addJSONRecipe(String, IData)` but I don't refer to it.

The api is here.
```java
class WorkbenchPlus {
    public static final WorkbenchPlus INSTANCE;


    /**
     * Add a recipe of ONE input and output.
     * Example <blockquote><pre>
     * WorkbenchPlus.INSTANCE.addSingleInputRecipe(
     *     "convert_glass", // recipe name
     *     &lt;tag:items:forge:stained_glass&gt;, // input
     *     &lt;item:minecraft:glass&gt;, // output
     *     1000 // energy
     * );
     * </pre></blockquote>
     *
     * @param recipeName the name of recipe. [a-z0-9/._-] is allowed.
     * @param input      the input ingredient.
     * @param output     the output item.
     * @param energy     required energy. Unit [FE]
     */
    @ZenCodeType.Method
    public void addSingleInputRecipe(String recipeName, IIngredient input, IItemStack output, @ZenCodeType.OptionalFloat(1000) float energy);

    /**
     * Add a recipe of multi inputs and one output.
     * Example <blockquote><pre>
     * WorkbenchPlus.INSTANCE.addMultiInputRecipe(
     *    "get_more_blaze_powder", // recipe name
     *    [&lt;item:minecraft:blaze_rod&gt;, &lt;item:minecraft:magma_cream&gt;], // inputs
     *    &lt;item:minecraft:blaze_powder&gt; * 5, // output
     *    1000 // energy
     * );
     * </pre></blockquote>
     *
     * @param recipeName the name of recipe. [a-z0-9/._-] is allowed.
     * @param inputs     array of the input ingredients.
     * @param output     the output item.
     * @param energy     required energy. Unit [FE]
     */
    @ZenCodeType.Method
    public void addMultiInputRecipe(String recipeName, IIngredient[] inputs, IItemStack output, @ZenCodeType.OptionalFloat(1000) float energy);

    /**
     * Add a recipe of multi inputs and one output.
     * Example <blockquote><pre>
     * var inputs = [
     *   [&lt;item:minecraft:coal&gt, &lt;item:minecraft:charcoal&gt] as IIngredient[],
     *   [&lt;tag:items:minecraft:logs&gt;] as IIngredient[]
     * ] as IIngredient[][];
     * WorkbenchPlus.INSTANCE.addRecipe(
     *    "get_more_torch", // recipe name
     *    inputs,
     *    &lt;item:minecraft:torch&gt; * 64, // output
     *    1000 // energy
     * );
     * </pre></blockquote>
     *
     * @param recipeName the name of recipe. [a-z0-9/._-] is allowed.
     * @param inputs     array of the input ingredients.
     * @param output     the output item.
     * @param energy     required energy. Unit [FE]
     */
    @ZenCodeType.Method
    public void addRecipe(String recipeName, IIngredient[][] inputs, IItemStack output, @ZenCodeType.OptionalFloat(1000) float energy);

    /**
     * @param recipeName  the name of recipe. [a-z0-9/._-] is allowed.
     * @param copyFrom    the item from which nbt is copied.
     * @param otherInputs other input item. Empty([]) is allowed if additional input is nothing.
     * @param output      the item to which nbt is moved
     * @param energy      required energy. Unit [FE]
     */
    @ZenCodeType.Method
    public void addEnchantmentCopyRecipe(String recipeName, IIngredient[] copyFrom, IIngredient[][] otherInputs, IItemStack output, @ZenCodeType.OptionalFloat(1000) float energy);
}
```

## Example
See quarry_example.zs in this directory.
