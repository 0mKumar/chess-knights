package com.oapps.chessknights

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationResult
import androidx.compose.animation.core.AnimationVector1D
import kotlin.math.roundToInt

suspend fun Animatable<Float, AnimationVector1D>.animateRoundSnap(): AnimationResult<Float, AnimationVector1D> {
    return animateTo(value.roundToInt().toFloat())
}

suspend fun Animatable<Float, AnimationVector1D>.dragBy(offset: Float){
    snapTo(value + offset)
}
