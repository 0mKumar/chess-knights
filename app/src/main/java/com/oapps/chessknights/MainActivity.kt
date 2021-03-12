package com.oapps.chessknights

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
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

val TAG = "Compose"

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

    ChessBox(modifier = Modifier.padding(16.dp)) {
        ChessBackground(whiteBottom = true)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            if (tryAwaitRelease()) {
                                Log.d(TAG, "ChessBoard: press released at $offset")
                                pieces
                                    .find { it.selected }
                                    ?.let {
                                        it.selected = false
                                        val size = maxWidth.toPx() / 8
                                        val to = Vec(
                                            (offset.x / size).toInt(),
                                            (offset.y / size).toInt()
                                        )
                                        it.moveTo(coroutineScope, to) {
                                            Log.d(TAG, "ChessBoard: moved")
                                        }
                                    }
                            }
                        },
                    )
                }
        )
        pieces.forEach { piece ->
            ChessPiece(
                piece = piece,
                size = maxWidth / 8,
                onDrag = {
                    piece.dragBy(coroutineScope, it)
                },
                onDragEnd = {
                    piece.snap(coroutineScope) {
                        pieces.remove(piece)
                        pieces.add(Piece(piece.vec, 'Q'))
                    }
                }, onClick = {
                    Log.d(TAG, "ChessBoard: press released on piece")
                    if (piece.selected) {
                        piece.selected = false
                    } else {
                        val selectedPiece = pieces.find { it.selected }
                        if (selectedPiece != null) {
                            selectedPiece.selected = false
                            selectedPiece.moveTo(coroutineScope, piece.vec) {}
                        } else {
                            piece.selected = true
                        }
                    }
                }
            )
        }
    }
}