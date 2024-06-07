package org.webctc.common.types.tecon.shape

import org.webctc.common.types.PosInt2D


interface IShape {
    val pos: PosInt2D
    val zIndex: Int
}