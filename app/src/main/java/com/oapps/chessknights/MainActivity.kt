package com.oapps.chessknights

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.oapps.chessknights.ui.theme.*
import kotlinx.coroutines.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChessKnightsTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Column {
                        Text(text = "Header", Modifier.padding(16.dp))
                        ChessBoard()
                        Text("Footer", Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}


@Composable
private fun ChessBoard() {
    val coroutineScope = rememberCoroutineScope()

    ChessBox(modifier = Modifier.padding(16.dp)
        .pointerInput("tap"){
            detectTapGestures {

            }
        }
    ) {
        ChessBackground(whiteBottom = true)
        pieces.forEach {
            val piece = it
            ChessPiece(
                piece = piece,
                size = maxWidth / 8,
                onDrag = {
                    coroutineScope.launch {
                        async { piece.offsetFractionX.dragBy(it.x) }
                        async { piece.offsetFractionY.dragBy(it.y) }
                    }
                },
                onDragEnd = {
                    coroutineScope.launch {
                        async {
                            val x = async { piece.offsetFractionX.animateRoundSnap() }
                            val y = async { piece.offsetFractionY.animateRoundSnap() }
                            awaitAll(x, y).let {
                                piece.vec = Vec(
                                    piece.offsetFractionX.value.roundToInt().coerceIn(0..7),
                                    piece.offsetFractionY.value.roundToInt().coerceIn(0..7),
                                )
                            }
                            pieces.remove(it)
                            pieces.add(Piece(it.vec, 'Q'))
                        }
                    }
                }
            )
        }
    }
}