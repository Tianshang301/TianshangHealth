package com.tianshang.health.feature.fitness.util

import androidx.annotation.StringRes
import com.tianshang.health.core.common.R

enum class ExerciseType(
    val value: String,
    @StringRes val displayNameResId: Int,
    val metValue: Float
) {
    RUNNING("running", R.string.exercise_running, 9.8f),
    WALKING("walking", R.string.exercise_walking, 3.5f),
    CYCLING("cycling", R.string.exercise_cycling, 7.5f),
    SWIMMING("swimming", R.string.exercise_swimming, 8.0f),
    YOGA("yoga", R.string.exercise_yoga, 3.0f),
    STRENGTH("strength", R.string.exercise_strength, 5.0f),
    DANCE("dance", R.string.exercise_dance, 5.5f),
    PILATES("pilates", R.string.exercise_pilates, 3.5f),
    HIIT("hiit", R.string.exercise_hiit, 12.0f),
    BASKETBALL("basketball", R.string.exercise_basketball, 7.5f),
    FOOTBALL("football", R.string.exercise_football, 8.5f),
    TENNIS("tennis", R.string.exercise_tennis, 7.0f),
    BADMINTON("badminton", R.string.exercise_badminton, 5.5f),
    TABLE_TENNIS("table_tennis", R.string.exercise_table_tennis, 4.0f),
    SKIPPING("skipping", R.string.exercise_skipping, 10.0f),
    HIKING("hiking", R.string.exercise_hiking, 5.3f),
    OTHER("other", R.string.exercise_other, 4.5f);

    companion object {
        fun fromValue(value: String): ExerciseType {
            return entries.find { it.value == value } ?: OTHER
        }
    }
}
