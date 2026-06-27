package com.tianshang.health.core.security.auth

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import java.security.MessageDigest

object Argon2Hasher {

    private const val SALT_LENGTH = 16
    private const val HASH_LENGTH = 32
    private const val ITERATIONS = 3
    private const val MEMORY = 65536 // 64 MB
    private const val PARALLELISM = 4

    fun hash(password: String, salt: ByteArray): ByteArray {
        val generator = Argon2BytesGenerator()

        val params = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withIterations(ITERATIONS)
            .withMemoryAsKB(MEMORY)
            .withParallelism(PARALLELISM)
            .withSalt(salt)
            .build()

        val hash = ByteArray(HASH_LENGTH)
        val passwordBytes = password.toByteArray()
        try {
            generator.init(params)
            generator.generateBytes(passwordBytes, hash)
        } finally {
            passwordBytes.fill(0)
        }

        return hash
    }

    fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_LENGTH)
        java.security.SecureRandom().nextBytes(salt)
        return salt
    }

    fun verify(password: String, salt: ByteArray, expectedHash: ByteArray): Boolean {
        val actualHash = hash(password, salt)
        return constantTimeEquals(actualHash, expectedHash)
    }

    fun hashPassword(password: String): Pair<ByteArray, ByteArray> {
        val salt = generateSalt()
        val hash = hash(password, salt)
        return Pair(salt, hash)
    }

    /**
     * Constant-time comparison to prevent timing attacks.
     * Uses MessageDigest.isEqual internally which is constant-time in modern JDK.
     */
    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        return MessageDigest.isEqual(a, b)
    }
}
