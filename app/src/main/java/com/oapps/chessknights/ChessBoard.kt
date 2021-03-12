package com.oapps.chessknights

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.PressGestureScope
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.oapps.chessknights.ui.theme.ChessLightColorPalette
import com.oapps.chessknights.ui.theme.LocalChessColor


@Preview
@Composable
fun ChessBackgroundPreview() {
    ChessBox {
        ChessBackground(whiteBottom = false)
        ChessPiece(
            piece = Piece("nf3"),
            size = maxWidth / 8,
            onDrag = {},
            onDragEnd = {},
            onClick = {}
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
fun BoxWithConstraintsScope.ChessBackground(modifier: Modifier = Modifier, whiteBottom: Boolean) {
    val palette = LocalChessColor.current
    val blockSize = with(LocalDensity.current) { maxWidth.toPx() / 8 }
    Canvas(modifier = modifier
        .matchParentSize(), onDraw = {
        fun isWhite(x: Int, y: Int) = ((x + y) % 2 == 0) == whiteBottom

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
fun ChessPiece(
    piece: Piece,
    size: Dp,
    modifier: Modifier = Modifier,
    onDrag: (dragAmount: Offset) -> Unit,
    onDragEnd: () -> Unit,
    onClick: () -> Unit
) {
    Image(
        painter = painterResource(id = piece.image),
        contentDescription = piece.name,
        modifier = modifier
            .offset(size * piece.offsetFractionX.value, size * piece.offsetFractionY.value)
            .size(size, size)
            .pointerInput(piece) {
                detectDragGestures(
                    onDragEnd = onDragEnd
                ) { change, dragAmount ->
                    change.consumeAllChanges()
                    onDrag(Offset(dragAmount.x / size.toPx(), dragAmount.y / size.toPx()))
                }
            }
            .clickable(interactionSource = remember{ MutableInteractionSource() }, indication = null) {
                onClick()
            }
    )
}