package com.oapps.lib.chess

class PGNParser {
    inner class Block(val header: List<String>, val movesBlock: String) {
        override fun toString(): String {
            return "Block(header=$header, moves='$movesBlock')"
        }
    }

    private fun parseBlock(pgn: String): Block {
        val regex = "\\[.*]".toRegex()
        val header = mutableListOf<String>()
        val moves = pgn
            .replace(regex) {
                header.add(it.value)
                ""
            }
            .trim('\n')
            .replace('\n', ' ')
        return Block(header, moves)
    }

    private fun splitMovesBlock(movesBlock: String): List<String> {
        val regex = "[\\d]+\\.".toRegex()
        return movesBlock
            .split(regex)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { it.split(' ') }
            .flatten()
    }

    fun parse(pgn: String): MutableList<Move> {
        val block = parseBlock(pgn)
        val chess = Chess()
        val rawMoves = splitMovesBlock(block.movesBlock)
        var color = true
        val moves = mutableListOf<Move>()
        for (it in rawMoves) {
            val move = chess.validator.moveFromSan(chess, it, color.also { color = !color })
            if(move == null){
                println("Can't parse legal move for $it at fen ${chess.generateFen()}")
                assert(false)
                break
            }else if(move.isValid()){
                chess.makeMove(move)
                chess.printAsciiBoard()
                moves.add(move)
            }else{
                println("Move $move is invalid")
                assert(false)
                break
            }
        }
        return moves
    }
}