package com.oapps.chessknights.logic

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.oapps.chessknights.Vec
import com.oapps.chessknights.ui.chess.Piece

class Chess(val pieces: SnapshotStateList<Piece>) {
    var state = State()

    enum class Color{
        WHITE,
        BLACK,
        UNSPECIFIED,
    }

    constructor(pieceList: List<List<String>>) : this(pieceList.flatten().map { Piece(it) }
        .let { pieces ->
            val list = mutableStateListOf<Piece>()
            pieces.forEach {
                list.add(it)
            }
            return@let list
        })


    fun findPieceAt(vec: Vec) = pieces.find { it.vec == vec }

    fun canMove(move: Move): Boolean {
        return MoveValidator.validateMove(this, move)
    }
}