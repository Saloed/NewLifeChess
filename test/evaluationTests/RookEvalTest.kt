package evaluationTests

import core.BitBoard
import evaluator.RookEvaluator
import org.junit.Test

import org.junit.Assert.assertEquals
import uci.FENParser

class RookEvalTest {
    private val rs = RookEvaluator()

    @Test
    fun testScorer() {
        val board = BitBoard()
        var fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        FENParser.loadPosition(fen, board)
        assertEquals(0, rs.evaluatePosition(board).toLong())

        fen = "rnbqkbnr/1ppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        FENParser.loadPosition(fen, board)
        assertEquals((-RookEvaluator.OPEN_FILE_SCORE).toLong(), rs.evaluatePosition(board).toLong())

        fen = "rnbqkbnr/pppppppp/8/8/8/8/1PPPPPP1/RNBQKBNR w KQkq - 0 1"
        FENParser.loadPosition(fen, board)
        assertEquals((2 * RookEvaluator.OPEN_FILE_SCORE).toLong(), rs.evaluatePosition(board).toLong())
    }

    @Test
    fun testConnectedScore() {
        val board = BitBoard()
        FENParser.loadPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/R4RK1 w KQkq - 0 1", board)
        assertEquals(150, rs.evaluatePosition(board).toLong())

        FENParser.loadPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/4RRK1 w KQkq - 0 1", board)
        assertEquals(150, rs.evaluatePosition(board).toLong())

        FENParser.loadPosition("rnbqkbnr/pppppppp/8/8/7P/7R/PPPPPPP1/1NBQKBNR w KQkq - 0 1", board)
        assertEquals(150, rs.evaluatePosition(board).toLong())
    }
}
