package evaluationTests

import core.BitBoard
import evaluator.KingSafetyEvaluator
import org.junit.Test

import org.junit.Assert.assertEquals
import uci.FENParser

class KingSafetyTest {
    private val scorer = KingSafetyEvaluator()
    private val board = BitBoard()

    @Test
    fun testStart() {
        FENParser.loadPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 1", board)
        assertEquals(0, scorer.evaluatePosition(board).toLong())
    }

    @Test
    fun testCastled() {
        FENParser.loadPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQ1RK1 w - - 0 1", board)
        assertEquals(250, scorer.evaluatePosition(board).toLong())
    }

    @Test
    fun testPawnGone() {
        FENParser.loadPosition("rnbq1rk1/1ppppppp/8/8/8/8/PPPPPP1P/RNBQ1RK1 w - - 0 1", board)
        assertEquals(-70, scorer.evaluatePosition(board).toLong())
    }

    @Test
    fun testFiancettoed() {
        FENParser.loadPosition("rnbq1rk1/pppppppp/8/8/8/8/PPPPPPBP/RNBQ1RK1 w - - 0 1", board)
       /* println()
        println("Test value")
        println()*/
        assertEquals(-30, scorer.evaluatePosition(board).toLong())
    }
}
