package com.example.backup

import android.os.Build
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object BackupManager {

    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16
    private const val IV_LENGTH = 12
    private const val TAG_LENGTH_BIT = 128

    /**
     * Encrypts the raw JSON string using AES-256-GCM derived with PBKDF2.
     * Returns a Base64 string containing: [16 bytes salt] + [12 bytes IV] + [ciphertext + tag]
     */
    fun encrypt(rawJson: String, password: String): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)

        val iv = ByteArray(IV_LENGTH)
        random.nextBytes(iv)

        val secretKey = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

        val cipherText = cipher.doFinal(rawJson.toByteArray(StandardCharsets.UTF_8))

        val combined = ByteArray(SALT_LENGTH + IV_LENGTH + cipherText.size)
        System.arraycopy(salt, 0, combined, 0, SALT_LENGTH)
        System.arraycopy(iv, 0, combined, SALT_LENGTH, IV_LENGTH)
        System.arraycopy(cipherText, 0, combined, SALT_LENGTH + IV_LENGTH, cipherText.size)

        return encodeBase64(combined)
    }

    /**
     * Decrypts the Base64 encoded payload with the given password.
     */
    fun decrypt(encryptedBase64: String, password: String): String {
        val combined = decodeBase64(encryptedBase64)
        if (combined.size < SALT_LENGTH + IV_LENGTH) {
            throw IllegalArgumentException("Invalid encrypted backup data format.")
        }

        val salt = ByteArray(SALT_LENGTH)
        System.arraycopy(combined, 0, salt, 0, SALT_LENGTH)

        val iv = ByteArray(IV_LENGTH)
        System.arraycopy(combined, SALT_LENGTH, iv, 0, IV_LENGTH)

        val cipherTextSize = combined.size - SALT_LENGTH - IV_LENGTH
        val cipherText = ByteArray(cipherTextSize)
        System.arraycopy(combined, SALT_LENGTH + IV_LENGTH, cipherText, 0, cipherTextSize)

        val secretKey = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        val plainBytes = cipher.doFinal(cipherText)
        return String(plainBytes, StandardCharsets.UTF_8)
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    fun isEncryptedPayload(text: String): Boolean {
        val trimmed = text.trim()
        return !trimmed.startsWith("{") && !trimmed.startsWith("[")
    }

    private fun encodeBase64(bytes: ByteArray): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            java.util.Base64.getEncoder().encodeToString(bytes)
        } else {
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        }
    }

    private fun decodeBase64(text: String): ByteArray {
        val cleaned = text.trim()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            java.util.Base64.getDecoder().decode(cleaned)
        } else {
            android.util.Base64.decode(cleaned, android.util.Base64.DEFAULT)
        }
    }
}
