package com.oapps.chessknights.ui.chess

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.oapps.chessknights.Vec

class Tile(
    val vec: Vec,
){
    var flags by mutableStateOf(0)

    fun contains(flag: Int): Boolean {
        return flags and flag > 0
    }

    fun add(vararg fs: Int): Tile{
        for (flag in fs){
            flags = flags or flag
        }
        return this
    }

    fun remove(vararg fs: Int): Tile{
        for (flag in fs){
            flags = flags and flag.inv()
        }
        return this
    }

    fun clear(): Tile{
        flags = 0
        return this
    }

    companion object{
        val CHECK = 1 shl 0
        val HIGHLIGHT_MOVE_FROM = 1 shl 1
        val HIGHLIGHT_MOVE_TO = 1 shl 2
        val PIECE_SELECTED = 1 shl 3
        val TILE_HIGHLIGHT = 1 shl 4
    }
}
