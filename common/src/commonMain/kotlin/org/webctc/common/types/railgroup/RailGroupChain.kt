package org.webctc.common.types.railgroup

import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
data class RailGroupChain(
    val chain: LinkedHashSet<UUID> = LinkedHashSet(),
    val key: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RailGroupChain) return false

        if (chain != other.chain) return false
        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chain.hashCode()
        result = 31 * result + key.hashCode()
        return result
    }
}
