package com.oapps.chessknights

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.oapps.chessknights.ui.theme.*
import com.oapps.chessknights.ui.theme.LocalChessColor
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
                        val coroutineScope = rememberCoroutineScope()
                        var pos by remember { mutableStateOf(IntOffset(0, 1)) }

                        coroutineScope.launch {
                            while (true) {
                                delay(500)
                                pos = pos.copy(
                                    (pos.x + (-1..1).random()).coerceIn(0..7),
                                    (pos.y + (-1..1).random()).coerceIn(0..7)
                                )
                            }
                        }
                        ChessBox(modifier = Modifier.padding(16.dp)) {
                            ChessBackground(whiteBottom = true)
                            SnappyPiece(
                                coroutineScope,
                                pos = pos,
                                image = R.drawable.wk,
                                onDragEnd = { x, y ->
                                    pos = IntOffset(x, y)
                                })
                        }
                        Text("Footer", Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun BoxWithConstraintsScope.SnappyPiece(
    coroutineScope: CoroutineScope,
    @DrawableRes image: Int,
    pos: IntOffset,
    onDragEnd: ((x: Int, y: Int) -> Unit)? = null
) {
    val blockSize = with(LocalDensity.current) { maxWidth.toPx() / 8 }
    val offsetBound = with(LocalDensity.current) { maxWidth.toPx() * 7 / 8 }
    val offsetX = remember { Animatable(blockSize * pos.x) }
    val offsetY = remember { Animatable(blockSize * pos.y) }
    if (pos.x * blockSize != offsetX.value || pos.y * blockSize != offsetY.value) {
        coroutineScope.launch {
            val toX = pos.x * blockSize
            val toY = pos.y * blockSize
            val x = async { offsetX.animateTo(toX) }
            val y = async { offsetY.animateTo(toY) }
            awaitAll(x, y)
        }
    }
    Image(
        painter = painterResource(id = image), contentDescription = "BB",
        Modifier
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .fillMaxSize(0.125f)
            .pointerInput("drag") {
                detectDragGestures(onDragEnd = {
                    val x = (offsetX.value / blockSize).roundToInt()
                    val y = (offsetY.value / blockSize).roundToInt()
                    onDragEnd?.invoke(x, y)
                }) { change, dragAmount ->
                    change.consumeAllChanges()
                    coroutineScope.launch {
                        offsetX.snapTo((offsetX.value + dragAmount.x).coerceIn(0f, offsetBound))
                        offsetY.snapTo((offsetY.value + dragAmount.y).coerceIn(0f, offsetBound))
                    }
                }
            }
    )

}

@Composable
fun BoxWithConstraintsScope.Drag2DGestures() {
    val offsetX = remember { mutableStateOf(0f) }
    val offsetY = remember { mutableStateOf(0f) }
    val offsetBound = with(LocalDensity.current) { maxWidth.toPx() * 7 / 8 }
    Box(
        Modifier
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .background(Color.Blue)
            .fillMaxSize(0.125f)
            .pointerInput("drag") {
                detectDragGestures(onDragEnd = {
                    Log.d("MainKt", "Drag end")
                }) { change, dragAmount ->
                    change.consumeAllChanges()
                    offsetX.value = (offsetX.value + dragAmount.x)
                        .coerceIn(0f, offsetBound)
                    offsetY.value = (offsetY.value + dragAmount.y)
                        .coerceIn(0f, offsetBound)
                }
            }
    )

}

@Composable
fun BoxWithConstraintsScope.ChessBackground(whiteBottom: Boolean) {
    val palette = LocalChessColor.current
    val blockSize = with(LocalDensity.current) { maxWidth.toPx() / 8 }
    Canvas(modifier = Modifier.fillMaxSize(), onDraw = {
        fun isWhite(x: Int, y: Int) = ((x + y) % 2 == 0) == whiteBottom

        for (x in 0..7) {
            for (y in 0..7) {
                val color = if (isWhite(x, y)) palette.surfaceWhite else palette.surfaceBlack
                drawRect(color, Offset(x * blockSize, y * blockSize), Size(blockSize, blockSize))
            }
        }
    })
}

@Composable
fun ChessBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxWithConstraintsScope.() -> Unit
) {
    val chessColors = ChessLightColorPalette

    val rememberedChessColors = remember {
        chessColors.copy()
    }

    CompositionLocalProvider(LocalChessColor provides rememberedChessColors) {
        Box(
            modifier
                .fillMaxWidth()
                .aspectRatio(1f, false)
        ) {
            BoxWithConstraints(content = content)
        }
    }
}

@Preview(showBackground = false)
@Composable
fun BackgroundPreview() {
    val coroutineScope = rememberCoroutineScope()
    ChessBox {
        ChessBackground(whiteBottom = false)
        SnappyPiece(coroutineScope = coroutineScope, image = R.drawable.bp, IntOffset(0, 0))
    }
}