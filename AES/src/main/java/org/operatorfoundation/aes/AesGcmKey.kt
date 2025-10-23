package org.operatorfoundation.aes

import java.security.SecureRandom

data class AesGcmKey(val bytes: ByteArray)
{
    init
    {
        require(bytes.size == 32)
        {
            "AES-GCM key must be exactly 32 bytes, but we got ${bytes.size}"
        }
    }

    // Override equals and hashCode since ByteArray uses reference equality by default
    override fun equals(other: Any?): Boolean
    {
        if (this == other) return true
        if (other !is AesGcmKey) return false
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int
    {
        return bytes.contentHashCode()
    }

    companion object
    {
        /**
         * Generates a new random 256-bit AES-GCM key using a cryptographically secure random number generator.
         *
         * @return A new AesGcmKey with cryptographically random bytes
         */
        fun generate(): AesGcmKey
        {
            val keyBytes = ByteArray(32)
            SecureRandom().nextBytes(keyBytes)
            return AesGcmKey(keyBytes)
        }
    }
}