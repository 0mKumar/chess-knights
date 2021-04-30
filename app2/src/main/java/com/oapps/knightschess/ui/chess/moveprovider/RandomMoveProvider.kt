package com.oapps.knightschess.ui.chess.moveprovider

import com.oapps.lib.chess.Chess
import com.oapps.lib.chess.State

class RandomMoveProvider: BaseMoveProvider() {
    override fun requestNextMove(chess: Chess, state: State.Capture): Boolean {
        return false
    }
}