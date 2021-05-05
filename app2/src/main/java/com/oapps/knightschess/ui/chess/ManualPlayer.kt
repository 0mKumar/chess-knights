package com.oapps.knightschess.ui.chess

import com.oapps.lib.chess.State

class ManualPlayer(val color: Boolean): Player {

    override fun canPickPiece(piece: DynamicPiece2, state: State.Capture): Boolean {
        if(color == state.activeColor) return true
        return false
    }

    override fun requestNextMove(gameManager: GameManager) = true
}