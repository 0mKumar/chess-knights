package com.oapps.lib.chess

data class IVec(val x: Int = 0, val y: Int = 0): Comparable<IVec> {
    companion object{
        val None = IVec(-1,-1)
    }

    constructor(notation: String) : this(notation[0] - 'a', notation[1] - '1')

    override fun toString() = "($x,$y,$loc)"

    operator fun plus(other: IVec) = IVec(x + other.x, y + other.y)
    operator fun minus(other: IVec) = IVec(x - other.x, y - other.y)
    operator fun times(value: Int) = IVec(x * value, y * value)

    // comparison on the basis of fen position
    override fun compareTo(other: IVec) = (other.y - y) * 8 + (x - other.x)
}