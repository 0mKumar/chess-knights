import com.oapps.lib.chess.Chess
import com.oapps.lib.chess.Move

fun main(){
    val chess = Chess()
    val reader = System.`in`.bufferedReader()
    while (true){
        while (!reader.ready());
        val line = reader.readLine()
        when(line){
            "b" -> chess.printAsciiBoard()
            else -> {
                when{
                    line.startsWith("m ") -> {
                        val move = Move(chess, line.removePrefix("m "))
                        chess.makeMove(move)
                        if(move.isValid()) {
                            chess.printAsciiBoard()
                            println(chess.generateFullFen())
                        }else{
                            println("Invalid move")
                        }
                    }
                    line.startsWith("b ") || line.startsWith("w ") -> {
                        val move = chess.validator.moveFromSan(chess, line.drop(2), line[0] == 'w')
                        if(move == null || !move.isValid()){
                            println("Invalid move")
                        }else {
                            chess.makeMove(move)
                            chess.printAsciiBoard()
                            println(chess.generateFullFen())
                        }
                    }
                    line.startsWith("f ") -> {
                        chess.reset(line.removePrefix("f "))
                    }
                }
            }
        }
    }
}