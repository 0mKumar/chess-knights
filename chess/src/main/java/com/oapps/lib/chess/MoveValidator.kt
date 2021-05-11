package com.oapps.lib.chess

import kotlin.math.absoluteValue
import kotlin.math.sign

sealed class MoveValidator {
    abstract fun validate(
        move: Move,
        checkIllegal: Boolean = true,
        skipColorCheck: Boolean = false
    ): ValidationResult

    inner class ValidationResult(var valid: Boolean = true) {
        var castlingRookFinalPos: IVec? = null
        var castling: Char? = null
        var promotion: Char? = null
        var createdEnPassantTarget: IVec? = null
        var enPassantCapturedPiece: Piece? = null
        var capture: State.Capture? = null
        var castleRook: Piece? = null
        var isOpponentInCheck: Boolean = false
        override fun toString(): String {
            return "ValidationResult(valid=$valid, castling=$castling, promotion=$promotion, createdEnPassantTarget=$createdEnPassantTarget, enPassantCapturedPiece=$enPassantCapturedPiece, capture=$capture, castleRook=$castleRook)"
        }
    }

    fun preCheckFails(move: Move, skipColorCheck: Boolean): Boolean {
        if (move.to.isInvalid) return true
        if (move.piece.vec == move.to) return true
        if (move.piece.kind == '-' || move.piece.vec.isInvalid) return true
        if (!skipColorCheck && move.chess.state.activeColor != move.color) return true
        move.attackedPiece?.let {
            if (it.color == move.color) return true
        }
        return false
    }

    fun moveFromSan(chess: Chess, san: String, color: Boolean = true): Move? {
        println("san = [${san}], color = [${color}]")
        var data = san.trim()
        if (data.startsWith("O-O")) {
            val side = if (data == "O-O") 6 else 2
            val rank = if (color) 0 else 7
            return Move(chess, Piece(IVec(4, rank), 'K'.ofColor(color)), IVec(side, rank))
        }
        if (data.endsWith('+') || data.endsWith('#')) data = data.dropLast(1)
        var promotion: Char? = null
        if (data.contains('=')) {
            if (data.length < 2) return null
            promotion = data.last()
            data = data.dropLast(2)
            if (!promotion.isValidPieceKind) return null
        }
        if (data.length < 2) return null
        val to = IVec(data.takeLast(2))
        if (to.isInvalid) return null
        data = data.dropLast(2)
        if (data.isNotEmpty()) {
            if (data.last() == 'x') data = data.dropLast(1)
        }

        val kind = when {
            data.isNotEmpty() && data[0].isUpperCase() && data[0].isValidPieceKind -> data[0].also {
                data = data.drop(1)
            }
            else -> 'P'
        }

        var file: Char? = null
        var rank: Char? = null

        if (data.length == 1) {
            if (data[0].isDigit()) rank = data[0]
            else file = data[0]
        } else if (data.length == 2) {
            file = data[0]
            rank = data[1]
        }

        if (rank != null) {
            if (rank !in '1'..'8') return null
        }
        if (file != null) {
            if (file !in 'a'..'h') return null
        }


        return chess.pieces.values
            .filter {
                it.kind == kind.ofColor(color)
                        && (file == null || it.vec.file[0] == file)
                        && (rank == null || it.vec.rank[0] == rank)
            }
            .map { Move(chess, it, to, promotesTo = promotion) }
            .find { it.isValid() }
    }

    fun sanForMove(move: Move): String {
//        println("MoveValidator.sanForMove")
//        println("move = [${move}]")
//        return "disabled"
        // TODO: 4/16/21 verify if this is latest move or throw exception
        if (!move.isValid()) return "?"
        val ambiguity = move.chess.pieces.values.toMutableList().filter {
            it != move.piece && it.kind == move.piece.kind && StandardValidator.canLegallyMove(
                move.chess,
                it,
                move.to
            )
        }
        move.validationResult.castling?.let {
            if (it.isKing) return "O-O"
            else if (it.isQueen) return "O-O-O"
        }
        val builder = StringBuilder()
        if (!move.piece.isPawn) {
            builder.append(move.piece.kind.asWhite)
        }

        if (ambiguity.isNotEmpty()) {
            builder.append(
                when {
                    ambiguity.none { it.vec.x == move.piece.vec.x } -> move.piece.vec.file
                    ambiguity.none { it.vec.y == move.piece.vec.y } -> move.piece.vec.rank
                    else -> move.piece.vec.loc
                }
            )
        }

        if (move.validationResult.enPassantCapturedPiece != null || move.attackedPiece != null) {
            if (move.piece.isPawn && builder.isEmpty()) builder.append(move.piece.vec.file)
            builder.append('x')
        }
        builder.append(move.to.loc)
        if (move.piece.isPawn && move.to.y.let { it == 0 || it == 7 }) {
            builder.append('=')
            builder.append((move.promotesTo ?: 'Q').asWhite)
        }

        move.chess.makeMove(move, validate = false, commit = true)
        val gameStatus = move.chess.gameStatus(move)
        if(gameStatus == Chess.GameStatus.CHECK_MATE){
            builder.append('#')
        }else if (move.validationResult.isOpponentInCheck) {
            builder.append('+')
        }
        move.chess.revertMove(move, validate = false, rollBack = true)

        return builder.toString()
    }

    object StandardValidator : MoveValidator() {
        override fun validate(
            move: Move,
            checkIllegal: Boolean,
            skipColorCheck: Boolean
        ): ValidationResult {
            val res = ValidationResult()
            res.capture = move.chess.state.capture()
            if (
                preCheckFails(move, skipColorCheck)
                || validatePieceSpecificFails(move, res, checkIllegal)
            ) {
                return res.apply { valid = false }
            }

            return res
        }

        private fun validatePieceSpecificFails(
            move: Move,
            res: ValidationResult,
            checkIllegal: Boolean
        ): Boolean {
            val incorrect = when (move.piece.kind.asWhite) {
                'R' -> validateRookFails(move, res)
                'B' -> validateBishopFails(move, res)
                'K' -> validateKingFails(move, res, checkIllegal)
                'Q' -> validateQueenFails(move, res)
                'P' -> validatePawnFails(move, res)
                'N' -> validateKnightFails(move, res)
                else -> return true
            }
            if (incorrect) return true

            if (!checkIllegal) return false
//            if(move.piece.isKing) return false
            val fakeMove = Move(move.chess, move.piece, move.to, move.promotesTo, false)
            move.chess.makeMove(fakeMove, validate = false, commit = false)
//            println("checking if check")
            return isCheck(move.chess, move.color)
                .also {
                    res.isOpponentInCheck = isCheck(move.chess, !move.color)
                    move.chess.revertMove(
                        fakeMove,
                        validate = false,
                        rollBack = false
                    )
                }
        }

        private fun validateRookFails(move: Move, res: ValidationResult): Boolean {
            if (move.diff.x != 0 && move.diff.y != 0) return true
            if (hasPieceInLine(move.piece.vec, move.diff.sign, move.to, move.chess)) return true
            return false
        }

        private fun validateBishopFails(move: Move, res: ValidationResult): Boolean {
            if (move.diff.absolute.run { x != y }) return true
            if (hasPieceInLine(move.piece.vec, move.diff.sign, move.to, move.chess)) return true
            return false
        }

        private fun validateQueenFails(move: Move, res: ValidationResult): Boolean {
            if (move.diff.x != 0 && move.diff.y != 0 && move.diff.absolute.run { x != y }) return true
            if (hasPieceInLine(move.piece.vec, move.diff.sign, move.to, move.chess)) return true
            return false
        }

        private fun validateKingFails(
            move: Move,
            res: ValidationResult,
            checkIllegal: Boolean
        ): Boolean {
            if (move.piece.vec.x == 4 && move.diff.y == 0 && move.diff.x.absoluteValue == 2 && move.piece.vec.y == if (move.color) 0 else 7) {
                println("checking castle")
                val castlingType = (if (move.diff.x > 0) 'K' else 'Q').ofColor(move.color)
                if (move.chess.state.canCastle(castlingType)) {
                    val castleRook =
                        move.chess.pieces[move.piece.vec.copy(x = if (castlingType.asWhite == 'K') 7 else 0)]
                    if (castleRook == null || !castleRook.isRook) {
                        println("Rook not found")
                        return true
                    }
                    if (hasPieceInLine(
                            move.piece.vec,
                            move.diff.sign,
                            castleRook.vec,
                            move.chess
                        )
                    ) {
                        println("has piece in between")
                        return true
                    }
                    // todo: verify below code
                    if (isCheck(move.chess, move.color, move.piece)) {
                        println("king is in check")
                        return true
                    }
                    val pathVec = move.piece.vec + move.diff.sign
                    val fakeMove = Move(move.chess, move.piece, pathVec, checkIllegal = false)
                    move.chess.makeMove(fakeMove, validate = false, commit = false)
                    if (isCheck(move.chess, move.color, move.chess.pieces[pathVec])
                            .also {
                                move.chess.revertMove(
                                    fakeMove,
                                    validate = false,
                                    rollBack = false
                                )
                            }
                    ) {
                        println("there is check in path")
                        return true
                    }
                    // todo: also validate no check in path of king
                    res.castling = castlingType
                    res.castleRook = castleRook
                    res.castlingRookFinalPos = move.piece.vec + move.diff.sign
                    println("Castle possible")
                } else {
                    println("cannot castle this way")
                    return true
                }
            }

            if (res.castling == null && move.diff.absolute.let { it.x > 1 || it.y > 1 }) return true

            return false
        }

        fun isCheck(
            chess: Chess,
            color: Boolean,
            king: Piece? = chess.pieces.values.find { it.isKing && it.kind.color == color }
        ): Boolean {
            king ?: return false
            for (opp in chess.pieces.values) {
                if (opp.kind.color == color) continue
                val move = Move(chess, opp, king.vec, checkIllegal = false)
                if (validate(move, checkIllegal = false, skipColorCheck = true).valid) {
                    return true
                }
            }
            return false
        }


        fun canLegallyMove(chess: Chess, piece: Piece, to: IVec): Boolean {
            println("StandardValidator.canLegallyMove")
            println("piece = [${piece}], to = [${to}]")
            val move = Move(chess, piece, to)
            return move.isValid()
        }

        private fun validateKnightFails(move: Move, res: ValidationResult): Boolean {
            if (move.diff.absolute.let { it.x !in 1..2 || it.y !in 1..2 || it.x == it.y }) return true
            return false
        }

        private fun validatePawnFails(move: Move, res: ValidationResult): Boolean {
            if (move.diff.x.absoluteValue > 1) return true
            if (move.diff.y.absoluteValue !in 1..2) return true
            val forward = if (move.color) 1 else -1
            if (move.diff.y.sign != forward) return true
            if ((move.diff.y == 2 && move.piece.vec.y != 1) || (move.diff.y == -2 && move.piece.vec.y != 6)) return true
            if (move.attackedPiece == null) {
                if (move.diff.x.absoluteValue == 1) {
                    if (!isEnpassant(move, res)) {
                        return true
                    }
                }
                if (move.diff.y.absoluteValue == 2) {
                    if (move.chess.pieces[move.piece.vec + move.diff.sign] != null) return true
                    res.createdEnPassantTarget = move.piece.vec + move.diff.sign
                }
            } else {
                if (move.diff.x == 0) return true
            }
            if (move.to.y == 0 || move.to.y == 7) {
                res.promotion = move.promotesTo
            }
            return false
        }

        private fun isEnpassant(move: Move, res: ValidationResult): Boolean {
            if (move.to == move.chess.state.enPassantTarget) {
                res.enPassantCapturedPiece = move.chess.pieces[move.to.copy(y = move.piece.vec.y)]
                if (res.enPassantCapturedPiece != null) return true
            }
            return false
        }

        private fun hasPieceInLine(from: IVec, dir: IVec, until: IVec? = null, chess: Chess) =
            from.inDirection(dir, until).any { chess.pieces[it] != null }

        fun getPossibleMoves(
            chess: Chess,
            piece: Piece,
            earlyReturnOneOrNone: Boolean = false
        ): List<Move> {
            return when (piece.kind.asWhite) {
                'P' -> possibleMovesForPawn(chess, piece, earlyReturnOneOrNone)
                'K' -> possibleMovesForKing(chess, piece, earlyReturnOneOrNone)
                'Q' -> possibleMovesForQueen(chess, piece, earlyReturnOneOrNone)
                'N' -> possibleMovesForKnight(chess, piece, earlyReturnOneOrNone)
                'B' -> possibleMovesForBishop(chess, piece, earlyReturnOneOrNone)
                'R' -> possibleMovesForRook(chess, piece, earlyReturnOneOrNone)
                else -> emptyList()
            }
        }

        private val rookDirs = sequenceOf(0 x 1, 0 x -1, 1 x 0, -1 x 0)
        private val bishopDirs = sequenceOf(1 x 1, 1 x -1, -1 x 1, -1 x -1)
        private val horseHops = sequenceOf(
            1 x 2, 1 x -2, -1 x 2, -1 x -2,
            2 x 1, 2 x -1, -2 x 1, -2 x -1
        )

        private fun possibleMovesForPawn(
            chess: Chess,
            piece: Piece,
            earlyReturnOneOrNone: Boolean
        ): List<Move> {
            val dy = if (piece.isWhite) 1 else -1
            val moves = listOf(-1 x dy, 0 x dy, 1 x dy, 0 x 2 * dy).map {
                Move(chess, piece, it + piece.vec)
            }
            return if (earlyReturnOneOrNone) {
                moves.firstOrNull { validate(it).valid }?.let { listOf(it) } ?: emptyList()
            } else {
                moves.filter { validate(it).valid }
            }
        }

        private fun possibleMovesForKing(
            chess: Chess,
            piece: Piece,
            earlyReturnOneOrNone: Boolean
        ): List<Move> {
            return collectPossibleMovesForDestinations(
                chess,
                piece,
                (bishopDirs + rookDirs).map { it + piece.vec },
                breakIfAttack = false,
                earlyReturnOneOrNone = earlyReturnOneOrNone
            )
        }

        private fun possibleMovesForKnight(
            chess: Chess,
            piece: Piece,
            earlyReturnOneOrNone: Boolean
        ): List<Move> {
            return collectPossibleMovesForDestinations(
                chess,
                piece,
                horseHops.map { it + piece.vec },
                breakIfAttack = false,
                earlyReturnOneOrNone = earlyReturnOneOrNone
            )
        }

        private fun possibleMovesForQueen(
            chess: Chess,
            piece: Piece,
            earlyReturnOneOrNone: Boolean
        ): List<Move> {
            return possibleMovesInDirs(chess, piece, bishopDirs + rookDirs, earlyReturnOneOrNone)
        }

        private fun possibleMovesForBishop(
            chess: Chess,
            piece: Piece,
            earlyReturnOneOrNone: Boolean
        ): List<Move> {
            return possibleMovesInDirs(chess, piece, bishopDirs, earlyReturnOneOrNone)
        }

        private fun possibleMovesForRook(
            chess: Chess,
            piece: Piece,
            earlyReturnOneOrNone: Boolean
        ): List<Move> {
            return possibleMovesInDirs(chess, piece, rookDirs, earlyReturnOneOrNone)
        }

        private fun possibleMovesInDirs(
            chess: Chess,
            piece: Piece,
            dirs: Sequence<IVec>,
            earlyReturnOneOrNone: Boolean = false
        ): List<Move> {
            val moves = mutableListOf<Move>()
            for (dir in dirs) {
                collectPossibleMovesForDestinations(
                    chess,
                    piece,
                    piece.vec.inDirection(dir),
                    movesBucket = moves,
                    earlyReturnOneOrNone = earlyReturnOneOrNone,
                    breakIfAttack = true
                )
                if(earlyReturnOneOrNone && moves.isNotEmpty()) return moves
            }
            return moves
        }

        private fun collectPossibleMovesForDestinations(
            chess: Chess,
            piece: Piece,
            destinations: Sequence<IVec>,
            breakIfAttack: Boolean = true,
            movesBucket: MutableList<Move> = mutableListOf(),
            earlyReturnOneOrNone: Boolean = false
        ): MutableList<Move> {
            for (to in destinations) {
                if (to.isInvalid) {
                    if (breakIfAttack) break
                    else continue
                }
                val fakeMove = Move(chess, piece, to, null, true)
                if (fakeMove.attackedPiece?.color == piece.kind.color) {
                    if (breakIfAttack) break
                    else continue
                }
                chess.makeMove(fakeMove, validate = false, commit = false)
                val isCheck = isCheck(chess, piece.kind.color)
                    .also {
                        chess.revertMove(
                            fakeMove,
                            validate = false,
                            rollBack = false
                        )
                    }
                if (!isCheck) {
                    movesBucket.add(fakeMove)
                    if (earlyReturnOneOrNone) return movesBucket
                }
                if (breakIfAttack && fakeMove.attackedPiece != null) break
            }
            return movesBucket
        }
    }

    object NoCheck : MoveValidator() {
        override fun validate(
            move: Move,
            checkIllegal: Boolean,
            skipColorCheck: Boolean
        ): ValidationResult {
            return ValidationResult(true).apply {
                capture = move.chess.state.capture()
            }
        }
    }
}
