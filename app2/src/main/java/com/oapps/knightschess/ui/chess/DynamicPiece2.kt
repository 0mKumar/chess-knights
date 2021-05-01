package com.oapps.knightschess.ui.chess

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import com.oapps.lib.chess.IVec
import com.oapps.lib.chess.Piece
import com.oapps.lib.chess.loc
import kotlinx.coroutines.*
import java.lang.Math.cbrt
import kotlin.coroutines.suspendCoroutine

class DynamicPiece2(piece: Piece) {

    override fun toString() = "DPiece($kind${vec.loc})"

    fun asPiece() = Piece(vec, kind)

    private val TAG = "DynamicPiece2"

    var vec by mutableStateOf(piece.vec)
        private set

    var kind by mutableStateOf(piece.kind)

    var offset by mutableStateOf(piece.vec.toOffset())
    var velocity: Offset? = null
    var animJob: Job? = null

    fun moveTo(scope: CoroutineScope, to: IVec, onComplete: ((cancelled: Boolean) -> Unit)? = null) {
        animJob?.cancel()
        animJob = scope.launch {
            var done = false
            try {
                vec = to
                animate(Offset.VectorConverter, offset, to.toOffset(), velocity, tween(
                    250 * cbrt(
                    (to.toOffset() - offset).getDistance()
                        .toDouble()
                ).toInt()
                )) { value, currentVelocity ->
                    offset = value
                    velocity = currentVelocity
                }
                velocity = null
                onComplete?.invoke(false)
                done = true
                Log.d(TAG, "moveTo: complete")
            }catch (e: CancellationException){
                Log.d(TAG, "moveTo: cancelled")
                throw e
            }finally {
                if(!done){
                    Log.d(TAG, "moveTo: cancel reporting")
                    onComplete?.invoke(true)
                }
            }
        }
    }

    fun snapDrag(by: Offset): Boolean {
        if(animJob?.isActive == true){
            // ongoing animation, so cannot drag
            return false
        }
        offset += by
        velocity = null
        return true
    }
}