package com.oapps.chessknights.logic

import com.oapps.chessknights.Vec
import com.oapps.chessknights.ui.chess.Piece

data class Move(val chess: Chess, val piece: Piece, val to: Vec, val promotesTo: Char = 'Q') {
    val props = mutableMapOf<Props, Any>()
    enum class Props{
        ATTACKED_PIECE,
        EN_PASSANT_STRING,
        CASTLING_CHAR,
    }

    constructor(chess: Chess, algebraic: String): this(chess, algebraic.substring(0..1).let { chess.findPieceAt(Vec(it))?: Piece(kind = '-') },
        Vec(algebraic.substring(2..3)),
        if(algebraic.length == 5) algebraic[4] else 'Q'
    )
}