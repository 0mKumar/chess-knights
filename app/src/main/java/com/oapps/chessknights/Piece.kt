package com.oapps.chessknights

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy

class Piece(
    vec: Vec = Vec(),
    kind: Char = 'p'
) {
    val name: String by lazy {
        pieceName[kind.toUpperCase()]?:"Unknown"
    }

    var vec by mutableStateOf(vec, structuralEqualityPolicy())
    var kind by mutableStateOf(kind)

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
}