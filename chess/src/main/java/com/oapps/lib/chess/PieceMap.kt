package com.oapps.lib.chess

import java.util.*

class PieceMap: TreeMap<IVec, Piece>(){
    private fun move(piece: Piece, to: IVec): Piece? {
        remove(piece)
        return put(piece.copy(vec = to))
    }

    fun move(move: Move, validate: Boolean = true){
        move(move.piece, move.to)
        if(validate && move.isValid()){
            move.validationResult.castling?.let {
                move(move.validationResult.castleRook!!, move.piece.vec + move.diff.sign)
            }
            move.validationResult.promotion?.let {
                put(Piece(move.to, it))
            }
        }
    }

    fun unMove(move: Move, validate: Boolean = true){
        move(move.piece.copy(vec = move.to), move.piece.vec)
        if(move.attackedPiece != null){
            put(Piece(move.to, move.attackedPiece))
        }
        if(validate && move.isValid()) {
            move.validationResult.castling?.let {
                move(
                    move.validationResult.castleRook!!.copy(vec = move.piece.vec + move.diff.sign),
                    move.validationResult.castleRook!!.vec
                )
            }
            move.validationResult.promotion?.let {
                put(Piece(move.piece.vec, move.piece.kind))
            }
        }
    }

    fun setFen(fen: String) {
        clear()
        fen.substringBefore(' ').split('/').forEachIndexed { i, row ->
            var currX = 0
            row.forEach { kind ->
                if (kind.isDigit()) {
                    currX += kind - '0'
                } else {
                    val element = Piece(IVec(currX++, 7 - i), kind)
                    put(element)
                }
            }
        }
    }

    fun generateFen(): String {
        val fen = StringBuilder()
        var x = 0
        var y = 7
        for (entry in this) {
            if (entry.key.y == y) {
                // still in same row
                val diff = entry.key.x - x
                if (diff > 0) {
                    fen.append(diff)
                }
                fen.append(entry.value.kind)
                x = entry.key.x + 1
            } else {
                if (8 - x > 0) {
                    fen.append(8 - x)
                }
                // row completed
                fen.append('/')
                val diff = y - entry.key.y
                if (diff > 0) {
                    for (i in 1 until diff) {
                        fen.append("8/")
                    }
                }
                if (entry.key.x > 0) {
                    fen.append(entry.key.x)
                }
                fen.append(entry.value.kind)
                y = entry.key.y
                x = entry.key.x + 1
            }
        }
        if (8 - x > 0) {
            fen.append(8 - x)
        }
        if(y > 0){
            for(i in 0 until y){
                fen.append("/8")
            }
        }
        return fen.toString()
    }
}