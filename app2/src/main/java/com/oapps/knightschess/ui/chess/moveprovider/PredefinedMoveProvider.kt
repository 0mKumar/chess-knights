package com.oapps.knightschess.ui.chess.moveprovider

import android.util.Log
import com.oapps.knightschess.ui.chess.GameManager
import com.oapps.lib.chess.Chess
import com.oapps.lib.chess.Move
import com.oapps.lib.chess.MoveValidator
import com.oapps.lib.chess.State

class PredefinedMoveProvider(private val moves: List<String>, val moveStringTypeSan: Boolean = false): BaseMoveProvider() {
    private val TAG = "PredefinedMove"
    var index = 0

    override fun requestNextMove(gameManager: GameManager, chess: Chess, state: State.Capture): Boolean {
        if(!accepts(state)) return false
        if(index >= moves.size) return false
        val move = if(moveStringTypeSan) {
            MoveValidator.StandardValidator.moveFromSan(chess, moves[index++], state.activeColor)
        }else {
            Move(chess, moves[index++])
        }

        if(move != null && move.isValid()) {
            onMoveReady?.invoke(gameManager, move, state)
            return true
        }else{
            Log.e(TAG, "requestNextMove: $move is invalid")
            index--
        }
        return false
    }
}