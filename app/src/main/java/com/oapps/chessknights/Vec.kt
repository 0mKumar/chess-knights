package com.oapps.chessknights

import kotlin.math.sign

data class Vec(var x: Int = 0, var y: Int = 0) {
    constructor(notation: String) : this(notation[0] - 'a', notation[1] - '1')

    fun whiteBottom(whiteBottom: Boolean) =
        if (whiteBottom) Vec(x, y * -1)
        else Vec(x * -1, y)

    fun loc() = "${'a' + x}${'1' + y}"

    operator fun plus(other: Vec) = Vec(x + other.x, y + other.y)
    operator fun minus(other: Vec) = Vec(x - other.x, y - other.y)
    operator fun times(value: Int) = Vec(x * value, y * value)
    fun direction(): Vec = Vec(x.sign, y.sign)
}