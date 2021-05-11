package com.oapps.knightschess.ui.chess

import com.oapps.lib.chess.State

interface Player {
    val color: Boolean
    fun canPickOrDragPieceFromUI(piece: DynamicPiece2, state: State.Capture): Boolean = false
    fun requestNextMove(gameManager: GameManager): Boolean
}