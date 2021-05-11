package com.oapps.knightschess.ui.chess

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.oapps.lib.chess.Piece

fun SnapshotStateList<DynamicPiece2>.find(piece: Piece) =
    find { it.vec == piece.vec && it.kind == piece.kind }

fun SnapshotStateList<DynamicPiece2>.find(piece: Piece, block: (DynamicPiece2) -> Unit) {
    find(piece)?.let(block)
}

fun SnapshotStateList<DynamicPiece2>.remove(piece: Piece) = find(piece){ remove(it) }

