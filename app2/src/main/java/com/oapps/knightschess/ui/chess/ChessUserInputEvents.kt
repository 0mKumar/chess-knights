package com.oapps.knightschess.ui.chess

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.unit.dp
import com.oapps.lib.chess.*
import kotlin.math.roundToInt

abstract class ChessUserInputEvents(private val gameManager: GameManager) {
    private val TAG = "ChessUIEvents"

    private fun tryPieceMoveTo(
        piece: DynamicPiece2,
        droppedTo: IVec,
        move: Move = Move(gameManager.chess, Piece(piece.vec, piece.kind), droppedTo)
    ) {
        if (gameManager.currentPlayer is ManualPlayer) {
            gameManager.tryMakeMove(piece, move)
        }
    }

    var draggedPiece: DynamicPiece2? = null

    open fun onPieceDragStart(piece: DynamicPiece2, offset: Offset) {
        Log.d(TAG, "onPieceDragStart: ")
        draggedPiece = piece
        // soundManager?.play(R.raw.select)
    }

    open fun onPieceDragEnd(piece: DynamicPiece2) {
        Log.d(TAG, "onPieceDragEnd: ")
        draggedPiece = null
        val droppedTo = IVec(
            piece.offset.x.roundToInt(),
            piece.offset.y.roundToInt()
        )
        tryPieceMoveTo(piece, droppedTo)
    }

    abstract fun Offset.toVecFraction(scope: PointerInputScope): Offset

    open fun onPieceDrag(
        piece: DynamicPiece2,
        change: PointerInputChange,
        dragAmount: Offset,
        scope: PointerInputScope
    ){
        if(gameManager.promotionRequest is PromotionRequest.Request) return
        if (!gameManager.currentPlayer.canPickOrDragPieceFromUI(piece, gameManager.chess.capturedState)) {
            return
        }
        Log.d(TAG, "onPieceDrag: ")
        change.consumeAllChanges()
        val drag = dragAmount.toVecFraction(scope)
        piece.snapDrag(drag)
    }

    var selectedPiece: DynamicPiece2? = null

    open fun onPieceClicked(piece: DynamicPiece2){
        Log.d(TAG, "onPieceClicked: doing...")
        if(gameManager.promotionRequest is PromotionRequest.Request) return
        when (selectedPiece) {
            piece -> {
                // simply unselect piece
                selectedPiece = null
            }
            null -> {
                if (gameManager.currentPlayer.canPickOrDragPieceFromUI(
                        piece,
                        gameManager.chess.capturedState
                    )
                ) {
                    selectedPiece = piece
                    val moves = MoveValidator.StandardValidator.getPossibleMoves(
                        gameManager.chess,
                        piece.asPiece()
                    )
                    Log.d(
                        TAG,
                        "onPieceClicked: possible moves for $piece = ${moves.size} ${moves.map { it.to.loc }}"
                    )
                }
            }
            else -> {
                selectedPiece?.let { tryPieceMoveTo(it, piece.vec) }
                selectedPiece = null
            }
        }
    }

    open fun onDragStart(offset: Offset) {}
    open fun onDragEnd() {}
    open fun onDrag(change: PointerInputChange, dragAmount: Offset) {}
    open fun squareTapped(square: IVec) {
        selectedPiece?.let {
            tryPieceMoveTo(it, square)
        }
        selectedPiece = null
    }
}