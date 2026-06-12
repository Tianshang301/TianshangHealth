package com.tianshang.health.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_health",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["date"]),
        Index(value = ["userId", "date"], unique = true)
    ]
)
data class DailyHealth(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val date: String, // ISO date format: yyyy-MM-dd

    // Exercise data
    val steps: Int? = null,
    val caloriesBurned: Float? = null,
    val exerciseMinutes: Int? = null,
    val exerciseType: String? = null,

    // Sleep data
    val sleepHours: Float? = null,
    val deepSleepHours: Float? = null,
    val sleepQuality: Int? = null, // 1-5

    // Nutrition data
    val caloriesIntake: Float? = null,
    val waterIntake: Float? = null, // in ml
    val proteinGrams: Float? = null,
    val carbsGrams: Float? = null,
    val fatGrams: Float? = null,

    // Physical data
    val weightKg: Float? = null,
    val heartRateResting: Int? = null,
    val bodyTemperature: Float? = null,

    // Mood data
    val moodScore: Int? = null, // 1-5
    val stressLevel: Int? = null, // 1-5

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
