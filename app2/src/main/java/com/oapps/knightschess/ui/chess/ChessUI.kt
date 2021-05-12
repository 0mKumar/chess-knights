package com.oapps.knightschess.ui.chess

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import com.oapps.knightschess.R
import com.oapps.knightschess.ui.chess.moveprovider.RandomMoveProvider
import com.oapps.knightschess.ui.chess.theme.Image
import com.oapps.lib.chess.*
import kotlinx.coroutines.delay


private val TAG = "ChessUI"

//@Preview(widthDp = 200)
@Composable
fun StaticChessBoardWhiteBottomPreview() {
    val fens = arrayOf(
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
        "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2",
        "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 2",
        "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
        "8/8/8/4p1K1/2k1P3/8/8/8 b - - 0 1",
        "4k2r/6r1/8/8/8/8/3R4/R3K3 w Qk - 0 1",
        "8/5k2/3p4/1p1Pp2p/pP2Pp1P/P4P1K/8/8 b - - 99 50",
        "r1b1k1nr/p2p1pNp/n2B4/1p1NP2P/6P1/3P1Q2/P1P1K3/q5b1"

    )
    StaticChessBoard(modifier = Modifier.fillMaxWidth(), fen = fens[6], true)
}

//@Preview(widthDp = 200)
@Composable
fun StaticChessBoardBlackBottomPreview() {
    val fens = arrayOf(
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
        "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2",
        "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 2",
        "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
        "8/8/8/4p1K1/2k1P3/8/8/8 b - - 0 1",
        "4k2r/6r1/8/8/8/8/3R4/R3K3 w Qk - 0 1",
        "8/5k2/3p4/1p1Pp2p/pP2Pp1P/P4P1K/8/8 b - - 99 50",
        "r1b1k1nr/p2p1pNp/n2B4/1p1NP2P/6P1/3P1Q2/P1P1K3/q5b1"

    )
    StaticChessBoard(modifier = Modifier.fillMaxWidth(), fen = fens[6], false)
}

@Preview(widthDp = 200)
@Composable
fun DynamicChessBoardPreview() {
    DynamicChessBoard(modifier = Modifier.fillMaxWidth())
}

/**
 * For displaying static fen positions
 */
@Composable
fun StaticChessBoard(modifier: Modifier = Modifier, fen: String, whiteBottom: Boolean = true, coordinates: Coordinates = Coordinates.None) {
    ChessBox(modifier, coordinates = coordinates) {
        ChessBackground(whiteBottom = whiteBottom, coordinates = coordinates)
        FENPiecesLayer(fen = fen, whiteBottom = whiteBottom)
    }
}

@Composable
fun DynamicChessBoard(
    modifier: Modifier = Modifier,
    chess: Chess = remember { Chess(options = Options(defaultPromotion = null)) },
    whiteBottom: MutableState<Boolean> = remember {
        mutableStateOf(true)
    },
    coordinates: Coordinates = Coordinates.Inside,
    images: Image = Image.Staunty,
) {
    // TODO: 4/26/21 account for pre moves

    val coroutineScope = rememberCoroutineScope()
    var fullFen by remember { mutableStateOf(chess.generateFullFen()) }
    val moves = remember { mutableStateListOf<Pair<String, String>>() }

    val gameManager = remember {
        GameManager(
            coroutineScope,
            chess,
            player1 = AutomaticPlayer(true, RandomMoveProvider(), 0),
            player2 = AutomaticPlayer(false, RandomMoveProvider(), 0),
            beforeMakeMove = {
                Log.d(TAG, "DynamicChessBoard: before make move $it")
                val san = MoveValidator.StandardValidator.sanForMove(it)
                if (it.color.isWhite) {
                    moves.add(san to "")
                } else {
                    if (moves.isEmpty()) moves.add("" to san)
                    else {
                        val last = moves.last().first
                        moves.removeAt(moves.size - 1)
                        moves.add(last to san)
                    }
                }
            },
            onMoveComplete = {
                Log.d(TAG, "DynamicChessBoard: after make move $it")
                fullFen = it.chess.generateFullFen()
            }
        )
    }

    val context = LocalContext.current
//    DisposableEffect(Unit) {
//        gameManager.soundManager = SoundManager(context, 3)
//        gameManager.soundManager?.start()
//        gameManager.soundManager?.load(R.raw.move)
//        gameManager.soundManager?.load(R.raw.error)
//        gameManager.soundManager?.load(R.raw.out_of_bound)
//        gameManager.soundManager?.load(R.raw.select)
//        gameManager.soundManager?.load(R.raw.check)
//        gameManager.soundManager?.load(R.raw.capture)
//        onDispose {
//            if (gameManager.soundManager != null) {
//                gameManager.soundManager?.cancel()
//                gameManager.soundManager = null
//            }
//        }
//    }

    Column {
        ChessBox(modifier, coordinates = coordinates) {
            ChessUI(gameManager, whiteBottom, coordinates, images)
        }
        StaticChessBoard(fen = gameManager.fen, modifier = Modifier.fillMaxWidth(0.24f))

        Text(fullFen, Modifier.clickable {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("fen", fullFen))
        })
        Column() {
            for (it in moves) {
                Text(it.first + "\t\t\t" + it.second)
            }
        }
    }

}

@Composable
private fun BoxWithConstraintsScope.ChessUI(
    gameManager: GameManager,
    whiteBottom: MutableState<Boolean>,
    coordinates: Coordinates,
    images: Image,
) {
    val size = maxWidth / 8

    val userInputEvents = remember {
        object : ChessUserInputEvents(gameManager) {
            override fun Offset.toVecFraction(scope: PointerInputScope): Offset {
                return this.div(with(scope) { size.toPx() })
                    .transformDirection(whiteBottom.value)
            }
        }
    }

    ChessBackground(Modifier.pointerInput(Unit) {
        detectTapGestures {
            val inp = it.div(size.toPx())
                .transform(whiteBottom.value, Offset(8f, 8f))
            val x = inp.x.toInt()
            val y = inp.y.toInt()
            userInputEvents.squareTapped(IVec(x, y))
        }
    }, whiteBottom = whiteBottom.value, coordinates = coordinates)

    DynamicPieceLayer(
        gameManager.pieces,
        whiteBottom = whiteBottom.value,
        chessUserInputEvents = userInputEvents,
        image = images
    )


    gameManager.promotionRequest.let { request ->
        if (request is PromotionRequest.Request) {
            RequestPromotionPiece(
                color = request.move.color,
                vecTo = request.move.to,
                onPieceTypeSelected = {
                    // TODO: 4/26/21 request state and account for pre move
                    request.onComplete(it)
                },
                image = images,
                whiteBottom = whiteBottom
            )
        }
    }

    // InteractionLayer(pieces = pieces, whiteBottom = whiteBottom, dragEvent = dragEvent)
    LaunchedEffect(key1 = Unit) {
        delay(1000)
        gameManager.requestNextMove()
    }
}


@Composable
fun BoxWithConstraintsScope.RequestPromotionPiece(
    color: Boolean,
    vecTo: IVec,
    onPieceTypeSelected: (Char) -> Unit,
    image: Image,
    whiteBottom: MutableState<Boolean>
) {
    val size = maxWidth / 8
    val shape = remember { RoundedCornerShape(4.dp) }
    Surface(
        color = Color.White, elevation = 6.dp, shape = shape,
        modifier = Modifier.offset(
            x = (size * vecTo.x).transformX(whiteBottom.value, maxWidth * 7 / 8),
            y = if (color.isBlack xor whiteBottom.value.not())
                (size * 4).transformY(whiteBottom.value, maxHeight)
            else 0.dp
        )
    ) {
        Column {
            "QRBN".let { if (color.isBlack xor whiteBottom.value) it else it.reversed() }.forEach {
                PromotionPieceButton(size, it.ofColor(color.isWhite), image, onPieceTypeSelected)
            }
        }
    }
}

@Composable
private fun PromotionPieceButton(
    size: Dp,
    pieceType: Char,
    image: Image,
    onClick: (Char) -> Unit
) {
    Image(
        painterResource(id = image[pieceType]),
        "",
        Modifier
            .size(size)
            .clickable {
                onClick(pieceType)
            }
    )
}

private val painterResourceForPiece = mapOf(
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

private val pieceName = mapOf(
    'R' to "Rook",
    'N' to "Knight",
    'B' to "Bishop",
    'Q' to "Queen",
    'K' to "King",
    'P' to "Pawn"
)

@Composable
private fun BoxWithConstraintsScope.FENPiecesLayer(fen: String, whiteBottom: Boolean = true) {
    val pieces = remember(fen) { fromFen(fen) }
    val size = maxWidth / 8
    for (entry in pieces) {
        StaticPieceImage(entry.value, size, whiteBottom)
    }
}

@Composable
private fun BoxWithConstraintsScope.DynamicPieceLayer(
    pieces: SnapshotStateList<DynamicPiece2>,
    whiteBottom: Boolean = true,
    chessUserInputEvents: ChessUserInputEvents,
    image: Image,
) {
    val size = maxWidth / 8
    for (piece in pieces) {
        DynamicPieceImage(
            piece,
            size = size,
            whiteBottom,
            chessUserInputEvents = chessUserInputEvents,
            image = image
        )
    }
}

@Composable
private fun StaticPieceImage(piece: Piece, size: Dp, whiteBottom: Boolean = true) {
    Image(
        modifier = Modifier
            .size(size, size)
            .offset(
                size * piece.vec.x.transformX(whiteBottom, 7),
                size * piece.vec.y.transformY(whiteBottom, 7)
            ),
        painter = painterResource(id = painterResourceForPiece[piece.kind] ?: R.drawable.bn),
        contentDescription = pieceName[piece.kind]
    )
}

@Composable
private fun DynamicPieceImage(
    piece: DynamicPiece2,
    size: Dp,
    whiteBottom: Boolean = true,
    chessUserInputEvents: ChessUserInputEvents,
    image: Image,
) {
    Image(
        modifier = Modifier
            .size(size, size)
            .offset(
                size * piece.offset.x.transformX(whiteBottom, 7f),
                size * piece.offset.y.transformY(whiteBottom, 7f)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                chessUserInputEvents.onPieceClicked(piece)
            }
            .pointerInput(piece) {
                detectDragGestures(
                    onDragStart = {
                        chessUserInputEvents.onPieceDragStart(piece, it)
                    },
                    onDragEnd = {
                        chessUserInputEvents.onPieceDragEnd(piece)
                    },
                    onDragCancel = {
                        chessUserInputEvents.onPieceDragEnd(piece)
                    },
                    onDrag = { change, dragAmount ->
                        chessUserInputEvents.onPieceDrag(piece, change, dragAmount, this)
                    }
                )
            },
        painter = painterResource(id = image[piece.kind]),
        contentDescription = pieceName[piece.kind]
    )
}

enum class Coordinates{
    Outside,
    Inside,
    None
}

@Composable
private fun ChessBox(
    modifier: Modifier = Modifier,
    whiteBottom: MutableState<Boolean> = remember { mutableStateOf(true) },
    coordinates: Coordinates = Coordinates.None,
    content: @Composable BoxWithConstraintsScope.() -> Unit
) {
    ConstraintLayout(constraintSet = remember(coordinates) {
        ConstraintSet {
            val chess = createRefFor("chess")
            val files = createRefFor("files")
            val ranks = createRefFor("ranks")

            if(coordinates == Coordinates.Outside) {
                constrain(ranks) {
                    top.linkTo(chess.top)
                    bottom.linkTo(chess.bottom)
                    start.linkTo(parent.start)
                    height = Dimension.fillToConstraints
                }

                constrain(chess) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    start.linkTo(ranks.end)
                    width = Dimension.fillToConstraints
                }

                constrain(files) {
                    top.linkTo(chess.bottom)
                    start.linkTo(chess.start)
                    end.linkTo(chess.end)
                    width = Dimension.fillToConstraints
                }

            } else{
                constrain(chess) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                    width = Dimension.fillToConstraints
                }
            }
        }
    }, modifier) {
        // actual chess box (must be square)
        BoxWithConstraints(
            Modifier
                .layoutId("chess")
                .aspectRatio(1f, false)
                .onGloballyPositioned {
                    Log.d(TAG, "ChessBox: chess is at ${it.positionInParent()}")
                    Log.d(TAG, "ChessBox: size = ${it.size}")
                },
            content = content
        )
        if(coordinates == Coordinates.Outside) {
            BoxWithConstraints(
                Modifier
                    .layoutId("files")
                    .onGloballyPositioned {
                        Log.d(TAG, "ChessBox: files is at ${it.positionInParent()}")
                        Log.d(TAG, "ChessBox: size = ${it.size}")
                    }
            ) {
                CoordinatesFile(whiteBottom = whiteBottom)
            }
            BoxWithConstraints(
                Modifier
                    .layoutId("ranks")
                    .onGloballyPositioned {
                        Log.d(TAG, "ChessBox: rank is at ${it.positionInParent()}")
                        Log.d(TAG, "ChessBox: size = ${it.size}")
                    }
            ) {
                CoordinatesRank(whiteBottom = whiteBottom)
            }
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.ChessBackground(modifier: Modifier = Modifier, whiteBottom: Boolean, coordinates: Coordinates) {
    val blockSize = with(LocalDensity.current) { maxWidth.toPx() / 8 }
    val shape = remember { RoundedCornerShape(1) }
    fun isWhite(x: Int, y: Int) = (x + y) % 2 == 0
    val colors =  Color(0xFFF3F6FA) to Color(0xFF6472C0)
    Surface(modifier.matchParentSize(), shape, elevation = 4.dp) {
        BoxWithConstraints(Modifier.matchParentSize()){
            Canvas(modifier = Modifier.matchParentSize()) {
                for (x in 0..7) {
                    for (y in 0..7) {
//                    val color = if (isWhite(x, y)) Color(0xFFD9D9FA) else Color(0xFF4949F5)
                        val color = if (isWhite(x, y)) colors.first else colors.second
                        drawRect(
                            color,
                            Offset(x * blockSize, y * blockSize),
                            Size(blockSize, blockSize)
                        )
                    }
                }
            }
            if(coordinates == Coordinates.Inside){
                InsideCoordinates(whiteBottom = whiteBottom, colors)
            }
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.InsideCoordinates(whiteBottom: Boolean, colors: Pair<Color, Color>){
    val fontSize = (maxWidth.value.sp / 36).let { if(it < 10.sp) 10.sp else it }

    (if (whiteBottom) ('8' downTo '1') else ('1'..'8')).forEachIndexed { i, rank ->
        Text(
            text = "$rank",
            lineHeight = fontSize,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = if(i % 2 == 0) colors.second else colors.first,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .offset(0.dp, maxHeight / 8 * i)
                .padding(start = 2.dp, top = 2.dp)
        )
    }

    (if (whiteBottom) ('a'..'h') else ('h' downTo 'a')).forEachIndexed { i, file ->
        Text(
            text = "$file",
            lineHeight = fontSize,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            color = if(i % 2 == 1) colors.second else colors.first,
            modifier = Modifier
                .width(maxWidth / 8)
                .offset(maxWidth / 8 * i, maxHeight - with(LocalDensity.current){fontSize.toDp()} - 4.dp)
                .padding(end = 4.dp)
        )
    }
}

@Composable
private fun BoxWithConstraintsScope.CoordinatesRank(whiteBottom: MutableState<Boolean>) {
    val fontSize = (maxHeight.value.sp / 36).let { if(it < 10.sp) 10.sp else it }
    (if (whiteBottom.value) ('8' downTo '1') else ('1'..'8')).forEachIndexed { i, rank ->
        Text(
            text = "$rank",
            lineHeight = fontSize,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .offset(0.dp, maxHeight / 8 * (i + 0.5f) - 8.dp)
                .padding(end = 4.dp)
        )
    }
}

@Composable
private fun BoxWithConstraintsScope.CoordinatesFile(whiteBottom: MutableState<Boolean>) {
    val fontSize = (maxWidth.value.sp / 36).let { if(it < 10.sp) 10.sp else it }
    (if (whiteBottom.value) ('a'..'h') else ('h' downTo 'a')).forEachIndexed { i, file ->
        Text(
            text = "$file",
            lineHeight = fontSize,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(maxWidth / 8)
                .offset(maxWidth / 8 * i, 0.dp)
        )
    }
}
//
//@Composable
//fun BoxWithConstraintsScope.InteractionLayer(
//    modifier: Modifier = Modifier,
//    pieces: SnapshotStateList<DynamicPiece>,
//    whiteBottom: MutableState<Boolean>,
//    dragEvent: DragEvent
//) {
//    var draggedPiece: DynamicPiece? = remember { null }
//    val size = maxWidth / 8
//
//    var rect by remember {
//        mutableStateOf(Rect.Zero)
//    }
//
//    Box(
//        Modifier
//            .offset(
//                size * rect.left.transformX(whiteBottom.value, 7f),
//                size * rect.top.transformY(whiteBottom.value, 7f)
//            )
//            .background(Color.Red.copy(0.4f))
//            .size(size, size)
//    )
//
//    var raw by remember { mutableStateOf(Offset.Zero) }
//    Box(
//        Modifier
//            .offset {
//                IntOffset(raw.x.toInt(), raw.y.toInt())
//            }
//            .background(Color.Green)
//            .size(2.dp, 2.dp)
//    )
//    Box(
//        modifier
//            .matchParentSize()
//            .pointerInput(Unit) {
//                detectDragGestures(
//                    onDragStart = { it ->
//                        raw = it
//
//                        rect = Rect(
//                            center = it
//                                .div(size.toPx())
//                                .minus(Offset(0.5f, 0.5f))
//                                .transform(whiteBottom.value, Offset(7f, 7f)),
//                            radius = 0.5f
//                        )
//
//                        Log.d(TAG, "InteractionLayer: onDragStart ${rect.center} $rect")
//                        draggedPiece = pieces.find { rect.contains(it.offset) }
//                        draggedPiece?.startDrag()
//                        Log.d(TAG, "InteractionLayer: $draggedPiece")
//                    },
//                    onDragEnd = {
//                        draggedPiece?.stopDrag()
//                        draggedPiece = null
//                        Log.d(TAG, "InteractionLayer: onDragEnd")
//                    },
//                    onDragCancel = {
//                        draggedPiece?.stopDrag()
//                        draggedPiece = null
//                        Log.d(TAG, "InteractionLayer: onDragCancel")
//                    },
//                    onDrag = { change, dragAmount ->
//                        Log.d(
//                            TAG,
//                            "InteractionLayer: millis ${change.previousPosition} ${change.position} $change."
//                        )
//
//                        change.consumeAllChanges()
//                        val drag = dragAmount
//                            .div(size.toPx())
//                            .transformDirection(whiteBottom.value)
//                        draggedPiece?.dragOffset?.let {
//                            Log.d(TAG, "InteractionLayer: drag by $drag")
//                            Log.d(TAG, "InteractionLayer: bef ${draggedPiece?.offset}")
//                            draggedPiece?.dragOffset = it + drag
//                            Log.d(TAG, "InteractionLayer: aft ${draggedPiece?.offset}")
//                        }
//                    }
//                )
////                detectTapGestures {
////                    Log.d(TAG, "InteractionLayer: tapped at $it")
////                }
//            }
////            .clickable {
////                Log.d(TAG, "InteractionLayer: clicked")
////            }
//    )
//}