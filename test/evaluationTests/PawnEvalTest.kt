package evaluationTests

import core.BitBoard
import evaluator.PawnStructureEvaluator
import org.junit.Assert.assertEquals
import org.junit.Test
import uci.FENParser

class PawnEvalTest {
    @Test
    fun testBasicScore() {
        val board = BitBoard().initialise()
        assertEquals(0, PawnStructureEvaluator().evaluatePosition(board).toLong())
    }

    @Test
    fun testIslands() {
        var board = BitBoard()
        FENParser.loadPosition("rnbqkbnr/ppp1pppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", board)
        assertEquals(PawnStructureEvaluator.S_ISLAND.toLong(), PawnStructureEvaluator().evaluatePosition(board).toLong())

        board = FENParser.loadPosition("rnbqkbnr/pppppppp/8/8/8/8/PP1PP1PP/RNBQKBNR w KQkq - 0 1", board)
        assertEquals((-2 * PawnStructureEvaluator.S_ISLAND).toLong(), PawnStructureEvaluator().evaluatePosition(board).toLong())
    }

    @Test
    fun testIsolated() {
        val board = BitBoard()
        FENParser.loadPosition("rnbqkbnr/pp1pp1pp/8/8/8/8/P1P1PPPP/RNBQKBNR w KQkq - 0 1", board)
        assertEquals((-2 * PawnStructureEvaluator.S_ISOLATED).toLong(), PawnStructureEvaluator().evaluatePosition(board).toLong())
    }

    @Test
    fun testDoubled() {
        // Both have islands, only one has doubled
        var board = BitBoard()
        FENParser.loadPosition("rnbqkbnr/pp1ppppp/8/8/8/3P4/PP1PPPPP/RNBQKBNR w KQkq - 0 1", board)
        assertEquals((-PawnStructureEvaluator.S_DOUBLED).toLong(), PawnStructureEvaluator().evaluatePosition(board).toLong())

        // Here - test pawn gets an advance bonus
        board = FENParser.loadPosition("rnbqkbnr/pp1ppppp/8/8/3P4/3P4/PP1PPPP1/RNBQKBNR w KQkq - 0 1", board)
        assertEquals((-2 * PawnStructureEvaluator.S_DOUBLED + 20).toLong(), PawnStructureEvaluator().evaluatePosition(board).toLong())
    }

    @Test
    fun testAdvanced() {
        var board = BitBoard()
        FENParser.loadPosition("rnbqkbnr/pppppppp/3P4/8/8/8/PPP1PPPP/RNBQKBNR w KQkq - 0 1", board)
        assertEquals(200, PawnStructureEvaluator().evaluatePosition(board).toLong())

        board = FENParser.loadPosition("rnbqkbnr/ppp1pppp/8/8/8/3p4/PPPPPPPP/RNBQKBNR w KQkq - 0 1", board)
        assertEquals(-200, PawnStructureEvaluator().evaluatePosition(board).toLong())
    }
}
