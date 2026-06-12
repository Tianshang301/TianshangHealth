package com.tianshang.health.feature.nutrition.util

object NutritionCalculator {

    private const val KCAL_TO_KJ = 4.184f

    // Macronutrient calorie values (kcal per gram)
    private const val PROTEIN_KCAL_PER_G = 4f
    private const val CARBS_KCAL_PER_G = 4f
    private const val FAT_KCAL_PER_G = 9f

    fun calculateCaloriesFromMacros(
        proteinGrams: Float?,
        carbsGrams: Float?,
        fatGrams: Float?
    ): Float {
        var total = 0f
        if (proteinGrams != null) total += proteinGrams * PROTEIN_KCAL_PER_G
        if (carbsGrams != null) total += carbsGrams * CARBS_KCAL_PER_G
        if (fatGrams != null) total += fatGrams * FAT_KCAL_PER_G
        return total
    }

    fun kcalToKj(kcal: Float): Float {
        return kcal * KCAL_TO_KJ
    }
}
