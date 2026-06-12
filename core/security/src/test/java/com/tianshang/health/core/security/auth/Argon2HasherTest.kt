package com.tianshang.health.core.security.auth

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Argon2HasherTest {

    @Test
    fun hash_returns_consistent_output_for_same_input() {
        val salt = Argon2Hasher.generateSalt()
        val hash1 = Argon2Hasher.hash("password123", salt)
        val hash2 = Argon2Hasher.hash("password123", salt)
        assertArrayEquals(hash1, hash2)
    }

    @Test
    fun hash_returns_different_output_for_different_passwords() {
        val salt = Argon2Hasher.generateSalt()
        val hash1 = Argon2Hasher.hash("password123", salt)
        val hash2 = Argon2Hasher.hash("different456", salt)
        assertFalse(hash1.contentEquals(hash2))
    }

    @Test
    fun hash_returns_different_output_for_different_salts() {
        val salt1 = Argon2Hasher.generateSalt()
        val salt2 = Argon2Hasher.generateSalt()
        val hash1 = Argon2Hasher.hash("password123", salt1)
        val hash2 = Argon2Hasher.hash("password123", salt2)
        assertFalse(hash1.contentEquals(hash2))
    }

    @Test
    fun hash_output_length_is_32_bytes() {
        val salt = Argon2Hasher.generateSalt()
        val hash = Argon2Hasher.hash("test", salt)
        assertEquals(32, hash.size)
    }

    @Test
    fun generateSalt_returns_16_bytes() {
        val salt = Argon2Hasher.generateSalt()
        assertEquals(16, salt.size)
    }

    @Test
    fun generateSalt_returns_unique_values() {
        val salt1 = Argon2Hasher.generateSalt()
        val salt2 = Argon2Hasher.generateSalt()
        assertFalse(salt1.contentEquals(salt2))
    }

    @Test
    fun verify_returns_true_for_correct_password() {
        val salt = Argon2Hasher.generateSalt()
        val hash = Argon2Hasher.hash("password123", salt)
        assertTrue(Argon2Hasher.verify("password123", salt, hash))
    }

    @Test
    fun verify_returns_false_for_wrong_password() {
        val salt = Argon2Hasher.generateSalt()
        val hash = Argon2Hasher.hash("password123", salt)
        assertFalse(Argon2Hasher.verify("wrong", salt, hash))
    }

    @Test
    fun verify_returns_false_for_wrong_salt() {
        val salt = Argon2Hasher.generateSalt()
        val hash = Argon2Hasher.hash("password123", salt)
        val wrongSalt = Argon2Hasher.generateSalt()
        assertFalse(Argon2Hasher.verify("password123", wrongSalt, hash))
    }

    @Test
    fun hashPassword_returns_valid_salt_and_hash_pair() {
        val (salt, hash) = Argon2Hasher.hashPassword("mypassword")
        assertNotNull(salt)
        assertNotNull(hash)
        assertEquals(16, salt.size)
        assertEquals(32, hash.size)
        assertTrue(Argon2Hasher.verify("mypassword", salt, hash))
    }

    @Test
    fun hashPassword_returns_unique_pairs() {
        val (salt1, hash1) = Argon2Hasher.hashPassword("same")
        val (salt2, hash2) = Argon2Hasher.hashPassword("same")
        assertFalse(salt1.contentEquals(salt2))
        assertFalse(hash1.contentEquals(hash2))
    }

    @Test
    fun hash_handles_empty_password() {
        val salt = Argon2Hasher.generateSalt()
        val hash = Argon2Hasher.hash("", salt)
        assertNotNull(hash)
        assertEquals(32, hash.size)
        assertTrue(Argon2Hasher.verify("", salt, hash))
    }

    @Test
    fun hash_handles_unicode_password() {
        val salt = Argon2Hasher.generateSalt()
        val hash = Argon2Hasher.hash("密码🔐", salt)
        assertNotNull(hash)
        assertEquals(32, hash.size)
        assertTrue(Argon2Hasher.verify("密码🔐", salt, hash))
    }

    @Test
    fun hash_handles_long_password() {
        val longPassword = "a".repeat(10000)
        val salt = Argon2Hasher.generateSalt()
        val hash = Argon2Hasher.hash(longPassword, salt)
        assertNotNull(hash)
        assertEquals(32, hash.size)
        assertTrue(Argon2Hasher.verify(longPassword, salt, hash))
    }
}
