package evaluationTests

import core.BitBoard
import evaluator.KnightEvaluator
import org.junit.Assert.assertEquals
import org.junit.Test
import uci.FENParser

class KnightEvalTest {
    private val scorer = KnightEvaluator()
    private val board = BitBoard()

    @Test
    fun testKnightInCorner() {
        FENParser.loadPosition("7k/8/8/8/8/8/8/N6K w - - 0 1", board)
        val expect = KnightEvaluator.targetScores[2]
        assertEquals(expect.toLong(), scorer.evaluatePosition(board).toLong())
    }

    @Test
    fun testKnightInCornerOwnPieceBlocking() {
        FENParser.loadPosition("7k/8/8/8/8/8/2R5/N6K w - - 0 1", board)
        val expect = KnightEvaluator.targetScores[2]
        assertEquals(expect.toLong(), scorer.evaluatePosition(board).toLong())
    }

    @Test
    fun testKnightInCornerPawnBlocking() {
        FENParser.loadPosition("7k/8/8/8/8/3p4/8/N6K w - - 0 1", board)
        val expect = KnightEvaluator.targetScores[1]
        assertEquals(expect.toLong(), scorer.evaluatePosition(board).toLong())
    }

    @Test
    fun testKnightInCornerAttacking() {
        FENParser.loadPosition("7k/8/8/8/8/8/2r5/N6K w - - 0 1", board)
        val expect = KnightEvaluator.targetScores[2]
        assertEquals(expect.toLong(), scorer.evaluatePosition(board).toLong())
    }

    @Test
    fun testKnightInMiddle() {
        FENParser.loadPosition("7k/8/8/8/3N4/8/8/7K w - - 0 1", board)
        val expect = KnightEvaluator.targetScores[8] + KnightEvaluator.rankScores[3]
        assertEquals(expect.toLong(), scorer.evaluatePosition(board).toLong())
    }

    @Test
    fun testBlackKnightInMiddle() {
        FENParser.loadPosition("7k/8/8/8/3n4/8/8/7K w - - 0 1", board)
        val expect = KnightEvaluator.targetScores[8] + KnightEvaluator.rankScores[4]
        assertEquals((-expect).toLong(), scorer.evaluatePosition(board).toLong())
    }

    @Test
    fun testKnightInMiddleNonSecure() {
        val expect = KnightEvaluator.targetScores[8] + KnightEvaluator.rankScores[3]

        FENParser.loadPosition("7k/4p3/8/8/3N4/2P5/8/7K w - - 0 1", board)
        assertEquals(expect.toLong(), scorer.evaluatePosition(board).toLong())
        FENParser.loadPosition("7k/2p5/8/8/3N4/2P5/8/7K w - - 0 1", board)
        assertEquals(expect.toLong(), scorer.evaluatePosition(board).toLong())

        FENParser.loadPosition("7k/8/8/2p5/3N4/2P5/8/7K w - - 0 1", board)
        assertEquals(expect.toLong(), scorer.evaluatePosition(board).toLong())
    }

    @Test
    fun testKnightInMiddleSecure() {
        FENParser.loadPosition("7k/8/8/8/3N4/2P5/8/7K w - - 0 1", board)

        var expect = KnightEvaluator.targetScores[8] + KnightEvaluator.rankScores[3] + KnightEvaluator.SECURE_BONUS
        assertEquals(expect.toLong(), scorer.evaluatePosition(board).toLong())

        FENParser.loadPosition("7k/8/8/8/3Np3/2P5/8/7K w - - 0 1", board)
        expect = KnightEvaluator.targetScores[7] + KnightEvaluator.rankScores[3] + KnightEvaluator.SECURE_BONUS
        assertEquals(expect.toLong(), scorer.evaluatePosition(board).toLong())
    }
}
