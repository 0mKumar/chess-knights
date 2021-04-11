package com.oapps.chessknights.ui.puzzles

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.oapps.chessknights.await
import com.oapps.chessknights.db.Puzzle
import com.oapps.chessknights.logic.Chess
import com.oapps.chessknights.logic.Move
import com.oapps.chessknights.models.AppUser
import com.oapps.chessknights.models.LiveChallenge
import com.oapps.chessknights.models.TimeControl
import com.oapps.chessknights.pushValue
import com.oapps.chessknights.ui.chess.Piece
import io.realm.Realm
import io.realm.RealmList
import io.realm.kotlin.where
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.channelFlow
import java.util.concurrent.Executors

class PuzzlesViewModel : ViewModel() {
    val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    fun newPuzzle() {
        viewModelScope.launch(dispatcher) {
//            val realm = Realm.getDefaultInstance()
//            realm.close()
            /**
             * "PuzzleId" : "0000D", "FEN" : "5rk1/1p3ppp/pq3b2/8/8/1P1Q1N2/P4PPP/3R2K1 w - - 2 27", "Moves" : "d3d6 f8d8 d6d8 f6d8", "Rating" : "1562", "RatingDeviation" : "74", "Popularity" : "97", "NbPlays" : "2962", "Themes" : [ "advantage", "endgame", "short" ], "GameUrl" : "https://lichess.org/F8M8OS71#53"
             */

            sendPuzzle(Puzzle(
                pid = "0000D",
                fen = "5rk1/1p3ppp/pq3b2/8/8/1P1Q1N2/P4PPP/3R2K1 w - - 2 27",
                moves = RealmList(*("d3d6 f8d8 d6d8 f6d8".split(' ').toTypedArray())),
                rating = 1562,
                ratingDeviation = 74
            ))
        }
    }

    suspend fun fetchPuzzle(puzzleId: String) {
        withContext(dispatcher){
            val realm = Realm.getDefaultInstance()
            val fetchedPuzzle = realm.where<Puzzle>().equalTo("pid", puzzleId).findFirst()
            realm.close()
            sendPuzzle(fetchedPuzzle)
        }
    }


    private val TAG = "PuzzleVm"
    private var user: AppUser = AppUser.None

    fun sendPuzzle(p: Puzzle?){
        viewModelScope.launch(Dispatchers.Main){
            if (p == null) {
                chess.pieces.clear()
            } else {
                chess.reset(p.fen)
                initialHalfMoveCount = chess.state.halfMoveCount
            }
            puzzle.postValue(p)
        }
    }

    val puzzle = MutableLiveData<Puzzle?>(null)

    private var initialHalfMoveCount = 2

    private val nextPuzzleIndex: Int
        get() = (chess.state.halfMoveCount - initialHalfMoveCount).also {
            Log.d(TAG, "nextPuzzleIndex = $it")
        }

    fun nextPuzzleMove(): Move? {
        return puzzle.value?.moves?.get(nextPuzzleIndex)?.let { Move(chess, it) }.also {
            Log.d(TAG, "nextPuzzleMove: $it")
        }
    }

    fun isMyTurn() = nextPuzzleIndex % 2 == 1
    fun puzzleComplete() = puzzle.value?.let { nextPuzzleIndex >= it.moves.size }?: true

    var positionIncorrect = false

    fun isInCorrect(completedMove: Move): Boolean {
        return (nextPuzzleMove()?.algebraic() == completedMove.algebraic()).also {
            positionIncorrect = positionIncorrect or it
        }
    }

    //    val chess = Chess("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
    val chess = Chess()
}