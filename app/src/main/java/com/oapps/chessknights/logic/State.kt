package com.oapps.chessknights.logic

import androidx.compose.runtime.*
import com.oapps.chessknights.Vec
import java.util.Stack

class State() {
    val history = Stack<Capture>()

    inner class Capture(state: State){
        val castling = state.castling
        val enPassantTarget = state.enPassantTarget.copy()
        val activeColor = state.activeColor
        val halfMoveCount = state.halfMoveCount
        val halfMoveClock = state.halfMoveClock
    }

    // no of moves since last capture or pawn move
    var halfMoveClock by mutableStateOf(0)
    var halfMoveCount by mutableStateOf(2)
    val fullMoveCount: Int
        get() = halfMoveCount / 2

    var castling by mutableStateOf("KkQq")
    var enPassantTarget by mutableStateOf(Vec.None)
    var activeColor by mutableStateOf(Chess.Color.UNSPECIFIED)

    fun resetCastling(whoCanCastle: String = "KQkq") {
        castling = whoCanCastle
    }

    fun canCastle(who: Char) = castling.contains(who)

    fun removeCastle(who: Char) {
        castling = castling.filter { it !=  who}
    }

    fun castlingString() = "KkQq".filter { canCastle(it) }.let {
        if(it.isEmpty()) "-" else it
    }
    fun enPassantString() = enPassantTarget.loc()

    fun activeColorString() = when (activeColor) {
        Chess.Color.WHITE -> "w"
        Chess.Color.BLACK -> "b"
        Chess.Color.UNSPECIFIED -> "-"
    }

    fun update(move: Move){
        activeColor = if(move.piece.isWhite()) Chess.Color.BLACK else Chess.Color.WHITE
        move.props.isCastling {
            removeCastle(it)
        }
        enPassantTarget = Vec.None
        move.props.createsEnPassantTarget {
            enPassantTarget = it
        }
        halfMoveCount++
        halfMoveClock++
        if(move.props.isAttack()){
            halfMoveClock = 0
        }
        history.add(Capture(this))
    }

    fun reset(fen: String) {
        history.clear()
        val data = fen.substringAfter(' ').split(' ')
        activeColor = when(data[0][0]){
            'w' -> Chess.Color.WHITE
            'b' -> Chess.Color.BLACK
            else -> Chess.Color.UNSPECIFIED
        }
        resetCastling(data[1])
        enPassantTarget = when(data[2]){
            "-" -> Vec.None
            else -> Vec(data[2])
        }
        halfMoveCount = data[3].toInt()
        val fullMoveCount = data[4].toInt()
        halfMoveCount = fullMoveCount * 2 + if (activeColor == Chess.Color.BLACK) 1 else 0
        history.add(Capture(this))
    }
}