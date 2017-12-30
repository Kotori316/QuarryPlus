package com.yogpc.qp.integration.jei

import java.util.{Collections, ArrayList => AList, List => JList}

import com.yogpc.qp.tile.WorkbenchRecipes
import mezz.jei.api.ingredients.IIngredients
import mezz.jei.api.recipe.IRecipeWrapper
import net.minecraft.item.ItemStack

class WorkBenchRecipeWrapper(recipe: WorkbenchRecipes) extends IRecipeWrapper with Ordered[WorkBenchRecipeWrapper] {

    override def getIngredients(ingredients: IIngredients): Unit = {
        val inputs = new AList[JList[ItemStack]](recipeSize)
        val outputs = new AList[ItemStack](1)

        recipe.inputs.foreach(t => {
            val in = new AList[ItemStack](1)
            in add t
            inputs add in
        })
        outputs add recipe.output.toStack()

        ingredients.setInputLists(classOf[ItemStack], inputs)
        ingredients.setOutputs(classOf[ItemStack], outputs)
    }

    val getEnergyRequired: Double = recipe.energy
    val recipeSize: Int = recipe.size

    override def compare(that: WorkBenchRecipeWrapper): Int = java.lang.Double.compare(getEnergyRequired, that.getEnergyRequired)
}

object WorkBenchRecipeWrapper {

    def getAll: JList[WorkBenchRecipeWrapper] = {
        val list = new AList[WorkBenchRecipeWrapper]()
        WorkbenchRecipes.getRecipeMap.collect { case (_, recipe) if recipe.showInJEI => new WorkBenchRecipeWrapper(recipe) }.foreach(list.add)
        Collections.sort(list)
        list
    }
}