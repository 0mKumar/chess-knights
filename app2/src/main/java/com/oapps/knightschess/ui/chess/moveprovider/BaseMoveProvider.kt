package com.oapps.knightschess.ui.chess.moveprovider

import com.oapps.lib.chess.Chess
import com.oapps.lib.chess.Move
import com.oapps.lib.chess.State

abstract class BaseMoveProvider {
    interface MoveReady{
        fun onMoveReady(move: Move, state: State.Capture)
    }
    var onMoveReady: MoveReady? = null
    abstract fun requestNextMove(chess: Chess, state: State.Capture): Boolean
}