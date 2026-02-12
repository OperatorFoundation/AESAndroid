package org.operatorfoundation.aes

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.junit.Test
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class AesCipherInstrumentedTest {
    private val cipher = AesCipher()

    @Test
    fun secureRandomProducesQualityRandomnessOnDevice() {
        val key1 = AesGcmKey.generate()
        val key2 = AesGcmKey.generate()
        val key3 = AesGcmKey.generate()

        assertFalse(key1.bytes.contentEquals(key2.bytes))
        assertFalse(key2.bytes.contentEquals(key3.bytes))
        assertFalse(key1.bytes.contentEquals(key3.bytes))

        assertFalse(key1.bytes.all { it == 0.toByte() })
        assertFalse(key2.bytes.all { it == 0.toByte() })
    }

    @Test
    fun encryptionPerformanceIsAcceptableOnDevice() {
        val key = AesGcmKey.generate()
        val plaintext = ByteArray(10 * 1024) { it.toByte() }

        val startTime = System.nanoTime()
        repeat(100) {
            cipher.encrypt(key, plaintext)
        }
        val endTime = System.nanoTime()

        val avgTimeMs = (endTime - startTime) / 1_000_000.0 / 100
        println("Average encryption time for 10KB: ${avgTimeMs}ms")

        assertTrue(avgTimeMs < 100.0)
    }
}