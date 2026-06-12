package com.tianshang.health.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tianshang.health.core.common.constants.HealthConstants

@Entity(
    tableName = "daily_steps",
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
data class DailySteps(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val date: String, // ISO date format: yyyy-MM-dd
    val count: Int = 0,
    val goal: Int = HealthConstants.DEFAULT_STEPS_GOAL,
    val isFromHardware: Boolean = true, // true = hardware sensor, false = accelerometer fallback
    val syncedToHealthConnect: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
