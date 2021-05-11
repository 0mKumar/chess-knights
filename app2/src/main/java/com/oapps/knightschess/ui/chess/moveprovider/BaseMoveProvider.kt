package com.oapps.knightschess.ui.chess.moveprovider

import com.oapps.knightschess.ui.chess.GameManager
import com.oapps.lib.chess.Chess
import com.oapps.lib.chess.Move
import com.oapps.lib.chess.State

abstract class BaseMoveProvider() {
    var accepts = { state: State.Capture -> true }
    var onMoveReady: ((gameManager: GameManager, move: Move, state: State.Capture) -> Unit)? = null
    abstract fun requestNextMove(gameManager: GameManager, chess: Chess, state: State.Capture): Boolean
}