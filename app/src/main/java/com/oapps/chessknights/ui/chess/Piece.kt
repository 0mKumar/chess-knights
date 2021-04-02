package com.oapps.chessknights.ui.chess

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.oapps.chessknights.*
import com.oapps.chessknights.logic.*
import kotlinx.coroutines.*
import kotlin.math.roundToInt

class Piece(
    var vec: Vec = Vec(),
    kind: Char = 'p'
) {
    val name: String by lazy {
        pieceName[kind.toUpperCase()] ?: "Unknown"
    }

    override fun toString(): String {
        return "Piece($kind at ${vec.loc()})"
    }

    var selected by mutableStateOf(false)

    //    var vec by mutableStateOf(vec, structuralEqualityPolicy())
    var kind by mutableStateOf(kind)

    fun dragBy(coroutineScope: CoroutineScope, offset: Offset) {
        coroutineScope.launch {
            async { offsetFractionX.dragBy(offset.x) }
            async { offsetFractionY.dragBy(offset.y) }
        }
    }

    val offsetFractionX = Animatable(vec.x.toFloat())
    val offsetFractionY = Animatable(vec.y.toFloat())

    companion object {
        val drawableImageResources = mapOf(
            'R' to R.drawable.wr,
            'N' to R.drawable.wn,
            'B' to R.drawable.wb,
            'Q' to R.drawable.wq,
            'K' to R.drawable.wk,
            'P' to R.drawable.wp,
            'r' to R.drawable.br,
            'n' to R.drawable.bn,
            'b' to R.drawable.bb,
            'q' to R.drawable.bq,
            'k' to R.drawable.bk,
            'p' to R.drawable.bp,
        )
        val pieceName = mapOf(
            'R' to "Rook",
            'N' to "Knight",
            'B' to "Bishop",
            'Q' to "Queen",
            'K' to "King",
            'P' to "Pawn"
        )
    }

    val image: Int
        get() = drawableImageResources[kind] ?: R.drawable.bn

    constructor(notation: String) : this(Vec(notation.substring(1)), notation[0])

    fun isWhite() = kind.isUpperCase()
    fun isBlack() = kind.isLowerCase()

    fun contains(chessOffset: Offset, sizePx: Float): Boolean {
        return Rect(
            Offset(offsetFractionX.value * sizePx, offsetFractionY.value * sizePx),
            sizePx
        ).contains(chessOffset)
    }

    fun snap(
        coroutineScope: CoroutineScope,
        onFailed: (CoroutineScope.() -> Unit)? = null,
        onComplete: (CoroutineScope.(move: Move) -> Unit)? = null,
        requestPromotionTo: ((move: Move) -> Unit)? = null
    ) {
        val to = Vec(offsetFractionX.value.roundToInt(), offsetFractionY.value.roundToInt())
        moveTo(coroutineScope, to, onFailed, false, onComplete, requestPromotionTo)
    }

    fun moveTo(
        coroutineScope: CoroutineScope,
        toActual: Vec,
        onFailed: (CoroutineScope.() -> Unit)? = null,
        skipCheck: Boolean = false,
        onComplete: (CoroutineScope.(move: Move) -> Unit)? = null,
        requestPromotionTo: ((move: Move) -> Unit)? = null
    ) {
        val to = toActual.copy()
        coroutineScope.launch {
            async {
                val move = Move(chess, this@Piece, to)
                if (!skipCheck) {
                    Log.d(TAG, "moveTo: trying to $move")
                    if (to.x !in 0..7 || to.y !in 0..7 || !MoveValidator.validateMove(
                            chess,
                            move
                        )
                    ) {
                        move.props[Move.Props.INVALID_BOOLEAN] = true
                        to.x = move.from.x
                        to.y = move.from.y
                        onFailed?.invoke(this)
                        Log.d(TAG, "moveTo: $move failed, reverting to ${to.loc()}")
                    }
                    move.props.isCastling { who ->
                        val rook = (vec + if (who.toUpperCase() == 'K') Vec(3, 0) else Vec(-4, 0))
                            .let { chess.findPieceAt(it) }
                        if (rook != null) {
                            move.props[Move.Props.CASTLING_ROOK_PIECE] = rook
                            move.props[Move.Props.CASTLING_ROOK_FROM_VEC] = rook.vec
                            val rookFinalPos =
                                vec + if (who.toUpperCase() == 'K') Vec(1, 0) else Vec(-1, 0)
                            rook.moveTo(coroutineScope, rookFinalPos, null, true)
                        } else {
                            to.x = move.from.x
                            to.y = move.from.y
                            onFailed?.invoke(this)
                            Log.d(
                                TAG,
                                "moveTo: $move failed (rook not found!), reverting to ${to.loc()}"
                            )
                        }
                    }
                }

                val x = async { offsetFractionX.animateTo(to.x.toFloat()) }
                val y = async { offsetFractionY.animateTo(to.y.toFloat()) }
                awaitAll(x, y).let {
                    vec = to
                }
                if (move.props.isValid()) {
                    Log.d(TAG, "moveTo: $move complete")
                    move.props.isAttack {
                        chess.pieces.remove(it)
                        Log.d(TAG, "moveTo: removing attacked piece $it")
                    }
                    if (move.isPromotion()) {
                        if (move.promotesTo == null) {
                            requestPromotionTo?.invoke(move)
                        }
                        while (move.promotesTo == null) {
                            delay(16)
                        }
                        kind = (move.promotesTo ?: 'Q').let {
                            if (isWhite()) it.toUpperCase() else it.toLowerCase()
                        }
                    }
                    onComplete?.invoke(this, move)
                }
                if(MoveValidator.isCheck(chess, true)){
                    Log.d(TAG, "moveTo: White is in check")
                }
                if(MoveValidator.isCheck(chess, false)){
                    Log.d(TAG, "moveTo: Black is in check")
                }
            }
        }
    }

    fun justMoveTo(
        move: Move,
        onFailed: (() -> Unit)? = null,
        onComplete: ((move: Move) -> Unit)? = null,
    ) {
        val to = move.to.copy()
//        val move = Move(chess, this@Piece, to)

        Log.d(TAG, "justMoveTo: $move")
        if (to.x !in 0..7 || to.y !in 0..7) {
            move.props[Move.Props.INVALID_BOOLEAN] = true
            to.x = move.from.x
            to.y = move.from.y
            onFailed?.invoke()
            Log.d(TAG, "justMoveTo: $move failed, reverting to ${to.loc()}")
        }
        move.props.isCastling { who ->
            val rook = (vec + if (who.toUpperCase() == 'K') Vec(3, 0) else Vec(-4, 0))
                .let { chess.findPieceAt(it) }
            if (rook != null) {
                move.props[Move.Props.CASTLING_ROOK_PIECE] = rook
                move.props[Move.Props.CASTLING_ROOK_FROM_VEC] = rook.vec
                val rookFinalPos = vec + if (who.toUpperCase() == 'K') Vec(1, 0) else Vec(-1, 0)
                rook.justMoveTo(Move(chess, rook, rookFinalPos))
            } else {
                to.x = move.from.x
                to.y = move.from.y
                onFailed?.invoke()
                Log.d(TAG, "justMoveTo: $move failed (rook not found!), reverting to ${to.loc()}")
            }
        }

        vec = to

        if (move.props.isValid()) {
            Log.d(TAG, "justMoveTo: $move complete")
            move.props.isAttack {
                Log.d(TAG, "justMoveTo: removed attacked piece $it")
                chess.pieces.remove(it)
            }
            if (move.isPromotion()) {
                kind = (move.promotesTo ?: 'Q').let {
                    if (isWhite()) it.toUpperCase() else it.toLowerCase()
                }
            }
            onComplete?.invoke(move)
        }
    }
}