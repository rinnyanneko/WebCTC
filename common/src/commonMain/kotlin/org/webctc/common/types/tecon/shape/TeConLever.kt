package org.webctc.common.types.tecon.shape

import kotlinx.serialization.Serializable
import org.webctc.common.types.PosInt2D
import org.webctc.common.types.tecon.route.IRouteComposition

@Serializable
data class TeConLever(
    override val pos: PosInt2D = PosInt2D.ZERO,
    val name: String = "",
    val rotation: Int = 0,
    val compositions: Set<IRouteComposition> = emptySet(),
    override val zIndex: Int = 0
) : IShape