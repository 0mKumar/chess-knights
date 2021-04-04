package com.oapps.chessknights.ui.live

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.oapps.chessknights.*
import com.oapps.chessknights.R
import com.oapps.chessknights.ui.chess.LiveGameViewModel
import com.oapps.chessknights.ui.chess.PlayableChessBoard
import com.oapps.chessknights.ui.theme.ChessKnightsTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@Composable
fun LivePlayScreen(darkMode: MutableState<Boolean>, internetAvailable: Boolean, gameId: String? = "") {
    val liveGameViewModel = viewModel(modelClass = LiveGameViewModel::class.java)
    LaunchedEffect(Firebase.auth.currentUser?.uid, gameId){
        if (gameId != null) {
//            liveGameViewModel.setup(gameId)
        }
    }
    Surface(color = MaterialTheme.colors.surface) {
        val whiteBottom = remember { mutableStateOf(true) }
        var showCoordinates by remember { mutableStateOf(true) }
        val scrollState = rememberScrollState()
        Column(
            Modifier
                .padding(start = 8.dp, end = 8.dp)
                .verticalScroll(scrollState)
        ) {
            val intSizeSpec = remember { tween<IntSize>(
                durationMillis = 800,
            ) }
            AnimatedVisibility(!internetAvailable,
                enter = expandVertically(animationSpec = intSizeSpec),
                exit = shrinkVertically(animationSpec = intSizeSpec),
                initiallyVisible = false) {
                Text("No Internet",
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Red),
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
            PlayerBanner(
                "My Opponent",
                "(1360)",
                "4:40",
                Modifier.padding(bottom = 32.dp, top = 16.dp),
                R.drawable.bp
            )
            PlayableChessBoard(whiteBottom, showCoordinates = showCoordinates)
            PlayerBanner(
                "Om Kumar",
                "(1459)",
                "4:35",
                Modifier.padding(top = 32.dp, bottom = 40.dp),
                R.drawable.wp,
                clockActive = true
            )

            TextButton(onClick = { whiteBottom.value = !whiteBottom.value }) {
                Icon(
                    painterResource(id = R.drawable.ic_twotone_flip_camera_android_24),
                    "Flip"
                )
                Text("Flip board", Modifier.padding(start = 8.dp))
            }

            TextButton(
                onClick = { showCoordinates = !showCoordinates },
                Modifier.padding(top = 16.dp)
            ) {
                Icon(
                    painterResource(id = if (showCoordinates) R.drawable.ic_twotone_label_off_24 else R.drawable.ic_twotone_label_24),
                    contentDescription = ""
                )
                Text(
                    text = if (showCoordinates) "Hide coordinates" else "Show coordinates",
                    Modifier.padding(start = 8.dp)
                )
            }

            TextButton(
                onClick = { darkMode.value = !darkMode.value },
                Modifier.padding(top = 16.dp)
            ) {
                Icon(
                    painterResource(id = if (darkMode.value) R.drawable.ic_twotone_wb_sunny_24 else R.drawable.ic_twotone_nights_stay_24),
                    contentDescription = ""
                )
                Text(
                    text = if (darkMode.value) "Light mode" else "Dark mode",
                    Modifier.padding(start = 8.dp)
                )
            }

            TextButton(onClick = {
                Log.d(TAG, "AppContent: ${chess.generateFen()}")
            }, Modifier.padding(top = 16.dp)) {
                Text("Print fen", Modifier.padding(start = 8.dp))
            }
            TextButton(onClick = {
                Log.d(TAG, "AppContent: ${chess.asciiBoard()}")
            }, Modifier.padding(top = 16.dp)) {
                Text("Print full board", Modifier.padding(start = 8.dp))
            }
            val coroutineScope = rememberCoroutineScope()
            TextButton(onClick = {
                Log.d(TAG, "AppContent: ${chess.refreshPieces(coroutineScope)}")
            }, Modifier.padding(top = 16.dp)) {
                Text("Force refresh", Modifier.padding(start = 8.dp))
            }
            if(internetAvailable) {
                Button(onClick = {
                    coroutineScope.launch {
                        val value = mapOf(
                            "id_w" to 13,
                            "id_b" to 15,
                            "time" to ServerValue.TIMESTAMP
                        )
                        val key = Firebase.database.reference.child("games").pushValue(value)
                        Log.d(TAG, "LivePlayScreen: Game created with id = $key")
                    }
                }) {
                    Text("Create game room")
                }
            }
            CreateChallenge(liveGameViewModel)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(widthDp = 400, showSystemUi = false, showBackground = false, backgroundColor = 0xFFFFFFFF)
@Composable
fun PlayerBannerPreview() {
    ChessKnightsTheme(darkTheme = true) {
        PlayerBanner("Om Kumar", "(1459)", "5:00", userImage = R.drawable.wp)
    }
}

@Composable
fun PlayerBanner(
    playerName: String,
    rating: String,
    timeLeft: String,
    modifier: Modifier = Modifier,
    userImage: Int,
    clockActive: Boolean = false
) {
    Row(
        modifier
            .height(36.dp)
            .fillMaxWidth()
    ) {
        Image(
            painterResource(id = userImage), contentDescription = "Player",
            Modifier
                .fillMaxHeight()
                .aspectRatio(1f, true)
                .background(Color.Gray, RoundedCornerShape(2.dp))
        )
        Text(playerName, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        Text(
            rating,
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 8.dp)
        )
        Box(
            contentAlignment = Alignment.CenterEnd, modifier = Modifier
                .fillMaxSize()
        ) {
            PlayerClock(timeLeft = timeLeft, clockActive = clockActive)
        }
    }
}

@Preview
@Composable
fun PlayerClockPreview() {
    ChessKnightsTheme(darkTheme = true) {
        PlayerClock(timeLeft = "5:00")
    }
}

@Preview
@Composable
fun PlayerClockActivePreview() {
    ChessKnightsTheme(darkTheme = true) {
        PlayerClock(timeLeft = "5:00", clockActive = true)
    }
}

@Composable
fun PlayerClock(modifier: Modifier = Modifier, timeLeft: String, clockActive: Boolean = false) {
    Text(
        timeLeft,
        color = Color.Black,
        textAlign = TextAlign.End,
        fontWeight = FontWeight.Medium,
        modifier = modifier
            .width(64.dp)
            .background(
                if (clockActive) Color.White else Color.Gray,
                remember { RoundedCornerShape(4.dp) })
            .padding(vertical = 4.dp, horizontal = 6.dp)
    )
}