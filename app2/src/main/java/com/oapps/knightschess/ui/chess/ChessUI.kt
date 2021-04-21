package com.oapps.knightschess.ui.chess

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.google.accompanist.coil.CoilImage
import com.oapps.audio.SoundManager
import com.oapps.knightschess.R
import com.oapps.lib.chess.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.Math.cbrt
import java.lang.Math.random
import kotlin.math.roundToInt

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
fun StaticChessBoard(modifier: Modifier = Modifier, fen: String, whiteBottom: Boolean = true) {
    ChessBox(modifier) {
        ChessBackground()
        FENPiecesLayer(fen = fen, whiteBottom = whiteBottom)
    }
}

@Composable
fun DynamicChessBoard(
    modifier: Modifier = Modifier,
    chess: Chess = remember { Chess() },
    whiteBottom: MutableState<Boolean> = remember {
        mutableStateOf(true)
    },
    soundManager: SoundManager? = null
) {
    val pieces = remember {
        chess.pieces
            .values
            .map { DynamicPiece(it) }
            .toMutableStateList()
    }

    LaunchedEffect(true) {
        while (false) {
            delay(1000)

            pieces.random().animateTo(IVec((0..7).random(), (0..7).random()))
//            pieces.random().kind = "PKQRN".random().ofColor(random() < 0.5)
        }
    }
    var fen by remember {
        mutableStateOf(chess.generateFen())
    }

    Column {

        ChessBox(modifier) {
            val size = maxWidth / 8

            val dragEvent = remember {
                object : DragEvent() {
                    override fun onPieceDragStart(piece: DynamicPiece, offset: Offset) {
                        Log.d(TAG, "onPieceDragStart: ")
                        draggedPiece = piece
                        piece.startDrag()
                    }

                    override fun onPieceDragEnd(piece: DynamicPiece) {
                        Log.d(TAG, "onPieceDragEnd: ")
                        draggedPiece = null
                        piece.stopDrag()
                        val droppedTo = IVec(
                            piece.dragOffset.x.roundToInt(),
                            piece.dragOffset.y.roundToInt()
                        )
                        val move = Move(chess, Piece(piece.vec, piece.kind), droppedTo)
                        if (move.isValid()) {
                            chess.makeMove(move)
                            fen = chess.generateFen()
                            move.isAttack { attackedPiece ->
                                pieces.find {
                                    it.vec == attackedPiece.vec && it.kind == attackedPiece.kind
                                }?.let {
                                    pieces.remove(it)
                                }

                            }
                            piece.animateTo(move.to, playSound = true)
                            move.isCastling { rook, to, _ ->
                                pieces.find { it.vec == rook.vec && it.kind == rook.kind }
                                    ?.animateTo(to)
                            }
                            move.isPromotion {
                                piece.kind = it ?: 'Q'.ofColor(piece.kind.color)
                            }
                        } else {
                            piece.animateTo(piece.vec)
                        }
                    }

                    override fun onPieceDrag(
                        piece: DynamicPiece,
                        change: PointerInputChange,
                        dragAmount: Offset,
                        scope: PointerInputScope
                    ) {
                        Log.d(TAG, "onPieceDrag: ")
                        change.consumeAllChanges()
                        val drag = dragAmount
                            .div(with(scope) { size.toPx() })
                            .transformDirection(whiteBottom.value)
                        piece.dragOffset += drag
                    }
                }
            }
            ChessBackground()
            DynamicPieceLayer(
                pieces,
                whiteBottom = whiteBottom.value,
                dragEvent = dragEvent
            ) { it, cancelled ->
                Log.d(TAG, "DynamicChessBoard: animated $it, cancelled = $cancelled")
                if (!cancelled) {

                }
                if (it.playSoundAtEnd && it.vec != it.lastVec) {
                    soundManager?.play(R.raw.move)
                    it.soundPlayed()
                }
            }
//        InteractionLayer(pieces = pieces, whiteBottom = whiteBottom, dragEvent = dragEvent)
        }
        StaticChessBoard(fen = fen, modifier = Modifier.fillMaxWidth(0.5f))

    }

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
    pieces: SnapshotStateList<DynamicPiece>,
    whiteBottom: Boolean = true,
    dragEvent: DragEvent,
    onFinishAnimation: ((DynamicPiece, cancelled: Boolean) -> Unit)? = null
) {
    val TAG = "ChessUI"

    val size = maxWidth / 8
    for (piece in pieces) {
        DynamicPieceImage(
            piece,
            size = size,
            whiteBottom,
            onFinishAnimation = onFinishAnimation,
            dragEvent = dragEvent
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
    piece: DynamicPiece,
    size: Dp,
    whiteBottom: Boolean = true,
    dragEvent: DragEvent,
    onFinishAnimation: ((DynamicPiece, cancelled: Boolean) -> Unit)? = null,
) {
    piece.offset = animatedPieceOffset(piece = piece) {
        onFinishAnimation?.invoke(piece, it)
        Log.d(TAG, "DynamicPieceImage: animation complete")
    }

    Image(
        modifier = Modifier
            .size(size, size)
            .offset(
                size * piece.offset.x.transformX(whiteBottom, 7f),
                size * piece.offset.y.transformY(whiteBottom, 7f)
            )
            .pointerInput(piece) {
                detectDragGestures(
                    onDragStart = {
                        dragEvent.onPieceDragStart(piece, it)
                    },
                    onDragEnd = {
                        dragEvent.onPieceDragEnd(piece)
                    },
                    onDragCancel = {
                        dragEvent.onPieceDragEnd(piece)
                    },
                    onDrag = { change, dragAmount ->
                        dragEvent.onPieceDrag(piece, change, dragAmount, this)
                    }
                )
            },
        painter = painterResource(id = painterResourceForPiece[piece.kind] ?: R.drawable.bn),
        contentDescription = pieceName[piece.kind]
    )
}

@Composable
fun animatedPieceOffset(
    piece: DynamicPiece,
    onComplete: ((cancelled: Boolean) -> Unit)? = null
): Offset {

    val target = piece.vec

    var running by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(piece.dragOffset) {
        Log.d(TAG, "animatedPieceOffset: new drag = ${piece.dragOffset}")
        piece.animation.snapTo(piece.dragOffset)
        running = false
    }

    LaunchedEffect(target, piece.dragging) {
        if(!piece.animate) return@LaunchedEffect
        if (piece.dragging) return@LaunchedEffect


        val targetOffset = target.toOffset()

        if (running) {
            onComplete?.invoke(true)
        }

        running = true

        piece.animate = false

        val res = piece.animation.animateTo(
            targetValue = targetOffset,
            animationSpec = tween(
                250 * cbrt(
                    (targetOffset - piece.animation.value).getDistance()
                        .toDouble()
                ).toInt()
            ),
        )
        onComplete?.invoke(false)
        running = false
    }
    return piece.animation.value
}

@Composable
private fun ChessBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxWithConstraintsScope.() -> Unit
) {
    BoxWithConstraints(modifier.aspectRatio(1f, false), content = content)
}

@Composable
private fun BoxWithConstraintsScope.ChessBackground(modifier: Modifier = Modifier) {
    val blockSize = with(LocalDensity.current) { maxWidth.toPx() / 8 }
    val shape = remember { RoundedCornerShape(1) }
    fun isWhite(x: Int, y: Int) = (x + y) % 2 == 0
    Surface(modifier.matchParentSize(), shape, elevation = 4.dp) {
        Canvas(modifier = Modifier.matchParentSize()) {
            for (x in 0..7) {
                for (y in 0..7) {
//                    val color = if (isWhite(x, y)) Color(0xFFD9D9FA) else Color(0xFF4949F5)
                    val color = if (isWhite(x, y)) Color(0xFFF3F6FA) else Color(0xFF5C54D3)
                    drawRect(
                        color,
                        Offset(x * blockSize, y * blockSize),
                        Size(blockSize, blockSize)
                    )
                }
            }
        }
    }
}

@Composable
fun BoxWithConstraintsScope.InteractionLayer(
    modifier: Modifier = Modifier,
    pieces: SnapshotStateList<DynamicPiece>,
    whiteBottom: MutableState<Boolean>,
    dragEvent: DragEvent
) {
    var draggedPiece: DynamicPiece? = remember { null }
    val size = maxWidth / 8

    var rect by remember {
        mutableStateOf(Rect.Zero)
    }

    Box(
        Modifier
            .offset(
                size * rect.left.transformX(whiteBottom.value, 7f),
                size * rect.top.transformY(whiteBottom.value, 7f)
            )
            .background(Color.Red.copy(0.4f))
            .size(size, size)
    )

    var raw by remember { mutableStateOf(Offset.Zero) }
    Box(
        Modifier
            .offset {
                IntOffset(raw.x.toInt(), raw.y.toInt())
            }
            .background(Color.Green)
            .size(2.dp, 2.dp)
    )
    Box(
        modifier
            .matchParentSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { it ->
                        raw = it

                        rect = Rect(
                            center = it
                                .div(size.toPx())
                                .minus(Offset(0.5f, 0.5f))
                                .transform(whiteBottom.value, Offset(7f, 7f)),
                            radius = 0.5f
                        )

                        Log.d(TAG, "InteractionLayer: onDragStart ${rect.center} $rect")
                        draggedPiece = pieces.find { rect.contains(it.offset) }
                        draggedPiece?.startDrag()
                        Log.d(TAG, "InteractionLayer: $draggedPiece")
                    },
                    onDragEnd = {
                        draggedPiece?.stopDrag()
                        draggedPiece = null
                        Log.d(TAG, "InteractionLayer: onDragEnd")
                    },
                    onDragCancel = {
                        draggedPiece?.stopDrag()
                        draggedPiece = null
                        Log.d(TAG, "InteractionLayer: onDragCancel")
                    },
                    onDrag = { change, dragAmount ->
                        Log.d(
                            TAG,
                            "InteractionLayer: millis ${change.previousPosition} ${change.position} $change."
                        )

                        change.consumeAllChanges()
                        val drag = dragAmount
                            .div(size.toPx())
                            .transformDirection(whiteBottom.value)
                        draggedPiece?.dragOffset?.let {
                            Log.d(TAG, "InteractionLayer: drag by $drag")
                            Log.d(TAG, "InteractionLayer: bef ${draggedPiece?.offset}")
                            draggedPiece?.dragOffset = it + drag
                            Log.d(TAG, "InteractionLayer: aft ${draggedPiece?.offset}")
                        }
                    }
                )
//                detectTapGestures {
//                    Log.d(TAG, "InteractionLayer: tapped at $it")
//                }
            }
//            .clickable {
//                Log.d(TAG, "InteractionLayer: clicked")
//            }
    )
}

open class DragEvent {
    var draggedPiece: DynamicPiece? = null
    open fun onPieceDragStart(piece: DynamicPiece, offset: Offset) {}
    open fun onPieceDragEnd(piece: DynamicPiece) {}
    open fun onPieceDrag(
        piece: DynamicPiece,
        change: PointerInputChange,
        dragAmount: Offset,
        scope: PointerInputScope
    ) {
    }

    open fun onDragStart(offset: Offset) {}
    open fun onDragEnd() {}
    open fun onDrag(change: PointerInputChange, dragAmount: Offset) {}
}

