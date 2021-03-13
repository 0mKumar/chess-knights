package com.oapps.chessknights

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oapps.chessknights.ui.chess.ChessBackground
import com.oapps.chessknights.ui.chess.ChessBox
import com.oapps.chessknights.ui.chess.ChessPiece
import com.oapps.chessknights.ui.theme.*
import kotlinx.coroutines.*

val TAG = "Compose"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chess.state.resetCastling("KkQq")
        setContent {
            ChessKnightsTheme(window) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    "Knight Chess",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                    fontSize = 24.sp,
                                    color = Color.White
                                )
                            },
                            actions = {
                                Icon(
                                    painterResource(id = R.drawable.ic_twotone_flip_camera_android_24),
                                    ""
                                )
                            }
                        )
                    },
                ) {
                    Surface(color = MaterialTheme.colors.background) {
                        val whiteBottom = remember { mutableStateOf(true) }
                        Column(Modifier.padding(16.dp)) {
                            Button(onClick = { whiteBottom.value = !whiteBottom.value }) {
                                Icon(
                                    painterResource(id = R.drawable.ic_twotone_flip_camera_android_24),
                                    "Flip"
                                )
                                Text("Flip board", Modifier.padding(start = 8.dp))
                            }
                            ChessBoard(whiteBottom)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChessBoard(whiteBottom: MutableState<Boolean>) {
    val coroutineScope = rememberCoroutineScope()

    ChessBox(modifier = Modifier.padding(vertical = 16.dp)) {
        ChessBackground(Modifier.clip(RoundedCornerShape(percent = 1)))
        ChessClickBase(coroutineScope, whiteBottom)
        ChessPiecesLayer(coroutineScope, whiteBottom)
    }
}

@Composable
private fun BoxWithConstraintsScope.ChessPiecesLayer(
    coroutineScope: CoroutineScope,
    whiteBottom: MutableState<Boolean>
) {
    chess.pieces.forEach { piece ->
        ChessPiece(
            piece = piece,
            size = maxWidth / 8,
            onDrag = {
                piece.dragBy(coroutineScope, it.transformDirection(whiteBottom.value))
            },
            onDragEnd = {
                piece.snap(coroutineScope)
            }, onClick = {
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
private fun BoxWithConstraintsScope.ChessClickBase(
    coroutineScope: CoroutineScope,
    whiteBottom: MutableState<Boolean>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { it ->
                        val offset =
                            it.transform(
                                whiteBottom.value,
                                Offset(maxWidth.toPx(), maxHeight.toPx())
                            )
                        if (tryAwaitRelease()) {
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