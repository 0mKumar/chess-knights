package com.oapps.chessknights.logic

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.oapps.chessknights.Vec
import com.oapps.chessknights.ui.chess.Piece
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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

    fun asciiBoard(): String{
        val board = Array(8){Array(8){ mutableListOf<Char>() } }
        for(piece in pieces){
            val (x, y) = piece.vec
            board[y][x].add(piece.kind)
        }
        var out = "\n"
        for(y in board.reversedArray()){
            for(p in y){
                out += String.format("%3s ", p.joinToString(""))
            }
            out += "\n"
        }
        return out.removeSuffix("\n")
    }

    fun refreshPieces(coroutineScope: CoroutineScope){
        coroutineScope.launch {
            pieces.forEach {
                it.offsetFractionX.snapTo(it.vec.x.toFloat())
                it.offsetFractionY.snapTo(it.vec.y.toFloat())
            }
        }
    }

    fun generateFen(): String {
        return pieces.groupBy { it.vec.y }.mapValues { entry ->
            entry.value.sortedBy {
                it.vec.x
            }.run {
                var row = ""
                var x = 0
                forEach { piece ->
                    val diff = piece.vec.x - x
                    if (diff > 0) {
                        row += diff
                    }
                    row += piece.kind
                    x = piece.vec.x + 1
                }
                if (8 - x > 0) {
                    row += 8 - x
                }
                row
            }
        }.run {
            var fen = ""
            var y = 7
            entries.toMutableList().sortedByDescending { it.key }.forEach { entry ->
                val diff = y - entry.key
                if (diff > 0) {
                    fen += "8/".repeat(diff)
                }
                fen += entry.value
                y = entry.key - 1
                fen += "/"
            }
            if (y > 0) {
                fen += y
            }
            fen.removeSuffix("/")
        }
    }

}