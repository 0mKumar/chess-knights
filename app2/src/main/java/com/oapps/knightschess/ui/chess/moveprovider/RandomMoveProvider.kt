package com.oapps.knightschess.ui.chess.moveprovider

import android.util.Log
import com.oapps.knightschess.ui.chess.GameManager
import com.oapps.lib.chess.*

class RandomMoveProvider : BaseMoveProvider() {
    private val TAG = "RMoveProv"
    override fun requestNextMove(gameManager: GameManager, chess: Chess, state: State.Capture): Boolean {
        Log.d(TAG, "requestNextMove called")
        if(!accepts(state)) return false
        Log.d(TAG, "requestNextMove executing")
        val p = chess.pieces.values.filter { it.kind.color == state.activeColor }.shuffled()
        val piece = p.firstOrNull {
            MoveValidator.StandardValidator.getPossibleMoves(
                chess,
                it,
                earlyReturnOneOrNone = true
            ).isNotEmpty()
        } ?: return false
        val move = MoveValidator.StandardValidator.getPossibleMoves(chess, piece).also {
            println("possible moves for ${piece.kind}${piece.vec.loc} [${it.size}] = ${it.joinToString(" "){it.to.loc}}")
        }.random()
        onMoveReady?.invoke(gameManager, move, state) ?: return false
        return true
    }
}