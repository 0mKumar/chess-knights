package com.oapps.chessknights.ui.chess

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.oapps.chessknights.transformX
import com.oapps.chessknights.transformY
import com.oapps.chessknights.ui.theme.ChessLightColorPalette
import com.oapps.chessknights.ui.theme.LocalChessColor


@Preview
@Composable
fun ChessBackgroundPreview() {
    val whiteBottom = true
    ChessBox {
        ChessBackground()
        ChessPiece(
            piece = Piece("na1"),
            size = maxWidth / 8,
            onDrag = {},
            onDragEnd = {},
            onClick = {},
            whiteBottom = mutableStateOf(true)
        )
    }
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
                (size * piece.offsetFractionX.value).transformX(whiteBottom.value, maxWidth * 7 / 8),
                (size * piece.offsetFractionY.value).transformY(whiteBottom.value, maxHeight * 7 / 8)
            )
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