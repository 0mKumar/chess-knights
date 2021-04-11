package com.oapps.chessknights.logic

import android.util.Log
import com.oapps.chessknights.TAG
import com.oapps.chessknights.Vec
import com.oapps.chessknights.ui.chess.Piece
import kotlinx.coroutines.*

class Move private constructor(
    val chess: Chess,
    val piece: Piece,
    val to: Vec,
    var promotesTo: Char? = null
) {
    val pieceKind = piece.kind
    val props = mutableMapOf<Props, Any>()
    val from: Vec = piece.vec.copy()

    enum class Props {
        ATTACKED_PIECE,
        EN_PASSANT_OCCURRED_FOR_TARGET,
        CASTLING_CHAR,
        INVALID_BOOLEAN,
        CASTLING_ROOK_PIECE,
        CASTLING_ROOK_FROM_VEC,
        ENPASSANT_TARGET_VEC,
    }

    constructor(chess: Chess, algebraic: String) : this(
        chess, algebraic.substring(0..1).let { chess.findPieceAt(Vec(it)) ?: Piece(kind = '-') },
        Vec(algebraic.substring(2..3)),
        if (algebraic.length == 5) algebraic[4] else null
    )

    companion object {
        operator fun invoke(chess: Chess, piece: Piece, to: Vec, promotesTo: Char? = null): Move {
            return Move(chess, piece, to.copy(), promotesTo)
        }
    }

    override fun toString(): String {
        return "Move(${piece.kind}, ${from.loc()} -> ${to.loc()}, props='${props}')"
    }

    fun isPromotion(): Boolean {
        if (pieceKind == 'P') {
            if (to.y == 0 || to.y == 7) {
                return true
            }
        }
        return false
    }

    fun algebraic() = from.loc() + to.loc()
}