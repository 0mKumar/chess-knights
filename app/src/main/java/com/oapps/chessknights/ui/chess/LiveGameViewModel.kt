package com.oapps.chessknights.ui.chess

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.oapps.chessknights.await
import com.oapps.chessknights.logic.Chess
import com.oapps.chessknights.logic.Move
import com.oapps.chessknights.models.AppUser
import com.oapps.chessknights.models.LiveChallenge
import com.oapps.chessknights.models.TimeControl
import com.oapps.chessknights.pushValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

class LiveGameViewModel : ViewModel() {
    private val TAG = "LiveVm"
    private var gameId = ""
    private var userId = ""
    private var user: AppUser = AppUser.None

    @ExperimentalCoroutinesApi
    suspend fun fetchUser(myUid: String){
        if(myUid == userId) return
        userId = myUid
        val snapshot = Firebase.database.reference.child("users").child(myUid).get().await()
        val liveUser = snapshot.getValue<AppUser>()
        if(liveUser != null){
            user = liveUser
        }
    }

    @ExperimentalCoroutinesApi
    fun setup(newGameId: String){
        viewModelScope.launch {
            if(user == AppUser.None){
                val uid = Firebase.auth.currentUser?.uid?:run{
                    Log.e(TAG, "setup: user not signed in")
                    return@launch
                }
                fetchUser(uid)
            }
            setGameId(newGameId)
        }
    }

    @ExperimentalCoroutinesApi
    fun setGameId(id: String){
        if(gameId != id){
            gameId = id
            viewModelScope.launch {
                val snapshot = Firebase.database.reference.child("games").child(gameId).get().await()

            }
        }
    }

    suspend fun createChallenge(myUid: String, timeControl: TimeControl): String {
        val challenge = LiveChallenge(myUid, timeControl = timeControl.notation())
        return Firebase.database.reference.child("challenge").pushValue(challenge)
    }

    private val pieces = listOf(
        listOf("Ra1", "Nb1", "Bc1", "Qd1", "Ke1", "Bf1", "Ng1", "Rh1"),
        List(8) { "P${'a' + it}2" },
        List(8) { "p${'a' + it}7" },
        listOf("ra8", "nb8", "bc8", "qd8", "ke8", "bf8", "ng8", "rh8")
    ).flatten().map { Piece(it) }.let { pieces ->
        val list = mutableStateListOf<Piece>()
        pieces.forEach {
            list.add(it)
        }
        return@let list
    }
    val chess = Chess(pieces)
    val moves = mutableStateListOf<Move>()
}