package com.oapps.chessknights.logic

import com.oapps.chessknights.Vec
import com.oapps.chessknights.ui.chess.Piece
import kotlin.math.absoluteValue

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
    ) = (move.to - move.piece.vec).let { diff ->
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
        if(diff.y != 0) return false
        if(diff.x.absoluteValue != 2) return false
        return (move.piece.isWhite() && move.piece.vec.loc() == "e1") ||
            (move.piece.isBlack() && move.piece.vec.loc() == "e8")
    }


    /**
     * Takes care of direction and attack, and en-passant
     */
    private fun isPawnMove(chess: Chess, move: Move, diff: Vec, pieceAtDest: Piece?): Boolean {
        if (diff.x.absoluteValue > 1) return false
        if (diff.y.absoluteValue !in 1..2) return false
        if ((diff.y > 0 && move.piece.isBlack()) || (diff.y < 0 && move.piece.isWhite())) return false
//        if(diff.y == 2 && )
        if(diff.x.absoluteValue == 1){
            if(pieceAtDest == null && move.to == chess.state.enPassantTarget) move.props[Move.Props.EN_PASSANT_STRING] = chess.state.enPassantString()
            if(pieceAtDest == null || move.to != chess.state.enPassantTarget) return false
        }else if(pieceAtDest != null) return false
        return true
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
    ) = (move.to - move.piece.vec).let { diff ->
        if (move.piece.kind.toUpperCase() in "QBR") {
            val dir = diff.direction()
            containsPieceInLine(move.piece.vec, dir, move.to, chess)
        } else false
    }

    private fun moveViolatesCastlingRights(chess: Chess, move: Move): Boolean {
        val diff = move.to - move.piece.vec
        if(move.piece.kind.toUpperCase() == 'K' && diff.x.absoluteValue == 2){
            var castlingType = if(diff.x > 0) 'K' else 'Q'
            if(move.piece.isBlack()) castlingType = castlingType.toLowerCase()
            if(!chess.state.canCastle(castlingType)) return true
            else move.props[Move.Props.CASTLING_CHAR] = castlingType
        }
        return false
    }

    fun validateMove(chess: Chess, move: Move): Boolean {
        val pieceAtDest = chess.findPieceAt(move.to)
        if(pieceAtDest != null) {
            move.props[Move.Props.ATTACKED_PIECE] = pieceAtDest
        }
        if(ownPieceAttacked(move, pieceAtDest)) return false
        if(!moveMaybePossible(chess, move, pieceAtDest)) return false
        if(moveContainsPieceInLine(chess, move)) return false
        if(moveViolatesCastlingRights(chess, move)) return false
        return true
    }
}