package com.tianshang.health.feature.nutrition.util

import androidx.annotation.StringRes
import com.tianshang.health.core.common.R

enum class MealType(
    val value: String,
    @StringRes val displayNameResId: Int
) {
    BREAKFAST("breakfast", R.string.meal_breakfast),
    LUNCH("lunch", R.string.meal_lunch),
    DINNER("dinner", R.string.meal_dinner),
    SNACK("snack", R.string.meal_snack);

    companion object {
        fun fromValue(value: String): MealType {
            return entries.find { it.value == value } ?: BREAKFAST
        }
    }
}
