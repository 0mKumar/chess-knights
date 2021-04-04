package com.oapps.chessknights.logic

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.oapps.chessknights.Vec
import com.oapps.chessknights.chess

class State() {
    inner class Capture(state: State){
        val castling = state.castling
        val enPassantTarget = state.enPassantTarget.copy()
        val activeColor = state.activeColor
    }

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
    fun enPassantString() = enPassantTarget.loc() ?: "-"

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
    }
}