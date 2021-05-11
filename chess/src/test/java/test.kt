import com.oapps.lib.chess.*
import junit.framework.TestCase.assertEquals
import org.junit.Test

class Test{
    private val fens = arrayOf(
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
        "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2",
        "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 2",
        "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
        "8/8/8/4p1K1/2k1P3/8/8/8 b - - 0 1",
        "4k2r/6r1/8/8/8/8/3R4/R3K3 w Qk - 0 1",
        "8/5k2/3p4/1p1Pp2p/pP2Pp1P/P4P1K/8/8 b - - 99 50",
        "r1b1k1nr/p2p1pNp/n2B4/1p1NP2P/6P1/3P1Q2/P1P1K3/q5b1",
        "rnbqkbnr/pppppppp/8/8/8/P7/1PPPPPPP/RNBQKBNR"
    )

    @Test
    fun fenTest(){
        val chess = Chess()
        for (fen in fens) {
            chess.reset(fen)
            assertEquals(fen.substringBefore(' '), chess.generateFen())
        }
    }

    @Test
    fun moveMakeRevertTest(){
        val chess = Chess()
        val move = chess.makeMove(Move(chess, Piece(IVec(0, 1), 'P'), IVec(0, 2)))
        assertEquals("rnbqkbnr/pppppppp/8/8/8/P7/1PPPPPPP/RNBQKBNR", chess.generateFen())
        chess.revertMove(move)
        assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR", chess.generateFen())
    }

    @Test
    fun fenTime(){
        val chess = Chess()
        val t = System.currentTimeMillis()
        chess.generateFen()
        val t2 = System.currentTimeMillis()
        println("Fen generated in ${t2 - t} ms")
    }

    @Test
    fun genFen(){
        val chess = Chess()
        for(fen in fens) {
            chess.reset(fen)
            assertEquals(fen.substringBefore(' '), chess.generateFen())
        }
    }

    @Test
    fun debugFindPossibleMove(){
        val chess = Chess("6r1/4P3/1p1k4/1N3Prp/R1B3p1/6Kp/n3R2N/B1Q5 b - - 15 55")
        val moves = MoveValidator.StandardValidator.getPossibleMoves(chess, chess[IVec("d6")]!!)
        println(moves)
    }

}