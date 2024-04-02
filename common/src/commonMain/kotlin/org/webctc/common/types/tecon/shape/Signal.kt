package org.webctc.common.types.tecon.shape

import kotlinx.serialization.Serializable
import org.webctc.common.types.PosInt
import org.webctc.common.types.PosInt2D

@Serializable
data class Signal(
    override val pos: PosInt2D = PosInt2D.ZERO,
    val signalPos: PosInt = PosInt.ZERO,
    val rotation: Int? = null,
    override val zIndex: Int = 0
) : IShape