package com.oapps.chessknights

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.oapps.chessknights.ui.chess.ChessBackground
import com.oapps.chessknights.ui.chess.ChessBox
import com.oapps.chessknights.ui.chess.ChessPiece
import com.oapps.chessknights.ui.theme.*
import kotlinx.coroutines.*

val TAG = "Compose"

var whiteBottom = false

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chess.state.resetCastling("KkQq")
        setContent {
            ChessKnightsTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Column {
                        Text(text = "Header", Modifier.padding(16.dp))
                        ChessBoard(whiteBottom)
                        Text("Footer", Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}


@Composable
private fun ChessBoard(whiteBottom: Boolean) {
    val coroutineScope = rememberCoroutineScope()

    ChessBox(modifier = Modifier.padding(16.dp)) {
        ChessBackground()
        ChessClickBase(coroutineScope, whiteBottom)
        ChessPiecesLayer(coroutineScope, whiteBottom)
    }
}

@Composable
private fun BoxWithConstraintsScope.ChessPiecesLayer(
    coroutineScope: CoroutineScope,
    whiteBottom: Boolean
) {
    chess.pieces.forEach { piece ->
        ChessPiece(
            piece = piece,
            size = maxWidth / 8,
            onDrag = {
                piece.dragBy(coroutineScope, it)
            },
            onDragEnd = {
                piece.snap(coroutineScope)
            }, onClick = {
                Log.d(TAG, "ChessBoard: press released on piece")
                if (piece.selected) {
                    piece.selected = false
                } else {
                    val selectedPiece = chess.pieces.find { it.selected }
                    if (selectedPiece != null) {
                        selectedPiece.selected = false
                        selectedPiece.moveTo(coroutineScope, piece.vec)
                    } else {
                        piece.selected = true
                    }
                }
            },
            whiteBottom = whiteBottom
        )
    }
}

@Composable
private fun BoxWithConstraintsScope.ChessClickBase(coroutineScope: CoroutineScope, whiteBottom: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { it ->
                        val offset = it.transform(whiteBottom, Offset(maxWidth.toPx(), maxHeight.toPx()))
                        if (tryAwaitRelease()) {
                            Log.d(TAG, "ChessBoard: press released at $offset")
                            chess.pieces
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
}