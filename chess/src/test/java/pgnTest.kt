import com.oapps.lib.chess.PGNParser
import org.junit.Test

class PGNTest{
    private val dummyPgn = """
        [Event "Live Chess"]
        [Site "Chess.com"]
        [Date "2021.04.07"]
        [Round "?"]
        [White "OmKumar10"]
        [Black "alebatta3"]
        [Result "1-0"]
        [ECO "A00"]
        [WhiteElo "1240"]
        [BlackElo "757"]
        [TimeControl "180"]
        [EndTime "1:33:58 PDT"]
        [Termination "OmKumar10 won by checkmate"]

        1. e3 e5 2. b3 Nc6 3. Bb2 d6 4. Nc3 g6 5. h3 Bg7 6. Nf3 Nf6 7. Be2 O-O 8. d3 Re8
        9. Qd2 e4 10. dxe4 Nxe4 11. Nxe4 Rxe4 12. Bxg7 Kxg7 13. O-O-O Kg8 14. Kb1 Rb4
        15. Nd4 Nxd4 16. exd4 Rb6 17. c4 Qf6 18. c5 dxc5 19. dxc5 Rc6 20. b4 b6 21. Qd8+
        Kg7 22. Qxf6+ Rxf6 23. Bf3 Rb8 24. cxb6 cxb6 25. a3 Ba6 26. Rd7 Rc8 27. Re1 Bb5
        28. Rxa7 Rd6 29. Ree7 Bc4 30. Be2 Bxe2 31. Rxe2 Rd1+ 32. Kb2 Rf1 33. Kb3 Rb1+
        34. Ka4 Rg1 35. g3 Rd8 36. Ree7 Rd5 37. Rxf7+ Kh6 38. Rxh7+ Kg5 39. h4+ Kf5 40.
        Raf7+ Ke6 41. Rb7 Rd6 42. Rhe7+ Kd5 43. Red7 Kc6 44. Rxd6+ Kxb7 45. Rxg6 Rb1 46.
        Kb5 Ra1 47. a4 Ra2 48. f4 Rg2 49. f5 Rxg3 50. Rxg3 Kc7 51. Rg7+ Kd6 52. Kxb6 Ke5
        53. h5 Kxf5 54. h6 Kf6 55. a5 Kf5 56. a6 Kf6 57. a7 Kf5 58. a8=Q Kf6 59. Kc6 Kf5
        60. Kd5 Kf6 61. b5 Kf5 62. Qf8# 1-0
    """.trimIndent()

    @Test
    fun parseTest(){
        val parser = PGNParser()
        println(parser.parse(dummyPgn))
    }
}