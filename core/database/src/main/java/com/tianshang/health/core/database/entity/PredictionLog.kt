package com.tianshang.health.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Stores prediction accuracy feedback for adaptive parameter tuning.
 * Tracks rules engine vs TFLite predictions side by side for comparison.
 */
@Entity(
    tableName = "prediction_logs",
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
        Index(value = ["createdAt"]),
        Index(value = ["tfliteModelUsed"])
    ]
)
data class PredictionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val predictedStartDate: String, // yyyy-MM-dd (final prediction used)
    val predictedEndDate: String?, // yyyy-MM-dd, nullable
    val actualStartDate: String?, // yyyy-MM-dd, filled when user records period
    val errorDays: Int?, // predicted - actual, null if actual unknown
    val confidence: String, // HIGH/MEDIUM/LOW/INSUFFICIENT_DATA
    val algorithmVersion: String = "v2.0",
    val decayFactorUsed: Float = 0.75f,
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null,
    // TFLite comparison fields
    val tflitePredictedStartDate: String? = null, // TFLite predicted start date
    val rulesPredictedStartDate: String? = null, // Rules engine predicted start date
    val tfliteModelUsed: String? = null, // model name or null if fallback
    val tfliteConfidence: Float? = null, // TFLite confidence (0-1)
    val agreementScore: Float? = null // consistency between rules and TFLite
)
