package com.oapps.chessknights.ui.puzzles

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Snackbar
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.Snapshot.Companion.observe
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oapps.chessknights.TAG
import com.oapps.chessknights.Vec
import com.oapps.chessknights.db.Puzzle
import com.oapps.chessknights.logic.Chess
import com.oapps.chessknights.logic.Move
import com.oapps.chessknights.logic.MoveValidator
import com.oapps.chessknights.ui.chess.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PuzzleScreen(puzzleId: String? = null) {
    Surface(color = MaterialTheme.colors.surface) {
        val whiteBottom = remember { mutableStateOf(false) }
        var showCoordinates by mutableStateOf(true)
        val scrollState = rememberScrollState()
        val puzzlesViewModel: PuzzlesViewModel = viewModel()

        val lifecycleOwner = LocalLifecycleOwner.current
        val coroutineScope = rememberCoroutineScope()

        DisposableEffect(lifecycleOwner) {
            val observer = Observer<Puzzle?> {
                Log.d(TAG, "PuzzleScreen: new puzzle")
                Log.d(TAG, "PuzzleScreen: ${puzzlesViewModel.puzzle}")
                whiteBottom.value = puzzlesViewModel.chess.state.activeColor == Chess.Color.BLACK
                val puzzle = puzzlesViewModel.puzzle.value?:return@Observer
                val move = puzzle.moves[0]?.let { Move(puzzlesViewModel.chess, it) }
                Log.d(TAG, "PuzzleScreen: start move = $move")
                if(move != null){
                    coroutineScope.launch {
                        delay(1000)
                        puzzlesViewModel.chess.findPieceAt(move.from)?.moveTo(puzzlesViewModel.chess, coroutineScope, move.to, requestPromotionTo = {
                            it.promotesTo = move.promotesTo
                        }, onComplete = {
                            it.chess.state.update(it)
                        })
                    }
                }
            }
            puzzlesViewModel.puzzle.observe(lifecycleOwner, observer)
            onDispose { puzzlesViewModel.puzzle.removeObserver(observer) }
        }

        LaunchedEffect(puzzleId) {
            if(puzzleId == null) {
                puzzlesViewModel.newPuzzle()
            }else{
                puzzlesViewModel.fetchPuzzle(puzzleId)
            }
        }
        Column(
            Modifier
                .padding(start = 8.dp, end = 8.dp)
                .verticalScroll(scrollState)
        ) {
            PuzzleChessBoard(chess = puzzlesViewModel.chess, whiteBottom = remember {
                mutableStateOf(puzzlesViewModel.chess.state.activeColor == Chess.Color.BLACK)
            }, showCoordinates = showCoordinates)
        }
    }
}

@Composable
fun PuzzleChessBoard(
    chess: Chess,
    whiteBottom: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    showCoordinates: Boolean = false,
) {
    val puzzlesViewModel: PuzzlesViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()
    var boardModifier = modifier
    if (showCoordinates) boardModifier = boardModifier.padding(start = 12.dp, bottom = 16.dp)
    val requestPromotesTo = remember { mutableStateOf(Pair(false, Move(chess, "a1a1"))) }
    val requestPromotionTo: (Move) -> Unit = { it: Move ->
        requestPromotesTo.value = Pair(true, it)
        Log.d(TAG, "PlayableChessBoard: got request to show dialog")
    }

    fun movePieceToRequest(piece: Piece, to: Vec) {
        if ((chess.state.activeColor == Chess.Color.BLACK) == piece.isBlack()) {
            if(!puzzlesViewModel.puzzleComplete() && !puzzlesViewModel.isMyTurn()){
                return
            }
            piece.moveTo(
                chess,
                coroutineScope,
                to,
                requestPromotionTo = requestPromotionTo,
                onComplete = { completedMove ->
                    val incorrect = puzzlesViewModel.isInCorrect(completedMove)
                    chess.state.update(completedMove)

                    if(incorrect){
                        Log.d(TAG, "movePieceToRequest: INCORRECT MOVE")
                        return@moveTo
                    }else if(puzzlesViewModel.puzzleComplete()){
                        Log.d(TAG, "movePieceToRequest: PUZZLE COMPLETE")
                        return@moveTo
                    }
                    
                    if(!puzzlesViewModel.puzzleComplete() && !puzzlesViewModel.isMyTurn() && !incorrect){
                        Log.d(TAG, "movePieceToRequest: next move is computers move")
                        val move = puzzlesViewModel.nextPuzzleMove()?: return@moveTo
                        coroutineScope.launch {
                            delay(500)
                            move.piece.moveTo(chess, coroutineScope, move.to, requestPromotionTo = {it.promotesTo = move.promotesTo}, onComplete = {
                                chess.state.update(it)
                            })
                        }
                    }
                })
        } else {
            piece.moveTo(chess, coroutineScope, piece.vec)
        }
    }

    val uiActions = object : ChessUIActions() {
        override fun onSquareTapped(tappedVec: Vec, piece: Piece?) {
            if (piece != null) {
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
            } else {
                chess.pieces
                    .find { it.selected }
                    ?.let { selectedPiece ->
                        deselectPiece(selectedPiece)
                        movePieceToRequest(selectedPiece, tappedVec)
                    }
            }
        }

        private fun selectPiece(piece: Piece) {
            if ((chess.state.activeColor == Chess.Color.BLACK) != piece.isBlack()) {
                return
            }
            piece.selected = true
            tiles[piece.vec] =
                (tiles[piece.vec] ?: Tile(piece.vec)).add(Tile.PIECE_SELECTED)

            MoveValidator.validMoves(chess, piece).forEach {
                tiles[it.to] =
                    (tiles[it.to] ?: Tile(it.to)).add(Tile.TILE_HIGHLIGHT)
            }
        }

        private fun deselectPiece(piece: Piece) {
            piece.selected = false
            tiles[piece.vec] =
                (tiles[piece.vec] ?: Tile(piece.vec)).remove(Tile.PIECE_SELECTED)
            tiles.values.forEach {
                it.remove(Tile.TILE_HIGHLIGHT)
            }
        }

        override fun onPieceDragStart(piece: Piece) {
            if ((chess.state.activeColor == Chess.Color.BLACK) == piece.isBlack())
                selectPiece(piece)
        }

        override fun onPieceDragEnd(piece: Piece) {
            deselectPiece(piece)
            movePieceToRequest(
                piece,
                Vec(
                    piece.offsetFractionX.value.roundToInt(),
                    piece.offsetFractionY.value.roundToInt()
                )
            )
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
        ChessPiecesLayer(chess, whiteBottom, uiActions = uiActions)
        if (requestPromotesTo.value.first) {
            RequestPromotionPiece(requestPromotesTo, whiteBottom)
        }
    }
}