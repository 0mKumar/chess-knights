package com.oapps.chessknights.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oapps.chessknights.R
import com.oapps.chessknights.ui.theme.ChessKnightsTheme

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
        Text(rating, color = Color.Gray, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 8.dp))
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