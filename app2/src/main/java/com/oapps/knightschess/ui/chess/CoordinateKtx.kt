package com.oapps.knightschess.ui.chess

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import com.oapps.lib.chess.IVec

fun Float.transformX(whiteBottom: Boolean, max: Float): Float {
    return if(whiteBottom) this else max - this
}

fun Float.transformY(whiteBottom: Boolean, max: Float): Float {
    return if(whiteBottom) max - this else this
}

fun Offset.transform(whiteBottom: Boolean, max: Offset): Offset {
    return Offset(x.transformX(whiteBottom, max.x), y.transformY(whiteBottom, max.y))
}

fun Offset.transformDirection(whiteBottom: Boolean): Offset {
    return Offset(if(whiteBottom) x else -x, if(whiteBottom) -y else y)
}

fun Dp.transformX(whiteBottom: Boolean, max: Dp) = if (whiteBottom) this else max - this
fun Dp.transformY(whiteBottom: Boolean, max: Dp) = if (whiteBottom) max - this else this

fun Int.transformX(whiteBottom: Boolean, max: Int) = if(whiteBottom) this else max - this
fun Int.transformY(whiteBottom: Boolean, max: Int) = if(whiteBottom) max - this else this

fun IVec.toOffset() = Offset(x.toFloat(), y.toFloat())