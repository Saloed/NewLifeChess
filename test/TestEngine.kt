import core.BitBoard
import core.ChessEngine
import core.moves.PawnCaptureGenerator
import org.junit.Assert.assertEquals
import org.junit.Test
import uci.FENParser
import java.util.*
import kotlin.system.exitProcess

class TestEngine {
    @Test
    fun testPawnCaptures1() {
        val board = BitBoard()
        FENParser.loadPosition("3k4/8/3p4/5P2/4P3/8/8/3K4 w - - 0 1", board)
        val generator = PawnCaptureGenerator()
        val moves = ArrayList<BitBoard.BitBoardMove>()
        generator.generateMoves(board, false, 0L, moves)
        assertEquals(0, moves.size.toLong())
    }

    @Test
    fun testPawnCaptures2() {
        val board = BitBoard()
        FENParser.loadPosition("3k4/8/8/3p1P2/4P3/8/8/3K4 w - - 0 1", board)
        val generator = PawnCaptureGenerator()
        val moves = ArrayList<BitBoard.BitBoardMove>()
        generator.generateThreatMoves(board, false, 0L, moves)
        assertEquals(1, moves.size.toLong())
        assertEquals("E4D5", moves[0].algebraic)
    }

}

fun main(arg: Array<String>) {
    val board = BitBoard().initialise()
    val engine = ChessEngine(5, 2)
    var bestMove: String?
    var nextMove: BitBoard.BitBoardMove

    var start = 0L
    var decisionTime = 0L
    var turn = 0
    println("Responsiveness test")
    while (decisionTime < 25000 && turn < 20) {
        start = System.currentTimeMillis()
        bestMove = engine.getPreferredMove(board)!!
        decisionTime = System.currentTimeMillis() - start
        nextMove = board.getMove(bestMove)
        board.makeMove(nextMove)
        turn++
        println("$turn ${decisionTime / 1000}s${decisionTime % 1000}ms $bestMove // nodes ${engine.nodes} qnodes ${engine.qnodes} " +
                "// NPS ${(engine.nodes.get() * 1000) / decisionTime}  QNPS  ${(engine.qnodes.get() * 1000) / decisionTime} " +
                "// TotalEvals ${engine.evalCalls} EPS ${(engine.evalCalls.get() * 1000) / decisionTime}")
    }
    //engine.stopExecutor()
    exitProcess(0)
}