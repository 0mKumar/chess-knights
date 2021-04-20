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
import com.oapps.audio.SoundManager
import com.oapps.knightschess.R
import com.oapps.lib.chess.*
import kotlinx.coroutines.delay
import java.lang.Math.cbrt
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
    whiteBottom: MutableState<Boolean> = remember {
        mutableStateOf(true)
    },
    soundManager: SoundManager? = null
) {
    val pieces = remember {
        mutableStateListOf(
            DynamicPiece(Piece(IVec(1, 1), 'k')),
            DynamicPiece(Piece(IVec(2, 1), 'Q')),
            DynamicPiece(Piece(IVec(1, 7), 'P')),
            DynamicPiece(Piece(IVec(7, 1), 'p')),
        )
    }
    LaunchedEffect(true) {
        while (false) {
            delay(2000)

            pieces.first().animateTo(IVec((0..7).random(), (0..7).random()))
//            pieces.values.random().kind = "PKQRN".random().ofColor(random() < 0.5)
        }
    }

    ChessBox(modifier) {
        val size = maxWidth / 8

        val dragEvent = remember {
            object : DragEvent() {
                override fun onPieceDragStart(piece: DynamicPiece, offset: Offset) {
                    draggedPiece = piece
                    piece.startDrag()
                }

                override fun onPieceDragEnd(piece: DynamicPiece) {
                    draggedPiece = null
                    piece.stopDrag()
                    val to = IVec(
                        piece.dragOffset.x.roundToInt(),
                        piece.dragOffset.y.roundToInt()
                    )
                    piece.animateTo(to)
                }

                override fun onPieceDrag(
                    piece: DynamicPiece,
                    change: PointerInputChange,
                    dragAmount: Offset,
                    scope: PointerInputScope
                ) {
                    change.consumeAllChanges()
                    val drag = dragAmount
                        .div(with(scope){size.toPx()})
                        .transformDirection(whiteBottom.value)
                    piece.dragOffset += drag
                }
            }
        }
        ChessBackground()
        DynamicPieceLayer(pieces, whiteBottom = whiteBottom.value, dragEvent = dragEvent) { it, cancelled ->
            Log.d(TAG, "DynamicChessBoard: animated $it, cancelled = $cancelled")
            if (it.vec != it.lastVec)
                soundManager?.play(R.raw.move)
        }
//        InteractionLayer(pieces = pieces, whiteBottom = whiteBottom, dragEvent = dragEvent)
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
            piece.kind,
            piece.vec,
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
    kind: Char,
    vec: IVec,
    size: Dp,
    whiteBottom: Boolean = true,
    dragEvent: DragEvent,
    onFinishAnimation: ((DynamicPiece, cancelled: Boolean) -> Unit)? = null,
) {
//    val transitionState = remember(piece) {
//        MutableTransitionState(vec)
//    }
//    val transition = updateTransition(transitionState, label = "piece")
//
//    val duration = (250 * (transitionState.currentState - vec)
//        .absolute.let {
//            cbrt((it.y + it.x).toDouble())
//        }).toInt()
//
//    piece.offset = transition.animateOffset(transitionSpec = {
//        tween(
//            duration,
//            0,
//            FastOutSlowInEasing
//        )
//    }, label = "Offset") {
//        Offset(it.x.toFloat(), it.y.toFloat())
//    }
//
//    transitionState.targetState = vec

    piece.offset = animatedPieceOffset(piece = piece, target = vec) {
        onFinishAnimation?.invoke(piece, it)
        Log.d(TAG, "DynamicPieceImage: animation complete")
    }

    Crossfade(
        kind, modifier = Modifier
            .size(size, size)
            .offset(
                size * piece.drawOffset.x.transformX(whiteBottom, 7f),
                size * piece.drawOffset.y.transformY(whiteBottom, 7f)
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
        animationSpec = spring()
    ) {
        Image(
            modifier = Modifier
                .fillMaxSize(),
            painter = painterResource(id = painterResourceForPiece[kind] ?: R.drawable.bn),
            contentDescription = pieceName[kind]
        )
    }
}

@Composable
fun animatedPieceOffset(
    piece: DynamicPiece,
    target: IVec,
    onComplete: ((cancelled: Boolean) -> Unit)? = null
): Offset {
    // Create an AnimationState to be updated by the animation.
    val animationState =
        remember(piece) { AnimationState(Offset.VectorConverter, target.toOffset()) }

    // Launch the suspend animation into the composition's CoroutineContext, and pass
    // `target` to LaunchedEffect so that when`target` changes the old animation job is
    // canceled, and a new animation is created with a new target.
    LaunchedEffect(target) {
        // This starts an animation that updates the animationState on each frame

//        if (piece.dragOffset != piece.offset) {
//            animationState.animateTo(piece.dragOffset, tween(0, easing = LinearEasing))
//        }

        val targetOffset = target.toOffset()

        if (!animationState.isFinished) {
            onComplete?.invoke(true)
        }

        animationState.animateTo(
            targetValue = targetOffset,
            // Use a low stiffness spring. This can be replaced with any type of `AnimationSpec`
            animationSpec = tween(
                250 * cbrt(
                    (targetOffset - animationState.value).getDistance()
                        .toDouble()
                ).toInt()
            ),
//            animationSpec = spring(1f, 200f),
            // If the previous animation was interrupted (i.e. not finished), configure the
            // animation as a sequential animation to continue from the time the animation was
            // interrupted.
            sequentialAnimation = !animationState.isFinished
        )
        // When the function above returns, the animation has finished.
        onComplete?.invoke(false)
    }
    // Return the value updated by the animation.
    return animationState.value.also {
        piece.dragOffset = it
    }
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

open class DragEvent{
    var draggedPiece: DynamicPiece? = null
    open fun onPieceDragStart(piece: DynamicPiece, offset: Offset){}
    open fun onPieceDragEnd(piece: DynamicPiece){}
    open fun onPieceDrag(piece: DynamicPiece, change: PointerInputChange, dragAmount: Offset, scope: PointerInputScope){}
    open fun onDragStart(offset: Offset){}
    open fun onDragEnd(){}
    open fun onDrag(change: PointerInputChange, dragAmount: Offset){}
}

