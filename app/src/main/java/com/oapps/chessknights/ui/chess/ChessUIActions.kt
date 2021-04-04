package com.oapps.chessknights.ui.chess

import androidx.compose.runtime.MutableState
import androidx.compose.ui.geometry.Offset
import com.oapps.chessknights.Vec
import com.oapps.chessknights.logic.Move

open class ChessUIActions() {
    open fun onSquareTapped(tappedVec: Vec, piece: Piece? = null) {}
    open fun onPieceDragStart(piece: Piece) {}
    open fun onPieceDragEnd(piece: Piece) {}
    open fun onPieceDrag(piece: Piece, fractionalOffset: Offset) {}
}