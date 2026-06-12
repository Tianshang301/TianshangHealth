package com.tianshang.health.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val gender: String = "female", // male/female/other
    val heightCm: Float? = null,
    val dateOfBirth: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    enum class Gender(val value: String) {
        MALE("male"),
        FEMALE("female"),
        OTHER("other");

        companion object {
            fun fromValue(value: String): Gender {
                return entries.find { it.value == value } ?: FEMALE
            }
        }
    }
}
