package com.yogpc.qp.integration.jei

import mezz.jei.api.recipe.{IRecipeHandler, IRecipeWrapper}

class WorkBenchRecipeHandler extends IRecipeHandler[WorkBenchRecipeWrapper] {

    override def getRecipeWrapper(recipe: WorkBenchRecipeWrapper): IRecipeWrapper = recipe

    override def getRecipeCategoryUid: String = WorkBenchRecipeCategory.UID

    override def getRecipeCategoryUid(recipe: WorkBenchRecipeWrapper): String = WorkBenchRecipeCategory.UID

    override def isRecipeValid(recipe: WorkBenchRecipeWrapper): Boolean = true

    override def getRecipeClass: Class[WorkBenchRecipeWrapper] = classOf[WorkBenchRecipeWrapper]
}
