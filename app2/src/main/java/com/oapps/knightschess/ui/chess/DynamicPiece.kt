package com.oapps.knightschess.ui.chess

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import com.oapps.lib.chess.IVec
import com.oapps.lib.chess.Piece

class DynamicPiece(piece: Piece) {
    var offset by mutableStateOf(piece.vec.toOffset())

    var lastVec = piece.vec
        private set

    var dragOffset by mutableStateOf(offset)

    var dragging by mutableStateOf(false)
        private set

    val drawOffset get() = offset

    var vec by mutableStateOf(piece.vec)
        private set

    fun startDrag(){
        dragging = true
        dragOffset = offset
    }

    fun stopDrag(){
        dragging = false
    }

    fun animateTo(to: IVec){
        lastVec = vec
        vec = to
    }

    override fun toString(): String {
        return "DynamicPiece(vec=$vec, kind=$kind)"
    }

    var kind by mutableStateOf(piece.kind)
}