package com.oapps.knightschess.ui.chess

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.oapps.knightschess.R
import com.oapps.lib.chess.*
import kotlinx.coroutines.delay
import java.lang.Math.cbrt
import java.lang.Math.random
import kotlin.math.abs
import kotlin.math.sqrt

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
fun DynamicChessBoard(modifier: Modifier = Modifier, whiteBottom: Boolean = true) {
    ChessBox(modifier) {
        ChessBackground()
        DynamicPieceLayer(whiteBottom = whiteBottom)
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
private fun BoxWithConstraintsScope.DynamicPieceLayer(whiteBottom: Boolean = true) {
    val TAG = "ChessUI"
    val pieces = remember {
        mutableStateListOf(
            DynamicPiece(Piece(IVec(1, 1), 'k')),
            DynamicPiece(Piece(IVec(2, 1), 'Q'))
        )
    }

    LaunchedEffect(true) {
        while (true) {
            delay(2500)
            pieces.random().vec = IVec((0..7).random(), (0..7).random())
            pieces.random().kind = "PKQRN".random().ofColor(random() < 0.5)
        }
    }

    val size = maxWidth / 8
    for (piece in pieces) {
        DynamicPieceImage(piece, piece.kind, piece.vec, size = size, whiteBottom)
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
    whiteBottom: Boolean = true
) {
    val transitionState = remember(piece) {
        MutableTransitionState(vec)
    }
    val transition = updateTransition(transitionState, label = "piece")

    val duration = (250 * (transitionState.currentState - vec)
        .absolute.let {
            cbrt((it.y + it.x).toDouble())
        }).toInt()

    piece.offset = transition.animateOffset(transitionSpec = {
        tween(
            duration,
            0,
            FastOutSlowInEasing
        )
    }, label = "Offset") {
        Offset(it.x.toFloat(), it.y.toFloat())
    }

    transitionState.targetState = vec

    Crossfade(
        kind, modifier = Modifier
            .size(size, size)
            .offset(
                size * piece.offset.value.x.transformX(whiteBottom, 7f),
                size * piece.offset.value.y.transformY(whiteBottom, 7f)
            ),
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
                    val color = if (isWhite(x, y)) Color(0x85D5F0E4) else Color(0xFF5D57B4)
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

