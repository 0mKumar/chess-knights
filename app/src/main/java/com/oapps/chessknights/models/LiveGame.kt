package com.oapps.chessknights.models

import com.google.firebase.database.ServerValue

// live/$gameId
class LiveGame(
    val wId: String = "",
    val bId: String = "",
    val wName: String = "",
    val bName: String = "",
    val bRating: Int = 1200,
    val wRating: Int = 1200,
    val time: Map<String, String> = ServerValue.TIMESTAMP
) {

}