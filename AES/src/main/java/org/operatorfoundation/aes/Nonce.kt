package org.operatorfoundation.aes

data class Nonce(val bytes: ByteArray)
{
    init
    {
        require(bytes.size == 12)
        {
            "Nonce must be exactly 12 bytes, be we got ${bytes.size}"
        }
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (other !is Nonce) return false

        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int
    {
        return bytes.contentHashCode()
    }
}
