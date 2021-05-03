package com.oapps.knightschess.ui.chess.moveprovider

import android.util.Log
import com.oapps.lib.chess.Chess
import com.oapps.lib.chess.Move
import com.oapps.lib.chess.State

class PredefinedMoveProvider(private val moves: List<String>): BaseMoveProvider() {
    private val TAG = "PredefinedMove"
    var index = 0

    override fun requestNextMove(chess: Chess, state: State.Capture): Boolean {
        if(!accepts(state)) return false
        if(index >= moves.size) return false
        val move = Move(chess, moves[index++])
        if(move.isValid()) {
            onMoveReady?.invoke(move, state)
            return true
        }else{
            Log.e(TAG, "requestNextMove: $move is invalid")
            index--
        }
        return false
    }
}