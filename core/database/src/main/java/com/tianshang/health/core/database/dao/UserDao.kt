package com.tianshang.health.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tianshang.health.core.database.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getById(userId: Long): User?

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getByIdFlow(userId: Long): Flow<User?>

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAll(): Flow<List<User>>

    @Query("SELECT * FROM users ORDER BY createdAt DESC LIMIT 1")
    suspend fun getFirst(): User?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getCount(): Int

    @Query("UPDATE users SET heightCm = :heightCm WHERE id = :userId")
    suspend fun updateHeight(userId: Long, heightCm: Float?)

    @Query("UPDATE users SET gender = :gender WHERE id = :userId")
    suspend fun updateGender(userId: Long, gender: String)

    @Query("UPDATE users SET dateOfBirth = :dateOfBirth WHERE id = :userId")
    suspend fun updateDateOfBirth(userId: Long, dateOfBirth: String?)
}
