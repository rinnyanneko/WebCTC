package org.webctc.common.types.mc

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import kotlinx.uuid.UUID

@Serializable
data class PlayerProfile(
    @JsonNames("uuid_formatted")
    val uuid: UUID,
    val username: String
)
