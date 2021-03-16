package com.oapps.chessknights.ui.chess

fun Char.ofColor(white: Boolean): Char {
    return if (white) toUpperCase() else toLowerCase()
}
