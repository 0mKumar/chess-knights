package com.oapps.chessknights.logic

import com.oapps.chessknights.Vec
import com.oapps.chessknights.ui.chess.Piece

class Move private constructor (val chess: Chess, val piece: Piece, val to: Vec, val promotesTo: Char = 'Q') {
    val props = mutableMapOf<Props, Any>()
    val from: Vec = piece.vec.copy()

    enum class Props{
        ATTACKED_PIECE,
        EN_PASSANT_STRING,
        CASTLING_CHAR,
        INVALID_BOOLEAN,
    }

    constructor(chess: Chess, algebraic: String): this(chess, algebraic.substring(0..1).let { chess.findPieceAt(Vec(it))?: Piece(kind = '-') },
        Vec(algebraic.substring(2..3)),
        if(algebraic.length == 5) algebraic[4] else 'Q'
    )

    companion object {
        operator fun invoke(chess: Chess, piece: Piece, to: Vec, promotesTo: Char = 'Q'): Move {
            return Move(chess, piece, to.copy(), promotesTo)
        }
    }

    override fun toString(): String {
        return "Move(${piece.kind}, ${from.loc()} -> ${to.loc()}, props='${props}')"
    }
}