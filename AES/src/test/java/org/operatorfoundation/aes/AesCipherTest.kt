package org.operatorfoundation.aes

import org.junit.Test
import org.junit.Assert.*
import kotlin.test.assertFailsWith

class AesCipherTest {
    private val cipher = AesCipher()

    @Test
    fun `encrypt produces non-empty ciphertext`() {
        val key = AesGcmKey.generate()
        val plaintext = "Hello, World!".toByteArray()

        val ciphertext = cipher.encrypt(key, plaintext)

        assertTrue(ciphertext.encrypted.isNotEmpty())
        assertEquals(plaintext.size, ciphertext.encrypted.size)
    }

    @Test
    fun `encrypt produces 12-byte nonce`() {
        val key = AesGcmKey.generate()
        val plaintext = "test".toByteArray()

        val ciphertext = cipher.encrypt(key, plaintext)

        assertEquals(12, ciphertext.nonce.bytes.size)
    }

    @Test
    fun `encrypt produces 16-byte authentication tag`() {
        val key = AesGcmKey.generate()
        val plaintext = "test".toByteArray()

        val ciphertext = cipher.encrypt(key, plaintext)

        assertEquals(16, ciphertext.tag.size)
    }

    @Test
    fun `encrypt and decrypt roundtrip preserves plaintext`() {
        val key = AesGcmKey.generate()
        val plaintext = "The quick brown fox jumps over the lazy dog".toByteArray()

        val ciphertext = cipher.encrypt(key, plaintext)
        val decrypted = cipher.decrypt(key, ciphertext)

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun `encrypt produces different nonces for same plaintext`() {
        val key = AesGcmKey.generate()
        val plaintext = "test".toByteArray()

        val ciphertext1 = cipher.encrypt(key, plaintext)
        val ciphertext2 = cipher.encrypt(key, plaintext)

        assertFalse(ciphertext1.nonce.bytes.contentEquals(ciphertext2.nonce.bytes))
    }

    @Test
    fun `encrypt produces different ciphertexts for same plaintext`() {
        val key = AesGcmKey.generate()
        val plaintext = "test".toByteArray()

        val ciphertext1 = cipher.encrypt(key, plaintext)
        val ciphertext2 = cipher.encrypt(key, plaintext)

        assertFalse(ciphertext1.encrypted.contentEquals(ciphertext2.encrypted))
    }

    @Test
    fun `decrypt with wrong key throws SecurityException`() {
        val correctKey = AesGcmKey.generate()
        val wrongKey = AesGcmKey.generate()
        val plaintext = "secret message".toByteArray()

        val ciphertext = cipher.encrypt(correctKey, plaintext)

        assertFailsWith<SecurityException> {
            cipher.decrypt(wrongKey, ciphertext)
        }
    }

    @Test
    fun `decrypt with tampered ciphertext throws SecurityException`() {
        val key = AesGcmKey.generate()
        val plaintext = "secret message".toByteArray()

        val ciphertext = cipher.encrypt(key, plaintext)

        // Tamper with the encrypted data
        val tampered = ciphertext.encrypted.clone()
        tampered[0] = (tampered[0] + 1).toByte()
        val tamperedCiphertext = Ciphertext(ciphertext.nonce, tampered, ciphertext.tag)

        assertFailsWith<SecurityException> {
            cipher.decrypt(key, tamperedCiphertext)
        }
    }

    @Test
    fun `decrypt with tampered tag throws SecurityException`() {
        val key = AesGcmKey.generate()
        val plaintext = "secret message".toByteArray()

        val ciphertext = cipher.encrypt(key, plaintext)

        // Tamper with the authentication tag
        val tampered = ciphertext.tag.clone()
        tampered[0] = (tampered[0] + 1).toByte()
        val tamperedCiphertext = Ciphertext(ciphertext.nonce, ciphertext.encrypted, tampered)

        assertFailsWith<SecurityException> {
            cipher.decrypt(key, tamperedCiphertext)
        }
    }

    @Test
    fun `decrypt with tampered nonce throws SecurityException`() {
        val key = AesGcmKey.generate()
        val plaintext = "secret message".toByteArray()

        val ciphertext = cipher.encrypt(key, plaintext)

        // Tamper with the nonce
        val tamperedNonceBytes = ciphertext.nonce.bytes.clone()
        tamperedNonceBytes[0] = (tamperedNonceBytes[0] + 1).toByte()
        val tamperedCiphertext = Ciphertext(
            Nonce(tamperedNonceBytes),
            ciphertext.encrypted,
            ciphertext.tag
        )

        assertFailsWith<SecurityException> {
            cipher.decrypt(key, tamperedCiphertext)
        }
    }

    @Test
    fun `encrypt handles empty plaintext`() {
        val key = AesGcmKey.generate()
        val plaintext = ByteArray(0)

        val ciphertext = cipher.encrypt(key, plaintext)
        val decrypted = cipher.decrypt(key, ciphertext)

        assertArrayEquals(plaintext, decrypted)
        assertEquals(0, decrypted.size)
    }

    @Test
    fun `encrypt handles large plaintext`() {
        val key = AesGcmKey.generate()
        val plaintext = ByteArray(1024 * 1024) { it.toByte() } // 1MB

        val ciphertext = cipher.encrypt(key, plaintext)
        val decrypted = cipher.decrypt(key, ciphertext)

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun `different keys produce different ciphertexts for same plaintext and nonce`() {
        val key1 = AesGcmKey.generate()
        val key2 = AesGcmKey.generate()
        val plaintext = "test".toByteArray()

        val ciphertext1 = cipher.encrypt(key1, plaintext)
        val ciphertext2 = cipher.encrypt(key2, plaintext)

        assertFalse(ciphertext1.encrypted.contentEquals(ciphertext2.encrypted))
    }

    @Test
    fun `encrypt handles binary data`() {
        val key = AesGcmKey.generate()
        val plaintext = byteArrayOf(0x00, 0xFF.toByte(), 0x01, 0xFE.toByte(), 0x7F)

        val ciphertext = cipher.encrypt(key, plaintext)
        val decrypted = cipher.decrypt(key, ciphertext)

        assertArrayEquals(plaintext, decrypted)
    }
}