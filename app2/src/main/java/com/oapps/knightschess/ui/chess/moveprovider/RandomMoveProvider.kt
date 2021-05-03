package com.oapps.knightschess.ui.chess.moveprovider

import com.oapps.lib.chess.Chess
import com.oapps.lib.chess.MoveValidator
import com.oapps.lib.chess.State
import com.oapps.lib.chess.color

class RandomMoveProvider : BaseMoveProvider() {
    override fun requestNextMove(chess: Chess, state: State.Capture): Boolean {
        val p = chess.pieces.values.filter { it.kind.color == state.activeColor }.shuffled()
        val piece = p.firstOrNull {
            MoveValidator.StandardValidator.getPossibleMoves(
                chess,
                it,
                earlyReturnOneOrNone = true
            ).isNotEmpty()
        } ?: return false
        val move = MoveValidator.StandardValidator.getPossibleMoves(chess, piece).random()
        onMoveReady?.invoke(move, state) ?: return false
        return true
    }
}