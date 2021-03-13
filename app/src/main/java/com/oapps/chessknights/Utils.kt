package com.oapps.chessknights

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationResult
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import kotlin.math.roundToInt

suspend fun Animatable<Float, AnimationVector1D>.animateRoundSnap(): AnimationResult<Float, AnimationVector1D> {
    return animateTo(value.roundToInt().toFloat())
}

suspend fun Animatable<Float, AnimationVector1D>.dragBy(offset: Float){
    snapTo(value + offset)
}

fun Float.transformX(whiteBottom: Boolean, max: Float): Float {
    return if(whiteBottom) this else max - this
}

fun Float.transformY(whiteBottom: Boolean, max: Float): Float {
    return if(whiteBottom) max - this else this
}

fun Offset.transform(whiteBottom: Boolean, max: Offset): Offset {
    return Offset(x.transformX(whiteBottom, max.x), y.transformY(whiteBottom, max.y))
}

fun Offset.transformDirection(whiteBottom: Boolean) = Offset(if(whiteBottom) x else -x, if(whiteBottom) -y else y)

fun Dp.transformX(whiteBottom: Boolean, max: Dp) = if (whiteBottom) this else max - this
fun Dp.transformY(whiteBottom: Boolean, max: Dp) = if (whiteBottom) max - this else this