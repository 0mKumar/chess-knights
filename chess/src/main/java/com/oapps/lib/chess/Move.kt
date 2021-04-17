package com.oapps.lib.chess

class Move(val chess: Chess, val piece: Piece, val to: IVec, var promotesTo: Char? = chess.options.defaultPromotion, checkIllegal: Boolean = true){
    val attackedPiece: Char? = chess.pieces[to]?.kind
    val diff = to - piece.vec

    val validationResult by lazy {
        chess.validator.validate(this, checkIllegal)
    }

    constructor(chess: Chess, alg: String, checkIllegal: Boolean = true): this(
        chess, alg.substring(0..1).let { chess.pieces[IVec(it)] ?: Piece() },
        IVec(alg.substring(2..3)),
        if (alg.length == 5) alg[4] else null,
        checkIllegal
    )

    fun isValid() = validationResult.valid

    override fun toString(): String {
        return "Move(piece=$piece, to=$to, promotesTo=$promotesTo, attackedPiece=$attackedPiece, diff=$diff)"
    }
}