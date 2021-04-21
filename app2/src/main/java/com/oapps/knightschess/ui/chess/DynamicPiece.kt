package com.oapps.knightschess.ui.chess

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import com.oapps.lib.chess.IVec
import com.oapps.lib.chess.Piece

class DynamicPiece(piece: Piece) {
    var offset by mutableStateOf(piece.vec.toOffset())

    val animation = Animatable(offset, Offset.VectorConverter)

    var lastVec = piece.vec
        private set

    var dragOffset by mutableStateOf(offset)

    var dragging by mutableStateOf(false)
        private set

    var vec by mutableStateOf(piece.vec)
        private set

    fun startDrag(){
        dragging = true
        dragOffset = offset
    }

    fun stopDrag(){
        dragging = false
    }

    var playSoundAtEnd by mutableStateOf(false)
        private set

    var animate = false

    fun animateTo(to: IVec, playSound: Boolean = false){
        animate = true
        if(to != vec){
            lastVec = vec
            vec = to
            if(playSound)
                playSoundAtEnd = true
        }
    }

    fun soundPlayed(){
        playSoundAtEnd = false
    }

    override fun toString(): String {
        return "DynamicPiece(offset=$offset, lastVec=$lastVec, dragOffset=$dragOffset, dragging=$dragging, vec=$vec, playSoundAtEnd=$playSoundAtEnd, kind=$kind)"
    }


    var kind by mutableStateOf(piece.kind)
}