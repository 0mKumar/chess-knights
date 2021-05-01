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

    fun isCastling(block: (rook: Piece, rookFinalPosition: IVec, castleType: Char) -> Unit){
        if(!isValid()) return
        val castling = validationResult.castling?: return
        val castleRook = validationResult.castleRook?: return
        val castlingRookFinalPos = validationResult.castlingRookFinalPos?: return
        block(castleRook, castlingRookFinalPos, castling)
    }

    fun isAttack(block: (attackedPiece: Piece) -> Unit){
        if(!isValid()) return
        val attackedPiece = attackedPiece?:return
        block(Piece(to, attackedPiece))
    }

    fun isEnpassant(block: (attackedPawn: Piece) -> Unit){
        if(!isValid()) return
        validationResult.enPassantCapturedPiece?.let(block)
    }

    fun isPromotion(block: (promotesTo: Char?) -> Unit){
        if(!isValid()) return
        if(piece.isPawn && (to.y == 0 || to.y == 7)){
            block(promotesTo)
        }
    }

    val isCastling get() = isValid() && validationResult.castling != null
    val isPromotion get() = isValid() && (piece.isPawn && (to.y == 0 || to.y == 7))
    val isAttack get() = isValid() && (attackedPiece != null || validationResult.enPassantCapturedPiece != null)

    override fun toString(): String {
        return "M($piece, t=$to, p=$promotesTo, a=$attackedPiece, d=$diff)"
    }
}