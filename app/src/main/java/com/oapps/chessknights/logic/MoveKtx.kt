package com.oapps.chessknights.logic

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

fun MutableMap<Move.Props, Any>.isAttack(block: (attackedPiece: Piece) -> Unit){
    (get(Move.Props.ATTACKED_PIECE) as? Piece)?.let(block)
}
