package com.tianshang.health.core.database.repository

import com.tianshang.health.core.common.util.ValidationUtils
import com.tianshang.health.core.database.dao.UserDao
import com.tianshang.health.core.database.entity.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {

    fun getAll(): Flow<List<User>> = userDao.getAll()

    suspend fun getById(userId: Long): User? {
        require(ValidationUtils.isValidId(userId)) { "Invalid userId: $userId" }
        return userDao.getById(userId)
    }

    suspend fun getFirst(): User? = userDao.getFirst()

    suspend fun insert(user: User): Long {
        require(user.name.isNotBlank()) { "User name cannot be blank" }
        return userDao.insert(user)
    }

    suspend fun update(user: User) {
        require(ValidationUtils.isValidId(user.id)) { "Invalid user id: ${user.id}" }
        require(user.name.isNotBlank()) { "User name cannot be blank" }
        userDao.update(user)
    }

    suspend fun delete(user: User) {
        require(ValidationUtils.isValidId(user.id)) { "Invalid user id: ${user.id}" }
        userDao.delete(user)
    }

    suspend fun getCount(): Int = userDao.getCount()

    suspend fun updateHeight(userId: Long, heightCm: Float?) {
        require(ValidationUtils.isValidId(userId)) { "Invalid userId: $userId" }
        require(ValidationUtils.isValidHeight(heightCm)) { "Invalid heightCm: $heightCm" }
        userDao.updateHeight(userId, heightCm)
    }

    suspend fun updateGender(userId: Long, gender: User.Gender) {
        require(ValidationUtils.isValidId(userId)) { "Invalid userId: $userId" }
        require(ValidationUtils.isValidGender(gender.value)) { "Invalid gender: ${gender.value}" }
        userDao.updateGender(userId, gender.value)
    }

    suspend fun updateDateOfBirth(userId: Long, dateOfBirth: String?) {
        require(ValidationUtils.isValidId(userId)) { "Invalid userId: $userId" }
        if (dateOfBirth != null) {
            require(ValidationUtils.isValidDateString(dateOfBirth)) { "Invalid dateOfBirth: $dateOfBirth" }
        }
        userDao.updateDateOfBirth(userId, dateOfBirth)
    }

    suspend fun getOrCreateDefault(): User {
        val existing = getFirst()
        if (existing != null) {
            return existing
        }

        val defaultUser = User(name = "Default User")
        val id = insert(defaultUser)
        return defaultUser.copy(id = id)
    }
}
