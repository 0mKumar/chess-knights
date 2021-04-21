package com.oapps.lib.chess

import kotlin.math.absoluteValue
import kotlin.math.sign

sealed class MoveValidator {
    abstract fun validate(move: Move, checkIllegal: Boolean = true): ValidationResult

    inner class ValidationResult(var valid: Boolean = true) {
        var castlingRookFinalPos: IVec? = null
        var castling: Char? = null
        var promotion: Char? = null
        var createdEnPassantTarget: IVec? = null
        var enPassantCapturedPiece: Piece? = null
        var capture: State.Capture? = null
        var castleRook: Piece? = null
        override fun toString(): String {
            return "ValidationResult(valid=$valid, castling=$castling, promotion=$promotion, createdEnPassantTarget=$createdEnPassantTarget, enPassantCapturedPiece=$enPassantCapturedPiece, capture=$capture, castleRook=$castleRook)"
        }
    }

    fun preCheckFails(move: Move): Boolean {
        if (move.to.isInvalid) return true
        if (move.piece.vec == move.to) return true
        if (move.piece.kind == '-' || move.piece.vec.isInvalid) return true
        move.attackedPiece?.let {
            if (it.color == move.color) return true
        }
        return false
    }

    fun moveFromSan(chess: Chess, san: String, color: Boolean = true): Move? {
        println("MoveValidator.moveFromSan")
        println("san = [${san}], color = [${color}]")
        var data = san.trim()
        if(data.startsWith("O-O")) {
            val side = if (data == "O-O") 6 else 2
            val rank = if (color) 0 else 7
            return Move(chess, Piece(IVec(4, rank), 'K'.ofColor(color)), IVec(side, rank))
        }
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
        // TODO: 4/16/21 verify if this is latest move or throw exception
        if (!move.isValid()) return "?"
        val ambiguity = move.chess.pieces.values.filter {
            it.kind == move.piece.kind && StandardValidator.canLegallyMove(move.chess, it, move.to)
        }.fold(false to false) { acc, piece ->
            (acc.first || piece.vec.x != move.piece.vec.x) to (acc.second || piece.vec.y != move.piece.vec.y)
        }
        move.validationResult.castling?.let {
            if(it.isKing) return "O-O"
            else if(it.isQueen) return "O-O-O"
        }
        val builder = StringBuilder()
        if (!move.piece.isPawn) {
            builder.append(move.piece.kind.asWhite)
        }
        builder.append(
            when (ambiguity) {
                false to false -> ""
                true to false -> move.piece.vec.file
                false to true -> move.piece.vec.rank
                else -> move.piece.vec.loc
            }
        )

        if (move.validationResult.enPassantCapturedPiece != null || move.attackedPiece != null) {
            if (move.piece.isPawn && builder.isEmpty()) builder.append(move.piece.vec.file)
            builder.append('x')
        }
        builder.append(move.to.loc)
        if (move.piece.isPawn && move.to.y.let { it == 0 || it == 7 }) {
            builder.append((move.promotesTo ?: 'Q').asWhite)
        }

        return builder.toString()
    }

    object StandardValidator : MoveValidator() {
        override fun validate(move: Move, checkIllegal: Boolean): ValidationResult {
            val res = ValidationResult()
            res.capture = move.chess.state.capture()
            if (preCheckFails(move) || validatePieceSpecificFails(move, res, checkIllegal)) {
                return res.apply { valid = false }
            }

            return res
        }

        private fun validatePieceSpecificFails(
            move: Move,
            res: ValidationResult,
            checkIllegal: Boolean
        ): Boolean {
            return when (move.piece.kind.asWhite) {
                'R' -> validateRookFails(move, res)
                'B' -> validateBishopFails(move, res)
                'K' -> validateKingFails(move, res, checkIllegal)
                'Q' -> validateQueenFails(move, res)
                'P' -> validatePawnFails(move, res)
                'N' -> validateKnightFails(move, res)
                else -> return true
            }
        }

        private fun validateRookFails(move: Move, res: ValidationResult): Boolean {
            if (move.diff.x != 0 && move.diff.y != 0) return true
            if (hasPieceInLine(move.piece.vec, move.diff.sign, move.to, move.chess)) return true
            return false
        }

        private fun validateBishopFails(move: Move, res: ValidationResult): Boolean {
            println("StandardValidator.validateBishopFails $move")
            if (move.diff.absolute.run{x != y}) return true
            println("StandardValidator.validateBishopFails 1")
            if (hasPieceInLine(move.piece.vec, move.diff.sign, move.to, move.chess)) return true
            println("StandardValidator.validateBishopFails 2")
            return false
        }

        private fun validateQueenFails(move: Move, res: ValidationResult): Boolean {
            if (move.diff.x != 0 && move.diff.y != 0 && move.diff.absolute.run{x != y}) return true
            if (hasPieceInLine(move.piece.vec, move.diff.sign, move.to, move.chess)) return true
            return false
        }

        private fun validateKingFails(
            move: Move,
            res: ValidationResult,
            checkIllegal: Boolean
        ): Boolean {
            println("StandardValidator.validateKingFails")
            println("move = [${move}], res = [${res}], checkIllegal = [${checkIllegal}]")
            if (move.diff.y == 0 && move.diff.x.absoluteValue == 2) {
                println("checking castle")
                val castlingType = if (move.diff.x > 0) 'K' else 'Q'
                if (move.chess.state.canCastle(castlingType.ofColor(move.piece.isWhite))) {
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
//                    if (isCheck(move.chess, move.color, move.piece)) {
//                        return true
//                    }
//                    val pathVec = move.piece.vec + move.diff.sign
//                    val fakeMove = Move(move.chess, move.piece, pathVec, checkIllegal = false)
//                    move.chess.makeMove(fakeMove, validate = false, commit = false)
//                    if (isCheck(move.chess, move.color, move.chess.pieces[pathVec])
//                            .also {
//                                move.chess.revertMove(
//                                    fakeMove,
//                                    validate = false,
//                                    rollBack = false
//                                )
//                            }
//                    ) {
//                        return true
//                    }
                    // todo: also validate no check in path of king
                    res.castling = castlingType
                    res.castleRook = castleRook
                    res.castlingRookFinalPos = move.piece.vec + move.diff.sign
                    println("Castle possible")
                } else {
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
                if (move.isValid()) {
                    return true
                }
            }
            return false
        }


        fun canLegallyMove(chess: Chess, piece: Piece, to: IVec): Boolean {
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
            if(move.to.y == 0 || move.to.y == 7){
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
    }

    object NoCheck : MoveValidator() {
        override fun validate(move: Move, checkIllegal: Boolean): ValidationResult {
            return ValidationResult(true).apply {
                capture = move.chess.state.capture()
            }
        }
    }
}
