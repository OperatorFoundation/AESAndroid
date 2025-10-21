package org.operatorfoundation.aes

/**
 * AES-GCM ciphertext
 *
 * @property nonce The initialization vector
 * @property encrypted The encrypted data bytes
 * @property tag The authentication tag (must be exactly 16 bytes for GCM)
 * @throws IllegalArgumentException if tag is not exactly 16 bytes
 */
data class Ciphertext(val nonce: Nonce, val encrypted: ByteArray, val tag: ByteArray)
{
    init
    {
        require(tag.size == 16)
        {
            "GCM authentication tag must be exactly 16 bytes, got ${tag.size}"
        }
    }

    override fun equals(other: Any?): Boolean
    {
        if (this == other) return true
        if (other !is Ciphertext) return false

        if (nonce != other.nonce) return false
        if (!encrypted.contentEquals(other.encrypted)) return false
        if (!tag.contentEquals(other.tag)) return false

        return true
    }

    override fun hashCode(): Int
    {
        // Standard hashCode combination algorithm for multi-field objects
        // We start with the first field's hash and combine subsequent fields
        // using a prime multiplier to reduce hash collisions
        var result = nonce.hashCode()

        // Prime number 31 is used because:
        // 1. It's odd (even numbers would lose information with overflow)
        // 2. It's prime (better distribution, fewer collisions)
        // 3. It's small enough to avoid frequent overflow
        // 4. JVM can optimize 31 * i as (i << 5) - i
        val primeMultiplier = 31

        result = primeMultiplier * result + encrypted.contentHashCode()
        result = primeMultiplier * result + tag.contentHashCode()

        return result
    }
}
