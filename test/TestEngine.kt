import core.BitBoard
import core.moves.MoveGenerator
import core.moves.PawnCaptureGenerator
import core.moves.PawnMoveGenerator
import org.junit.Assert.assertEquals
import org.junit.Test
import uci.FENParser
import java.util.*

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

    @Test
    fun testPawnMoves1() {
        val board = BitBoard()
        FENParser.loadPosition("3k4/8/8/3p1P2/4P3/8/8/3K4 w - - 0 1", board)
        val generator = MoveGenerator(board)
        generator.setGenerators(PawnMoveGenerator())
        val moves = generator.threateningMoves

        assertEquals(0, moves.size.toLong())
    }


}
