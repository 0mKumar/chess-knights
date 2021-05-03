package com.oapps.knightschess.ui.chess.moveprovider

import com.oapps.lib.chess.Chess
import com.oapps.lib.chess.Move
import com.oapps.lib.chess.State

class PredefinedMoveProvider(private val moves: List<String>): BaseMoveProvider() {
    var index = 0
    override fun requestNextMove(chess: Chess, state: State.Capture): Boolean {
        val move = Move(chess, moves[index++])
        onMoveReady?.invoke(move, state)
        return true
    }
}