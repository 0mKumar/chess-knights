package com.oapps.lib.chess

class Chess(
    fen: String = startPos,
    var options: Options = Options(),
    val validator: MoveValidator = MoveValidator.StandardValidator
) {
    inline val capturedState get() = state.stateCapture

    private val _pieces = fromFen(fen)
    val pieces: Map<IVec, Piece> get() = _pieces

    val state = State(fen)

    fun reset(fen: String = startPos) {
        _pieces.setFen(fen)
    }

    fun makeMove(move: Move, validate: Boolean = true, commit: Boolean = validate): Move {
        if (validate) {
            if (!move.isValid()) return move
        }
        _pieces.move(move, validate)
        if (commit) {
            state.commit(move)
        }

        return move
    }

    fun revertMove(move: Move, validate: Boolean = true, rollBack: Boolean = validate): Boolean {
        if (validate) {
            if (!move.isValid()) return false
        }
        _pieces.unMove(move, validate)
        if (rollBack) {
            state.rollBack(move)
        }
        return true
    }

    enum class GameStatus {
        CHECK_MATE,
        STALE_MATE,
        NORMAL
    }

    fun gameStatus(lastMove: Move? = null): GameStatus {
        val hasNoLegalMove = pieces.values.toMutableList().all {
            it.kind.color != state.activeColor || MoveValidator.StandardValidator.getPossibleMoves(
                chess = this,
                piece = it,
                earlyReturnOneOrNone = true
            ).isEmpty()
        }
        if (hasNoLegalMove) {
            val isCheck = lastMove?.validationResult?.isOpponentInCheck
                ?: MoveValidator.StandardValidator.isCheck(this, state.activeColor)
            if (isCheck) {
                println("checkmate #")
                return GameStatus.CHECK_MATE
            }
            println("stalemate")
            return GameStatus.STALE_MATE
        }
        return GameStatus.NORMAL
    }

    fun generateFen() = _pieces.generateFen()
    fun generateFullFen() = generateFen() + " " + state.generateFenExtra()

    fun printAsciiBoard() {
        val board = Array(8) { Array(8) { mutableListOf<Char>() } }
        for (piece in pieces) {
            val (x, y) = piece.value.vec
            board[y][x].add(piece.value.kind)
        }
        val out = StringBuilder()
        var file = 8
        for (y in board.reversedArray()) {
            out.append(file).append("  ")
            file--
            for (p in y) {
                out.append(String.format("%2s ", p.joinToString("")))
            }
            out.append("\n")
        }
        out.append("\n    ").append(('a'..'h').joinToString("  "))
        println(out.toString())
    }

    operator fun get(vec: IVec): Piece? = pieces[vec]

    companion object {
        const val startPos = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        const val emptyPos = "8/8/8/8/8/8/8/8 w - - 0 1"
    }
}