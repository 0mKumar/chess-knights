package com.oapps.lib.chess

data class Piece(val vec: IVec = IVec.None, val kind: Char = '-') {
    override fun toString() = "Piece($kind${vec.loc})"
}
