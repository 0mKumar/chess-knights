package com.oapps.chessknights

data class Vec(var x: Int = 0, var y: Int = 0) {
    constructor(notation: String) : this(notation[0] - 'a', notation[1] - '1')
}