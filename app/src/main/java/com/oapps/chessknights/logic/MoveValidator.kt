package com.oapps.chessknights.logic

import android.util.Log
import com.oapps.chessknights.TAG
import com.oapps.chessknights.Vec
import com.oapps.chessknights.ui.chess.Piece
import com.oapps.chessknights.ui.chess.ofColor
import kotlin.math.absoluteValue
import kotlin.math.sign

object MoveValidator {
    private fun ownPieceAttacked(
        move: Move,
        pieceAtDest: Piece?
    ): Boolean {
        if (pieceAtDest != null) {
            if (move.piece.isWhite() == pieceAtDest.isWhite()) {
                return true
            }
        }
        return false
    }

    private fun moveMaybePossible(
        chess: Chess,
        move: Move,
        pieceAtDest: Piece?
    ) = (move.to - move.from).let { diff ->
        when (move.piece.kind.toUpperCase()) {
            'R' -> isRookMove(diff)
            'B' -> isBishopMove(diff)
            'Q' -> isRookMove(diff) || isBishopMove(diff)
            'K' -> isKingMove(diff) || isCastleMove(move, diff)
            'N' -> isKnightMove(diff)
            'P' -> isPawnMove(chess, move, diff, pieceAtDest)
            else -> true
        }
    }

    private fun isCastleMove(move: Move, diff: Vec): Boolean {
        if (diff.y != 0) return false
        if (diff.x.absoluteValue != 2) return false
        return (move.piece.isWhite() && move.from.loc() == "e1") ||
                (move.piece.isBlack() && move.from.loc() == "e8")
    }


    /**
     * Takes care of direction and attack, and en-passant
     */
    private fun isPawnMove(chess: Chess, move: Move, diff: Vec, pieceAtDest: Piece?): Boolean {
        fun check(): Boolean {
            if (diff.x.absoluteValue > 1) return false
            if (diff.y.absoluteValue !in 1..2) return false
            if ((diff.y > 0 && move.piece.isBlack()) || (diff.y < 0 && move.piece.isWhite())) return false
            if ((diff.y == 2 && move.from.y != 1) || (diff.y == -2 && move.from.y != 6)) return false
            if (diff.x.absoluteValue == 1) {
                if (diff.y.absoluteValue != 1) return false
                Log.d(TAG, "isPawnMove check: enpassant target = ${chess.state.enPassantString()}")
                if (pieceAtDest == null && move.to == chess.state.enPassantTarget) {
                    val attackedPiece =
                        chess.findPieceAt(chess.state.enPassantTarget + Vec(y = if (move.piece.isWhite()) -1 else 1))
                            ?: return false
                    move.props[Move.Props.ATTACKED_PIECE] = attackedPiece
                    move.props[Move.Props.EN_PASSANT_OCCURRED_FOR_TARGET] =
                        chess.state.enPassantTarget
                    return true
                }
                if (pieceAtDest == null && move.to != chess.state.enPassantTarget) return false
                if (pieceAtDest != null) return true
            } else if (pieceAtDest != null) return false
            return true
        }
        if (check()) {
            if(diff.y.absoluteValue == 2){
                move.props[Move.Props.ENPASSANT_TARGET_VEC] = move.from + Vec(0, diff.y.sign)
                Log.d(TAG, "isPawnMove: ENPASSANT_TARGET_VEC $move")
            }
            return true
        }
        return false
    }

    private fun isKnightMove(diff: Vec) =
        (diff.x.absoluteValue == 1 && diff.y.absoluteValue == 2) ||
                (diff.x.absoluteValue == 2 && diff.y.absoluteValue == 1)

    private fun isKingMove(diff: Vec) = diff.x.absoluteValue <= 1 && diff.y.absoluteValue <= 1

    private fun isRookMove(diff: Vec) = diff.x == 0 || diff.y == 0

    private fun isBishopMove(diff: Vec) = diff.x.absoluteValue == diff.y.absoluteValue

    private fun vectorsInDirection(
        fromExclusive: Vec,
        dir: Vec,
        toExclusive: Vec? = null
    ): Sequence<Vec> {
        return object : Iterator<Vec> {
            var next = fromExclusive + dir
            override fun hasNext() = next.x in 0..7 && next.y in 0..7 && toExclusive != next

            override fun next() = next.also { next += dir }
        }.asSequence()
    }

    private fun containsPieceInLine(
        fromExclusive: Vec,
        dir: Vec,
        toExclusive: Vec? = null,
        chess: Chess
    ): Boolean {
        return vectorsInDirection(
            fromExclusive,
            dir,
            toExclusive
        ).any { chess.findPieceAt(it) != null }
    }

    private fun moveContainsPieceInLine(
        chess: Chess,
        move: Move
    ) = (move.to - move.from).let { diff ->
        if (move.piece.kind.toUpperCase() in "QBR") {
            val dir = diff.direction()
            containsPieceInLine(move.from, dir, move.to, chess)
        } else false
    }

    private fun moveViolatesCastlingRights(chess: Chess, move: Move): Boolean {
        val diff = move.to - move.from
        if (move.piece.kind.toUpperCase() == 'K' && diff.x.absoluteValue == 2) {
            var castlingType = if (diff.x > 0) 'K' else 'Q'
            if (move.piece.isBlack()) castlingType = castlingType.toLowerCase()
            if (!chess.state.canCastle(castlingType)) return true
            else move.props[Move.Props.CASTLING_CHAR] = castlingType
        }
        return false
    }

    private fun revertMove(chess: Chess, move: Move) {
        Log.d(TAG, "revertMove: $move")
        if (move.piece.vec == move.from) {
            Log.d(TAG, "revertMove: move was already reverted")
            return
        }
        move.piece.vec = move.from
        if (move.isPromotion()) {
            Log.d(TAG, "revertMove: promotion revert")
            move.piece.kind = move.pieceKind
        }
        move.props.isAttack {
            chess.pieces.add(it)
            Log.d(TAG, "revertMove: attack revert")
        }
        move.props.isCastling {
            move.props.getCastlingRookInitialVec()?.let {
                move.props.getCastlingRook()?.vec = it
            }
        }
    }

    private fun isLegal(chess: Chess, move: Move): Boolean {
        Log.d(TAG, "isLegal: checking $move")
        val ourKing = chess.pieces.find { it.kind == 'K'.ofColor(move.piece.isWhite()) }
        if (ourKing == null) {
            Log.d(TAG, "isLegal: Where is our King lol! Chess state invalid")
            return true
        }
        Log.d(TAG, "isLegal: just move to called")
        move.piece.justMoveTo(chess, move)

        val oppPieces = chess.pieces.filter { it.isWhite() != move.piece.isWhite() }
        for (oppPiece in oppPieces) {
            val fakeMove = Move(chess, oppPiece, ourKing.vec)
            if (validateMove(chess, fakeMove, false) && fakeMove.props.isAttack()) {
                Log.d(TAG, "isLegal: $move not legal, threat piece is $oppPiece")
                move.props[Move.Props.INVALID_BOOLEAN] = true
                revertMove(chess, move)
                return false
            }
        }

        Log.d(TAG, "isLegal: revert called")
        revertMove(chess, move)
        return true
    }

    fun isCheck(chess: Chess, opponentColorWhite: Boolean): Boolean {
        val oppKing = chess.pieces.find { it.kind == 'K'.ofColor(opponentColorWhite) }
        if (oppKing == null) {
            Log.d(TAG, "isCheck: Where is opponent's King lol! Chess state invalid")
            return false
        }
        val myPieces = chess.pieces.filter { it.isWhite() != opponentColorWhite }
        for (myPiece in myPieces) {
            val fakeMove = Move(chess, myPiece, oppKing.vec)
            if (validateMove(chess, fakeMove, false) && fakeMove.props.isAttack()) {
                Log.d(TAG, "isCheck: from $myPiece")
                return true
            }
        }

        return false
    }

    fun validateMove(chess: Chess, move: Move, checkIllegal: Boolean = true): Boolean {
        fun validateStep1(): Boolean {
            if (move.from == move.to) {
                Log.d(TAG, "validateMove: to == dest")
                return false
            }
            val pieceAtDest = chess.findPieceAt(move.to)
            if (pieceAtDest != null) {
                move.props[Move.Props.ATTACKED_PIECE] = pieceAtDest
            }
            if (ownPieceAttacked(move, pieceAtDest)) return false
            if (!moveMaybePossible(chess, move, pieceAtDest)) return false
            if (moveContainsPieceInLine(chess, move)) return false
            if (moveViolatesCastlingRights(chess, move)) return false
            return true
        }

        // TODO: 15/03/21 Handle illegal castling
        if (!validateStep1()) return false

        fun validateStep2(): Boolean {
            if (checkIllegal) {
                if (!isLegal(chess, move)) return false
            }
            return true
        }

        if (!validateStep2()) return false
        return true
    }

    fun validMoves(chess: Chess, piece: Piece): List<Move> {
        val kind = piece.kind.toUpperCase()
        val moves = mutableListOf<Move>()
        when (kind) {
            'R', 'B', 'Q', 'K' -> {
                val dirs = mutableListOf<Vec>()
                if (kind in "RQK") {
                    dirs.add(Vec(1, 0))
                    dirs.add(Vec(0, 1))
                    dirs.add(Vec(-1, 0))
                    dirs.add(Vec(0, -1))
                }
                if (kind in "BQK") {
                    dirs.add(Vec(1, 1))
                    dirs.add(Vec(-1, 1))
                    dirs.add(Vec(-1, -1))
                    dirs.add(Vec(1, -1))
                }
                if (kind == 'K') {
                    for (dir in dirs) {
                        val fakeMove = Move(chess, piece, piece.vec + dir)
                        Log.d(TAG, "validMoves: Checking $fakeMove")
                        if (validateMove(chess, fakeMove).also { revertMove(chess, fakeMove) }) {
                            moves.add(fakeMove)
                        }
                    }
                } else {
                    for (dir in dirs) {
                        for (to in vectorsInDirection(piece.vec, dir)) {
                            val fakeMove = Move(chess, piece, to)
                            Log.d(TAG, "validMoves: Checking $fakeMove")
                            if (validateMove(chess, fakeMove).also {
                                    revertMove(
                                        chess,
                                        fakeMove
                                    )
                                }) {
                                moves.add(fakeMove)
                            } else {
                                break
                            }
                        }
                    }
                }
            }
            'P' -> {
                val dirs = mutableListOf<Vec>()
                val ySign = if (piece.isBlack()) -1 else 1
                for (x in -1..1) {
                    dirs.add(Vec(x, 1 * ySign))
                }
                dirs.add(Vec(0, 2 * ySign))
                for (dir in dirs) {
                    val fakeMove = Move(chess, piece, piece.vec + dir)
                    Log.d(TAG, "validMoves: Checking $fakeMove")
                    if (validateMove(chess, fakeMove).also { revertMove(chess, fakeMove) }) {
                        moves.add(fakeMove)
                    }
                }
            }
            'N' -> {
                val dirs = mutableListOf<Vec>()
                for (i in -1..1 step 2) {
                    for (j in -2..2 step 4) {
                        dirs.add(Vec(i, j))
                        dirs.add(Vec(j, i))
                    }
                }
                for (dir in dirs) {
                    val fakeMove = Move(chess, piece, piece.vec + dir)
                    Log.d(TAG, "validMoves: Checking $fakeMove")
                    if (validateMove(chess, fakeMove).also { revertMove(chess, fakeMove) }) {
                        moves.add(fakeMove)
                    }
                }
            }
        }
        return moves.filter { it.props.isValid() }
    }
}