package evaluationTests

import core.BitBoard
import evaluator.BishopMobilityEvaluator
import evaluator.BishopPairEvaluator
import org.junit.Assert.assertEquals
import org.junit.Test
import uci.FENParser

class BishopEvalTest {
    private val scorer = BishopPairEvaluator()
    private val scorer2 = BishopMobilityEvaluator()
    private val board = BitBoard()

    @Test
    fun testBishopScore1() {
        assertEquals(0, scorer.evaluatePosition(board.initialise()).toLong())
    }

    @Test
    fun testBishopScore2() {
        FENParser.loadPosition("2b1kb2/8/8/8/8/8/8/7K w - - 0 1", board)
        assertEquals(-150, scorer.evaluatePosition(board).toLong())
    }

    @Test
    fun testBishopScore3() {
        FENParser.loadPosition("2b1kb2/q7/8/8/8/8/8/7K w - - 0 1", board)
        assertEquals(-300, scorer.evaluatePosition(board).toLong())
    }

    @Test
    fun testBishopScore4() {
        FENParser.loadPosition("2b1kb2/q7/8/8/8/8/8/2B2B1K w - - 0 1", board)
        assertEquals(-150, scorer.evaluatePosition(board).toLong())
    }

    @Test
    fun testMobilityScore1() {
        FENParser.loadPosition("7k/8/8/8/3B4/8/8/7K w - - 0 1", board)
        assertEquals(150, scorer2.evaluatePosition(board).toLong())
    }

    @Test
    fun testMobilityScore2() {
        FENParser.loadPosition("7k/8/8/8/7B/8/8/7K w - - 0 1", board)
        assertEquals(75, scorer2.evaluatePosition(board).toLong())
    }

    @Test
    fun testMobilityScore3() {
        FENParser.loadPosition("7k/8/8/2P5/3B4/8/8/7K w - - 0 1", board)
        assertEquals(75, scorer2.evaluatePosition(board).toLong())
    }
}
