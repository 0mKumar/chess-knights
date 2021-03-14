package com.oapps.chessknights

import android.os.Bundle
import android.util.Log
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
            val darkMode = remember { mutableStateOf(true) }
            ChessKnightsTheme(window, darkTheme = darkMode.value) {
                Scaffold(
                    topBar = {
                        AppTopBar()
                    },
                ) {
                    AppContent(darkMode)
                }
            }
        }
    }
}

@Composable
fun AppContent(darkMode: MutableState<Boolean>) {
    Surface(color = MaterialTheme.colors.background) {
        val whiteBottom = remember { mutableStateOf(true) }
        var showCoordinates by remember { mutableStateOf(true) }
        Column(Modifier.padding(start = 8.dp, top = 16.dp, end = 8.dp)) {
            TextButton(onClick = { whiteBottom.value = !whiteBottom.value }) {
                Icon(
                    painterResource(id = R.drawable.ic_twotone_flip_camera_android_24),
                    "Flip"
                )
                Text("Flip board", Modifier.padding(start = 8.dp))
            }

            TextButton(onClick = { showCoordinates = !showCoordinates }, Modifier.padding(top = 16.dp)) {
                Icon(painterResource(id = if(showCoordinates) R.drawable.ic_twotone_label_off_24 else R.drawable.ic_twotone_label_24), contentDescription = "")
                Text(text = if (showCoordinates) "Hide coordinates" else "Show coordinates", Modifier.padding(start = 8.dp))
            }

            TextButton(onClick = { darkMode.value = !darkMode.value }, Modifier.padding(top = 16.dp)) {
                Icon(painterResource(id = if(darkMode.value) R.drawable.ic_twotone_wb_sunny_24 else R.drawable.ic_twotone_nights_stay_24), contentDescription = "")
                Text(text = if (darkMode.value) "Light mode" else "Dark mode", Modifier.padding(start = 8.dp))
            }

            PlayableChessBoard(whiteBottom, Modifier.padding(vertical = 16.dp), showCoordinates)
            TextButton(onClick = {
                Log.d(TAG, "AppContent: ${chess.generateFen()}")
            }) {
                Text("Print fen", Modifier.padding(start = 8.dp))
            }
            TextButton(onClick = {
                Log.d(TAG, "AppContent: ${chess.asciiBoard()}")
            }) {
                Text("Print full board", Modifier.padding(start = 8.dp))
            }
            val coroutineScope = rememberCoroutineScope()
            TextButton(onClick = {
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