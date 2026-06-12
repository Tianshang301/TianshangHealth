package com.tianshang.health.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "period_records",
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
        Index(value = ["startDate"]),
        Index(value = ["userId", "startDate"])
    ]
)
data class PeriodRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val startDate: String, // ISO date format: yyyy-MM-dd
    val endDate: String? = null, // ISO date format: yyyy-MM-dd
    val flowLevel: Int? = null, // 1=Light, 2=Medium, 3=Heavy
    val painLevel: Int? = null, // 0=None, 1=Mild, 2=Moderate, 3=Severe
    val notes: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
