package com.oapps.chessknights.ui.chess

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oapps.chessknights.*
import com.oapps.chessknights.logic.Move
import com.oapps.chessknights.ui.theme.ChessLightColorPalette
import com.oapps.chessknights.ui.theme.LocalChessColor
import kotlinx.coroutines.CoroutineScope


@Preview(showBackground = true)
@Composable
fun PlayableChessPreview() {
    val whiteBottom = remember { mutableStateOf(true) }

    PlayableChessBoard(whiteBottom = whiteBottom)
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

@Composable
fun BoxWithConstraintsScope.ChessBackground(modifier: Modifier = Modifier) {
    val palette = LocalChessColor.current
    val blockSize = with(LocalDensity.current) { maxWidth.toPx() / 8 }
    Canvas(modifier = modifier
        .matchParentSize(), onDraw = {
        fun isWhite(x: Int, y: Int) = (x + y) % 2 == 0

        for (x in 0..7) {
            for (y in 0..7) {
                val color = if (isWhite(x, y)) palette.surfaceWhite else palette.surfaceBlack
                drawRect(
                    color,
                    Offset(x * blockSize, y * blockSize),
                    Size(blockSize, blockSize)
                )
            }
        }
    })
}

@Composable
fun BoxWithConstraintsScope.ChessPiece(
    piece: Piece,
    size: Dp,
    modifier: Modifier = Modifier,
    onDrag: (dragAmount: Offset) -> Unit,
    onDragEnd: () -> Unit,
    onClick: () -> Unit,
    whiteBottom: MutableState<Boolean>
) {
    Image(
        painter = painterResource(id = piece.image),
        contentDescription = piece.name,
        modifier = modifier
            .offset(
                (size * piece.offsetFractionX.value).transformX(
                    whiteBottom.value,
                    maxWidth * 7 / 8
                ),
                (size * piece.offsetFractionY.value).transformY(
                    whiteBottom.value,
                    maxHeight * 7 / 8
                )
            )
            .background(if (piece.selected) Color.Yellow.copy(alpha = 0.5f) else Color.Transparent)
            .size(size, size)
            .pointerInput(piece) {
                detectDragGestures(
                    onDragEnd = onDragEnd
                ) { change, dragAmount ->
                    change.consumeAllChanges()
                    onDrag(
                        Offset(dragAmount.x / size.toPx(), dragAmount.y / size.toPx())
                    )
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick()
            }
    )
}


@Composable
fun BoxWithConstraintsScope.ChessPiecesLayer(
    coroutineScope: CoroutineScope,
    whiteBottom: MutableState<Boolean>
) {
    chess.pieces.sortBy { it.offsetFractionX.isRunning }
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
fun BoxWithConstraintsScope.ChessClickBase(
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
                                ?.let { selectedPiece ->
                                    selectedPiece.selected = false
                                    val size = maxWidth.toPx() / 8
                                    val to = Vec(
                                        (offset.x / size).toInt(),
                                        (offset.y / size).toInt()
                                    )
                                    selectedPiece.moveTo(coroutineScope, to)
                                }
                        }
                    },
                )
            }
    )
}

@Composable
fun PlayableChessBoard(whiteBottom: MutableState<Boolean>, modifier: Modifier = Modifier, showCoordinates: Boolean = false) {
    val coroutineScope = rememberCoroutineScope()
    val shape = remember { RoundedCornerShape(1) }
    var boardModifier = modifier
    if(showCoordinates) boardModifier = boardModifier.padding(start = 12.dp, bottom = 16.dp)
    ChessBox(boardModifier) {
        ChessBackground(Modifier.clip(shape).shadow(16.dp, shape))
        if(showCoordinates) {
            Coordinates(whiteBottom)
        }
        ChessClickBase(coroutineScope, whiteBottom)
        ChessPiecesLayer(coroutineScope, whiteBottom)
    }
}

@Composable
fun BoxWithConstraintsScope.Coordinates(
    whiteBottom: MutableState<Boolean>,
) {
    CoordinatesFile(whiteBottom)
    CoordinatesRank(whiteBottom)
}

@Composable
private fun BoxWithConstraintsScope.CoordinatesRank(whiteBottom: MutableState<Boolean>) {
    (if (whiteBottom.value) ('8' downTo '1') else ('1'..'8')).forEachIndexed { i, rank ->
        Text(
            text = "$rank",
            lineHeight = 12.sp,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Left,
            modifier = Modifier.offset((-12).dp, maxHeight / 8 * (i + 0.5f) - 8.dp)
        )
    }
}

@Composable
private fun BoxWithConstraintsScope.CoordinatesFile(whiteBottom: MutableState<Boolean>) {
    (if (whiteBottom.value) ('a'..'h') else ('h' downTo 'a')).forEachIndexed { i, file ->
        Text(
            text = "$file",
            lineHeight = 12.sp,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.offset(maxWidth / 8 * (i + 0.5f), maxHeight)
        )
    }
}