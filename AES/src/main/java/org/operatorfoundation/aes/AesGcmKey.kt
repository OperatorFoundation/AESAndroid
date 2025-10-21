package org.operatorfoundation.aes

data class AesGcmKey(val bytes: ByteArray)
{
    init
    {
        require(bytes.size == 32)
        {
            "AES-GCM key must be exactly 32 bytes, be we got ${bytes.size}"
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
}
