package com.oapps.chessknights

import androidx.compose.runtime.mutableStateListOf

val pieces = listOf(
    listOf("Ra1", "Nb1", "Bc1", "Qd1", "Ke1", "Bf1", "Ng1", "Rh1"),
    List(8) { "P${'a' + it}2" },
    List(8) { "p${'a' + it}7" },
    listOf("ra8", "nb8", "bc8", "qd8", "ke8", "bf8", "ng8", "rh8")
).flatten().map { Piece(it) }.let { pieces ->
    val list = mutableStateListOf<Piece>()
    pieces.forEach{
        list.add(it)
    }
    return@let list
}