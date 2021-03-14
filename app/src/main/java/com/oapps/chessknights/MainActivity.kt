package com.oapps.chessknights

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oapps.chessknights.ui.chess.*
import com.oapps.chessknights.ui.theme.*

val TAG = "Compose"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chess.state.resetCastling("KkQq")
        setContent {
            ChessKnightsTheme(window) {
                Scaffold(
                    topBar = {
                        AppTopBar()
                    },
                ) {
                    AppContent()
                }
            }
        }
    }
}

@Composable
fun AppContent() {
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
            PlayableChessBoard(whiteBottom, Modifier.padding(vertical = 16.dp))
            Button(onClick = {
                Log.d(TAG, "AppContent: ${chess.generateFen()}")
            }) {
                Text("Print fen", Modifier.padding(start = 8.dp))
            }
            Button(onClick = {
                Log.d(TAG, "AppContent: ${chess.asciiBoard()}")
            }) {
                Text("Print full board", Modifier.padding(start = 8.dp))
            }
            val coroutineScope = rememberCoroutineScope()
            Button(onClick = {
                Log.d(TAG, "AppContent: ${chess.refreshPieces(coroutineScope)}")
            }) {
                Text("Force refresh", Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
fun AppTopBar() {
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
}