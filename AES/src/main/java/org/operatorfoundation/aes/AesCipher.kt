package org.operatorfoundation.aes

import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

class AesCipher
{
    /**
     * Encrypts plaintext using AES-GCM with the provided key.
     *
     * @param key The 256-bit AES-GCM encryption key
     * @param plaintext The data to encrypt
     * @return Ciphertext containing the nonce, encrypted data, and authentication tag
     */
    fun encrypt(key: AesGcmKey, plaintext: ByteArray): Ciphertext
    {
        val nonce = ByteArray(12).apply { SecureRandom().nextBytes(this) }

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val keySpec = SecretKeySpec(key.bytes, "AES")
        val gcmSpec = GCMParameterSpec(128, nonce) // 128-bit auth tag

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)
        val encrypted = cipher.doFinal(plaintext)

        // GCM appends the tag to the ciphertext
        val ciphertextBytes = encrypted.dropLast(16).toByteArray()
        val tag = encrypted.takeLast(16).toByteArray()

        return Ciphertext(
            nonce = Nonce(nonce),
            encrypted = ciphertextBytes,
            tag = tag
        )
    }

    /**
     * Decrypts ciphertext using AES-GCM with the provided key.
     *
     * @param key The 256-bit AES-GCM decryption key
     * @param ciphertext The ciphertext to decrypt (contains nonce, encrypted data, and tag)
     * @return The decrypted plaintext bytes
     * @throws SecurityException if authentication tag verification fails
     */
    fun decrypt(key: AesGcmKey, ciphertext: Ciphertext): ByteArray
    {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val keySpec = SecretKeySpec(key.bytes, "AES")
        val gcmSpec = GCMParameterSpec(128, ciphertext.nonce.bytes)

        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)

        // Combine encrypted data and tag for Java's Cipher API
        val combined = ciphertext.encrypted + ciphertext.tag

        return try {
            cipher.doFinal(combined)
        } catch (e: Exception) {
            throw SecurityException("Authentication tag verification failed", e)
        }
    }
}