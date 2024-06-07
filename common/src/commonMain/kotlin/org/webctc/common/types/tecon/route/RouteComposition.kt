package org.webctc.common.types.tecon.route

import kotlinx.serialization.Serializable
import org.webctc.common.types.PosInt
import org.webctc.common.types.railgroup.RailGroupChain


interface IRouteComposition {}

@Serializable
data class RouteCompositionRedStone(
    val redStonePosSet: Set<PosInt> = setOf()
) : IRouteComposition

@Serializable
data class RouteCompositionLock(
    val chain: RailGroupChain = RailGroupChain()
) : IRouteComposition