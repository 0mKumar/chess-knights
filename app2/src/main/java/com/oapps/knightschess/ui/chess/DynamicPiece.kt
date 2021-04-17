package com.oapps.knightschess.ui.chess

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import com.oapps.lib.chess.Piece

class DynamicPiece(piece: Piece) {
    var offset: State<Offset> = mutableStateOf(Offset(piece.vec.x.toFloat(), piece.vec.y.toFloat()))
//    var dragOffset by mutableStateOf(Offset.Zero)
//    var dragging by mutableStateOf(false)
    var vec by mutableStateOf(piece.vec)
    var kind by mutableStateOf(piece.kind)
}