package com.oapps.lib.chess

class State(fen: String) {
    init {
        reset(fen)
    }

    inner class Capture(state: State){
        val castling = state.castling
        val enPassantTarget = state.enPassantTarget.copy()
        val activeColor = state.activeColor
        val halfMoveCount = state.halfMoveCount
        val halfMoveClock = state.halfMoveClock
        override fun toString(): String {
            return "Capture(castling='$castling', enPassantTarget=$enPassantTarget, activeColor=$activeColor, halfMoveCount=$halfMoveCount, halfMoveClock=$halfMoveClock)"
        }
    }



    // no of moves since last capture or pawn move
    var halfMoveClock = 0
    private var halfMoveCount = 2
    val fullMoveCount get() = halfMoveCount / 2

    var castling = "KkQq"
    var enPassantTarget = IVec.None
    var activeColor = true

    private fun resetCastling(whoCanCastle: String = "KQkq") {
        castling = whoCanCastle
    }

    fun canCastle(who: Char) = castling.contains(who)

    private fun removeCastle(who: Char) {
        castling = castling.filter { it !=  who}
    }

    private fun addCastle(who: Char) {
        if(!canCastle(who)) castling += who
    }



    fun castlingString() = "KkQq".filter { canCastle(it) }.let {
        if(it.isEmpty()) "-" else it
    }

    fun enPassantString() = enPassantTarget.loc

    fun activeColorString() = when (activeColor) {
        true -> "w"
        false -> "b"
    }

    fun commit(move: Move): Boolean{
        if(!move.isValid()){
            println("Cannot commit invalid move to state")
            return false
        }
        activeColor = !move.color

        move.validationResult.castling?.let {
            removeCastle(it)
        }
        enPassantTarget = IVec.None
        move.validationResult.createdEnPassantTarget?.let {
            enPassantTarget = it
        }
        halfMoveCount++
        halfMoveClock++
        if(move.validationResult.enPassantCapturedPiece != null || move.attackedPiece != null){
            halfMoveClock = 0
        }
        return true
    }

    fun rollBack(move: Move): Boolean {
        if(!move.isValid()){
            println("Cannot rollback invalid move from state")
            return false
        }
        val capture = move.validationResult.capture?: return false
        activeColor = capture.activeColor
        castling = capture.castling
        enPassantTarget = capture.enPassantTarget
        halfMoveClock = capture.halfMoveClock
        halfMoveCount = capture.halfMoveCount
        return true
    }

    fun reset(fen: String) {
        val data = fen.substringAfter(' ').split(' ')
        activeColor = data[0][0] == 'w'
        resetCastling(data[1])
        enPassantTarget = when(data[2]){
            "-" -> IVec.None
            else -> IVec(data[2])
        }
        halfMoveCount = data[3].toInt()
        val fullMoveCount = data[4].toInt()
        halfMoveCount = fullMoveCount * 2 + if (activeColor.isBlack) 1 else 0
    }

    fun capture() = Capture(this)

    override fun toString(): String {
        return "State(halfMoveClock=$halfMoveClock, fullMoveCount=$fullMoveCount, castling='$castling', enPassantTarget=$enPassantTarget, activeColor=$activeColor)"
    }
}