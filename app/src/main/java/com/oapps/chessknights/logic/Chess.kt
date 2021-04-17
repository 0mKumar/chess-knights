package com.oapps.chessknights.logic

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import com.oapps.chessknights.Vec
import com.oapps.chessknights.ui.chess.Piece
import com.oapps.chessknights.ui.chess.Tile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class Chess() {
    val debug = false
    private val TAG = "Chess"
    val pieces = mutableStateListOf<Piece>()
    var state = State()

    var tiles = mutableStateMapOf<Vec, Tile>()

    enum class Color{
        WHITE,
        BLACK,
        UNSPECIFIED,
    }

    constructor(pieceList: List<List<String>>): this(){
        pieces.addAll(pieceList.flatten().map { Piece(it) })
    }

    constructor(fen: String) : this() {
        pieces.addAll(piecesFromFen(fen))
    }


    fun findPieceAt(vec: Vec) = pieces.find { it.vec == vec }

    fun canMove(move: Move): Boolean {
        return MoveValidator.validateMove(this, move)
    }

    fun asciiBoard(): String{
        val board = Array(8){Array(8){ mutableListOf<Char>() } }
        for(piece in pieces){
            val (x, y) = piece.vec
            board[y][x].add(piece.kind)
        }
        var out = "\n"
        for(y in board.reversedArray()){
            for(p in y){
                out += String.format("%3s ", p.joinToString(""))
            }
            out += "\n"
        }
        return out.removeSuffix("\n")
    }

    fun refreshPieces(coroutineScope: CoroutineScope){
        coroutineScope.launch {
            pieces.forEach {
                it.offsetFractionX.snapTo(it.vec.x.toFloat())
                it.offsetFractionY.snapTo(it.vec.y.toFloat())
            }
        }
    }

    fun reset(fen: String){
        pieces.clear()
        pieces.addAll(piecesFromFen(fen))
        state.reset(fen)
        tiles.clear()
    }

    fun piecesFromFen(fen: String): List<Piece>{
        if(debug) Log.d(TAG, "piecesFromFen: $fen")
        val pieces = mutableListOf<Piece>()
        fen.substringBefore(' ').split('/').forEachIndexed{ i, row ->
            if(debug) Log.d(TAG, "piecesFromFen: adding new row $i $row")
            var currX = 0
            row.forEach{ kind ->
                if(debug) Log.d(TAG, "piecesFromFen: currX = $currX, kind = $kind")
                if(kind.isDigit()){
                    currX += kind - '0'
                }else{
                    val element = Piece(Vec(currX++, 7 - i), kind)
                    pieces.add(element)
                    if(debug) Log.d(TAG, "piecesFromFen: added piece $element")
                }
            }
        }
        return pieces
    }

    fun generateFen(): String {
        return pieces.groupBy { it.vec.y }.mapValues { entry ->
            entry.value.sortedBy {
                it.vec.x
            }.run {
                var row = ""
                var x = 0
                forEach { piece ->
                    val diff = piece.vec.x - x
                    if (diff > 0) {
                        row += diff
                    }
                    row += piece.kind
                    x = piece.vec.x + 1
                }
                if (8 - x > 0) {
                    row += 8 - x
                }
                row
            }
        }.run {
            var fen = ""
            var y = 7
            entries.toMutableList().sortedByDescending { it.key }.forEach { entry ->
                val diff = y - entry.key
                if (diff > 0) {
                    fen += "8/".repeat(diff)
                }
                fen += entry.value
                y = entry.key - 1
                fen += "/"
            }
            if (y > 0) {
                fen += y
            }
            fen.removeSuffix("/")
        }
    }

}