package evaluationTests

import core.BitBoard
import evaluator.MaterialEvaluator
import org.junit.Test

import org.junit.Assert.assertEquals
import uci.FENParser

class MaterialEvalTest {
    private val scorer = MaterialEvaluator()
    private val board = BitBoard()

    @Test
    fun testAllPieces() {
        board.initialise()
        assertEquals(0, scorer.evaluatePosition(board).toLong())
    }

    @Test
    fun testAllWhite() {
        FENParser.loadPosition("4k3/8/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 1", board)
        assertEquals(39000, scorer.evaluatePosition(board).toLong())
    }

    @Test
    fun testAllBlack() {
        FENParser.loadPosition("rnbqkbnr/pppppppp/8/8/8/8/8/4K3 w - - 0 1", board)
        assertEquals(-39000, scorer.evaluatePosition(board).toLong())
    }

    @Test
    fun testRookValue() {
        FENParser.loadPosition("1nbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 1", board)
        assertEquals(5000, scorer.evaluatePosition(board).toLong())
    }

    @Test
    fun testKnightValue() {
        FENParser.loadPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/R1BQKBNR w - - 0 1", board)
        assertEquals(-3000, scorer.evaluatePosition(board).toLong())
    }

    @Test
    fun testBishopValue() {
        FENParser.loadPosition("rn1qk1nr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 1", board)
        assertEquals(6000, scorer.evaluatePosition(board).toLong())
    }

    @Test
    fun testQueenValue() {
        FENParser.loadPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNB1KBNR w - - 0 1", board)
        assertEquals(-9000, scorer.evaluatePosition(board).toLong())
    }

    @Test
    fun testPawnValue() {
        FENParser.loadPosition("rnbqkbnr/p1p2pp1/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 1", board)
        assertEquals(4000, scorer.evaluatePosition(board).toLong())
    }
}
