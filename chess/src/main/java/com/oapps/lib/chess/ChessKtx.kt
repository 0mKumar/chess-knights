package com.oapps.lib.chess

import javax.print.attribute.standard.MediaSize
import kotlin.math.absoluteValue
import kotlin.math.sign

val Char.isWhite
    get() = this.isUpperCase()
val Char.isBlack
    get() = this.isLowerCase()
val Char.asWhite
    get() = this.toUpperCase()
val Char.asBlack
    get() = this.toLowerCase()

fun Char.ofColor(white: Boolean) = if (white) asWhite else asBlack
val Char.color
    get() = this.isWhite

val Piece.isWhite
    get() = kind.isWhite
val Piece.isBlack
    get() = kind.isBlack

val Char.isRook
    get() = asWhite == 'R'
val Char.isKnight
    get() = asWhite == 'N'
val Char.isQueen
    get() = asWhite == 'Q'
val Char.isKing
    get() = asWhite == 'K'
val Char.isBishop
    get() = asWhite == 'B'
val Char.isPawn
    get() = asWhite == 'P'
val Char.isValidPieceKind get() = this.asWhite in "PBRQKN"

val Piece.isRook
    get() = kind.isRook
val Piece.isKnight
    get() = kind.isRook
val Piece.isQueen
    get() = kind.isQueen
val Piece.isKing
    get() = kind.isKing
val Piece.isBishop
    get() = kind.isBishop
val Piece.isPawn
    get() = kind.isPawn

fun PieceMap.put(piece: Piece) = put(piece.vec, piece)
fun PieceMap.remove(piece: Piece) = remove(piece.vec)


fun fromFen(fen: String) = PieceMap().apply {
    setFen(fen)
}

val IVec.file get() = "${'a' + x}"
val IVec.rank get() = "${'1' + y}"

val IVec.loc get() = "${'a' + x}${'1' + y}"
val IVec.absolute get() = IVec(x.absoluteValue, y.absoluteValue)
val IVec.sign get() = IVec(x.sign, y.sign)

val IVec.isValid get() = x in 0..7 && y in 0..7
val IVec.isInvalid get() = x !in 0..7 || y !in 0..7

val Move.color get() = piece.kind.color


fun IVec.inDirection(dir: IVec, until: IVec? = null) = object : Iterator<IVec> {
    var next = this@inDirection + dir
    override fun hasNext() = next.isValid && until != next

    override fun next() = next.also { next += dir }
}.asSequence()

infix fun Int.x(other: Int) = IVec(this, other)
