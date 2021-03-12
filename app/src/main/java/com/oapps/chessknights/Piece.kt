package com.oapps.chessknights

import androidx.annotation.Px
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class Piece(
    vec: Vec = Vec(),
    kind: Char = 'p'
) {
    val name: String by lazy {
        pieceName[kind.toUpperCase()]?:"Unknown"
    }

    var selected by mutableStateOf(false)

    var vec by mutableStateOf(vec, structuralEqualityPolicy())
    var kind by mutableStateOf(kind)

    fun dragBy(coroutineScope: CoroutineScope, offset: Offset){
        coroutineScope.launch {
            async { offsetFractionX.dragBy(offset.x) }
            async { offsetFractionY.dragBy(offset.y) }
        }
    }

    val offsetFractionX = Animatable(vec.x.toFloat())
    val offsetFractionY = Animatable(vec.y.toFloat())

    companion object {
        val drawableImageResources = mapOf(
            'R' to R.drawable.wr,
            'N' to R.drawable.wn,
            'B' to R.drawable.wb,
            'Q' to R.drawable.wq,
            'K' to R.drawable.wk,
            'P' to R.drawable.wp,
            'r' to R.drawable.br,
            'n' to R.drawable.bn,
            'b' to R.drawable.bb,
            'q' to R.drawable.bq,
            'k' to R.drawable.bk,
            'p' to R.drawable.bp,
        )
        val pieceName = mapOf(
            'R' to "Rook",
            'N' to "Knight",
            'B' to "Bishop",
            'Q' to "Queen",
            'K' to "King",
            'P' to "Pawn"
        )
    }

    val image: Int
        get() = drawableImageResources[kind] ?: R.drawable.bn

    constructor(notation: String) : this(Vec(notation.substring(1)), notation[0])

    fun isWhite() = kind.isUpperCase()
    fun isBlack() = kind.isLowerCase()

    fun contains(chessOffset: Offset, sizePx: Float): Boolean {
        return Rect(Offset(offsetFractionX.value * sizePx, offsetFractionY.value * sizePx), sizePx).contains(chessOffset)
    }

    fun snap(coroutineScope: CoroutineScope, onComplete: CoroutineScope.() -> Unit) {
        coroutineScope.launch {
            async {
                val x = async { offsetFractionX.animateRoundSnap() }
                val y = async { offsetFractionY.animateRoundSnap() }
                awaitAll(x, y).let {
                    vec = Vec(
                        offsetFractionX.value.roundToInt().coerceIn(0..7),
                        offsetFractionY.value.roundToInt().coerceIn(0..7),
                    )
                }
                onComplete()
            }
        }
    }

    fun moveTo(coroutineScope: CoroutineScope, to: Vec, onComplete: CoroutineScope.() -> Unit) {
        coroutineScope.launch {
            async {
                val x = async { offsetFractionX.animateTo(to.x.toFloat()) }
                val y = async { offsetFractionY.animateTo(to.y.toFloat()) }
                awaitAll(x, y).let {
                    vec = Vec(
                        offsetFractionX.value.roundToInt().coerceIn(0..7),
                        offsetFractionY.value.roundToInt().coerceIn(0..7),
                    )
                }
                onComplete()
            }
        }
    }
}