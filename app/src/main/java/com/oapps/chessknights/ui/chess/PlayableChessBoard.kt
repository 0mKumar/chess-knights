package com.oapps.chessknights.ui.chess

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.zIndex
import com.oapps.chessknights.*
import com.oapps.chessknights.R
import com.oapps.chessknights.logic.Chess
import com.oapps.chessknights.logic.Move
import com.oapps.chessknights.logic.MoveValidator
import com.oapps.chessknights.ui.live.PlayerBanner
import com.oapps.chessknights.ui.theme.ChessKnightsTheme
import com.oapps.chessknights.ui.theme.ChessLightColorPalette
import com.oapps.chessknights.ui.theme.LocalChessColor
import kotlin.math.roundToInt

var tiles = mutableStateMapOf<Vec, Tile>()

@Preview(showBackground = true)
@Composable
fun PlayableChessPreview() {
    val whiteBottom = remember { mutableStateOf(true) }

    ChessKnightsTheme {
        Column {
            PlayerBanner(
                "Opponent",
                "(1579)",
                "4:45",
                Modifier.padding(bottom = 16.dp),
                R.drawable.wp
            )
            PlayableChessBoard(whiteBottom = whiteBottom, showCoordinates = true)
            PlayerBanner("Myself", "(3200)", "5:38", Modifier.padding(top = 16.dp), R.drawable.wp)
        }
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
    val shape = remember { RoundedCornerShape(1) }
    Surface(modifier.matchParentSize(), shape, elevation = 4.dp) {
        Canvas(modifier = Modifier
            .matchParentSize(), onDraw = {
            fun isWhite(x: Int, y: Int) = (x + y) % 2 == 0

            for (x in 0..7) {
                for (y in 0..7) {
                    val color = if (isWhite(x, y)) palette.surfaceLight else palette.surfaceDark
                    drawRect(
                        color,
                        Offset(x * blockSize, y * blockSize),
                        Size(blockSize, blockSize)
                    )
                }
            }
        })
    }
}

@Composable
private fun BoxWithConstraintsScope.TileHighlightLayer(whiteBottom: MutableState<Boolean>){
    tiles.values.forEach{
        TileBackgroundDecoration(tile = it, size = maxWidth / 8, whiteBottom = whiteBottom)
    }
}

@Composable
private fun BoxWithConstraintsScope.TileBackgroundDecoration(
    modifier: Modifier = Modifier,
    tile: Tile,
    size: Dp,
    whiteBottom: MutableState<Boolean>
){
    val palette = LocalChessColor.current
    fun isWhite(x: Int, y: Int) = (x + y) % 2 == 0

    val color = remember(tile.flags) {
        if(isWhite(tile.vec.x, tile.vec.y)) {
            when {
                tile.contains(Tile.PIECE_SELECTED) -> palette.tileBackgroundPieceSelectedLight
                tile.contains(Tile.TILE_HIGHLIGHT) -> palette.tileBackgroundHighlightLight
                else -> Color.Transparent
            }
        }else{
            when {
                tile.contains(Tile.PIECE_SELECTED) -> palette.tileBackgroundPieceSelectedDark
                tile.contains(Tile.TILE_HIGHLIGHT) -> palette.tileBackgroundHighlightDark
                else -> Color.Transparent
            }
        }
    }
    Box(modifier = modifier
        .offset(
            (size * tile.vec.x).transformX(
                whiteBottom.value,
                maxWidth * 7 / 8
            ),
            (size * tile.vec.y).transformY(
                whiteBottom.value,
                maxHeight * 7 / 8
            )
        )
        .size(size, size)
        .background(color)
    )
}

@Composable
fun BoxWithConstraintsScope.ChessPiece(
    piece: Piece,
    size: Dp,
    modifier: Modifier = Modifier,
    onDrag: (dragAmount: Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragStart: () -> Unit,
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
//            .background(if (piece.selected) Color.Yellow.copy(alpha = 0.5f) else Color.Transparent)
            .size(size, size)
            .pointerInput(piece) {
                detectDragGestures(
                    onDragStart = { onDragStart() },
                    onDragEnd = onDragEnd
                ) { change, dragAmount ->
                    change.consumeAllChanges()
                    onDrag(
                        Offset(dragAmount.x / size.toPx(), dragAmount.y / size.toPx())
                    )
                }
            }
            .zIndex(if (piece.selected) 8f.also {
                Log.d(TAG, "ChessPiece: $piece is high")} else 0f)
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
    whiteBottom: MutableState<Boolean>,
    uiActions: ChessUIActions
) {
//    chess.pieces.sortBy { it.offsetFractionX.isRunning }

    chess.pieces.forEach { piece ->
        ChessPiece(
            piece = piece,
            size = maxWidth / 8,
            onDrag = {
                uiActions.onPieceDrag(piece, it.transformDirection(whiteBottom.value))
            },
            onDragStart = {
                uiActions.onPieceDragStart(piece)
            },
            onDragEnd = {
                uiActions.onPieceDragEnd(piece)
            }, onClick = {
                uiActions.onSquareTapped(piece.vec, piece)
            },
            whiteBottom = whiteBottom
        )
    }
}


@Composable
fun BoxWithConstraintsScope.ChessClickBase(
    whiteBottom: MutableState<Boolean>,
    uiActions: ChessUIActions
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        val offset =
                            it.transform(
                                whiteBottom.value,
                                Offset(maxWidth.toPx(), maxHeight.toPx())
                            )
                        if (tryAwaitRelease()) {
                            val size = maxWidth.toPx() / 8
                            val tapLocation = Vec(
                                (offset.x / size).toInt(),
                                (offset.y / size).toInt()
                            )
                            uiActions.onSquareTapped(tapLocation)
                        }
                    },
                )
            }
    )
}

@Composable
fun PlayableChessBoard(
    whiteBottom: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    showCoordinates: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()
    var boardModifier = modifier
    if (showCoordinates) boardModifier = boardModifier.padding(start = 12.dp, bottom = 16.dp)
    val requestPromotesTo = remember { mutableStateOf(Pair(false, Move(chess, "a1a1"))) }
    val requestPromotionTo: (Move) -> Unit = { it: Move ->
        requestPromotesTo.value = Pair(true, it)
        Log.d(TAG, "PlayableChessBoard: got request to show dialog")
    }

    fun movePieceToRequest(piece: Piece, to: Vec){
        if((chess.state.activeColor == Chess.Color.BLACK) == piece.isBlack()) {
            piece.moveTo(coroutineScope, to, requestPromotionTo = requestPromotionTo, onComplete = {
                chess.state.update(it)
            })
        }else{
            piece.moveTo(coroutineScope, piece.vec)
        }
    }

    val uiActions = object: ChessUIActions() {
        override fun onSquareTapped(tappedVec: Vec, piece: Piece?) {
            if(piece != null) {
                if (piece.selected) {
                    deselectPiece(piece)
                } else {
                    val selectedPiece = chess.pieces.find { it.selected }
                    if (selectedPiece != null) {
                        deselectPiece(selectedPiece)
                        movePieceToRequest(selectedPiece, piece.vec)
                    } else {
                        val validMoves = MoveValidator.validMoves(chess, piece)
                        Log.d(TAG, "${validMoves.size} moves for $piece")
                        Log.d(TAG, validMoves.toString())
                        selectPiece(piece)
                    }
                }
            }else{
                chess.pieces
                    .find { it.selected }
                    ?.let { selectedPiece ->
                        deselectPiece(selectedPiece)
                        movePieceToRequest(selectedPiece, tappedVec)
                    }
            }
        }

        private fun selectPiece(piece: Piece) {
            if((chess.state.activeColor == Chess.Color.BLACK) != piece.isBlack()) {
                return
            }
            piece.selected = true
            tiles[piece.vec] =
                (tiles[piece.vec] ?: Tile(piece.vec)).add(Tile.PIECE_SELECTED)

            MoveValidator.validMoves(chess, piece).forEach{
                tiles[it.to] =
                    (tiles[it.to] ?: Tile(it.to)).add(Tile.TILE_HIGHLIGHT)
            }
        }

        private fun deselectPiece(piece: Piece) {
            piece.selected = false
            tiles[piece.vec] =
                (tiles[piece.vec] ?: Tile(piece.vec)).remove(Tile.PIECE_SELECTED)
            tiles.values.forEach{
                it.remove(Tile.TILE_HIGHLIGHT)
            }
        }

        override fun onPieceDragStart(piece: Piece) {
            if((chess.state.activeColor == Chess.Color.BLACK) == piece.isBlack())
                selectPiece(piece)
        }

        override fun onPieceDragEnd(piece: Piece) {
            deselectPiece(piece)
            movePieceToRequest(piece, Vec(piece.offsetFractionX.value.roundToInt(), piece.offsetFractionY.value.roundToInt()))
//            piece.snap(coroutineScope, requestPromotionTo = requestPromotionTo)
        }

        override fun onPieceDrag(piece: Piece, fractionalOffset: Offset) {
            piece.dragBy(coroutineScope, fractionalOffset)
        }
    }

    ChessBox(boardModifier) {
        ChessBackground()
        if (showCoordinates) {
            Coordinates(whiteBottom)
        }
        TileHighlightLayer(whiteBottom = whiteBottom)
        ChessClickBase(
            whiteBottom = whiteBottom,
            uiActions = uiActions
        )
        ChessPiecesLayer(whiteBottom, uiActions = uiActions)
        if (requestPromotesTo.value.first) {
            RequestPromotionPiece(requestPromotesTo, whiteBottom)
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.RequestPromotionPiece(requestPromotesTo: MutableState<Pair<Boolean, Move>>, whiteBottom: MutableState<Boolean>) {
    val onClick: (Char) -> Unit = {
        requestPromotesTo.value = Pair(false, requestPromotesTo.value.second)
        requestPromotesTo.value.second.promotesTo = it
    }
    val size = maxWidth / 8
    val white = requestPromotesTo.value.second.piece.isWhite()

    val shape = remember { RoundedCornerShape(4.dp) }
    val piece = requestPromotesTo.value.second.piece
    Surface(color = Color.White, elevation = 4.dp, shape = shape,
        modifier = Modifier.offset(
            x = (size * piece.vec.x).transformX(whiteBottom.value, maxWidth * 7 / 8),
            y = if(piece.isBlack() xor whiteBottom.value.not()) (size * 4).transformY(whiteBottom.value, maxHeight) else 0.dp
        )){
        Column{
            "QRBN".let { if(piece.isBlack() xor whiteBottom.value) it else it.reversed() }.forEach{
                PromotionPieceButton(size, it.ofColor(white), onClick = onClick)
            }
        }
    }
}

@Composable
private fun PromotionPieceButton(
    size: Dp,
    pieceType: Char,
    onClick: (Char) -> Unit
) {
    Image(
        painterResource(id = Piece.drawableImageResources[pieceType] ?: R.drawable.bn),
        "",
        Modifier
            .size(size)
            .clickable {
                onClick(pieceType)
            }
    )
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
            modifier = Modifier.offset(maxWidth / 8 * (i + 0.5f), maxHeight + 2.dp)
        )
    }
}