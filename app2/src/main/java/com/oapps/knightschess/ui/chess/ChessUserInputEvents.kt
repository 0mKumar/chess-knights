package com.oapps.knightschess.ui.chess

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import com.oapps.lib.chess.IVec

open class ChessUserInputEvents {
    var draggedPiece: DynamicPiece2? = null
    open fun onPieceDragStart(piece: DynamicPiece2, offset: Offset) {}
    open fun onPieceDragEnd(piece: DynamicPiece2) {}
    open fun onPieceDrag(
        piece: DynamicPiece2,
        change: PointerInputChange,
        dragAmount: Offset,
        scope: PointerInputScope
    ) {
    }

    var selectedPiece: DynamicPiece2? = null
    open fun onPieceClicked(piece: DynamicPiece2){}

    open fun onDragStart(offset: Offset) {}
    open fun onDragEnd() {}
    open fun onDrag(change: PointerInputChange, dragAmount: Offset) {}
    open fun squareTapped(square: IVec) {}
}