package com.oapps.chessknights.logic

import com.oapps.chessknights.Vec

class State() {
    private val castling = mutableSetOf<Char>()
    val enPassantTarget: Vec? = null
    var activeColor = Chess.Color.UNSPECIFIED


    fun resetCastling(whoCanCastle: String = "KQkq") {
        castling.clear()
        whoCanCastle.forEach { castling.add(it) }
    }

    fun canCastle(who: Char) = castling.contains(who)

    fun removeCastle(who: Char) {
        castling.remove(who)
    }

    fun castlingString() = "KkQq".filter { canCastle(it) }.let {
        if(it.isEmpty()) "-" else it
    }
    fun enPassantString() = enPassantTarget?.loc() ?: "-"

    fun activeColorString() = when (activeColor) {
        Chess.Color.WHITE -> "w"
        Chess.Color.BLACK -> "b"
        Chess.Color.UNSPECIFIED -> "-"
    }
}