package org.webctc.common.types.tecon.shape

import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID
import org.webctc.common.types.PosInt2D

@Serializable
data class RailLine(
    override val pos: PosInt2D = PosInt2D.ZERO,
    val start: PosInt2D = PosInt2D.ZERO,
    val end: PosInt2D,
    override val railGroupList: Set<UUID> = setOf(),
    override val zIndex: Int = 0
) : RailShape