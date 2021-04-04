package com.oapps.chessknights.logic

import com.oapps.chessknights.Vec
import com.oapps.chessknights.ui.chess.Piece

fun MutableMap<Move.Props, Any>.isInvalid(): Boolean {
    return get(Move.Props.INVALID_BOOLEAN) == true
}

fun MutableMap<Move.Props, Any>.isValid(): Boolean {
    return get(Move.Props.INVALID_BOOLEAN) != true
}

fun MutableMap<Move.Props, Any>.isCastling(block: (who: Char) -> Unit){
    (get(Move.Props.CASTLING_CHAR) as? Char)?.let(block)
}

fun MutableMap<Move.Props, Any>.createsEnPassantTarget(block: (target: Vec) -> Unit){
    (get(Move.Props.ENPASSANT_TARGET_VEC) as? Vec)?.let(block)
}

fun MutableMap<Move.Props, Any>.isEnPassant(block: (enPassantTarget: Vec) -> Unit){
    (get(Move.Props.EN_PASSANT_OCCURRED_FOR_TARGET) as? Vec)?.let(block)
}

fun MutableMap<Move.Props, Any>.isAttack(block: (attackedPiece: Piece) -> Unit){
    (get(Move.Props.ATTACKED_PIECE) as? Piece)?.let(block)
}

fun MutableMap<Move.Props, Any>.isAttack() = (get(Move.Props.ATTACKED_PIECE) is Piece)


fun MutableMap<Move.Props, Any>.getCastlingRook() = get(Move.Props.CASTLING_ROOK_PIECE) as? Piece
fun MutableMap<Move.Props, Any>.getCastlingRookInitialVec() = get(Move.Props.CASTLING_ROOK_FROM_VEC) as? Vec

