package com.oapps.knightschess.ui.chess

import com.oapps.lib.chess.State

interface Player {
    fun canPickPiece(piece: DynamicPiece2, state: State.Capture): Boolean
    fun requestNextMove(gameManager: GameManager): Boolean
}