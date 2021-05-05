package com.oapps.knightschess.ui.chess

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.oapps.audio.SoundManager
import com.oapps.knightschess.R
import com.oapps.lib.chess.Chess
import com.oapps.lib.chess.Move
import com.oapps.lib.chess.isWhite
import kotlinx.coroutines.CoroutineScope

val White = true
val Black = false

class GameManager(
    private val coroutineScope: CoroutineScope,
    private val chess: Chess = Chess(),
    val player1: Player = ManualPlayer(White),
    val player2: Player = ManualPlayer(Black),
    private val onMoveComplete: ((move: Move) -> Unit)? = null
) {

    private var soundManager: SoundManager? = null

    @Composable
    fun Update(){
        val context = LocalContext.current
        DisposableEffect(Unit) {
            soundManager = SoundManager(context, 3)
            soundManager?.start()
            soundManager?.load(R.raw.move)
            soundManager?.load(R.raw.error)
            soundManager?.load(R.raw.out_of_bound)
            soundManager?.load(R.raw.select)
            soundManager?.load(R.raw.check)
            soundManager?.load(R.raw.capture)
            onDispose {
                if (soundManager != null) {
                    soundManager?.cancel()
                    soundManager = null
                }
            }
        }
    }

    val pieces = chess.pieces
        .values
        .map { DynamicPiece2(it) }
        .toMutableStateList()

    var promotionRequest by mutableStateOf<PromotionRequest>(PromotionRequest.None)
        private set

    var fen by mutableStateOf(chess.generateFullFen())

    val moves = mutableListOf<Move>()

    val currentPlayer get() = if(chess.state.activeColor.isWhite) player1 else player2

    fun requestNextMove() {
        currentPlayer.requestNextMove(this)
    }

    private fun tryMakeMove(piece: DynamicPiece2, move: Move) {
        if (move.isValid()) {
            move.isAttack { attackedPiece ->
                pieces.find {
                    it.vec == attackedPiece.vec && it.kind == attackedPiece.kind
                }?.let {
                    pieces.remove(it)
                }
            }
            move.isCastling { rook, to, _ ->
                pieces.find { it.vec == rook.vec && it.kind == rook.kind }
                    ?.moveTo(coroutineScope, to)
            }
            move.isPromotion {
                if (it != null)
                    piece.kind = it
            }
            piece.moveTo(coroutineScope, move.to) {
                if (move.isAttack) {
                    soundManager?.play(R.raw.capture)
                }
                if (move.validationResult.isOpponentInCheck) {
                    soundManager?.play(R.raw.check)
                }
                when {
                    else -> {
                        soundManager?.play(R.raw.move)
                    }
                }
                if (move.isPromotion && move.promotesTo == null) {
                    promotionRequest = PromotionRequest.Request(move) {
                        piece.kind = it
                        move.promotesTo = it
                        commitMove(move)
                        promotionRequest = PromotionRequest.None
                    }
                } else {
                    commitMove(move)
                }
            }
        } else {
            piece.moveTo(coroutineScope, piece.vec)
            soundManager?.play(R.raw.out_of_bound)
        }
    }

    private fun commitMove(move: Move) {
        chess.makeMove(move)
        fen = chess.generateFen()
        moves.add(move)
        onMoveComplete?.invoke(move)
        requestNextMove()
    }
}