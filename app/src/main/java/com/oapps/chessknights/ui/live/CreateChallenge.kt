package com.oapps.chessknights.ui.live

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.oapps.chessknights.TAG
import com.oapps.chessknights.models.TimeControl
import com.oapps.chessknights.ui.chess.LiveGameViewModel
import kotlinx.coroutines.launch

@Composable
fun CreateChallenge(liveGameViewModel: LiveGameViewModel) {
    val timeControls = remember {
        listOf(
            "15+10", "15+5", "15+2", "15+0",
            "10+5", "10+3", "10+1", "10+0",
            "5+5", "5+3", "5+1", "5+0",
            "3+5", "3+2", "3+1", "3+0"
        )
            .map { TimeControl(it) }
            .chunked(4)
    }

    var selectedTimeControl by remember { mutableStateOf(timeControls[1][3]) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Divider(
            Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colors.secondary.copy(alpha = 0.6f)
        )
        Text(
            "Time Control",
            Modifier.padding(horizontal = 16.dp),
            style = TextStyle(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colors.primary.copy(alpha = 0.8f)
        )
        Divider(
            Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colors.secondary.copy(alpha = 0.6f)
        )
    }
    Column {
        timeControls.forEach { items ->
            Row(
                Modifier
                    .fillMaxWidth(1f)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                items.forEach {
                    if (selectedTimeControl == it) {
                        Button(
                            onClick = { selectedTimeControl = it },
                            modifier = Modifier
                                .height(40.dp)
                                .aspectRatio(2f, true),
                        ) {
                            Text(it.notation())
                        }
                    } else {
                        OutlinedButton(
                            onClick = { selectedTimeControl = it }, modifier = Modifier
                                .height(40.dp)
                                .aspectRatio(2f, true),
                            border = BorderStroke(1.dp, MaterialTheme.colors.secondary)
                        ) {
                            Text(it.notation())
                        }
                    }
                }
            }
        }
    }
    Text(text = "${selectedTimeControl.timeMinutes()} minute ${selectedTimeControl.type()} ${if (selectedTimeControl.hasIncrement()) "with ${selectedTimeControl.incrementSeconds()} second increment" else "without increment"}",
     fontWeight = FontWeight.Medium)

    Button(onClick = {
        Firebase.auth.currentUser?.let{
            liveGameViewModel.viewModelScope.launch {
                val challengeId = liveGameViewModel.createChallenge(it.uid, selectedTimeControl)
                Log.d(TAG, "CreateChallenge: challenge created with key = $challengeId")
            }
        }
    }) {
        Text("Create challenge")
    }
}