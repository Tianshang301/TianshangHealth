package com.tianshang.health.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_symptoms",
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
data class DailySymptom(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val date: String, // ISO date format: yyyy-MM-dd
    val symptoms: String? = null, // JSON array of symptom names
    val sexualActivity: Boolean? = null,
    val ovulationTestResult: String? = null, // "positive", "negative", "unclear"
    val cervicalMucus: String? = null, // "dry", "sticky", "creamy", "watery", "egg_white"
    val bodyTemperature: Float? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
