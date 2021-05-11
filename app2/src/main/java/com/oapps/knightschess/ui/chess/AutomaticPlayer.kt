package com.oapps.knightschess.ui.chess

import com.oapps.knightschess.ui.chess.moveprovider.BaseMoveProvider
import com.oapps.knightschess.ui.chess.moveprovider.RandomMoveProvider
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import kotlin.reflect.KClass

class AutomaticPlayer(override val color: Boolean, private val moveProvider: BaseMoveProvider = RandomMoveProvider(), delay: Long = 0): Player {
    init {
        moveProvider.accepts = {
            it.activeColor == color
        }
        moveProvider.onMoveReady = { gameManager, move, state ->
            gameManager.coroutineScope.launch {
                delay(delay)
                gameManager.pieces.find(move.piece) {
                    gameManager.tryMakeMove(it, move)
                }
            }
        }
    }

    override fun requestNextMove(gameManager: GameManager): Boolean {
        if(gameManager.chess.capturedState.activeColor == color){
            return moveProvider.requestNextMove(gameManager, gameManager.chess, gameManager.chess.capturedState)
        }
        return false
    }
}