package com.oapps.chessknights

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
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

data class Vec(var x: Int = 0, var y: Int = 0) {
    constructor(notation: String) : this(notation[0] - 'a', notation[1] - '1')
}

data class Piece(
    val vec: MutableState<Vec> = mutableStateOf(Vec()),
    val kind: MutableState<Char> = mutableStateOf('p')
) {
    companion object {
        val drawableImageResources = mapOf(
            'R' to R.drawable.wr,
            'N' to R.drawable.wn,
            'B' to R.drawable.wb,
            'Q' to R.drawable.wq,
            'K' to R.drawable.wk,
            'P' to R.drawable.wp,
            'r' to R.drawable.br,
            'n' to R.drawable.bn,
            'b' to R.drawable.bb,
            'q' to R.drawable.bq,
            'k' to R.drawable.bk,
            'p' to R.drawable.bp,
        )
    }

    val image: Int
        get() = drawableImageResources[kind.value] ?: R.drawable.bn

    constructor(notation: String) : this() {
        vec.value = Vec(notation.substring(1))
        kind.value = notation[0]
    }

    fun isWhite() = kind.value.isUpperCase()
    fun isBlack() = kind.value.isLowerCase()
}

val pieces = listOf(
    listOf("Ra1", "Nb1", "Bc1", "Qd1", "Ke1", "Bf1", "Ng1", "Rh1"),
    List(8) { "P${'a' + it}2" },
    List(8) { "p${'a' + it}7" },
    listOf("ra8", "nb8", "bc8", "qd8", "ke8", "bf8", "ng8", "rh8")
).flatten().map { Piece(it) }.let { pieces ->
    val list = mutableStateListOf<Piece>()
    pieces.forEach {
        list.add(it)
    }
    return@let list
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChessKnightsTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Column {
                        Text(text = "Header", Modifier.padding(16.dp))
                        val coroutineScope = rememberCoroutineScope()

                        coroutineScope.launch {
                            while (true) {
                                delay(500)
                                pieces.random().let { piece ->
                                    val vec = Vec(
                                        (piece.vec.value.x + (-1..1).random()).coerceIn(0..7),
                                        (piece.vec.value.y + (-1..1).random()).coerceIn(0..7)
                                    )
                                    if(!pieces.any { it.vec.value == vec })
                                        piece.vec.value = vec
                                }
                            }
                        }
                        ChessBox(modifier = Modifier.padding(16.dp)) {
                            ChessBackground(whiteBottom = true)
                            pieces.forEach {
                                SnappyPiece(
                                    coroutineScope,
                                    pos = it.vec.value,
                                    image = it.image,
                                    onDragEnd = { x, y ->
                                        it.vec.value = Vec(x, y)
                                    })
                            }
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
    modifier: Modifier = Modifier,
    pos: Vec = Vec(),
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
        modifier = modifier
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
        SnappyPiece(coroutineScope = coroutineScope, image = R.drawable.bp, pos = Vec(0, 0))
    }
}