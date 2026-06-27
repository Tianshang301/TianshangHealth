package com.tianshang.health.feature.period.backup

import androidx.room.withTransaction
import com.google.gson.Gson
import com.tianshang.health.core.database.TianshangDatabase
import com.tianshang.health.core.database.dao.DailyHealthDao
import com.tianshang.health.core.database.dao.DailySymptomDao
import com.tianshang.health.core.database.dao.PeriodRecordDao
import com.tianshang.health.core.database.dao.StepsDao
import com.tianshang.health.core.database.dao.UserDao
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedBackupManager @Inject constructor(
    private val database: TianshangDatabase,
    private val userDao: UserDao,
    private val periodRecordDao: PeriodRecordDao,
    private val dailySymptomDao: DailySymptomDao,
    private val dailyHealthDao: DailyHealthDao,
    private val dailyStepsDao: StepsDao
) {
    private val gson = Gson()
    private val random = SecureRandom()

    companion object {
        private const val ALGORITHM = "AES/GCM/NoPadding"
        private const val TAG_LENGTH_BIT = 128
        private const val IV_LENGTH_BYTE = 12
        private const val SALT_LENGTH_BYTE = 16
        private const val KEY_LENGTH = 256
        private const val ITERATIONS = 100000
        private const val BACKUP_MAGIC = "TSHBACKUP"
        private const val MAX_BACKUP_SIZE_BYTES = 50 * 1024 * 1024 // 50 MB
        private const val MAX_STRING_FIELD_LENGTH = 10000
        private const val CURRENT_BACKUP_VERSION = 1
    }

    suspend fun exportBackup(password: String, outputStream: OutputStream) {
        val user = userDao.getFirst() ?: throw IllegalStateException("No user found")

        val data = BackupData(
            users = listOfNotNull(userDao.getFirst()),
            periodRecords = periodRecordDao.getByUserIdList(user.id),
            dailySymptoms = dailySymptomDao.getByDateRange(user.id, "1970-01-01", "9999-12-31"),
            dailyHealth = dailyHealthDao.getByDateRange(user.id, "1970-01-01", "9999-12-31"),
            dailySteps = dailyStepsDao.getByDateRange(user.id, "1970-01-01", "9999-12-31")
        )

        val json = gson.toJson(data)
        val jsonBytes = json.toByteArray(Charsets.UTF_8)

        val salt = ByteArray(SALT_LENGTH_BYTE).apply { random.nextBytes(this) }
        val iv = ByteArray(IV_LENGTH_BYTE).apply { random.nextBytes(this) }

        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BIT, iv))

        val ciphertext = cipher.doFinal(jsonBytes)

        outputStream.use { out ->
            out.write(BACKUP_MAGIC.toByteArray(Charsets.UTF_8))
            out.write(salt)
            out.write(iv)
            out.write(ciphertext)
            out.flush()
        }
    }

    suspend fun importBackup(password: String, inputStream: InputStream): Int {
        val (salt, iv, ciphertext) = inputStream.use { stream ->
            val magic = ByteArray(BACKUP_MAGIC.toByteArray(Charsets.UTF_8).size)
            stream.read(magic)
            val magicStr = String(magic, Charsets.UTF_8)
            if (magicStr != BACKUP_MAGIC) throw IllegalArgumentException("Invalid backup file")

            val salt = ByteArray(SALT_LENGTH_BYTE)
            stream.read(salt)
            val iv = ByteArray(IV_LENGTH_BYTE)
            stream.read(iv)
            val ciphertext = stream.readBytes()
            if (ciphertext.size > MAX_BACKUP_SIZE_BYTES) {
                throw IllegalArgumentException("Backup file too large")
            }
            Triple(salt, iv, ciphertext)
        }

        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BIT, iv))

        val jsonBytes = cipher.doFinal(ciphertext)
        val json = String(jsonBytes, Charsets.UTF_8)

        val data = gson.fromJson(json, BackupData::class.java)
        validateBackupData(data)

        return database.withTransaction {
            var count = 0
            val existingUser = userDao.getFirst()
            val targetUserId = existingUser?.id ?: 0L

            for (record in data.periodRecords) {
                val recordWithUserId = record.copy(userId = targetUserId, id = 0)
                periodRecordDao.insert(recordWithUserId)
                count++
            }

            for (symptom in data.dailySymptoms) {
                val symptomWithUserId = symptom.copy(userId = targetUserId, id = 0)
                dailySymptomDao.insert(symptomWithUserId)
                count++
            }

            for (health in data.dailyHealth) {
                val healthWithUserId = health.copy(userId = targetUserId, id = 0)
                dailyHealthDao.insert(healthWithUserId)
                count++
            }

            for (steps in data.dailySteps) {
                val stepsWithUserId = steps.copy(userId = targetUserId, id = 0)
                dailyStepsDao.insert(stepsWithUserId)
                count++
            }

            count
        }
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec)
    }

    private fun validateBackupData(data: BackupData) {
        if (data.version > CURRENT_BACKUP_VERSION) {
            throw IllegalArgumentException("Unsupported backup version: ${data.version}")
        }
        for (record in data.periodRecords) {
            require(record.flowLevel in 0..5) { "Invalid flowLevel: ${record.flowLevel}" }
            require(record.painLevel in 0..10) { "Invalid painLevel: ${record.painLevel}" }
            require(record.startDate.length <= 10) { "Invalid date length" }
            record.endDate?.let { require(it.length <= 10) { "Invalid date length" } }
            record.notes?.let { require(it.length <= MAX_STRING_FIELD_LENGTH) { "Notes too long" } }
        }
        for (health in data.dailyHealth) {
            health.sleepHours?.let { require(it in 0f..24f) { "Invalid sleepHours: $it" } }
            health.weightKg?.let { require(it in 1f..500f) { "Invalid weight" } }
            health.heartRateResting?.let { require(it in 20..300) { "Invalid heartRate" } }
            health.moodScore?.let { require(it in 1..5) { "Invalid moodScore" } }
            health.stressLevel?.let { require(it in 1..5) { "Invalid stressLevel" } }
            health.waterIntake?.let { require(it in 0f..10000f) { "Invalid waterIntake" } }
            health.caloriesIntake?.let { require(it in 0f..10000f) { "Invalid caloriesIntake" } }
        }
        for (symptom in data.dailySymptoms) {
            symptom.notes?.let { require(it.length <= MAX_STRING_FIELD_LENGTH) { "Symptom notes too long" } }
        }
    }
}
