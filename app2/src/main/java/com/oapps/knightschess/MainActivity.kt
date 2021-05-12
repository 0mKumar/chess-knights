package com.oapps.knightschess

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.oapps.knightschess.ui.chess.Coordinates
import com.oapps.knightschess.ui.chess.DynamicChessBoard
import com.oapps.knightschess.ui.chess.theme.Image
import com.oapps.knightschess.ui.theme.ChessKnightsTheme


class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChessKnightsTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        var images by remember { mutableStateOf<Image>(Image.Staunty) }
                        var name by remember { mutableStateOf(images.type) }
                        var ind by remember { mutableStateOf(0) }
                        var coordinates by remember { mutableStateOf(Coordinates.Inside) }
                        CoordinateSelection{
                            coordinates = it
                        }
                        val whiteBottom = mutableStateOf(true)
                        Button(onClick = { whiteBottom.value = !whiteBottom.value }) {
                            Text(text = "Flip")
                        }
                        DynamicChessBoard(modifier = Modifier.fillMaxWidth(), images = images, coordinates = coordinates, whiteBottom = whiteBottom)
                        Button(onClick = {
                            Log.d(TAG, "onCreate: $ind")
                            ind++
                            Log.d(TAG, "onCreate: total themes = ${Image.Type.values().size}")
                            if(ind >= Image.Type.values().size){
                                ind = 0
                                Log.d(TAG, "onCreate: making 0")
                            }
                            Log.d(TAG, "onCreate: index = $ind")
                            images = Image.from(Image.Type.values()[ind])
                            name = images.type
                        }) {
                            Text(name)
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun DropDownList(
    requestToOpen: Boolean = false,
    list: List<String>,
    request: (Boolean) -> Unit,
    selectedString: (String) -> Unit
) {
    DropdownMenu(
        modifier = Modifier.fillMaxWidth(),
        expanded = requestToOpen,
        onDismissRequest = { request(false) },
    ) {
        list.forEach {
            DropdownMenuItem(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    request(false)
                    selectedString(it)
                }
            ) {
                Text(it, modifier = Modifier.wrapContentWidth(Alignment.Start))
            }
        }
    }
}

@Composable
fun CoordinateSelection(onSelect: (Coordinates) -> Unit) {
    val countryList = listOf(
        "None",
        "Inside",
        "Outside",
    )
    val text = remember { mutableStateOf("Inside") } // initial value
    val isOpen = remember { mutableStateOf(false) } // initial value
    val openCloseOfDropDownList: (Boolean) -> Unit = {
        isOpen.value = it
    }
    val userSelectedString: (String) -> Unit = {
        text.value = it
        val sel = when(it){
            countryList[0] -> Coordinates.None
            countryList[1] -> Coordinates.Inside
            countryList[2] -> Coordinates.Outside
            else -> Coordinates.Inside
        }
        onSelect(sel)
    }
    Box {
        Column {
            OutlinedTextField(
                value = text.value,
                onValueChange = { text.value = it },
                label = { Text(text = "Coordinates") },
                modifier = Modifier.fillMaxWidth()
            )
            DropDownList(
                requestToOpen = isOpen.value,
                list = countryList,
                openCloseOfDropDownList,
                userSelectedString
            )
        }
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Transparent)
                .padding(10.dp)
                .clickable(
                    onClick = { isOpen.value = true }
                )
        )
    }
}