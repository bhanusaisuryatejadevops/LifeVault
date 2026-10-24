package com.example

import com.example.backup.BackupManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupManagerTest {

    @Test
    fun testEncryptionDecryptionRoundtrip() {
        val originalJson = """{"app":"LifeVault","version":1,"items":[{"title":"MacBook","price":2499.0}]}"""
        val password = "MySecretMasterPassword!99"

        val encryptedBase64 = BackupManager.encrypt(originalJson, password)
        assertNotNullOrEmpty(encryptedBase64)
        assertNotEquals(originalJson, encryptedBase64)
        assertTrue(BackupManager.isEncryptedPayload(encryptedBase64))

        val decrypted = BackupManager.decrypt(encryptedBase64, password)
        assertEquals(originalJson, decrypted)
    }

    @Test
    fun testWrongPasswordFails() {
        val originalJson = """{"secret":"data"}"""
        val correctPassword = "CorrectPassword123"
        val wrongPassword = "WrongPassword999"

        val encrypted = BackupManager.encrypt(originalJson, correctPassword)

        assertThrows(Exception::class.java) {
            BackupManager.decrypt(encrypted, wrongPassword)
        }
    }

    @Test
    fun testIsEncryptedPayloadDetector() {
        assertTrue(BackupManager.isEncryptedPayload("SGVsbG8gV29ybGQgQmFzZTY0IEVuY29kZWQ="))
        assertFalse(BackupManager.isEncryptedPayload("""{"title":"Not Encrypted"}"""))
        assertFalse(BackupManager.isEncryptedPayload("""[{"item":1}]"""))
    }

    private fun assertNotNullOrEmpty(str: String?) {
        assertTrue(!str.isNullOrBlank())
    }
}
