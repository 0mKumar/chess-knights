package com.oapps.knightschess.ui.chess

import com.oapps.lib.chess.Move

sealed class PromotionRequest{
    class Request(val move: Move, val onComplete: (Char) -> Unit): PromotionRequest()
    object None: PromotionRequest()
}